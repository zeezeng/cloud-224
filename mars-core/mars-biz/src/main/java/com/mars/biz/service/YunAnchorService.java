package com.mars.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mars.biz.dto.YunAnchorBatchCreateResult;
import com.mars.biz.dto.YunAnchorPageRow;
import com.mars.biz.dto.YunSyncResult;
import com.mars.biz.entity.YunAnchor;

import java.util.List;

/**
 * 云224主播 Service
 */
public interface YunAnchorService extends IService<YunAnchor> {

    Page<YunAnchorPageRow> page(Integer page, Integer pageSize, String anchorId, String anchorName,
                                String roomId, String guildName, Integer status);

    YunAnchor fetchPreview(String anchorId);

    YunAnchor fetchPreview(String anchorId, String dataSource);

    void create(YunAnchor anchor);

    YunAnchorBatchCreateResult batchCreate(List<String> anchorIds);

    YunAnchorBatchCreateResult batchCreate(List<String> anchorIds, String dataSource);

    void update(YunAnchor anchor);

    void delete(Long[] ids);

    YunSyncResult sync(Long id);

    YunSyncResult sync(Long id, String dataSource);

    YunSyncResult syncAll();

    YunSyncResult syncAll(String dataSource);
}
