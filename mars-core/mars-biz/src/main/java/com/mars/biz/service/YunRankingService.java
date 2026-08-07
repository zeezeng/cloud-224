package com.mars.biz.service;

import com.mars.biz.dto.YunRankingResponse;

/**
 * 云224小程序排行 Service
 */
public interface YunRankingService {

    YunRankingResponse getAnchorGiftRanking(String period, Integer page, Integer pageSize);
}
