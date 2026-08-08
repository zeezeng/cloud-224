package com.mars.admin.controller.yun;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mars.biz.dto.YunAnchorBatchCreateRequest;
import com.mars.biz.dto.YunAnchorBatchCreateResult;
import com.mars.biz.dto.YunAnchorPageRow;
import com.mars.biz.dto.YunSyncProgress;
import com.mars.biz.dto.YunSyncResult;
import com.mars.biz.entity.YunAnchor;
import com.mars.biz.service.YunAnchorService;
import com.mars.common.result.PageResult;
import com.mars.common.result.Result;
import com.mars.system.annotation.Log;
import com.mars.system.annotation.Log.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 云224主播管理
 */
@RestController
@RequestMapping("/yun/anchor")
@RequiredArgsConstructor
public class YunAnchorController {

    private final YunAnchorService yunAnchorService;

    @GetMapping("/page")
    @SaCheckPermission("yun:anchor:list")
    public Result<PageResult<YunAnchorPageRow>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String anchorId,
            @RequestParam(required = false) String anchorName,
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) String guildName,
            @RequestParam(required = false) Integer status) {
        var result = yunAnchorService.page(page, pageSize, anchorId, anchorName, roomId, guildName, status);
        return Result.ok(PageResult.of(result));
    }

    @GetMapping("/fetch-preview")
    @SaCheckPermission("yun:anchor:add")
    public Result<YunAnchor> fetchPreview(@RequestParam String anchorId,
                                          @RequestParam(required = false) String dataSource) {
        return Result.ok(yunAnchorService.fetchPreview(anchorId, dataSource));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("yun:anchor:query")
    public Result<YunAnchor> getInfo(@PathVariable Long id) {
        return Result.ok(yunAnchorService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("yun:anchor:add")
    @Log(title = "云224主播", businessType = BusinessType.INSERT)
    public Result<Void> add(@RequestBody YunAnchor anchor) {
        yunAnchorService.create(anchor);
        return Result.ok();
    }

    @PostMapping("/batch")
    @SaCheckPermission("yun:anchor:add")
    @Log(title = "云224主播批量新增", businessType = BusinessType.INSERT)
    public Result<YunAnchorBatchCreateResult> batchAdd(@RequestBody YunAnchorBatchCreateRequest request) {
        return Result.ok(yunAnchorService.batchCreate(
                request == null ? null : request.getAnchorIds(),
                request == null ? null : request.getDataSource()
        ));
    }

    @PutMapping("/{id}/status")
    @SaCheckPermission("yun:anchor:edit")
    @Log(title = "云224主播状态", businessType = BusinessType.UPDATE)
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        yunAnchorService.updateStatus(id, status);
        return Result.ok();
    }

    @PutMapping("/{id}/show-rank")
    @SaCheckPermission("yun:anchor:edit")
    @Log(title = "云224主播榜单展示", businessType = BusinessType.UPDATE)
    public Result<Void> updateShowRank(@PathVariable Long id, @RequestParam Integer showRank) {
        yunAnchorService.updateShowRank(id, showRank);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("yun:anchor:edit")
    @Log(title = "云224主播", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@RequestBody YunAnchor anchor) {
        yunAnchorService.update(anchor);
        return Result.ok();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("yun:anchor:remove")
    @Log(title = "云224主播", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long[] ids) {
        yunAnchorService.delete(ids);
        return Result.ok();
    }

    @PostMapping("/{id}/sync")
    @SaCheckPermission("yun:anchor:sync")
    @Log(title = "云224主播礼物同步", businessType = BusinessType.UPDATE)
    public Result<YunSyncResult> sync(@PathVariable Long id,
                                      @RequestParam(required = false) String dataSource) {
        return Result.ok(yunAnchorService.sync(id, dataSource));
    }

    @PostMapping("/sync-all")
    @SaCheckPermission("yun:anchor:sync")
    @Log(title = "云224主播礼物同步", businessType = BusinessType.UPDATE)
    public Result<YunSyncProgress> syncAll(@RequestParam(required = false) String dataSource) {
        return Result.ok(yunAnchorService.startSyncAll(dataSource));
    }

    @GetMapping("/sync-all/progress")
    @SaCheckPermission("yun:anchor:sync")
    public Result<YunSyncProgress> syncAllProgress(@RequestParam String taskId) {
        return Result.ok(yunAnchorService.getSyncAllProgress(taskId));
    }
}
