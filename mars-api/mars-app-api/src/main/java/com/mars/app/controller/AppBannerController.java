package com.mars.app.controller;

import com.mars.biz.entity.AppBanner;
import com.mars.biz.service.AppBannerService;
import com.mars.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * App 首页轮播图公开接口
 */
@RestController("appBannerPublicController")
@RequestMapping("/app/banner")
@RequiredArgsConstructor
public class AppBannerController {

    private final AppBannerService appBannerService;

    /**
     * 首页启用轮播图列表
     */
    @GetMapping("/list")
    public Result<List<AppBanner>> list() {
        return Result.ok(appBannerService.listEnabled());
    }
}
