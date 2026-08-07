package com.mars.biz.dto;

import com.mars.biz.entity.YunAnchor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 主播管理分页行
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class YunAnchorPageRow extends YunAnchor {

    private BigDecimal todayGiftValue;

    private BigDecimal yesterdayGiftValue;

    private BigDecimal monthGiftValue;

    private LocalDateTime todaySyncedAt;

    private LocalDateTime yesterdaySyncedAt;

    private LocalDateTime monthSyncedAt;
}
