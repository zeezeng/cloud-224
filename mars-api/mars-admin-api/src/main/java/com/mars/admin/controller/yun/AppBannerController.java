package com.mars.admin.controller.yun;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mars.biz.entity.AppBanner;
import com.mars.biz.service.AppBannerService;
import com.mars.common.result.PageResult;
import com.mars.common.result.Result;
import com.mars.system.annotation.Log;
import com.mars.system.annotation.Log.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * App 首页轮播图管理
 */
@RestController("adminAppBannerController")
@RequestMapping("/yun/banner")
@RequiredArgsConstructor
public class AppBannerController {

    private final AppBannerService appBannerService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    @SaCheckPermission("yun:banner:list")
    public Result<PageResult<AppBanner>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer jumpType,
            @RequestParam(required = false) Integer status) {
        var result = appBannerService.page(page, pageSize, title, jumpType, status);
        return Result.ok(PageResult.of(result));
    }

    /**
     * 获取详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("yun:banner:query")
    public Result<AppBanner> getInfo(@PathVariable Long id) {
        return Result.ok(appBannerService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    @SaCheckPermission("yun:banner:add")
    @Log(title = "App首页轮播图", businessType = BusinessType.INSERT)
    public Result<Void> add(@RequestBody AppBanner banner) {
        appBannerService.create(banner);
        return Result.ok();
    }

    /**
     * 修改
     */
    @PutMapping
    @SaCheckPermission("yun:banner:edit")
    @Log(title = "App首页轮播图", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@RequestBody AppBanner banner) {
        appBannerService.update(banner);
        return Result.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{ids}")
    @SaCheckPermission("yun:banner:remove")
    @Log(title = "App首页轮播图", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long[] ids) {
        appBannerService.delete(ids);
        return Result.ok();
    }
}
