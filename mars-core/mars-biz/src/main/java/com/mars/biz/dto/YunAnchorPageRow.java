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

    // 今日
    private BigDecimal todaySrValue;
    private Integer todaySrUserCount;
    private BigDecimal todayLwValue;
    private Integer todayLwUserCount;
    private BigDecimal todayStreamHours;

    // 昨日
    private BigDecimal yesterdaySrValue;
    private Integer yesterdaySrUserCount;
    private BigDecimal yesterdayLwValue;
    private Integer yesterdayLwUserCount;
    private BigDecimal yesterdayStreamHours;

    // 本月
    private BigDecimal monthSrValue;
    private Integer monthSrUserCount;
    private BigDecimal monthLwValue;
    private Integer monthLwUserCount;
    private BigDecimal monthStreamHours;

    private LocalDateTime todaySyncedAt;

    private LocalDateTime yesterdaySyncedAt;

    private LocalDateTime monthSyncedAt;
}
