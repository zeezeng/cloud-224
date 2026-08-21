package com.mars.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mars.biz.dto.YunAnchorPageRow;
import com.mars.biz.dto.YunSeasonAnchorPageRow;
import com.mars.biz.dto.YunSeasonCopyRequest;
import com.mars.biz.dto.YunSeasonMemberBatchRequest;
import com.mars.biz.dto.YunSeasonMemberBatchResult;
import com.mars.biz.dto.YunSeasonPageRow;
import com.mars.biz.entity.YunAnchor;
import com.mars.biz.entity.YunSeason;
import com.mars.biz.entity.YunSeasonAnchor;
import com.mars.biz.mapper.YunAnchorMapper;
import com.mars.biz.mapper.YunSeasonAnchorMapper;
import com.mars.biz.mapper.YunSeasonMapper;
import com.mars.biz.service.YunAnchorService;
import com.mars.biz.service.YunSeasonService;
import com.mars.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 主播赛季 Service 实现
 */
@Service
@RequiredArgsConstructor
public class YunSeasonServiceImpl extends ServiceImpl<YunSeasonMapper, YunSeason> implements YunSeasonService {

    private static final int STATUS_ENABLED = 1;
    private static final int APP_DISPLAY_ENABLED = 1;
    private static final int ELIMINATED_NO = 0;
    private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.ZERO;
    private static final String DATA_SOURCE_DOSEEING = "DOSEEING";
    private static final String DATA_SOURCE_DOSEEING_HUYA = "DOSEEING_HUYA";
    private static final String PLATFORM_DOUYU = "DOUYU";
    private static final String PLATFORM_HUYA = "HUYA";
    private static final int MAX_BATCH_ADD_SIZE = 500;

    private final YunSeasonAnchorMapper seasonAnchorMapper;
    private final YunAnchorMapper anchorMapper;
    private final YunAnchorService anchorService;

    @Override
    public Page<YunSeasonPageRow> page(Integer page, Integer pageSize, String seasonCode, String seasonName, Integer status) {
        Page<YunSeason> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<YunSeason> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(seasonCode)) {
            wrapper.like(YunSeason::getSeasonCode, seasonCode.trim());
        }
        if (StringUtils.hasText(seasonName)) {
            wrapper.like(YunSeason::getSeasonName, seasonName.trim());
        }
        if (status != null) {
            wrapper.eq(YunSeason::getStatus, status);
        }
        wrapper.orderByAsc(YunSeason::getSort).orderByDesc(YunSeason::getId);
        Page<YunSeason> seasonPage = this.page(pageParam, wrapper);

        List<Long> seasonIds = seasonPage.getRecords().stream()
                .map(YunSeason::getId)
                .filter(id -> id != null && id > 0)
                .toList();
        Map<Long, List<YunSeasonAnchor>> memberMap = membersBySeasonIds(seasonIds);

        List<YunSeasonPageRow> rows = seasonPage.getRecords().stream().map(season -> {
            List<YunSeasonAnchor> members = memberMap.getOrDefault(season.getId(), Collections.emptyList());
            YunSeasonPageRow row = new YunSeasonPageRow();
            BeanUtils.copyProperties(season, row);
            row.setMemberCount(members.size());
            row.setCaptainCount((int) members.stream().filter(member -> equalsOne(member.getCaptainFlag())).count());
            row.setEliminatedCount((int) members.stream().filter(member -> equalsOne(member.getEliminated())).count());
            row.setActiveCount((int) members.stream().filter(member -> !equalsOne(member.getEliminated())).count());
            return row;
        }).toList();

        Page<YunSeasonPageRow> result = new Page<>(seasonPage.getCurrent(), seasonPage.getSize(), seasonPage.getTotal());
        result.setRecords(rows);
        return result;
    }

    @Override
    public List<YunSeason> listEnabled() {
        LambdaQueryWrapper<YunSeason> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YunSeason::getStatus, STATUS_ENABLED)
                .orderByAsc(YunSeason::getSort)
                .orderByDesc(YunSeason::getId);
        return this.list(wrapper);
    }

    @Override
    public YunSeason currentAppSeason() {
        LambdaQueryWrapper<YunSeason> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YunSeason::getStatus, STATUS_ENABLED)
                .eq(YunSeason::getAppDisplay, APP_DISPLAY_ENABLED)
                .orderByAsc(YunSeason::getSort)
                .orderByDesc(YunSeason::getId)
                .last("LIMIT 1");
        return this.getOne(wrapper, false);
    }

    @Override
    public YunSeasonPageRow currentAppSeasonWithStats() {
        YunSeason season = currentAppSeason();
        if (season == null) {
            return null;
        }
        List<YunSeasonAnchor> members = seasonAnchorMapper.selectList(
                new LambdaQueryWrapper<YunSeasonAnchor>()
                        .eq(YunSeasonAnchor::getSeasonId, season.getId()));
        YunSeasonPageRow row = new YunSeasonPageRow();
        BeanUtils.copyProperties(season, row);
        row.setMemberCount(members.size());
        row.setCaptainCount((int) members.stream().filter(member -> equalsOne(member.getCaptainFlag())).count());
        row.setEliminatedCount((int) members.stream().filter(member -> equalsOne(member.getEliminated())).count());
        row.setActiveCount((int) members.stream().filter(member -> !equalsOne(member.getEliminated())).count());
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(YunSeason season) {
        normalizeAndValidateSeason(season, true);
        this.save(season);
        closeOtherAppDisplayIfNeeded(season);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(YunSeason season) {
        if (season == null || season.getId() == null) {
            throw new BusinessException("赛季ID不能为空");
        }
        normalizeAndValidateSeason(season, false);
        this.updateById(season);
        closeOtherAppDisplayIfNeeded(season);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
        seasonAnchorMapper.delete(new LambdaQueryWrapper<YunSeasonAnchor>().in(YunSeasonAnchor::getSeasonId, Arrays.asList(ids)));
        this.removeByIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YunSeason copy(Long id, YunSeasonCopyRequest request) {
        if (id == null) {
            throw new BusinessException("赛季ID不能为空");
        }
        YunSeason source = this.getById(id);
        if (source == null) {
            throw new BusinessException("赛季不存在");
        }
        YunSeason season = new YunSeason();
        season.setSeasonCode(trim(request == null ? null : request.getSeasonCode(), 32));
        if (!StringUtils.hasText(season.getSeasonCode())) {
            throw new BusinessException("请输入新的赛季编号");
        }
        season.setSeasonName(trim(firstText(request == null ? null : request.getSeasonName(), source.getSeasonName()), 100));
        season.setCoverImageUrl(trim(firstText(request == null ? null : request.getCoverImageUrl(), source.getCoverImageUrl()), 500));
        season.setStatus(request != null && request.getStatus() != null ? request.getStatus() : source.getStatus());
        season.setAppDisplay(0);
        season.setStartTime(source.getStartTime());
        season.setEndTime(source.getEndTime());
        season.setSort(source.getSort());
        season.setTotalBonus(source.getTotalBonus());
        season.setRemark(trim(firstText(request == null ? null : request.getRemark(), source.getRemark()), 500));
        normalizeAndValidateSeason(season, true);
        this.save(season);

        boolean copyMembers = request == null || request.getCopyMembers() == null || request.getCopyMembers();
        if (copyMembers) {
            List<YunSeasonAnchor> members = seasonAnchorMapper.selectList(
                    new LambdaQueryWrapper<YunSeasonAnchor>()
                            .eq(YunSeasonAnchor::getSeasonId, source.getId())
                            .orderByAsc(YunSeasonAnchor::getSort)
                            .orderByAsc(YunSeasonAnchor::getId)
            );
            int index = 0;
            for (YunSeasonAnchor member : members) {
                YunSeasonAnchor copied = copyMember(member, season.getId(), index++);
                seasonAnchorMapper.insert(copied);
            }
        }
        return season;
    }

    @Override
    public Page<YunSeasonAnchorPageRow> memberPage(Long seasonId, Integer page, Integer pageSize, String keyword,
                                                   Integer eliminated, Integer captainFlag) {
        Page<YunSeasonAnchor> pageParam = new Page<>(page, pageSize);
        Page<YunSeasonAnchor> memberPage = queryMemberPage(seasonId, pageParam, keyword, eliminated, captainFlag);
        List<YunSeasonAnchorPageRow> rows = toMemberRows(memberPage.getRecords());
        Page<YunSeasonAnchorPageRow> result = new Page<>(memberPage.getCurrent(), memberPage.getSize(), memberPage.getTotal());
        result.setRecords(rows);
        return result;
    }

    @Override
    public Page<YunAnchorPageRow> candidateAnchorPage(Long seasonId, Integer page, Integer pageSize, String anchorId,
                                                      String anchorName, String roomId, String guildName, Integer status) {
        if (seasonId == null) {
            throw new BusinessException("赛季ID不能为空");
        }
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

        Set<Long> joinedAnchorIds = joinedAnchorRefIds(seasonId);
        if (!joinedAnchorIds.isEmpty()) {
            wrapper.notIn(YunAnchor::getId, joinedAnchorIds);
        }
        wrapper.orderByAsc(YunAnchor::getSort).orderByDesc(YunAnchor::getId);

        Page<YunAnchor> anchorPage = anchorMapper.selectPage(pageParam, wrapper);
        List<YunAnchorPageRow> rows = anchorPage.getRecords().stream().map(anchor -> {
            YunAnchorPageRow row = new YunAnchorPageRow();
            BeanUtils.copyProperties(anchor, row);
            return row;
        }).toList();
        Page<YunAnchorPageRow> result = new Page<>(anchorPage.getCurrent(), anchorPage.getSize(), anchorPage.getTotal());
        result.setRecords(rows);
        return result;
    }

    @Override
    public List<YunSeasonAnchorPageRow> listMembers(Long seasonId) {
        List<YunSeasonAnchor> members = seasonAnchorMapper.selectList(
                new LambdaQueryWrapper<YunSeasonAnchor>()
                        .eq(YunSeasonAnchor::getSeasonId, seasonId)
                        .orderByAsc(YunSeasonAnchor::getEliminated)
                        .orderByDesc(YunSeasonAnchor::getCaptainFlag)
                        .orderByAsc(YunSeasonAnchor::getSort)
                        .orderByDesc(YunSeasonAnchor::getId)
        );
        return toMemberRows(members);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YunSeasonMemberBatchResult addMembers(Long seasonId, YunSeasonMemberBatchRequest request) {
        if (seasonId == null) {
            throw new BusinessException("赛季ID不能为空");
        }
        List<Long> anchorRefIds = normalizeBatchAnchorRefIds(request == null ? null : request.getAnchorRefIds());
        List<AnchorImportInput> anchorInputs = normalizeBatchAnchorInputs(request == null ? null : request.getAnchorIds());
        if (anchorRefIds.isEmpty() && anchorInputs.isEmpty()) {
            throw new BusinessException("请选择或输入主播");
        }
        YunSeasonMemberBatchResult result = new YunSeasonMemberBatchResult();
        result.setTotalCount(anchorRefIds.size() + anchorInputs.size());

        int sortIndex = 0;
        for (Long anchorRefId : anchorRefIds) {
            try {
                YunAnchor anchor = anchorMapper.selectById(anchorRefId);
                if (anchor == null) {
                    throw new BusinessException("主播不存在");
                }
                if (existsMember(seasonId, anchor.getId())) {
                    throw new BusinessException("该主播已加入当前赛季");
                }
                YunSeasonAnchor member = buildMemberFromAnchor(seasonId, anchor, request, sortIndex++);
                seasonAnchorMapper.insert(member);
                result.getSuccessAnchorIds().add(member.getAnchorId());
            }
            catch (Exception e) {
                result.getErrors().add(anchorRefId + "：" + e.getMessage());
            }
        }

        for (AnchorImportInput input : anchorInputs) {
            try {
                YunAnchor anchor = resolveOrCreateAnchor(input);
                if (existsMember(seasonId, anchor.getId())) {
                    throw new BusinessException("该主播已加入当前赛季");
                }
                YunSeasonAnchor member = buildMemberFromAnchor(seasonId, anchor, request, sortIndex++);
                seasonAnchorMapper.insert(member);
                result.getSuccessAnchorIds().add(member.getAnchorId());
            }
            catch (Exception e) {
                result.getErrors().add(input.rawInput() + "：" + e.getMessage());
            }
        }
        result.setSuccessCount(result.getSuccessAnchorIds().size());
        result.setFailCount(result.getErrors().size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMember(YunSeasonAnchor member) {
        if (member == null || member.getId() == null) {
            throw new BusinessException("赛季成员ID不能为空");
        }
        normalizeAndValidateMember(member);
        seasonAnchorMapper.updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMembers(Long seasonId, Long[] ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
        LambdaQueryWrapper<YunSeasonAnchor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(seasonId != null, YunSeasonAnchor::getSeasonId, seasonId)
                .in(YunSeasonAnchor::getId, Arrays.asList(ids));
        seasonAnchorMapper.delete(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetMembers(Long seasonId) {
        if (seasonId == null) {
            throw new BusinessException("赛季ID不能为空");
        }
        seasonAnchorMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<YunSeasonAnchor>()
                .eq(YunSeasonAnchor::getSeasonId, seasonId)
                .set(YunSeasonAnchor::getEliminated, ELIMINATED_NO)
                .set(YunSeasonAnchor::getFailTimes, 0)
                .set(YunSeasonAnchor::getNextEliminationAmount, DEFAULT_AMOUNT));
    }

    private Page<YunSeasonAnchor> queryMemberPage(Long seasonId, Page<YunSeasonAnchor> pageParam, String keyword,
                                                  Integer eliminated, Integer captainFlag) {
        LambdaQueryWrapper<YunSeasonAnchor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YunSeasonAnchor::getSeasonId, seasonId);
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            wrapper.and(query -> query.like(YunSeasonAnchor::getAnchorName, value)
                    .or()
                    .like(YunSeasonAnchor::getAnchorId, value)
                    .or()
                    .like(YunSeasonAnchor::getRoomId, value)
                    .or()
                    .like(YunSeasonAnchor::getTeamName, value));
        }
        if (eliminated != null) {
            wrapper.eq(YunSeasonAnchor::getEliminated, eliminated);
        }
        if (captainFlag != null) {
            wrapper.eq(YunSeasonAnchor::getCaptainFlag, captainFlag);
        }
        wrapper.orderByAsc(YunSeasonAnchor::getEliminated)
                .orderByAsc(YunSeasonAnchor::getFailTimes)
                .orderByDesc(YunSeasonAnchor::getCaptainFlag)
                .orderByAsc(YunSeasonAnchor::getSort)
                .orderByDesc(YunSeasonAnchor::getId);
        return seasonAnchorMapper.selectPage(pageParam, wrapper);
    }

    private List<YunSeasonAnchorPageRow> toMemberRows(List<YunSeasonAnchor> members) {
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, YunAnchor> anchorMap = anchorMap(members);
        return members.stream().map(member -> {
            YunSeasonAnchorPageRow row = new YunSeasonAnchorPageRow();
            BeanUtils.copyProperties(member, row);
            YunAnchor anchor = member.getAnchorRefId() == null ? null : anchorMap.get(member.getAnchorRefId());
            if (anchor != null) {
                row.setAnchorStatus(anchor.getStatus());
                row.setShowRank(anchor.getShowRank());
                row.setGuildName(anchor.getGuildName());
                row.setRoomStatus(anchor.getRoomStatus());
                row.setLastStartTime(anchor.getLastStartTime());
                if (!StringUtils.hasText(row.getAvatarUrl())) {
                    row.setAvatarUrl(anchor.getAvatarUrl());
                }
                if (!StringUtils.hasText(row.getBigImageUrl())) {
                    row.setBigImageUrl(anchor.getBigImageUrl());
                }
            }
            return row;
        }).toList();
    }

    private Map<Long, YunAnchor> anchorMap(List<YunSeasonAnchor> members) {
        Set<Long> ids = members.stream()
                .map(YunSeasonAnchor::getAnchorRefId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<YunAnchor> anchors = anchorMapper.selectBatchIds(ids);
        return anchors.stream().collect(Collectors.toMap(YunAnchor::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private Map<Long, List<YunSeasonAnchor>> membersBySeasonIds(List<Long> seasonIds) {
        if (seasonIds == null || seasonIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<YunSeasonAnchor> members = seasonAnchorMapper.selectList(
                new LambdaQueryWrapper<YunSeasonAnchor>()
                        .in(YunSeasonAnchor::getSeasonId, seasonIds)
                        .orderByAsc(YunSeasonAnchor::getEliminated)
                        .orderByDesc(YunSeasonAnchor::getCaptainFlag)
                        .orderByAsc(YunSeasonAnchor::getSort)
                        .orderByDesc(YunSeasonAnchor::getId)
        );
        return members.stream().collect(Collectors.groupingBy(YunSeasonAnchor::getSeasonId, LinkedHashMap::new, Collectors.toList()));
    }

    private YunSeasonAnchor buildMemberFromAnchor(Long seasonId, YunAnchor anchor, YunSeasonMemberBatchRequest request, int sortIndex) {
        YunSeasonAnchor member = new YunSeasonAnchor();
        member.setSeasonId(seasonId);
        member.setAnchorRefId(anchor.getId());
        member.setAnchorId(trim(anchor.getAnchorId(), 64));
        member.setPlatform(trim(anchor.getPlatform(), 16));
        member.setRoomId(trim(anchor.getRoomId(), 64));
        member.setAnchorName(trim(anchor.getAnchorName(), 100));
        member.setAvatarUrl(trim(anchor.getAvatarUrl(), 500));
        member.setBigImageUrl(trim(anchor.getBigImageUrl(), 500));
        member.setTeamName(trim(request == null ? null : request.getTeamName(), 100));
        member.setCaptainFlag(request != null && request.getCaptainFlag() != null ? request.getCaptainFlag() : 0);
        member.setEliminated(ELIMINATED_NO);
        member.setFailTimes(0);
        member.setNextEliminationAmount(DEFAULT_AMOUNT);
        member.setSort(sortIndex);
        return member;
    }

    private YunSeasonAnchor copyMember(YunSeasonAnchor source, Long seasonId, int sortIndex) {
        YunSeasonAnchor member = new YunSeasonAnchor();
        BeanUtils.copyProperties(source, member, "id", "createTime", "updateTime", "createBy", "updateBy", "deleted");
        member.setSeasonId(seasonId);
        member.setEliminated(ELIMINATED_NO);
        member.setFailTimes(0);
        member.setNextEliminationAmount(DEFAULT_AMOUNT);
        member.setSort(sortIndex);
        return member;
    }

    private boolean existsMember(Long seasonId, Long anchorRefId) {
        if (seasonId == null || anchorRefId == null) {
            return false;
        }
        LambdaQueryWrapper<YunSeasonAnchor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YunSeasonAnchor::getSeasonId, seasonId)
                .eq(YunSeasonAnchor::getAnchorRefId, anchorRefId);
        return seasonAnchorMapper.selectCount(wrapper) > 0;
    }

    private Set<Long> joinedAnchorRefIds(Long seasonId) {
        if (seasonId == null) {
            return Collections.emptySet();
        }
        return seasonAnchorMapper.selectList(
                        new LambdaQueryWrapper<YunSeasonAnchor>()
                                .select(YunSeasonAnchor::getAnchorRefId)
                                .eq(YunSeasonAnchor::getSeasonId, seasonId)
                                .isNotNull(YunSeasonAnchor::getAnchorRefId)
                ).stream()
                .map(YunSeasonAnchor::getAnchorRefId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private YunAnchor resolveOrCreateAnchor(AnchorImportInput input) {
        YunAnchor anchor = findAnchorByPlatformAndId(input.platform(), input.anchorId());
        if (anchor != null) {
            return anchor;
        }
        try {
            YunAnchor created = anchorService.fetchPreview(input.anchorId(), input.dataSource());
            created.setAnchorId(input.anchorId());
            created.setRoomId(StringUtils.hasText(created.getRoomId()) ? created.getRoomId() : input.anchorId());
            created.setDataSource(input.dataSource());
            created.setPlatform(input.platform());
            anchorService.create(created);
            return created.getId() == null ? findAnchorByPlatformAndId(input.platform(), input.anchorId()) : created;
        }
        catch (BusinessException e) {
            YunAnchor existing = findAnchorByPlatformAndId(input.platform(), input.anchorId());
            if (existing != null) {
                return existing;
            }
            throw e;
        }
    }

    private YunAnchor findAnchorByPlatformAndId(String platform, String anchorId) {
        if (!StringUtils.hasText(platform) || !StringUtils.hasText(anchorId)) {
            return null;
        }
        LambdaQueryWrapper<YunAnchor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YunAnchor::getPlatform, platform)
                .and(query -> query.eq(YunAnchor::getAnchorId, anchorId).or().eq(YunAnchor::getRoomId, anchorId))
                .last("LIMIT 1");
        return anchorMapper.selectOne(wrapper);
    }

    private void normalizeAndValidateSeason(YunSeason season, boolean create) {
        if (season == null) {
            throw new BusinessException("赛季信息不能为空");
        }
        if (!StringUtils.hasText(season.getSeasonCode())) {
            throw new BusinessException("赛季编号不能为空");
        }
        season.setSeasonCode(trim(season.getSeasonCode(), 32));
        season.setSeasonName(trim(season.getSeasonName(), 100));
        season.setCoverImageUrl(trim(season.getCoverImageUrl(), 500));
        season.setRemark(trim(season.getRemark(), 500));
        if (season.getStatus() == null) {
            season.setStatus(STATUS_ENABLED);
        }
        if (season.getAppDisplay() == null) {
            season.setAppDisplay(0);
        }
        if (equalsOne(season.getAppDisplay())) {
            season.setStatus(STATUS_ENABLED);
        }
        if (season.getSort() == null) {
            season.setSort(0);
        }
        if (season.getTotalBonus() == null) {
            season.setTotalBonus(BigDecimal.ZERO);
        }
        if (season.getTotalBonus().signum() < 0) {
            throw new BusinessException("赛季总奖金不能为负数");
        }
        LambdaQueryWrapper<YunSeason> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(YunSeason::getSeasonCode, season.getSeasonCode());
        if (!create) {
            wrapper.ne(YunSeason::getId, season.getId());
        }
        if (this.count(wrapper) > 0) {
            throw new BusinessException("赛季编号已存在");
        }
    }

    private void closeOtherAppDisplayIfNeeded(YunSeason season) {
        if (season == null || season.getId() == null || !equalsOne(season.getAppDisplay())) {
            return;
        }
        this.update(new LambdaUpdateWrapper<YunSeason>()
                .ne(YunSeason::getId, season.getId())
                .eq(YunSeason::getAppDisplay, APP_DISPLAY_ENABLED)
                .set(YunSeason::getAppDisplay, 0));
    }

    private void normalizeAndValidateMember(YunSeasonAnchor member) {
        member.setAnchorId(trim(member.getAnchorId(), 64));
        member.setPlatform(trim(member.getPlatform(), 16));
        member.setRoomId(trim(member.getRoomId(), 64));
        member.setAnchorName(trim(member.getAnchorName(), 100));
        member.setAvatarUrl(trim(member.getAvatarUrl(), 500));
        member.setBigImageUrl(trim(member.getBigImageUrl(), 500));
        member.setTeamName(trim(member.getTeamName(), 100));
        member.setRemark(trim(member.getRemark(), 500));
        if (member.getCaptainFlag() == null) {
            member.setCaptainFlag(0);
        }
        if (member.getEliminated() == null) {
            member.setEliminated(ELIMINATED_NO);
        }
        if (member.getFailTimes() == null) {
            member.setFailTimes(0);
        }
        if (member.getNextEliminationAmount() == null) {
            member.setNextEliminationAmount(DEFAULT_AMOUNT);
        }
        if (member.getSort() == null) {
            member.setSort(0);
        }
    }

    private List<Long> normalizeBatchAnchorRefIds(List<Long> anchorRefIds) {
        if (anchorRefIds == null || anchorRefIds.isEmpty()) {
            return Collections.emptyList();
        }
        return anchorRefIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    private List<AnchorImportInput> normalizeBatchAnchorInputs(List<String> anchorIds) {
        if (anchorIds == null || anchorIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> keys = new LinkedHashSet<>();
        List<AnchorImportInput> result = new ArrayList<>();
        for (String value : anchorIds) {
            for (String token : splitAnchorTokens(value)) {
                AnchorImportInput input = parseAnchorImportInput(token);
                String key = input.platform() + ":" + input.anchorId();
                if (keys.add(key)) {
                    result.add(input);
                }
            }
        }
        if (result.size() > MAX_BATCH_ADD_SIZE) {
            throw new BusinessException("单次最多批量导入" + MAX_BATCH_ADD_SIZE + "个主播");
        }
        return result;
    }

    private List<String> splitAnchorTokens(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split("[\\s,，;；]+"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private AnchorImportInput parseAnchorImportInput(String value) {
        String raw = value.trim();
        String normalized = raw.replace('：', ':');
        String lower = normalized.toLowerCase();
        String anchorId = normalized;
        String dataSource = DATA_SOURCE_DOSEEING;
        String platform = PLATFORM_DOUYU;
        if (lower.startsWith("dy:")) {
            anchorId = normalized.substring(3).trim();
        }
        else if (lower.startsWith("hy:")) {
            anchorId = normalized.substring(3).trim();
            dataSource = DATA_SOURCE_DOSEEING_HUYA;
            platform = PLATFORM_HUYA;
        }
        if (!StringUtils.hasText(anchorId)) {
            throw new BusinessException("主播ID不能为空");
        }
        return new AnchorImportInput(raw, anchorId, dataSource, platform);
    }

    private record AnchorImportInput(String rawInput, String anchorId, String dataSource, String platform) {
    }

    private boolean equalsOne(Integer value) {
        return value != null && value == 1;
    }

    private String trim(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}
