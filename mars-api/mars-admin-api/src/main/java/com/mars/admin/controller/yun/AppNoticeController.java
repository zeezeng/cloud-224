package com.mars.admin.controller.yun;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mars.biz.entity.AppNotice;
import com.mars.biz.service.AppNoticeService;
import com.mars.common.result.PageResult;
import com.mars.common.result.Result;
import com.mars.system.annotation.Log;
import com.mars.system.annotation.Log.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * App 首页公告管理
 */
@RestController("adminAppNoticeController")
@RequestMapping("/yun/notice")
@RequiredArgsConstructor
public class AppNoticeController {

    private final AppNoticeService appNoticeService;

    @GetMapping("/page")
    @SaCheckPermission("yun:notice:list")
    public Result<PageResult<AppNotice>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer noticeType) {
        var result = appNoticeService.page(page, pageSize, title, status, noticeType);
        return Result.ok(PageResult.of(result));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("yun:notice:query")
    public Result<AppNotice> getInfo(@PathVariable Long id) {
        return Result.ok(appNoticeService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("yun:notice:add")
    @Log(title = "App首页公告", businessType = BusinessType.INSERT)
    public Result<Void> add(@RequestBody AppNotice notice) {
        appNoticeService.create(notice);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("yun:notice:edit")
    @Log(title = "App首页公告", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@RequestBody AppNotice notice) {
        appNoticeService.update(notice);
        return Result.ok();
    }

    @PutMapping("/{id}/publish")
    @SaCheckPermission("yun:notice:edit")
    @Log(title = "App首页公告发布", businessType = BusinessType.UPDATE)
    public Result<Void> publish(@PathVariable Long id) {
        appNoticeService.publish(id);
        return Result.ok();
    }

    @PutMapping("/{id}/offline")
    @SaCheckPermission("yun:notice:edit")
    @Log(title = "App首页公告下线", businessType = BusinessType.UPDATE)
    public Result<Void> offline(@PathVariable Long id) {
        appNoticeService.offline(id);
        return Result.ok();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("yun:notice:remove")
    @Log(title = "App首页公告", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long[] ids) {
        appNoticeService.delete(ids);
        return Result.ok();
    }
}
