package com.mars.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mars.biz.dto.BojiangAnchorInfo;
import com.mars.biz.dto.YunSyncProgress;
import com.mars.biz.dto.YunSyncResult;
import com.mars.biz.entity.YunAnchor;
import com.mars.biz.entity.YunAnchorGiftStat;
import com.mars.biz.entity.YunSyncLog;
import com.mars.biz.mapper.YunAnchorGiftStatMapper;
import com.mars.biz.mapper.YunAnchorMapper;
import com.mars.biz.mapper.YunSyncLogMapper;
import com.mars.biz.service.AnchorDataClient;
import com.mars.biz.service.YunAnchorGiftSyncService;
import com.mars.common.exception.BusinessException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 云224主播礼物同步 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YunAnchorGiftSyncServiceImpl implements YunAnchorGiftSyncService {

    private static final String SYNC_TYPE_ANCHOR_GIFT = "ANCHOR_GIFT";
    private static final String PERIOD_TYPE_DAY = "DAY";
    private static final String PERIOD_TYPE_MONTH = "MONTH";
    private static final String STATUS_ENABLED = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_PARTIAL = "PARTIAL";
    private static final String BATCH_ABORT_MESSAGE = "首个失败已中止后续批量同步";

    private static final String DATA_SOURCE_DOSEEING = "DOSEEING";

    // ========== 风控防护：按需缓存 + 随机间隔 ==========
    /** AUTO 自动同步的远程拉取缓存有效期(ms)：有效期内不再重复请求远程接口 */
    private static final long CACHE_TTL_MS = 10 * 60 * 1000L;
    /** 每次真实远程请求前的随机间隔下限(ms)，打散请求节奏避免被风控 */
    private static final long RANDOM_DELAY_MIN_MS = 500L;
    /** 随机间隔上限(ms) */
    private static final long RANDOM_DELAY_MAX_MS = 1500L;

    /**
     * 最近一次从远程成功拉取的缓存（key: anchorId:periodKey -> 时间戳）。
     * 仅对 AUTO 定时同步生效，手动同步始终强制刷新。
     */
    private final Map<String, Long> lastRemoteFetchAt = new ConcurrentHashMap<>();

    private final List<AnchorDataClient> anchorDataClients;
    private final YunAnchorMapper anchorMapper;
    private final YunAnchorGiftStatMapper giftStatMapper;
    private final YunSyncLogMapper syncLogMapper;

    /**
     * 异步同步任务执行器（单线程，避免多个全量同步并发写库）。
     */
    private final ExecutorService syncExecutor = Executors.newSingleThreadExecutor();

    /**
     * 异步同步任务进度缓存（taskId -> progress）。
     */
    private final Map<String, YunSyncProgress> syncProgressMap = new ConcurrentHashMap<>();

    @PreDestroy
    public void shutdownSyncExecutor() {
        syncExecutor.shutdownNow();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YunSyncResult syncAnchor(Long id, String triggerType) {
        return syncAnchor(id, triggerType, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YunSyncResult syncAnchor(Long id, String triggerType, String dataSource) {
        YunAnchor anchor = anchorMapper.selectById(id);
        if (anchor == null) {
            throw new BusinessException("主播不存在");
        }
        return syncAnchors(List.of(anchor), currentPeriods(true, true, true), triggerType, dataSource, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YunSyncResult syncAll(String triggerType) {
        return syncAll(triggerType, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YunSyncResult syncAll(String triggerType, String dataSource) {
        return syncAnchors(enabledAnchors(), currentPeriods(true, true, true), triggerType, dataSource, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YunSyncResult syncTodayAndMonth(String triggerType) {
        return syncTodayAndMonth(triggerType, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YunSyncResult syncTodayAndMonth(String triggerType, String dataSource) {
        return syncAnchors(enabledAnchors(), currentPeriods(true, false, true), triggerType, dataSource, false);
    }

    private List<YunAnchor> enabledAnchors() {
        LambdaQueryWrapper<YunAnchor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YunAnchor::getStatus, 1)
                .orderByAsc(YunAnchor::getSort)
                .orderByDesc(YunAnchor::getId);
        return anchorMapper.selectList(wrapper);
    }

    @Override
    public YunSyncProgress startSyncAll(String triggerType, String dataSource) {
        List<YunAnchor> anchors = enabledAnchors();
        List<SyncPeriod> periods = currentPeriods(true, true, true);
        List<String> finishedTaskIds = syncProgressMap.values().stream()
                .filter(progress -> Boolean.FALSE.equals(progress.getRunning()))
                .map(YunSyncProgress::getTaskId)
                .toList();
        finishedTaskIds.forEach(syncProgressMap::remove);

        YunSyncProgress progress = new YunSyncProgress();
        progress.setTaskId(UUID.randomUUID().toString());
        progress.setRunning(true);
        progress.setTotalCount(anchors.size() * periods.size());
        progress.setStartedAt(LocalDateTime.now());
        syncProgressMap.put(progress.getTaskId(), progress);

        syncExecutor.submit(() -> {
            YunSyncResult result = new YunSyncResult();
            result.setStartedAt(progress.getStartedAt());
            result.setTotalCount(progress.getTotalCount());
            try {
                for (YunAnchor anchor : anchors) {
                    for (SyncPeriod period : periods) {
                        progress.setCurrentAnchorId(anchor.getAnchorId());
                        progress.setCurrentPeriodKey(period.periodKey());
                        boolean success = syncOne(anchor, period, dataSource, triggerType, result);
                        progress.setSuccessCount(result.getSuccessCount());
                        progress.setFailCount(result.getFailCount());
                        progress.setCompletedCount(progress.getCompletedCount() + 1);
                        if (!success) {
                            appendBatchAbortMessage(result);
                        }
                        progress.setErrors(new ArrayList<>(result.getErrors()));
                        if (!success) {
                            break;
                        }
                    }
                    if (result.getFailCount() > 0) {
                        break;
                    }
                }
                result.setEndedAt(LocalDateTime.now());
                saveLog(result, periods, triggerType);
                progress.setErrors(new ArrayList<>(result.getErrors()));
            } catch (Exception e) {
                log.error("异步同步全部主播失败", e);
            } finally {
                progress.setRunning(false);
                progress.setEndedAt(LocalDateTime.now());
            }
        });
        return progress;
    }

    @Override
    public YunSyncProgress getSyncAllProgress(String taskId) {
        YunSyncProgress progress = syncProgressMap.get(taskId);
        if (progress == null) {
            throw new BusinessException("同步任务不存在或已过期");
        }
        return progress;
    }

    private YunSyncResult syncAnchors(List<YunAnchor> anchors, List<SyncPeriod> periods, String triggerType,
                                      String requestedDataSource, boolean stopOnFirstFailure) {
        YunSyncResult result = new YunSyncResult();
        LocalDateTime startedAt = LocalDateTime.now();
        result.setStartedAt(startedAt);
        result.setTotalCount(anchors.size() * periods.size());

        for (YunAnchor anchor : anchors) {
            for (SyncPeriod period : periods) {
                boolean success = syncOne(anchor, period, requestedDataSource, triggerType, result);
                if (stopOnFirstFailure && !success) {
                    appendBatchAbortMessage(result);
                    result.setEndedAt(LocalDateTime.now());
                    saveLog(result, periods, triggerType);
                    return result;
                }
            }
        }

        result.setEndedAt(LocalDateTime.now());
        saveLog(result, periods, triggerType);
        return result;
    }

    private boolean syncOne(YunAnchor anchor, SyncPeriod period, String requestedDataSource, String triggerType,
                            YunSyncResult result) {
        String cacheKey = anchor.getAnchorId() + ":" + period.periodKey();
        try {
            // AUTO 定时同步且缓存未过期时，跳过远程请求直接复用库中数据
            if (shouldSkipForCache(cacheKey, triggerType)) {
                result.setSuccessCount(result.getSuccessCount() + 1);
                return true;
            }
            // 真实远程请求前随机等待，打散请求节奏降低风控风险
            sleepRandomDelay();
            AnchorDataClient client = resolveClient(requestedDataSource, anchor.getDataSource());
            BojiangAnchorInfo info = period.month() == null
                    ? client.fetchDailyAnchor(anchor.getAnchorId(), period.date())
                    : client.fetchMonthAnchor(anchor.getAnchorId(), period.month());
            saveStat(anchor, info, period, client.sourceCode());
            updateAnchorProfile(anchor, info, client.sourceCode());
            lastRemoteFetchAt.put(cacheKey, System.currentTimeMillis());
            result.setSuccessCount(result.getSuccessCount() + 1);
            return true;
        } catch (Exception e) {
            result.setFailCount(result.getFailCount() + 1);
            String message = anchor.getAnchorId() + " " + period.periodKey() + ": " + e.getMessage();
            if (result.getErrors().size() < 20) {
                result.getErrors().add(message);
            }
            log.warn("同步主播礼物数据失败: {}", message);
            return false;
        }
    }

    /**
     * AUTO 自动同步且缓存未过期时跳过远程请求；手动同步始终强制刷新。
     */
    private boolean shouldSkipForCache(String cacheKey, String triggerType) {
        if (!"AUTO".equals(triggerType)) {
            return false;
        }
        Long lastFetch = lastRemoteFetchAt.get(cacheKey);
        return lastFetch != null && System.currentTimeMillis() - lastFetch < CACHE_TTL_MS;
    }

    private void sleepRandomDelay() {
        long delay = RANDOM_DELAY_MIN_MS
                + ThreadLocalRandom.current().nextLong(RANDOM_DELAY_MAX_MS - RANDOM_DELAY_MIN_MS + 1);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void appendBatchAbortMessage(YunSyncResult result) {
        if (result.getErrors().size() >= 20 || result.getErrors().contains(BATCH_ABORT_MESSAGE)) {
            return;
        }
        result.getErrors().add(BATCH_ABORT_MESSAGE);
    }

    private void saveStat(YunAnchor anchor, BojiangAnchorInfo info, SyncPeriod period, String sourceCode) {
        LambdaQueryWrapper<YunAnchorGiftStat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YunAnchorGiftStat::getAnchorId, anchor.getAnchorId())
                .eq(YunAnchorGiftStat::getPeriodType, period.periodType())
                .eq(YunAnchorGiftStat::getPeriodKey, period.periodKey());
        YunAnchorGiftStat stat = giftStatMapper.selectOne(wrapper);
        boolean create = stat == null;
        if (create) {
            stat = new YunAnchorGiftStat();
            stat.setAnchorId(anchor.getAnchorId());
            stat.setPeriodType(period.periodType());
            stat.setPeriodKey(period.periodKey());
        }

        stat.setRoomId(valueOrFallback(info.getRoomId(), anchor.getRoomId()));
        stat.setExternalRankNo(info.getExternalRankNo());
        stat.setGiftTotalValue(info.getGiftTotalValue());
        stat.setPaidGiftValue(info.getPaidGiftValue());
        stat.setBagGiftValue(info.getBagGiftValue());
        stat.setFishballGiftCount(info.getFishballGiftCount());
        stat.setGiftUserCount(info.getGiftUserCount());
        stat.setPaidGiftUserCount(info.getPaidGiftUserCount());
        stat.setStreamHours(info.getStreamHours());
        stat.setActiveAudienceCount(info.getActiveAudienceCount());
        stat.setDanmuCount(info.getDanmuCount());
        stat.setDanmuUserCount(info.getDanmuUserCount());
        stat.setDurationText(info.getDurationText());
        stat.setRoomStatus(info.getRoomStatus());
        stat.setLived(info.getLived());
        stat.setLastStartTime(info.getLastStartTime());
        stat.setSourceUpdateTime(valueOrFallback(info.getSourceUpdateTime(), sourceCode));
        stat.setRawJson(info.getRawJson());
        stat.setSyncedAt(LocalDateTime.now());

        if (create) {
            giftStatMapper.insert(stat);
        } else {
            giftStatMapper.updateById(stat);
        }
    }

    private void updateAnchorProfile(YunAnchor anchor, BojiangAnchorInfo info, String sourceCode) {
        YunAnchor update = new YunAnchor();
        update.setId(anchor.getId());
        update.setLastGiftSyncTime(LocalDateTime.now());
        update.setRoomStatus(info.getRoomStatus());
        update.setLastStartTime(info.getLastStartTime());

        if (anchor.getAutoUpdateProfile() == null || anchor.getAutoUpdateProfile() == 1) {
            update.setRoomId(limit(valueOrFallback(info.getRoomId(), anchor.getRoomId()), 64));
            update.setAnchorName(limit(valueOrFallback(info.getAnchorName(), anchor.getAnchorName()), 100));
            update.setAvatarUrl(limit(valueOrFallback(info.getAvatarUrl(), anchor.getAvatarUrl()), 500));
            update.setRoomTitle(limit(valueOrFallback(info.getRoomTitle(), anchor.getRoomTitle()), 255));
            update.setCategoryId(limit(valueOrFallback(info.getCategoryId(), anchor.getCategoryId()), 64));
            update.setCategoryName(limit(valueOrFallback(info.getCategoryName(), anchor.getCategoryName()), 100));
            update.setGuildNo(limit(valueOrFallback(info.getGuildNo(), anchor.getGuildNo()), 100));
            update.setGuildName(limit(valueOrFallback(info.getGuildName(), anchor.getGuildName()), 100));
            update.setDataSource(sourceCode);
            update.setLastProfileSyncTime(LocalDateTime.now());
        }

        anchorMapper.updateById(update);
    }

    private void saveLog(YunSyncResult result, List<SyncPeriod> periods, String triggerType) {
        YunSyncLog log = new YunSyncLog();
        log.setSyncType(SYNC_TYPE_ANCHOR_GIFT);
        log.setPeriodType(periods.size() == 1 ? periods.get(0).periodType() : "MIXED");
        log.setPeriodKey(periods.size() == 1 ? periods.get(0).periodKey() : periods.stream()
                .map(SyncPeriod::periodKey)
                .reduce((a, b) -> a + "," + b)
                .orElse(""));
        log.setTriggerType(StringUtils.hasText(triggerType) ? triggerType : "MANUAL");
        log.setStatus(result.getFailCount() == 0 ? STATUS_ENABLED : result.getSuccessCount() > 0 ? STATUS_PARTIAL : STATUS_FAILED);
        log.setTotalCount(result.getTotalCount());
        log.setSuccessCount(result.getSuccessCount());
        log.setFailCount(result.getFailCount());
        log.setErrorMessage(result.getErrors().isEmpty() ? null : String.join("; ", result.getErrors()));
        log.setStartedAt(result.getStartedAt());
        log.setEndedAt(result.getEndedAt());
        syncLogMapper.insert(log);
    }

    private List<SyncPeriod> currentPeriods(boolean today, boolean yesterday, boolean month) {
        LocalDate now = LocalDate.now();
        List<SyncPeriod> periods = new ArrayList<>();
        if (today) {
            periods.add(new SyncPeriod(PERIOD_TYPE_DAY, now.toString(), now, null));
        }
        if (yesterday) {
            LocalDate yesterdayDate = now.minusDays(1);
            periods.add(new SyncPeriod(PERIOD_TYPE_DAY, yesterdayDate.toString(), yesterdayDate, null));
        }
        if (month) {
            YearMonth currentMonth = YearMonth.now();
            periods.add(new SyncPeriod(PERIOD_TYPE_MONTH, currentMonth.toString(), null, currentMonth));
        }
        return periods;
    }

    private AnchorDataClient resolveClient(String requestedDataSource, String anchorDataSource) {
        String source = normalizeSource(requestedDataSource);
        if (!StringUtils.hasText(source)) {
            source = normalizeSource(anchorDataSource);
        }
        if (!StringUtils.hasText(source)) {
            source = DATA_SOURCE_DOSEEING;
        }
        Map<String, AnchorDataClient> clientMap = anchorDataClients.stream()
                .collect(Collectors.toMap(client -> normalizeSource(client.sourceCode()), client -> client, (a, b) -> a));
        AnchorDataClient client = clientMap.get(source);
        if (client == null) {
            throw new BusinessException("不支持的数据源: " + source + "，可选 " + DATA_SOURCE_DOSEEING);
        }
        return client;
    }

    private String normalizeSource(String dataSource) {
        if (!StringUtils.hasText(dataSource)) {
            return null;
        }
        String value = dataSource.trim().toUpperCase(Locale.ROOT);
        return "MANUAL".equals(value) || "AUTO".equals(value) || "BOJIANG".equals(value) ? null : value;
    }

    private String valueOrFallback(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String limit(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private record SyncPeriod(String periodType, String periodKey, LocalDate date, YearMonth month) {
    }
}
