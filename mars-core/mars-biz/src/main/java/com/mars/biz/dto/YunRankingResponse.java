package com.mars.biz.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 小程序主播礼物排行响应
 */
@Data
public class YunRankingResponse {

    private String period;

    private String periodKey;

    private String periodLabel;

    private LocalDateTime latestSyncTime;

    private Long total;

    private Long page;

    private Long pageSize;

    private List<YunRankingRecord> list;
}
