package com.mars.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mars.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 云224主播礼物统计快照
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("yun_anchor_gift_stat")
public class YunAnchorGiftStat extends BaseEntity {

    private String anchorId;

    private String roomId;

    /**
     * 统计周期：DAY/MONTH
     */
    private String periodType;

    /**
     * 统计周期键：yyyy-MM-dd 或 yyyy-MM
     */
    private String periodKey;

    /**
     * 全站排名
     */
    private Integer externalRankNo;

    /**
     * 礼物总值
     */
    private BigDecimal giftTotalValue;

    /**
     * 付费礼物金额
     */
    private BigDecimal paidGiftValue;

    /**
     * 背包礼物金额
     */
    private BigDecimal bagGiftValue;

    /**
     * 鱼丸礼物数量
     */
    private BigDecimal fishballGiftCount;

    /**
     * 送礼人数
     */
    private Integer giftUserCount;

    /**
     * 付费送礼人数(SR人数)
     */
    private Integer paidGiftUserCount;

    /**
     * 开播小时
     */
    private BigDecimal streamHours;

    /**
     * 活跃观众
     */
    private Integer activeAudienceCount;

    /**
     * 弹幕数量
     */
    private Integer danmuCount;

    /**
     * 弹幕人数
     */
    private Integer danmuUserCount;

    private String durationText;

    private Integer roomStatus;

    private Boolean lived;

    private String lastStartTime;

    /**
     * 源数据更新时间
     */
    private String sourceUpdateTime;

    /**
     * 原始响应行JSON
     */
    private String rawJson;

    private LocalDateTime syncedAt;
}
