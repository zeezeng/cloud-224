package com.mars.biz.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小程序主播礼物排行行
 */
@Data
public class YunRankingRecord {

    private Integer rankNo;

    private String anchorId;

    private String roomId;

    private String name;

    private String avatar;

    private String guildName;

    private BigDecimal giftTotalValue;

    private BigDecimal paidGiftValue;

    private Integer giftUserCount;

    private LocalDateTime syncedAt;
}
