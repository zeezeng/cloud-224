package com.mars.biz.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 播酱主播数据映射
 */
@Data
public class BojiangAnchorInfo {

    private String anchorId;

    private String roomId;

    private String anchorName;

    private String avatarUrl;

    private String roomTitle;

    private String categoryId;

    private String categoryName;

    private String guildNo;

    private String guildName;

    private Integer externalRankNo;

    private BigDecimal giftTotalValue;

    private BigDecimal paidGiftValue;

    private BigDecimal bagGiftValue;

    private BigDecimal fishballGiftCount;

    private Integer giftUserCount;

    private Integer activeAudienceCount;

    private Integer danmuCount;

    private Integer danmuUserCount;

    private String durationText;

    private Integer roomStatus;

    private Boolean lived;

    private String lastStartTime;

    private String sourceUpdateTime;

    private String rawJson;
}
