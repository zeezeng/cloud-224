package com.mars.biz.dto;

import lombok.Data;

import java.util.List;

/**
 * 云224主播批量新增请求
 */
@Data
public class YunAnchorBatchCreateRequest {

    /**
     * 主播ID列表，当前等同斗鱼 rid
     */
    private List<String> anchorIds;

    /**
     * 创建资料使用的数据源：DOSEEING
     */
    private String dataSource;
}
