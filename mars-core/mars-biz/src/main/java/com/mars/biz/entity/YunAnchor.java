package com.mars.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mars.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 云224主播资料
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("yun_anchor")
public class YunAnchor extends BaseEntity {

    /**
     * 主播业务唯一ID，当前等同播酱/斗鱼房间号
     */
    private String anchorId;

    /**
     * 直播间号
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
     * 直播间标题
     */
    private String roomTitle;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 公会编号
     */
    private String guildNo;

    /**
     * 公会名称
     */
    private String guildName;

    /**
     * 主播简介
     */
    private String bio;

    /**
     * 房间状态
     */
    private Integer roomStatus;

    /**
     * 最近开播时间
     */
    private String lastStartTime;

    /**
     * 状态(0-禁用 1-启用)
     */
    private Integer status;

    /**
     * 是否在排行展示(0-否 1-是)
     */
    private Integer showRank;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;

    /**
     * 数据来源(MANUAL/BOJIANG)
     */
    private String dataSource;

    /**
     * 同步时是否自动更新主播资料(0-否 1-是)
     */
    private Integer autoUpdateProfile;

    /**
     * 最近资料同步时间
     */
    private LocalDateTime lastProfileSyncTime;

    /**
     * 最近礼物同步时间
     */
    private LocalDateTime lastGiftSyncTime;

    /**
     * 备注
     */
    private String remark;
}
