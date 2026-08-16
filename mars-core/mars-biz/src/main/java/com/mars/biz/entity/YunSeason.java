package com.mars.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mars.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 主播赛季
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("yun_season")
public class YunSeason extends BaseEntity {

    /**
     * 赛季编号，如 S10.5
     */
    private String seasonCode;

    /**
     * 赛季名称
     */
    private String seasonName;

    /**
     * 赛季封面图
     */
    private String coverImageUrl;

    /**
     * 状态(0-停用 1-启用)
     */
    private Integer status;

    /**
     * 客户端显示(0-否 1-是)，同一时间仅一个赛季展示
     */
    private Integer appDisplay;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
