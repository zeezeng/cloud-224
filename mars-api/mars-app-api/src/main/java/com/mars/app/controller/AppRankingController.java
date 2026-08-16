package com.mars.app.controller;

import com.mars.biz.dto.YunRankingResponse;
import com.mars.biz.service.YunRankingService;
import com.mars.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 主播排行公开接口
 */
@RestController
@RequestMapping("/app/ranking")
@RequiredArgsConstructor
public class AppRankingController {

    private final YunRankingService yunRankingService;

    @GetMapping("/anchor-gifts")
    public Result<YunRankingResponse> anchorGiftRanking(
            @RequestParam(defaultValue = "today") String period,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.ok(yunRankingService.getAnchorGiftRanking(period, page, pageSize, keyword));
    }
}
