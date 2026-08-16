package com.mars.app.controller;

import com.mars.biz.dto.YunSeasonAnchorPageRow;
import com.mars.biz.dto.YunSeasonPageRow;
import com.mars.biz.entity.YunSeason;
import com.mars.biz.service.YunSeasonService;
import com.mars.common.result.PageResult;
import com.mars.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * App 赛季公开接口
 */
@RestController
@RequestMapping("/app/season")
@RequiredArgsConstructor
public class AppSeasonController {

    private final YunSeasonService yunSeasonService;

    @GetMapping("/list")
    public Result<List<YunSeason>> list() {
        YunSeason currentSeason = yunSeasonService.currentAppSeason();
        return Result.ok(currentSeason == null ? List.of() : List.of(currentSeason));
    }

    @GetMapping("/current")
    public Result<YunSeasonPageRow> current() {
        return Result.ok(yunSeasonService.currentAppSeasonWithStats());
    }

    @GetMapping("/{id}/members")
    public Result<PageResult<YunSeasonAnchorPageRow>> memberPage(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        var result = yunSeasonService.memberPage(id, page, pageSize, keyword, null, null);
        return Result.ok(PageResult.of(result));
    }
}
