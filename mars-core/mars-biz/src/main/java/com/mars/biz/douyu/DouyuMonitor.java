package com.mars.biz.douyu;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mars.biz.entity.YunAnchor;
import com.mars.biz.entity.YunAnchorGiftStat;
import com.mars.biz.mapper.YunAnchorGiftStatMapper;
import com.mars.biz.mapper.YunAnchorMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 斗鱼实时数据监听器。
 *
 * <p>启动后自动加载启用主播，为每个直播间建立弹幕连接；实时累计付费礼物(SR)、
 * 总礼物(LW)、送礼人数、弹幕与开播时长，并周期性落库到 yun_anchor_gift_stat。</p>
 */
@Slf4j
@Component
public class DouyuMonitor {

    private static final String PERIOD_TYPE_DAY = "DAY";
    private static final String PERIOD_TYPE_MONTH = "MONTH";
    private static final String SOURCE_CODE = "DOUYU";

    /**
     * 免费礼物标识（鱼丸等），giftType==2 视为免费礼物，不计入付费(SR)。
     */
    private static final String FREE_GIFT_TYPE = "2";

    /**
     * 每批并行建立的连接数，错开连接建立时间以规避斗鱼对同一 IP 并发连接数的风控限制。
     */
    private static final int CONNECT_BATCH_SIZE = 10;

    /**
     * 相邻两批连接之间的间隔毫秒数。
     */
    private static final long CONNECT_BATCH_INTERVAL_MS = 3_000;

    @Value("${mars.douyu.monitor.enabled:true}")
    private boolean monitorEnabled;

    @Value("${mars.douyu.monitor.hosts:wss://danmuproxy.douyu.com:8501,wss://danmuproxy.douyu.com:8502,wss://danmuproxy.douyu.com:8503,wss://danmuproxy.douyu.com:8504,wss://danmuproxy.douyu.com:8505,wss://danmuproxy.douyu.com:8506}")
    private String hostsRaw;

    private final YunAnchorMapper anchorMapper;
    private final YunAnchorGiftStatMapper giftStatMapper;

    private final Map<String, RoomState> rooms = new ConcurrentHashMap<>();
    private final Set<String> pendingRooms = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService connector = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "douyu-connector");
        t.setDaemon(true);
        return t;
    });
    private List<String> hosts = List.of();

    public DouyuMonitor(YunAnchorMapper anchorMapper, YunAnchorGiftStatMapper giftStatMapper) {
        this.anchorMapper = anchorMapper;
        this.giftStatMapper = giftStatMapper;
    }

    @PostConstruct
    public void init() {
        if (!monitorEnabled) {
            log.info("[斗鱼] 实时监听已禁用");
            return;
        }
        hosts = Arrays.stream(hostsRaw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        if (hosts.isEmpty()) {
            log.warn("[斗鱼] 未配置弹幕服务器地址，实时监听未启动");
            return;
        }
        reloadRooms();
        log.info("[斗鱼] 实时监听启动完成，监听 {} 个直播间", rooms.size());
    }

    @PreDestroy
    public void shutdown() {
        flushAll();
        connector.shutdownNow();
        rooms.values().forEach(room -> room.client.stop());
        rooms.clear();
        pendingRooms.clear();
        log.info("[斗鱼] 实时监听已停止");
    }

    /**
     * 定时刷新直播间列表（每 5 分钟）：新增主播、移除已停用主播。
     */
    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    public void reloadRooms() {
        if (!monitorEnabled) {
            return;
        }
        List<YunAnchor> anchors = enabledAnchors();
        Map<String, YunAnchor> byRoom = new ConcurrentHashMap<>();
        for (YunAnchor anchor : anchors) {
            if (StringUtils.hasText(anchor.getRoomId())) {
                byRoom.put(anchor.getRoomId(), anchor);
            }
        }
        for (String roomId : new ArrayList<>(rooms.keySet())) {
            if (!byRoom.containsKey(roomId)) {
                RoomState removed = rooms.remove(roomId);
                if (removed != null) {
                    removed.client.stop();
                    log.info("[斗鱼] 移除直播间 {}", roomId);
                }
            }
        }
        // 收集需要新建连接的直播间，分批建立连接，错开时间规避斗鱼并发风控
        List<YunAnchor> toConnect = new ArrayList<>();
        for (YunAnchor anchor : anchors) {
            String roomId = anchor.getRoomId();
            if (!StringUtils.hasText(roomId)) {
                continue;
            }
            if (rooms.containsKey(roomId) || pendingRooms.contains(roomId)) {
                continue;
            }
            pendingRooms.add(roomId);
            toConnect.add(anchor);
        }
        for (int i = 0; i < toConnect.size(); i += CONNECT_BATCH_SIZE) {
            List<YunAnchor> batch = toConnect.subList(i, Math.min(i + CONNECT_BATCH_SIZE, toConnect.size()));
            long delay = (i / CONNECT_BATCH_SIZE) * CONNECT_BATCH_INTERVAL_MS;
            connector.schedule(() -> batch.forEach(this::connectRoom), delay, TimeUnit.MILLISECONDS);
        }
        if (!toConnect.isEmpty()) {
            log.info("[斗鱼] 待连接分区 {} 个直播号，分 {} 批错峰连接", toConnect.size(),
                    (toConnect.size() + CONNECT_BATCH_SIZE - 1) / CONNECT_BATCH_SIZE);
        }
    }

    /**
     * 为单个直播间建立监听连接并加入 rooms。连接建立后持续保持，不丢失后续实时数据。
     */
    private void connectRoom(YunAnchor anchor) {
        String roomId = anchor.getRoomId();
        RoomState state = new RoomState(anchor.getAnchorId(), roomId);
        seedFromDb(state);
        state.client = new DouyuDanmakuClient(roomId, anchor.getAnchorId(), hosts, this::onMessage);
        rooms.put(roomId, state);
        pendingRooms.remove(roomId);
        state.client.start();
        log.info("[斗鱼] 开始监听直播间 {} 主播 {}", roomId, anchor.getAnchorId());
    }

    /**
     * 定时落库（每 60 秒）。
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void flushAll() {
        rooms.values().forEach(this::flushRoom);
    }

    private void onMessage(String roomId, Map<String, String> fields) {
        RoomState state = rooms.get(roomId);
        if (state == null) {
            return;
        }
        String type = fields.get("type");
        if (type == null) {
            return;
        }
        switch (type) {
            case "dgb" -> handleGift(state, fields);
            case "chatmsg" -> handleDanmaku(state, fields);
            case "onlinegift" -> handleFreeGift(state, fields);
            default -> {
                // 其他消息无需处理
            }
        }
    }

    private void handleGift(RoomState state, Map<String, String> fields) {
        String uid = fields.get("uid");
        long count = parseLong(fields.get("giftCount"), 1);
        long price = parseLong(fields.get("price"), 0);
        String giftType = fields.get("giftType");
        boolean paid = price > 0 && !FREE_GIFT_TYPE.equals(giftType);

        state.totalValueCents += price * count;
        if (paid) {
            state.paidValueCents += price * count;
        }
        if (StringUtils.hasText(uid)) {
            if (paid) {
                state.paidNewUsers.add(uid);
            }
            state.totalNewUsers.add(uid);
        }
    }

    private void handleFreeGift(RoomState state, Map<String, String> fields) {
        String uid = fields.get("uid");
        if (StringUtils.hasText(uid)) {
            state.totalNewUsers.add(uid);
        }
    }

    private void handleDanmaku(RoomState state, Map<String, String> fields) {
        state.danmuCount++;
        String uid = fields.get("uid");
        if (StringUtils.hasText(uid)) {
            state.danmuNewUsers.add(uid);
        }
    }

    private void flushRoom(RoomState state) {
        if (state == null) {
            return;
        }
        state.accumulateStreamSeconds();
        LocalDate today = LocalDate.now();
        YearMonth month = YearMonth.now();
        try {
            saveStat(state, PERIOD_TYPE_DAY, today.toString(), today);
            saveStat(state, PERIOD_TYPE_MONTH, month.toString(), null);
        } catch (Exception e) {
            log.warn("[斗鱼] 房间 {} 落库失败: {}", state.roomId, e.getMessage());
        }
    }

    private void saveStat(RoomState state, String periodType, String periodKey, LocalDate day) {
        LambdaQueryWrapper<YunAnchorGiftStat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YunAnchorGiftStat::getAnchorId, state.anchorId)
                .eq(YunAnchorGiftStat::getPeriodType, periodType)
                .eq(YunAnchorGiftStat::getPeriodKey, periodKey);
        YunAnchorGiftStat stat = giftStatMapper.selectOne(wrapper);
        boolean create = stat == null;
        if (create) {
            stat = new YunAnchorGiftStat();
            stat.setAnchorId(state.anchorId);
            stat.setPeriodType(periodType);
            stat.setPeriodKey(periodKey);
        }

        stat.setRoomId(state.roomId);
        stat.setPaidGiftValue(centsToYuan(state.paidValueCents));
        stat.setGiftTotalValue(centsToYuan(state.totalValueCents));
        stat.setBagGiftValue(BigDecimal.ZERO);
        stat.setPaidGiftUserCount(state.baselinePaidUserCount + state.paidNewUsers.size());
        stat.setGiftUserCount(state.baselineTotalUserCount + state.totalNewUsers.size());
        stat.setDanmuCount((int) Math.min(Integer.MAX_VALUE, state.danmuCount));
        stat.setDanmuUserCount(state.baselineDanmuUserCount + state.danmuNewUsers.size());
        stat.setStreamHours(BigDecimal.valueOf(state.streamSeconds)
                .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP));
        stat.setRoomStatus(1);
        stat.setLived(true);
        stat.setDurationText(day == null ? yearMonthText(periodKey) : day.toString() + " 实时");
        stat.setSourceUpdateTime(SOURCE_CODE);
        stat.setSyncedAt(LocalDateTime.now());

        if (create) {
            giftStatMapper.insert(stat);
        } else {
            giftStatMapper.updateById(stat);
        }
    }

    private String yearMonthText(String periodKey) {
        return periodKey + " 实时";
    }

    private void seedFromDb(RoomState state) {
        LocalDate today = LocalDate.now();
        YearMonth month = YearMonth.now();
        seedOne(state, PERIOD_TYPE_DAY, today.toString());
        seedOne(state, PERIOD_TYPE_MONTH, month.toString());
    }

    private void seedOne(RoomState state, String periodType, String periodKey) {
        try {
            LambdaQueryWrapper<YunAnchorGiftStat> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(YunAnchorGiftStat::getAnchorId, state.anchorId)
                    .eq(YunAnchorGiftStat::getPeriodType, periodType)
                    .eq(YunAnchorGiftStat::getPeriodKey, periodKey);
            YunAnchorGiftStat stat = giftStatMapper.selectOne(wrapper);
            if (stat == null) {
                return;
            }
            state.paidValueCents = yuanToCents(stat.getPaidGiftValue());
            state.totalValueCents = yuanToCents(stat.getGiftTotalValue());
            state.danmuCount = stat.getDanmuCount() == null ? 0 : stat.getDanmuCount().longValue();
            state.baselinePaidUserCount = stat.getPaidGiftUserCount() == null ? 0 : stat.getPaidGiftUserCount();
            state.baselineTotalUserCount = stat.getGiftUserCount() == null ? 0 : stat.getGiftUserCount();
            state.baselineDanmuUserCount = stat.getDanmuUserCount() == null ? 0 : stat.getDanmuUserCount();
            if (stat.getStreamHours() != null) {
                state.streamSeconds = (long) (stat.getStreamHours().doubleValue() * 3600);
            }
        } catch (Exception e) {
            log.warn("[斗鱼] 房间 {} 初始化基线数据失败: {}", state.roomId, e.getMessage());
        }
    }

    private List<YunAnchor> enabledAnchors() {
        LambdaQueryWrapper<YunAnchor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YunAnchor::getStatus, 1)
                .orderByAsc(YunAnchor::getSort)
                .orderByDesc(YunAnchor::getId);
        return anchorMapper.selectList(wrapper);
    }

    private long parseLong(String value, long defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return (long) Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private BigDecimal centsToYuan(long cents) {
        return BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private long yuanToCents(BigDecimal yuan) {
        if (yuan == null) {
            return 0;
        }
        return yuan.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    /**
     * 单个直播间的实时累计状态。
     */
    private static class RoomState {
        final String anchorId;
        final String roomId;
        DouyuDanmakuClient client;

        volatile long paidValueCents;
        volatile long totalValueCents;
        volatile long danmuCount;
        volatile long streamSeconds;
        volatile long lastFlushMs = System.currentTimeMillis();

        int baselinePaidUserCount;
        int baselineTotalUserCount;
        int baselineDanmuUserCount;

        final Set<String> paidNewUsers = new CopyOnWriteArraySet<>();
        final Set<String> totalNewUsers = new CopyOnWriteArraySet<>();
        final Set<String> danmuNewUsers = new CopyOnWriteArraySet<>();

        RoomState(String anchorId, String roomId) {
            this.anchorId = anchorId;
            this.roomId = roomId;
        }

        void accumulateStreamSeconds() {
            long now = System.currentTimeMillis();
            if (client != null && client.isConnected()) {
                streamSeconds += (now - lastFlushMs) / 1000;
            }
            lastFlushMs = now;
        }
    }
}