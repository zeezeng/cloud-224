package com.mars.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mars.biz.dto.YunRankingRecord;
import com.mars.biz.dto.YunRankingResponse;
import com.mars.biz.entity.YunAnchor;
import com.mars.biz.entity.YunAnchorGiftStat;
import com.mars.biz.mapper.YunAnchorGiftStatMapper;
import com.mars.biz.mapper.YunAnchorMapper;
import com.mars.biz.service.YunRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 云224小程序排行 Service 实现
 */
@Service
@RequiredArgsConstructor
public class YunRankingServiceImpl implements YunRankingService {

    private static final String PERIOD_TYPE_DAY = "DAY";
    private static final String PERIOD_TYPE_MONTH = "MONTH";

    private final YunAnchorMapper anchorMapper;
    private final YunAnchorGiftStatMapper giftStatMapper;

    @Override
    public YunRankingResponse getAnchorGiftRanking(String period, Integer page, Integer pageSize, String keyword) {
        PeriodInfo periodInfo = resolvePeriod(period);
        int currentPage = Math.max(page == null ? 1 : page, 1);
        int size = Math.min(Math.max(pageSize == null ? 20 : pageSize, 1), 100);

        LambdaQueryWrapper<YunAnchor> anchorWrapper = new LambdaQueryWrapper<>();
        anchorWrapper.eq(YunAnchor::getStatus, 1)
                .eq(YunAnchor::getShowRank, 1)
                .orderByAsc(YunAnchor::getSort)
                .orderByDesc(YunAnchor::getId);
        List<YunAnchor> anchors = anchorMapper.selectList(anchorWrapper);

        List<String> anchorIds = anchors.stream().map(YunAnchor::getAnchorId).filter(StringUtils::hasText).toList();
        Map<String, YunAnchor> anchorMap = anchors.stream()
                .collect(Collectors.toMap(YunAnchor::getAnchorId, Function.identity(), (a, b) -> a));

        List<YunRankingRecord> sortedRecords = anchorIds.isEmpty()
                ? List.of()
                : buildSortedRecords(periodInfo, anchorMap, anchorIds);

        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim().toLowerCase();
            sortedRecords = sortedRecords.stream()
                    .filter(record -> matches(record, value))
                    .collect(Collectors.toList());
        }

        for (int i = 0; i < sortedRecords.size(); i++) {
            sortedRecords.get(i).setRankNo(i + 1);
        }

        int from = Math.min((currentPage - 1) * size, sortedRecords.size());
        int to = Math.min(from + size, sortedRecords.size());
        List<YunRankingRecord> pageList = sortedRecords.subList(from, to);
        LocalDateTime latestSyncTime = sortedRecords.stream()
                .map(YunRankingRecord::getSyncedAt)
                .filter(item -> item != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        YunRankingResponse response = new YunRankingResponse();
        response.setPeriod(periodInfo.period());
        response.setPeriodKey(periodInfo.periodKey());
        response.setPeriodLabel(periodInfo.label());
        response.setLatestSyncTime(latestSyncTime);
        response.setTotal((long) sortedRecords.size());
        response.setPage((long) currentPage);
        response.setPageSize((long) size);
        response.setList(pageList);
        return response;
    }

    private boolean matches(YunRankingRecord record, String value) {
        return contains(record.getName(), value)
                || contains(record.getAnchorId(), value)
                || contains(record.getRoomId(), value)
                || contains(record.getGuildName(), value);
    }

    private boolean contains(String source, String value) {
        return StringUtils.hasText(source) && source.toLowerCase().contains(value);
    }

    private List<YunRankingRecord> buildSortedRecords(PeriodInfo periodInfo, Map<String, YunAnchor> anchorMap, List<String> anchorIds) {
        LambdaQueryWrapper<YunAnchorGiftStat> statWrapper = new LambdaQueryWrapper<>();
        statWrapper.in(YunAnchorGiftStat::getAnchorId, anchorIds)
                .eq(YunAnchorGiftStat::getPeriodType, periodInfo.periodType())
                .eq(YunAnchorGiftStat::getPeriodKey, periodInfo.periodKey());
        return giftStatMapper.selectList(statWrapper).stream()
                .map(stat -> toRecord(anchorMap.get(stat.getAnchorId()), stat))
                .filter(record -> record != null)
                .sorted(Comparator
                        .comparing(YunRankingRecord::getPaidGiftValue, Comparator.nullsLast(BigDecimal::compareTo)).reversed()
                        .thenComparing(YunRankingRecord::getAnchorId, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    private YunRankingRecord toRecord(YunAnchor anchor, YunAnchorGiftStat stat) {
        if (anchor == null) {
            return null;
        }
        YunRankingRecord record = new YunRankingRecord();
        record.setAnchorId(anchor.getAnchorId());
        record.setRoomId(StringUtils.hasText(anchor.getRoomId()) ? anchor.getRoomId() : anchor.getAnchorId());
        record.setName(StringUtils.hasText(anchor.getAnchorName()) ? anchor.getAnchorName() : anchor.getAnchorId());
        record.setAvatar(anchor.getAvatarUrl());
        record.setGuildName(isValidGuildName(anchor.getGuildName()) ? anchor.getGuildName() : null);
        record.setGiftTotalValue(stat.getGiftTotalValue() == null ? BigDecimal.ZERO : stat.getGiftTotalValue());
        record.setPaidGiftValue(stat.getPaidGiftValue() == null ? BigDecimal.ZERO : stat.getPaidGiftValue());
        record.setGiftUserCount(stat.getGiftUserCount());
        record.setSyncedAt(stat.getSyncedAt());
        return record;
    }

    /**
     * 判断公会名是否为有意义的中文名称，排除 UUID 或纯标识符类字符串。
     */
    private static boolean isValidGuildName(String guildName) {
        if (!StringUtils.hasText(guildName)) {
            return false;
        }
        // 排除纯字母/数字/连字符/下划线组成的标识符（如 UUID、短码）
        return !guildName.trim().matches("^[a-zA-Z0-9\\-_]+$");
    }

    private PeriodInfo resolvePeriod(String period) {
        String normalizedPeriod = StringUtils.hasText(period) ? period.trim().toLowerCase() : "today";
        LocalDate today = LocalDate.now();
        return switch (normalizedPeriod) {
            case "yesterday" -> new PeriodInfo("yesterday", PERIOD_TYPE_DAY, today.minusDays(1).toString(), "昨日榜单");
            case "month" -> new PeriodInfo("month", PERIOD_TYPE_MONTH, YearMonth.now().toString(), "本月榜单");
            default -> new PeriodInfo("today", PERIOD_TYPE_DAY, today.toString(), "今日榜单");
        };
    }

    private record PeriodInfo(String period, String periodType, String periodKey, String label) {
    }
}
