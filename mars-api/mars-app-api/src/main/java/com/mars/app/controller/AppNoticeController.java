package com.mars.app.controller;

import com.mars.biz.entity.AppNotice;
import com.mars.biz.service.AppNoticeService;
import com.mars.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * App 首页公告公开接口
 */
@RestController("appNoticePublicController")
@RequestMapping("/app/notice")
@RequiredArgsConstructor
public class AppNoticeController {

    private final AppNoticeService appNoticeService;

    /**
     * 首页跑马灯公告列表
     */
    @GetMapping("/list")
    public Result<List<AppNotice>> list(@RequestParam(required = false) Integer limit) {
        return Result.ok(appNoticeService.listPublished(limit));
    }

    /**
     * 当前有效的弹窗公告列表
     */
    @GetMapping("/popup")
    public Result<List<AppNotice>> popup() {
        return Result.ok(appNoticeService.listActivePopup());
    }
}
