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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 在看（doseeing）主播公开接口客户端。
 */
@Component
@RequiredArgsConstructor
public class DoseeingClient implements AnchorDataClient {

    private static final String SOURCE_CODE = "DOSEEING";
    private static final String BASE_URL = "https://www.doseeing.com";
    private static final String ROOM_STAT_URL = BASE_URL + "/api/room_stat";
    private static final String SUGGEST_URL = BASE_URL + "/api/suggest_all";
    private static final String CONFIG_GROUP_YUN = "yunDataSource";
    private static final int TIMEOUT_MS = 10000;

    // ========== 风控防护：指数退避 + 熔断 ==========
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

    private final SystemConfigHelper configHelper;

    @Override
    public String sourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public BojiangAnchorInfo fetchAnchorProfile(String anchorId) {
        String normalizedAnchorId = requireAnchorId(anchorId);
        try {
            return fetchRoomStat(normalizedAnchorId, "today", null, null);
        } catch (RuntimeException todayError) {
            try {
                return fetchRoomStat(normalizedAnchorId, "yesterday", LocalDate.now().minusDays(1), null);
            } catch (RuntimeException yesterdayError) {
                return fetchSuggestAnchorProfile(normalizedAnchorId);
            }
        }
    }

    @Override
    public BojiangAnchorInfo fetchDailyAnchor(String anchorId, LocalDate date) {
        String normalizedAnchorId = requireAnchorId(anchorId);
        LocalDate today = LocalDate.now();
        String hours;
        if (date == null || Objects.equals(date, today)) {
            hours = "today";
        } else if (Objects.equals(date, today.minusDays(1))) {
            hours = "yesterday";
        } else {
            throw new BusinessException("在看公开接口仅支持同步今天和昨天，不支持历史日期");
        }
        return fetchRoomStat(normalizedAnchorId, hours, date == null ? today : date, null);
    }

    @Override
    public BojiangAnchorInfo fetchMonthAnchor(String anchorId, YearMonth month) {
        String normalizedAnchorId = requireAnchorId(anchorId);
        YearMonth currentMonth = YearMonth.now();
        if (month != null && !Objects.equals(month, currentMonth)) {
            throw new BusinessException("在看公开接口仅支持同步本月，不支持历史月份");
        }
        return fetchRoomStat(normalizedAnchorId, "thismonth", null, month == null ? currentMonth : month);
    }

    private BojiangAnchorInfo fetchRoomStat(String anchorId, String hours, LocalDate date, YearMonth month) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("room", anchorId);
        params.put("hours", hours);
        JSONObject json = requestJson(ROOM_STAT_URL, params, anchorId);
        JSONArray stats = json.getJSONArray("stats");
        if (stats == null || stats.isEmpty()) {
            throw new BusinessException("在看未返回主播统计: " + anchorId);
        }
        JSONObject stat = JSONUtil.parseObj(stats.get(0));
        JSONObject room = json.getJSONObject("room");
        JSONObject meta = json.getJSONObject("meta");
        return toAnchorInfo(anchorId, stat, room, meta, date, month, hours, json);
    }

    private BojiangAnchorInfo fetchSuggestAnchorProfile(String anchorId) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "room");
        params.put("nickname", anchorId);
        JSONObject json = requestJson(SUGGEST_URL, params, anchorId);
        JSONObject suggest = json.getJSONObject("suggest");
        JSONArray rooms = suggest == null ? null : suggest.getJSONArray("room");
        if (rooms == null || rooms.isEmpty()) {
            throw new BusinessException("在看未匹配到主播ID: " + anchorId);
        }
        for (Object item : rooms) {
            JSONObject row = JSONUtil.parseObj(item);
            if (anchorId.equals(text(row, "user_id"))) {
                BojiangAnchorInfo info = new BojiangAnchorInfo();
                info.setAnchorId(anchorId);
                info.setRoomId(anchorId);
                info.setAnchorName(text(row, "nickname"));
                info.setRawJson(row.toString());
                return info;
            }
        }
        throw new BusinessException("在看未匹配到主播ID: " + anchorId);
    }

    private JSONObject requestJson(String url, Map<String, Object> params, String roomId) {
        // 熔断期内快速失败；有失败历史时按指数退避等待，避免再次被风控
        throttleIfNeeded();
        String proxyBase = getProxy();
        // 仅当日/月统计类请求（含 room/hours 参数）走代理，suggest 兜底接口直连在看
        boolean useProxy = StringUtils.hasText(proxyBase)
                && params.containsKey("room")
                && params.containsKey("hours");
        String response;
        try {
            response = useProxy
                    ? requestViaProxy(proxyBase, params, roomId)
                    : requestDirect(url, params, roomId);
        } catch (BusinessException e) {
            onRequestFailure();
            throw e;
        }

        if (!StringUtils.hasText(response)) {
            onRequestFailure();
            throw new BusinessException("在看接口返回为空");
        }
        try {
            JSONObject parsed = JSONUtil.parseObj(response);
            onRequestSuccess();
            return parsed;
        } catch (Exception e) {
            onRequestFailure();
            throw new BusinessException("在看接口返回格式异常");
        }
    }

    /**
     * 请求前节流：熔断期内快速失败；否则按连续失败次数指数退避。
     */
    private void throttleIfNeeded() {
        long now = System.currentTimeMillis();
        long openUntil = circuitOpenUntilMs.get();
        if (now < openUntil) {
            throw new BusinessException("在看数据源已触发熔断，暂停请求（剩余 " + (openUntil - now) / 1000 + " 秒）");
        }
        int failures = consecutiveFailures.get();
        if (failures > 0) {
            long backoffMs = Math.min(BACKOFF_BASE_MS * (1L << Math.min(failures - 1, 10)), BACKOFF_MAX_MS);
            log.info("在看数据源最近连续失败 {} 次，退避 {} ms 后重试", failures, backoffMs);
            sleep(backoffMs);
        }
    }

    /**
     * 请求成功：清零连续失败计数。
     */
    private void onRequestSuccess() {
        consecutiveFailures.set(0);
    }

    /**
     * 请求失败：累加连续失败次数，达到阈值则触发熔断。
     */
    private void onRequestFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= CIRCUIT_FAILURE_THRESHOLD) {
            circuitOpenUntilMs.set(System.currentTimeMillis() + CIRCUIT_OPEN_MS);
            log.warn("在看数据源连续失败 {} 次，触发熔断 {} 秒", failures, CIRCUIT_OPEN_MS / 1000);
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

    /**
     * 直连在看接口。
     */
    private String requestDirect(String url, Map<String, Object> params, String roomId) {
        String response;
        try (HttpResponse httpResponse = HttpRequest.get(url)
                .form(params)
                .timeout(TIMEOUT_MS)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0")
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("Referer", BASE_URL + "/room/" + roomId)
                .header("Cookie", getCookie())
                .execute()) {
            checkHttpStatus(httpResponse);
            response = httpResponse.body();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("请求在看接口失败: " + e.getMessage());
        }
        return response;
    }

    /**
     * 通过阿里云函数(在看代理)请求：代理返回 {@code {reachable, status, body}}，body 为在看原始 JSON 字符串。
     */
    private String requestViaProxy(String proxyBase, Map<String, Object> params, String roomId) {
        String room = String.valueOf(params.get("room"));
        String hours = String.valueOf(params.get("hours"));
        StringBuilder url = new StringBuilder(proxyBase)
                .append("/probe?room=").append(room)
                .append("&hours=").append(hours);
        String cookie = getCookie();
        if (StringUtils.hasText(cookie)) {
            url.append("&cookie=").append(URLEncoder.encode(cookie, StandardCharsets.UTF_8));
        }
        try (HttpResponse httpResponse = HttpRequest.get(url.toString())
                .timeout(TIMEOUT_MS + 15000)
                .execute()) {
            String fcBody = httpResponse.body();
            JSONObject fcJson = JSONUtil.parseObj(fcBody);
            if (!fcJson.getBool("reachable", false)) {
                throw new BusinessException("在看代理抓取失败: HTTP " + httpResponse.getStatus() + " " + shortText(fcBody));
            }
            String body = fcJson.getStr("body");
            if (!StringUtils.hasText(body)) {
                throw new BusinessException("在看代理返回为空");
            }
            return body;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("请求在看代理接口失败: " + e.getMessage());
        }
    }

    private void checkHttpStatus(HttpResponse httpResponse) {
        if (httpResponse.getStatus() >= 300 && httpResponse.getStatus() < 400) {
            throw new BusinessException("在看接口需要登录或被重定向: HTTP " + httpResponse.getStatus());
        }
        if (!httpResponse.isOk()) {
            throw new BusinessException("在看接口 HTTP " + httpResponse.getStatus());
        }
    }

    private String shortText(String value) {
        if (value == null) {
            return "";
        }
        return value.length() > 500 ? value.substring(0, 500) : value;
    }

    private BojiangAnchorInfo toAnchorInfo(String anchorId, JSONObject stat, JSONObject room, JSONObject meta,
                                           LocalDate date, YearMonth month, String hours, JSONObject raw) {
        BojiangAnchorInfo info = new BojiangAnchorInfo();
        info.setAnchorId(anchorId);
        info.setRoomId(firstText(stat, room, "rid"));
        if (!StringUtils.hasText(info.getRoomId())) {
            info.setRoomId(anchorId);
        }
        info.setAnchorName(room == null ? null : text(room, "nn"));
        info.setAvatarUrl(normalizeAvatar(room == null ? null : text(room, "av")));
        info.setRoomTitle(room == null ? null : text(room, "rn"));
        info.setCategoryName(firstCategoryName(room == null ? null : text(room, "cid_names")));
        info.setGuildNo(room == null ? null : text(room, "gonghui_id"));
        info.setGuildName(room == null ? null : text(room, "gonghui_name"));
        info.setGiftTotalValue(centsToYuan(decimal(stat, "gift.all.price")));
        info.setPaidGiftValue(centsToYuan(decimal(stat, "gift.paid.price")));
        info.setBagGiftValue(info.getGiftTotalValue().subtract(info.getPaidGiftValue()).max(BigDecimal.ZERO));
        info.setPaidGiftUserCount(integer(stat, "gift.paid.uv"));
        info.setStreamHours(streamHours(meta));
        info.setFishballGiftCount(BigDecimal.ZERO);
        info.setGiftUserCount(integer(stat, "gift.all.uv"));
        info.setActiveAudienceCount(room == null ? null : integer(room, "ol"));
        info.setDanmuCount(integer(stat, "chat.pv"));
        info.setDanmuUserCount(integer(stat, "chat.uv"));
        info.setDurationText(durationText(meta, hours));
        info.setRoomStatus((room == null ? null : integer(room, "ol")) == null ? null : 1);
        info.setLived(true);
        info.setLastStartTime(room == null ? null : timestampText(room.get("ts")));
        info.setSourceUpdateTime(month != null ? month.toString() : date != null ? date.toString() : hours);
        info.setRawJson(raw.toString());
        return info;
    }

    private String getCookie() {
        return configHelper.getString(CONFIG_GROUP_YUN, "doseeingCookie", "");
    }

    private String getProxy() {
        return configHelper.getString(CONFIG_GROUP_YUN, "doseeingProxy", "");
    }

    private String requireAnchorId(String anchorId) {
        if (!StringUtils.hasText(anchorId)) {
            throw new BusinessException("主播ID不能为空");
        }
        return anchorId.trim();
    }

    private String firstCategoryName(String categoryNames) {
        if (!StringUtils.hasText(categoryNames)) {
            return null;
        }
        String first = categoryNames.split(",", 2)[0].trim();
        return limit(first, 100);
    }

    private String limit(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private String normalizeAvatar(String avatar) {
        if (!StringUtils.hasText(avatar)) {
            return null;
        }
        String value = avatar.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        if (value.startsWith("//")) {
            return "https:" + value;
        }
        return "https://apic.douyucdn.cn/upload/" + value + "_big.jpg";
    }

    private String durationText(JSONObject meta, String hours) {
        if (meta == null) {
            return hours;
        }
        String count = text(meta, "count");
        String unit = text(meta, "unit");
        if (!StringUtils.hasText(count) || !StringUtils.hasText(unit)) {
            return hours;
        }
        return "统计周期：" + count + ("day".equals(unit) ? "天" : "分钟");
    }

    private BigDecimal streamHours(JSONObject meta) {
        if (meta == null) {
            return null;
        }
        String count = text(meta, "count");
        String unit = text(meta, "unit");
        if (!StringUtils.hasText(count)) {
            return null;
        }
        try {
            BigDecimal n = new BigDecimal(count.replace(",", "").trim());
            if ("day".equals(unit)) {
                return n.multiply(BigDecimal.valueOf(24)).setScale(2, RoundingMode.HALF_UP);
            }
            if ("minute".equals(unit) || "min".equals(unit)) {
                return n.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            }
            return n.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    private String timestampText(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return StringUtils.hasText(text) ? text : null;
    }

    private String firstText(JSONObject a, JSONObject b, String key) {
        String value = a == null ? null : text(a, key);
        return StringUtils.hasText(value) ? value : b == null ? null : text(b, key);
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
}
