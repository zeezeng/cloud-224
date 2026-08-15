package com.mars.biz.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mars.biz.dto.BojiangAnchorInfo;
import com.mars.common.exception.BusinessException;
import com.mars.system.helper.SystemConfigHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 在看（doseeing）虎牙主播数据客户端。
 * <p>
 * 礼物/弹幕等统计走在看公开的虎牙 rank 接口（无需登录）：
 * {@code https://www.doseeing.com/huya/data/api/rank?rids={rid}&dt={0|1|thismonth}&rank_type=chat_pv}
 * 其中 dt=1 今日、dt=0 昨日、dt=thismonth 本月；主播资料（昵称/头像/分类/标题）解析虎牙房间页
 * 内置的 TT_PROFILE_INFO / TT_ROOM_DATA 对象。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DoseeingHuyaClient implements AnchorDataClient {

    private static final String SOURCE_CODE = "DOSEEING_HUYA";
    private static final String HUYA_BASE_URL = "https://www.doseeing.com/huya";
    private static final String RANK_URL = HUYA_BASE_URL + "/data/api/rank";
    private static final String HUYA_ROOM_DATA_URL = "https://www.doseeing.com/huya/data/room/";
    private static final String HUYA_ROOM_PAGE_URL = "https://www.huya.com/";
    private static final String CONFIG_GROUP_YUN = "yunDataSource";
    private static final int TIMEOUT_MS = 10000;

    private final SystemConfigHelper configHelper;

    // ========== 风控防护：指数退避 + 熔断（与斗鱼客户端一致） ==========
    /** 失败退避基数(ms)：第 n 次失败后等待 base * 2^(n-1) */
    private static final long BACKOFF_BASE_MS = 1000L;
    /** 退避上限(ms)，避免等待过久 */
    private static final long BACKOFF_MAX_MS = 16000L;
    /** 连续失败达到该次数后触发熔断 */
    private static final int CIRCUIT_FAILURE_THRESHOLD = 5;
    /** 熔断时长(ms)：熔断期内请求快速失败，不再发 HTTP 请求 */
    private static final long CIRCUIT_OPEN_MS = 60_000L;

    /** 连续失败次数（成功即清零） */
    private final AtomicInteger consecutiveFailures = new AtomicInteger();
    /** 熔断解除时间戳 */
    private final AtomicLong circuitOpenUntilMs = new AtomicLong(0L);

    private static final Pattern PROFILE_PATTERN = Pattern.compile("var TT_PROFILE_INFO = (\\{[^}]*\\})");
    private static final Pattern ROOM_DATA_PATTERN = Pattern.compile("var TT_ROOM_DATA = (\\{[^}]*\\})");
    /** 在看虎牙房间数据页中的"分类/公会"行，如：<div class="gonghui"><span>公会</span><span>集梦传媒</span></div> */
    private static final Pattern GONGHUI_DIV_PATTERN = Pattern.compile(
            "<div[^>]*class=\"gonghui\"[^>]*>\\s*<span[^>]*>(公会|分类)</span>\\s*<span[^>]*>([^<]*)</span>\\s*</div>");
    /** 主播昵称 */
    private static final Pattern NICKNAME_PATTERN = Pattern.compile(
            "<span[^>]*class=\"nickname\"[^>]*>([^<]*)</span>");
    /** 主播头像 */
    private static final Pattern AVATAR_PATTERN = Pattern.compile(
            "<img[^>]*src=\"(https://huyaimg\\.msstatic\\.com/avatar/[^\"]+)\"");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    @Override
    public String sourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public BojiangAnchorInfo fetchAnchorProfile(String anchorId) {
        String normalized = requireAnchorId(anchorId);
        BojiangAnchorInfo info = fetchRankStat(normalized, "0", null, null); // dt=0 为今日统计
        // 先解析虎牙官网资料（昵称/头像/分类/标题/状态/开播时间）
        fillProfileFromRoomPage(info, normalized);
        // 再尝试从在看虎牙房间数据页补充公会信息（需登录 Cookie，可单独失效不影响整体）
        fillProfileFromDoseeingPage(info, normalized);
        return info;
    }

    @Override
    public BojiangAnchorInfo fetchDailyAnchor(String anchorId, LocalDate date) {
        String normalized = requireAnchorId(anchorId);
        LocalDate today = LocalDate.now();
        LocalDate target = date == null ? today : date;
        String dt;
        if (Objects.equals(target, today)) {
            dt = "0"; // 在看虎牙 rank 接口 dt=0 表示今天（实测 dt=0=今日、dt=1=昨日）
        } else if (Objects.equals(target, today.minusDays(1))) {
            dt = "1"; // dt=1 表示昨天
        } else {
            throw new BusinessException("在看虎牙接口仅支持同步今天和昨天，不支持历史日期");
        }
        return fetchRankStat(normalized, dt, target, null);
    }

    @Override
    public BojiangAnchorInfo fetchMonthAnchor(String anchorId, YearMonth month) {
        String normalized = requireAnchorId(anchorId);
        YearMonth current = YearMonth.now();
        if (month != null && !Objects.equals(month, current)) {
            throw new BusinessException("在看虎牙接口仅支持同步本月，不支持历史月份");
        }
        return fetchRankStat(normalized, "thismonth", null, current);
    }

    private BojiangAnchorInfo fetchRankStat(String anchorId, String dt, LocalDate date, YearMonth month) {
        String url = RANK_URL + "?rids=" + anchorId + "&dt=" + dt + "&rank_type=chat_pv";
        JSONObject json = requestJson(url);
        JSONObject result = json == null ? null : json.getJSONObject("result");
        JSONArray rows = result == null ? null : result.getJSONArray("result");
        if (rows == null || rows.isEmpty()) {
            throw new BusinessException("在看虎牙未返回主播统计: " + anchorId);
        }
        JSONObject stat = JSONUtil.parseObj(rows.get(0));
        return toAnchorInfo(anchorId, stat, date, month, dt, json.toString());
    }

    /**
     * 解析虎牙房间页补充主播资料；失败仅告警，不影响统计结果。
     */
    private void fillProfileFromRoomPage(BojiangAnchorInfo info, String roomId) {
        try {
            String html = fetchRoomPage(roomId);
            JSONObject profile = extractJson(html, PROFILE_PATTERN);
            if (profile != null) {
                info.setAnchorName(text(profile, "nick"));
                info.setAvatarUrl(text(profile, "avatar"));
            }
            JSONObject roomData = extractJson(html, ROOM_DATA_PATTERN);
            if (roomData != null) {
                info.setCategoryName(text(roomData, "gameFullName"));
                info.setRoomTitle(text(roomData, "introduction"));
                String state = text(roomData, "state");
                info.setRoomStatus("ON".equals(state) ? 1 : ("OFF".equals(state) ? 0 : null));
                info.setLastStartTime(epochToTime(text(roomData, "startTime")));
            }
        } catch (Exception e) {
            log.warn("解析虎牙房间页资料失败，roomId={}: {}", roomId, e.getMessage());
        }
    }

    /**
     * 从在看虎牙房间数据页（{@code /huya/data/room/{rid}}）补充主播资料，重点解析公会信息。
     * <p>该页为服务端渲染 HTML 且需登录（带 doseeingCookie），未配置 Cookie、登录失效或请求失败时
     * 仅告警并返回 false，不影响已有资料与统计结果。
     */
    private boolean fillProfileFromDoseeingPage(BojiangAnchorInfo info, String roomId) {
        String cookie = getCookie();
        if (!StringUtils.hasText(cookie)) {
            log.info("未配置 doseeingCookie，跳过在看虎牙房间页解析（无公会信息），roomId={}", roomId);
            return false;
        }
        try {
            String html = fetchDoseeingRoomPage(roomId, cookie);
            if (isDoseeingLoginPage(html)) {
                log.warn("doseeingCookie 已失效，请求在看虎牙房间页被重定向到登录页，roomId={}", roomId);
                return false;
            }
            // 公会 / 分类
            Matcher gonghuiMatcher = GONGHUI_DIV_PATTERN.matcher(html);
            while (gonghuiMatcher.find()) {
                String label = gonghuiMatcher.group(1).trim();
                String value = gonghuiMatcher.group(2).trim();
                if ("公会".equals(label) && StringUtils.hasText(value)) {
                    info.setGuildName(value);
                } else if ("分类".equals(label) && StringUtils.hasText(value)) {
                    info.setCategoryName(value);
                }
            }
            // 昵称（虎牙官网未取到时再补充）
            Matcher nickMatcher = NICKNAME_PATTERN.matcher(html);
            if (nickMatcher.find() && !StringUtils.hasText(info.getAnchorName())) {
                info.setAnchorName(nickMatcher.group(1).trim());
            }
            // 头像（虎牙官网未取到时再补充）
            Matcher avatarMatcher = AVATAR_PATTERN.matcher(html);
            if (avatarMatcher.find() && !StringUtils.hasText(info.getAvatarUrl())) {
                info.setAvatarUrl(avatarMatcher.group(1).trim());
            }
            log.info("已从在看虎牙房间页解析资料，roomId={}, 公会={}, 分类={}", roomId, info.getGuildName(), info.getCategoryName());
            return true;
        } catch (Exception e) {
            log.warn("解析在看虎牙房间页资料失败，roomId={}: {}", roomId, e.getMessage());
            return false;
        }
    }

    private String fetchDoseeingRoomPage(String roomId, String cookie) {
        try (HttpResponse httpResponse = HttpRequest.get(HUYA_ROOM_DATA_URL + roomId)
                .timeout(TIMEOUT_MS)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0")
                .header("Accept", "text/html,application/xhtml+xml,*/*;q=0.8")
                .header("Referer", HUYA_BASE_URL + "/room/" + roomId)
                .header("Cookie", cookie)
                .execute()) {
            if (httpResponse.getStatus() >= 300 && httpResponse.getStatus() < 400) {
                throw new BusinessException("需要登录或被重定向: HTTP " + httpResponse.getStatus());
            }
            if (!httpResponse.isOk()) {
                throw new BusinessException("在看虎牙房间页 HTTP " + httpResponse.getStatus());
            }
            return httpResponse.body();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("请求在看虎牙房间页失败: " + e.getMessage());
        }
    }

    /** 判断是否为在看登录页（无 Cookie/登录失效时会被重定向到登录页） */
    private boolean isDoseeingLoginPage(String html) {
        return StringUtils.hasText(html)
                && (html.contains("微信登录") || html.contains("打开微信扫一扫")
                || html.contains("登录 - 在看直播排行榜"));
    }

    private String getCookie() {
        return configHelper.getString(CONFIG_GROUP_YUN, "doseeingCookie", "");
    }

    private String fetchRoomPage(String roomId) {
        try (HttpResponse httpResponse = HttpRequest.get(HUYA_ROOM_PAGE_URL + roomId)
                .timeout(TIMEOUT_MS)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0")
                .header("Accept", "text/html,application/xhtml+xml,*/*;q=0.8")
                .execute()) {
            if (!httpResponse.isOk()) {
                throw new BusinessException("虎牙房间页 HTTP " + httpResponse.getStatus());
            }
            return httpResponse.body();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("请求虎牙房间页失败: " + e.getMessage());
        }
    }

    private JSONObject extractJson(String html, Pattern pattern) {
        if (!StringUtils.hasText(html)) {
            return null;
        }
        Matcher matcher = pattern.matcher(html);
        if (!matcher.find()) {
            return null;
        }
        String json = matcher.group(1);
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return JSONUtil.parseObj(json);
        } catch (Exception e) {
            log.debug("解析虎牙房间页内嵌对象失败: {}", e.getMessage());
            return null;
        }
    }

    private JSONObject requestJson(String url) {
        // 熔断期内快速失败；有失败历史时按指数退避等待，避免再次被风控
        throttleIfNeeded();
        String response;
        try (HttpResponse httpResponse = HttpRequest.get(url)
                .timeout(TIMEOUT_MS)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0")
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("Referer", HUYA_BASE_URL + "/room/" + url)
                .execute()) {
            checkHttpStatus(httpResponse);
            response = httpResponse.body();
        } catch (BusinessException e) {
            onRequestFailure();
            throw e;
        } catch (Exception e) {
            onRequestFailure();
            throw new BusinessException("请求在看虎牙接口失败: " + e.getMessage());
        }

        if (!StringUtils.hasText(response)) {
            onRequestFailure();
            throw new BusinessException("在看虎牙接口返回为空");
        }
        try {
            JSONObject parsed = JSONUtil.parseObj(response);
            onRequestSuccess();
            return parsed;
        } catch (Exception e) {
            onRequestFailure();
            throw new BusinessException("在看虎牙接口返回格式异常");
        }
    }

    /**
     * 请求前节流：熔断期内快速失败；否则按连续失败次数指数退避。
     */
    private void throttleIfNeeded() {
        long now = System.currentTimeMillis();
        long openUntil = circuitOpenUntilMs.get();
        if (now < openUntil) {
            throw new BusinessException("在看虎牙数据源已触发熔断，暂停请求（剩余 " + (openUntil - now) / 1000 + " 秒）");
        }
        int failures = consecutiveFailures.get();
        if (failures > 0) {
            long backoffMs = Math.min(BACKOFF_BASE_MS * (1L << Math.min(failures - 1, 10)), BACKOFF_MAX_MS);
            log.info("在看虎牙数据源最近连续失败 {} 次，退避 {} ms 后重试", failures, backoffMs);
            sleep(backoffMs);
        }
    }

    private void onRequestSuccess() {
        consecutiveFailures.set(0);
    }

    private void onRequestFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= CIRCUIT_FAILURE_THRESHOLD) {
            circuitOpenUntilMs.set(System.currentTimeMillis() + CIRCUIT_OPEN_MS);
            log.warn("在看虎牙数据源连续失败 {} 次，触发熔断 {} 秒", failures, CIRCUIT_OPEN_MS / 1000);
        }
    }

    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void checkHttpStatus(HttpResponse httpResponse) {
        if (httpResponse.getStatus() >= 300 && httpResponse.getStatus() < 400) {
            throw new BusinessException("在看虎牙接口需要登录或被重定向: HTTP " + httpResponse.getStatus());
        }
        if (!httpResponse.isOk()) {
            throw new BusinessException("在看虎牙接口 HTTP " + httpResponse.getStatus());
        }
    }

    private BojiangAnchorInfo toAnchorInfo(String anchorId, JSONObject stat, LocalDate date, YearMonth month,
                                           String dt, String raw) {
        BojiangAnchorInfo info = new BojiangAnchorInfo();
        info.setAnchorId(anchorId);
        info.setRoomId(text(stat, "rid"));
        if (!StringUtils.hasText(info.getRoomId())) {
            info.setRoomId(anchorId);
        }
        BigDecimal giftTotal = centsToYuan(decimal(stat, "gift.all.price"));
        BigDecimal paid = centsToYuan(decimal(stat, "gift.paid.price"));
        info.setGiftTotalValue(giftTotal);
        info.setPaidGiftValue(paid);
        info.setBagGiftValue(giftTotal.subtract(paid).max(BigDecimal.ZERO));
        info.setPaidGiftUserCount(integer(stat, "gift.paid.uv"));
        info.setGiftUserCount(integer(stat, "gift.all.uv"));
        info.setActiveAudienceCount(integer(stat, "active.uv"));
        info.setDanmuCount(integer(stat, "chat.pv"));
        info.setDanmuUserCount(integer(stat, "chat.uv"));
        info.setStreamHours(minutesToHours(integer(stat, "online.minutes")));
        info.setFishballGiftCount(BigDecimal.ZERO);
        info.setLived(true);
        info.setDurationText(month != null ? "统计周期：本月" : ("0".equals(dt) ? "统计周期：今日" : "统计周期：昨日"));
        info.setSourceUpdateTime(month != null ? month.toString() : date != null ? date.toString() : dt);
        info.setRawJson(raw);
        return info;
    }

    private String requireAnchorId(String anchorId) {
        if (!StringUtils.hasText(anchorId)) {
            throw new BusinessException("主播ID不能为空");
        }
        return anchorId.trim();
    }

    private String text(JSONObject row, String key) {
        if (row == null) {
            return null;
        }
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    private Integer integer(JSONObject row, String key) {
        Object value = row == null ? null : row.get(key);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value).replace(",", "").trim()).intValue();
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal decimal(JSONObject row, String key) {
        Object value = row == null ? null : row.get(key);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(String.valueOf(value).replace(",", "").trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal centsToYuan(BigDecimal cents) {
        if (cents == null) {
            return BigDecimal.ZERO;
        }
        return cents.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal minutesToHours(Integer minutes) {
        if (minutes == null) {
            return null;
        }
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private String epochToTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            long epoch = Long.parseLong(value.trim());
            return Instant.ofEpochSecond(epoch).atZone(SHANGHAI).format(TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}
