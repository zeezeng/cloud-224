package com.mars.biz.dto;

import lombok.Data;

import java.util.List;

/**
 * 赛季批量添加成员请求
 */
@Data
public class YunSeasonMemberBatchRequest {

    /**
     * 已有主播主表ID列表，来自弹窗勾选。
     */
    private List<Long> anchorRefIds;

    /**
     * 房间号/主播ID列表，可带平台前缀：dy: 斗鱼、hy: 虎牙；不带前缀默认斗鱼。
     */
    private List<String> anchorIds;

    private String teamName;

    private Integer captainFlag;
}
