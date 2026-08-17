package com.mars.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mars.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 主播赛季成员
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("yun_season_anchor")
public class YunSeasonAnchor extends BaseEntity {

    /**
     * 赛季ID
     */
    private Long seasonId;

    /**
     * 主播主表ID
     */
    private Long anchorRefId;

    /**
     * 主播业务ID
     */
    private String anchorId;

    /**
     * 平台
     */
    private String platform;

    /**
     * 房间号
     */
    private String roomId;

    /**
     * 主播名称
     */
    private String anchorName;

    /**
     * 主播头像
     */
    private String avatarUrl;

    /**
     * 主播大图
     */
    private String bigImageUrl;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 是否队长(0-否 1-是)
     */
    private Integer captainFlag;

    /**
     * 是否淘汰(0-否 1-是)
     */
    private Integer eliminated;

    /**
     * 失败次数
     */
    private Integer failTimes;

    /**
     * 下次淘汰金额
     */
    private BigDecimal nextEliminationAmount;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
