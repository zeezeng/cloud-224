package com.mars.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mars.biz.dto.BojiangAnchorInfo;
import com.mars.biz.dto.YunAnchorBatchCreateResult;
import com.mars.biz.dto.YunAnchorPageRow;
import com.mars.biz.dto.YunSyncProgress;
import com.mars.biz.dto.YunSyncResult;
import com.mars.biz.entity.YunAnchor;
import com.mars.biz.entity.YunAnchorGiftStat;
import com.mars.biz.mapper.YunAnchorGiftStatMapper;
import com.mars.biz.mapper.YunAnchorMapper;
import com.mars.biz.service.AnchorDataClient;
import com.mars.biz.service.YunAnchorGiftSyncService;
import com.mars.biz.service.YunAnchorService;
import com.mars.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 云224主播 Service 实现
 */
@Service
@RequiredArgsConstructor
public class YunAnchorServiceImpl extends ServiceImpl<YunAnchorMapper, YunAnchor> implements YunAnchorService {

    private static final int STATUS_ENABLED = 1;
    private static final int SHOW_RANK_ENABLED = 1;
    private static final int AUTO_UPDATE_PROFILE_ENABLED = 1;
    private static final String DATA_SOURCE_MANUAL = "MANUAL";
    private static final String DATA_SOURCE_DOSEEING = "DOSEEING";
    private static final String PERIOD_TYPE_DAY = "DAY";
    private static final String PERIOD_TYPE_MONTH = "MONTH";
    private static final int MAX_BATCH_CREATE_SIZE = 100;

    private final List<AnchorDataClient> anchorDataClients;
    private final YunAnchorGiftSyncService syncService;
    private final YunAnchorGiftStatMapper giftStatMapper;

    @Override
    public Page<YunAnchorPageRow> page(Integer page, Integer pageSize, String anchorId, String anchorName,
                                       String roomId, String guildName, Integer status) {
        Page<YunAnchor> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<YunAnchor> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(anchorId)) {
            wrapper.like(YunAnchor::getAnchorId, anchorId.trim());
        }
        if (StringUtils.hasText(anchorName)) {
            wrapper.like(YunAnchor::getAnchorName, anchorName.trim());
        }
        if (StringUtils.hasText(roomId)) {
            wrapper.like(YunAnchor::getRoomId, roomId.trim());
        }
        if (StringUtils.hasText(guildName)) {
            wrapper.like(YunAnchor::getGuildName, guildName.trim());
        }
        if (status != null) {
            wrapper.eq(YunAnchor::getStatus, status);
        }
        wrapper.orderByAsc(YunAnchor::getSort).orderByDesc(YunAnchor::getId);
        Page<YunAnchor> anchorPage = this.page(pageParam, wrapper);

        List<String> anchorIds = anchorPage.getRecords().stream()
                .map(YunAnchor::getAnchorId)
                .filter(StringUtils::hasText)
                .toList();
        LocalDate todayDate = LocalDate.now();
        Map<String, YunAnchorGiftStat> todayStats = statMap(anchorIds, PERIOD_TYPE_DAY, todayDate.toString());
        Map<String, YunAnchorGiftStat> yesterdayStats = statMap(anchorIds, PERIOD_TYPE_DAY, todayDate.minusDays(1).toString());
        Map<String, YunAnchorGiftStat> monthStats = statMap(anchorIds, PERIOD_TYPE_MONTH, YearMonth.now().toString());

        List<YunAnchorPageRow> rows = anchorPage.getRecords().stream().map(anchor -> {
            YunAnchorPageRow row = new YunAnchorPageRow();
            BeanUtils.copyProperties(anchor, row);
            YunAnchorGiftStat today = todayStats.get(anchor.getAnchorId());
            YunAnchorGiftStat yesterday = yesterdayStats.get(anchor.getAnchorId());
            YunAnchorGiftStat month = monthStats.get(anchor.getAnchorId());
            row.setTodayGiftValue(today == null ? BigDecimal.ZERO : today.getGiftTotalValue());
            row.setYesterdayGiftValue(yesterday == null ? BigDecimal.ZERO : yesterday.getGiftTotalValue());
            row.setMonthGiftValue(month == null ? BigDecimal.ZERO : month.getGiftTotalValue());
            row.setTodaySyncedAt(today == null ? null : today.getSyncedAt());
            row.setYesterdaySyncedAt(yesterday == null ? null : yesterday.getSyncedAt());
            row.setMonthSyncedAt(month == null ? null : month.getSyncedAt());
            return row;
        }).toList();

        Page<YunAnchorPageRow> result = new Page<>(anchorPage.getCurrent(), anchorPage.getSize(), anchorPage.getTotal());
        result.setRecords(rows);
        return result;
    }

    @Override
    public YunAnchor fetchPreview(String anchorId) {
        return fetchPreview(anchorId, DATA_SOURCE_DOSEEING);
    }

    @Override
    public YunAnchor fetchPreview(String anchorId, String dataSource) {
        AnchorDataClient client = resolveClient(dataSource);
        BojiangAnchorInfo info = client.fetchAnchorProfile(anchorId);
        YunAnchor anchor = toAnchor(info);
        anchor.setStatus(STATUS_ENABLED);
        anchor.setShowRank(SHOW_RANK_ENABLED);
        anchor.setSort(0);
        anchor.setDataSource(client.sourceCode());
        anchor.setAutoUpdateProfile(AUTO_UPDATE_PROFILE_ENABLED);
        return anchor;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(YunAnchor anchor) {
        normalizeAndValidate(anchor, true);
        this.save(anchor);
    }

    @Override
    public YunAnchorBatchCreateResult batchCreate(List<String> anchorIds) {
        return batchCreate(anchorIds, DATA_SOURCE_DOSEEING);
    }

    @Override
    public YunAnchorBatchCreateResult batchCreate(List<String> anchorIds, String dataSource) {
        List<String> normalizedAnchorIds = normalizeBatchAnchorIds(anchorIds);
        AnchorDataClient client = resolveClient(dataSource);
        YunAnchorBatchCreateResult result = new YunAnchorBatchCreateResult();
        result.setTotalCount(normalizedAnchorIds.size());

        for (String anchorId : normalizedAnchorIds) {
            try {
                if (existsByAnchorId(anchorId)) {
                    result.getErrors().add(anchorId + "：主播ID已存在");
                    continue;
                }
                YunAnchor anchor = previewAnchor(anchorId, client);
                anchor.setAnchorId(anchorId);
                normalizeAndValidate(anchor, true);
                this.save(anchor);
                result.getSuccessAnchorIds().add(anchorId);
            } catch (Exception e) {
                result.getErrors().add(anchorId + "：" + e.getMessage());
            }
        }

        result.setSuccessCount(result.getSuccessAnchorIds().size());
        result.setFailCount(result.getErrors().size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException("主播ID不能为空");
        }
        YunAnchor anchor = new YunAnchor();
        anchor.setId(id);
        anchor.setStatus(status);
        this.updateById(anchor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShowRank(Long id, Integer showRank) {
        if (id == null) {
            throw new BusinessException("主播ID不能为空");
        }
        YunAnchor anchor = new YunAnchor();
        anchor.setId(id);
        anchor.setShowRank(showRank);
        this.updateById(anchor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(YunAnchor anchor) {
        if (anchor == null || anchor.getId() == null) {
            throw new BusinessException("主播ID不能为空");
        }
        normalizeAndValidate(anchor, false);
        this.updateById(anchor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
        this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public YunSyncResult sync(Long id) {
        return sync(id, null);
    }

    @Override
    public YunSyncResult sync(Long id, String dataSource) {
        return syncService.syncAnchor(id, "MANUAL", dataSource);
    }

    @Override
    public YunSyncResult syncAll() {
        return syncAll(null);
    }

    @Override
    public YunSyncResult syncAll(String dataSource) {
        return syncService.syncAll("MANUAL", dataSource);
    }

    @Override
    public YunSyncProgress startSyncAll(String dataSource) {
        return syncService.startSyncAll("MANUAL", dataSource);
    }

    @Override
    public YunSyncProgress getSyncAllProgress(String taskId) {
        return syncService.getSyncAllProgress(taskId);
    }

    private Map<String, YunAnchorGiftStat> statMap(List<String> anchorIds, String periodType, String periodKey) {
        if (anchorIds == null || anchorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<YunAnchorGiftStat> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(YunAnchorGiftStat::getAnchorId, anchorIds)
                .eq(YunAnchorGiftStat::getPeriodType, periodType)
                .eq(YunAnchorGiftStat::getPeriodKey, periodKey);
        return giftStatMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(YunAnchorGiftStat::getAnchorId, Function.identity(), (a, b) -> a));
    }

    private List<String> normalizeBatchAnchorIds(List<String> anchorIds) {
        if (anchorIds == null || anchorIds.isEmpty()) {
            throw new BusinessException("主播ID不能为空");
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String anchorId : anchorIds) {
            if (StringUtils.hasText(anchorId)) {
                normalized.add(anchorId.trim());
            }
        }
        if (normalized.isEmpty()) {
            throw new BusinessException("主播ID不能为空");
        }
        if (normalized.size() > MAX_BATCH_CREATE_SIZE) {
            throw new BusinessException("单次最多批量新增" + MAX_BATCH_CREATE_SIZE + "个主播");
        }
        return new ArrayList<>(normalized);
    }

    private boolean existsByAnchorId(String anchorId) {
        LambdaQueryWrapper<YunAnchor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YunAnchor::getAnchorId, anchorId);
        return this.count(wrapper) > 0;
    }

    private void normalizeAndValidate(YunAnchor anchor, boolean create) {
        if (anchor == null) {
            throw new BusinessException("主播信息不能为空");
        }
        if (!StringUtils.hasText(anchor.getAnchorId())) {
            throw new BusinessException("主播ID不能为空");
        }
        anchor.setAnchorId(anchor.getAnchorId().trim());
        if (!StringUtils.hasText(anchor.getRoomId())) {
            anchor.setRoomId(anchor.getAnchorId());
        } else {
            anchor.setRoomId(anchor.getRoomId().trim());
        }
        trimFields(anchor);
        if (anchor.getStatus() == null) {
            anchor.setStatus(STATUS_ENABLED);
        }
        if (anchor.getShowRank() == null) {
            anchor.setShowRank(SHOW_RANK_ENABLED);
        }
        if (anchor.getSort() == null) {
            anchor.setSort(0);
        }
        if (anchor.getAutoUpdateProfile() == null) {
            anchor.setAutoUpdateProfile(AUTO_UPDATE_PROFILE_ENABLED);
        }
        if (!StringUtils.hasText(anchor.getDataSource())) {
            anchor.setDataSource(DATA_SOURCE_MANUAL);
        }

        LambdaQueryWrapper<YunAnchor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YunAnchor::getAnchorId, anchor.getAnchorId());
        if (!create) {
            wrapper.ne(YunAnchor::getId, anchor.getId());
        }
        if (this.count(wrapper) > 0) {
            throw new BusinessException("主播ID已存在");
        }
    }

    private void trimFields(YunAnchor anchor) {
        anchor.setAnchorName(trim(anchor.getAnchorName(), 100));
        anchor.setAvatarUrl(trim(anchor.getAvatarUrl(), 500));
        anchor.setRoomTitle(trim(anchor.getRoomTitle(), 255));
        anchor.setCategoryId(trim(anchor.getCategoryId(), 64));
        anchor.setCategoryName(trim(anchor.getCategoryName(), 100));
        anchor.setGuildNo(trim(anchor.getGuildNo(), 100));
        anchor.setGuildName(trim(anchor.getGuildName(), 100));
        anchor.setBio(trim(anchor.getBio(), 1000));
        anchor.setDataSource(trim(anchor.getDataSource(), 32));
        anchor.setRemark(trim(anchor.getRemark(), 500));
    }

    private String trim(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private AnchorDataClient resolveClient(String dataSource) {
        String source = normalizeSource(dataSource);
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
        return DATA_SOURCE_MANUAL.equals(value) || "AUTO".equals(value) || "BOJIANG".equals(value) ? null : value;
    }

    private YunAnchor previewAnchor(String anchorId, AnchorDataClient client) {
        BojiangAnchorInfo info = client.fetchAnchorProfile(anchorId);
        YunAnchor anchor = toAnchor(info);
        anchor.setStatus(STATUS_ENABLED);
        anchor.setShowRank(SHOW_RANK_ENABLED);
        anchor.setSort(0);
        anchor.setDataSource(client.sourceCode());
        anchor.setAutoUpdateProfile(AUTO_UPDATE_PROFILE_ENABLED);
        return anchor;
    }

    private YunAnchor toAnchor(BojiangAnchorInfo info) {
        YunAnchor anchor = new YunAnchor();
        anchor.setAnchorId(info.getAnchorId());
        anchor.setRoomId(info.getRoomId());
        anchor.setAnchorName(info.getAnchorName());
        anchor.setAvatarUrl(info.getAvatarUrl());
        anchor.setRoomTitle(info.getRoomTitle());
        anchor.setCategoryId(info.getCategoryId());
        anchor.setCategoryName(info.getCategoryName());
        anchor.setGuildNo(info.getGuildNo());
        anchor.setGuildName(info.getGuildName());
        anchor.setRoomStatus(info.getRoomStatus());
        anchor.setLastStartTime(info.getLastStartTime());
        return anchor;
    }
}
