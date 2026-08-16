package com.mars.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mars.biz.dto.YunAnchorPageRow;
import com.mars.biz.dto.YunSeasonAnchorPageRow;
import com.mars.biz.dto.YunSeasonCopyRequest;
import com.mars.biz.dto.YunSeasonMemberBatchRequest;
import com.mars.biz.dto.YunSeasonMemberBatchResult;
import com.mars.biz.dto.YunSeasonPageRow;
import com.mars.biz.entity.YunSeason;
import com.mars.biz.entity.YunSeasonAnchor;

import java.util.List;

/**
 * 主播赛季 Service
 */
public interface YunSeasonService extends IService<YunSeason> {

    Page<YunSeasonPageRow> page(Integer page, Integer pageSize, String seasonCode, String seasonName, Integer status);

    List<YunSeason> listEnabled();

    YunSeason currentAppSeason();

    YunSeasonPageRow currentAppSeasonWithStats();

    void create(YunSeason season);

    void update(YunSeason season);

    void delete(Long[] ids);

    YunSeason copy(Long id, YunSeasonCopyRequest request);

    Page<YunSeasonAnchorPageRow> memberPage(Long seasonId, Integer page, Integer pageSize, String keyword,
                                            Integer eliminated, Integer captainFlag);

    Page<YunAnchorPageRow> candidateAnchorPage(Long seasonId, Integer page, Integer pageSize, String anchorId,
                                               String anchorName, String roomId, String guildName, Integer status);

    List<YunSeasonAnchorPageRow> listMembers(Long seasonId);

    YunSeasonMemberBatchResult addMembers(Long seasonId, YunSeasonMemberBatchRequest request);

    void updateMember(YunSeasonAnchor member);

    void deleteMembers(Long seasonId, Long[] ids);

    void resetMembers(Long seasonId);
}
