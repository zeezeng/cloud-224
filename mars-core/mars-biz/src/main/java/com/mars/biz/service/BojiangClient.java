package com.mars.biz.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mars.biz.dto.BojiangAnchorInfo;
import com.mars.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 播酱公开接口客户端
 */
@Component
public class BojiangClient implements AnchorDataClient {

    private static final String BASE_URL = "https://www.bojianger.com";
    private static final String DAILY_ANCHOR_LIST = BASE_URL + "/data/api/common/anchor_list.do";
    private static final String MONTH_ANCHOR_LIST = BASE_URL + "/data/api/common/anchor_list_month.do";
    private static final String SEARCH_ANCHOR = BASE_URL + "/data/api/common/search_anchor_new.do";
    private static final int TIMEOUT_MS = 10000;
    private static final int PAGE_SIZE = 20;
    private static final int MAX_PUBLIC_LIST_PAGES = 150;

    @Override
    public String sourceCode() {
        return "BOJIANG";
    }

    @Override
    public BojiangAnchorInfo fetchAnchorProfile(String anchorId) {
        String normalizedAnchorId = requireAnchorId(anchorId);
        try {
            return fetchDailyAnchor(normalizedAnchorId, LocalDate.now());
        } catch (RuntimeException todayError) {
            try {
                return fetchDailyAnchor(normalizedAnchorId, LocalDate.now().minusDays(1));
            } catch (RuntimeException yesterdayError) {
                try {
                    return fetchMonthAnchor(normalizedAnchorId, YearMonth.now());
                } catch (RuntimeException monthError) {
                    return fetchSearchAnchorProfile(normalizedAnchorId);
                }
            }
        }
    }

    @Override
    public BojiangAnchorInfo fetchDailyAnchor(String anchorId, LocalDate date) {
        String normalizedAnchorId = requireAnchorId(anchorId);
        Map<String, Object> params = baseParams(normalizedAnchorId);
        params.put("date", date.toString());
        JSONObject row = requestPagedAndFind(DAILY_ANCHOR_LIST, params, normalizedAnchorId);
        return toAnchorInfo(row);
    }

    @Override
    public BojiangAnchorInfo fetchMonthAnchor(String anchorId, YearMonth month) {
        String normalizedAnchorId = requireAnchorId(anchorId);
        Map<String, Object> params = baseParams(normalizedAnchorId);
        params.put("month", month.toString());
        JSONObject row = requestPagedAndFind(MONTH_ANCHOR_LIST, params, normalizedAnchorId);
        return toAnchorInfo(row);
    }

    private BojiangAnchorInfo fetchSearchAnchorProfile(String anchorId) {
        JSONObject row = requestAndFind(SEARCH_ANCHOR, searchParams(anchorId), anchorId);
        return toAnchorInfo(row);
    }

    private JSONObject requestPagedAndFind(String url, Map<String, Object> params, String anchorId) {
        Map<String, Object> pageParams = new LinkedHashMap<>(params);
        pageParams.put("pageSize", PAGE_SIZE);
        int maxPages = MAX_PUBLIC_LIST_PAGES;
        int scannedPages = 0;

        for (int pageNum = 1; pageNum <= maxPages; pageNum++) {
            pageParams.put("pageNum", pageNum);
            JSONObject data = requestData(url, pageParams);
            scannedPages++;
            JSONObject row = findRow(data, anchorId);
            if (row != null) {
                return row;
            }

            Integer pages = data.getInt("pages");
            if (pages != null && pages > 0) {
                maxPages = Math.min(MAX_PUBLIC_LIST_PAGES, pages);
            }
            Boolean hasNextPage = data.getBool("hasNextPage");
            if (Boolean.FALSE.equals(hasNextPage)) {
                break;
            }
        }

        throw new BusinessException("播酱公开榜单已扫描 " + scannedPages + " 页，未匹配到主播ID: " + anchorId);
    }

    private JSONObject requestAndFind(String url, Map<String, Object> params, String anchorId) {
        JSONObject data = requestData(url, params);
        JSONObject row = findRow(data, anchorId);
        if (row != null) {
            return row;
        }
        throw new BusinessException("播酱未匹配到主播ID: " + anchorId);
    }

    private JSONObject requestData(String url, Map<String, Object> params) {
        String response;
        try {
            response = HttpUtil.get(url, params, TIMEOUT_MS);
        } catch (Exception e) {
            throw new BusinessException("请求播酱接口失败: " + e.getMessage());
        }

        if (!StringUtils.hasText(response)) {
            throw new BusinessException("播酱接口返回为空");
        }

        JSONObject json;
        try {
            json = JSONUtil.parseObj(response);
        } catch (Exception e) {
            throw new BusinessException("播酱接口返回格式异常");
        }

        Integer code = json.getInt("code");
        if (!Objects.equals(code, 0)) {
            throw new BusinessException("播酱接口异常: " + json.getStr("msg", "未知错误"));
        }

        JSONObject data = json.getJSONObject("data");
        if (data == null) {
            throw new BusinessException("播酱接口未返回数据");
        }
        return data;
    }

    private JSONObject findRow(JSONObject data, String anchorId) {
        JSONArray rows = data.getJSONArray("rows");
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        for (Object item : rows) {
            JSONObject row = JSONUtil.parseObj(item);
            if (anchorId.equals(String.valueOf(row.get("rid")))) {
                return row;
            }
        }
        return null;
    }

    private Map<String, Object> baseParams(String anchorId) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("keyword", anchorId);
        params.put("categoryName", "total");
        params.put("categoryId", 0);
        params.put("clubName", "total");
        params.put("clubNo", "total");
        params.put("orderBy", "yc_gift_value");
        params.put("getType", "all");
        params.put("pageNum", 1);
        params.put("pageSize", PAGE_SIZE);
        return params;
    }

    private Map<String, Object> searchParams(String anchorId) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("keyword", anchorId);
        params.put("tit", "p1");
        params.put("total", false);
        params.put("pageNum", 1);
        params.put("pageSize", 20);
        return params;
    }

    private BojiangAnchorInfo toAnchorInfo(JSONObject row) {
        BojiangAnchorInfo info = new BojiangAnchorInfo();
        info.setAnchorId(text(row, "rid"));
        info.setRoomId(text(row, "rid"));
        info.setAnchorName(text(row, "name"));
        info.setAvatarUrl(text(row, "avator"));
        info.setRoomTitle(text(row, "room_title"));
        info.setCategoryId(text(row, "cate_id"));
        info.setCategoryName(text(row, "cate_name"));
        info.setGuildNo(text(row, "club_no"));
        info.setGuildName(text(row, "club_name"));
        info.setExternalRankNo(integer(row, "num"));
        info.setGiftTotalValue(decimal(row, "yc_gift_value"));
        info.setPaidGiftValue(decimal(row, "gift_new_yc"));
        info.setBagGiftValue(decimal(row, "gift_new_bag"));
        info.setFishballGiftCount(decimal(row, "gift_new_yw"));
        info.setGiftUserCount(integer(row, "gift_person_count"));
        info.setActiveAudienceCount(integer(row, "audience_count"));
        info.setDanmuCount(integer(row, "danmu_count"));
        info.setDanmuUserCount(integer(row, "danmu_person_count"));
        info.setDurationText(text(row, "duration"));
        info.setRoomStatus(integer(row, "room_status"));
        info.setLived(bool(row, "lived"));
        info.setLastStartTime(firstText(row, "lastStartTime", "start_time"));
        info.setSourceUpdateTime(firstText(row, "update_time", "expire_mark", "date"));
        info.setRawJson(row.toString());
        return info;
    }

    private String requireAnchorId(String anchorId) {
        if (!StringUtils.hasText(anchorId)) {
            throw new BusinessException("主播ID不能为空");
        }
        return anchorId.trim();
    }

    private String text(JSONObject row, String key) {
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    private String firstText(JSONObject row, String... keys) {
        for (String key : keys) {
            String value = text(row, key);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private Integer integer(JSONObject row, String key) {
        Object value = row.get(key);
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
        Object value = row.get(key);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(String.valueOf(value).replace(",", "").trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Boolean bool(JSONObject row, String key) {
        Object value = row.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
