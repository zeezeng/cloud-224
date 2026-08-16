package com.mars.admin.controller.yun;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mars.biz.dto.YunAnchorPageRow;
import com.mars.biz.dto.YunSeasonAnchorPageRow;
import com.mars.biz.dto.YunSeasonCopyRequest;
import com.mars.biz.dto.YunSeasonMemberBatchRequest;
import com.mars.biz.dto.YunSeasonMemberBatchResult;
import com.mars.biz.dto.YunSeasonPageRow;
import com.mars.biz.entity.YunSeason;
import com.mars.biz.entity.YunSeasonAnchor;
import com.mars.biz.service.YunSeasonService;
import com.mars.common.result.PageResult;
import com.mars.common.result.Result;
import com.mars.system.annotation.Log;
import com.mars.system.annotation.Log.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 主播赛季管理
 */
@RestController
@RequestMapping("/yun/season")
@RequiredArgsConstructor
public class YunSeasonController {

    private final YunSeasonService yunSeasonService;

    @GetMapping("/page")
    @SaCheckPermission("yun:season:list")
    public Result<PageResult<YunSeasonPageRow>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String seasonCode,
            @RequestParam(required = false) String seasonName,
            @RequestParam(required = false) Integer status) {
        var result = yunSeasonService.page(page, pageSize, seasonCode, seasonName, status);
        return Result.ok(PageResult.of(result));
    }

    @GetMapping("/list")
    @SaCheckPermission("yun:season:list")
    public Result<java.util.List<YunSeason>> list() {
        return Result.ok(yunSeasonService.listEnabled());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("yun:season:query")
    public Result<YunSeason> detail(@PathVariable Long id) {
        return Result.ok(yunSeasonService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("yun:season:add")
    @Log(title = "主播赛季", businessType = BusinessType.INSERT)
    public Result<Void> add(@RequestBody YunSeason season) {
        yunSeasonService.create(season);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("yun:season:edit")
    @Log(title = "主播赛季", businessType = BusinessType.UPDATE)
    public Result<Void> edit(@RequestBody YunSeason season) {
        yunSeasonService.update(season);
        return Result.ok();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("yun:season:remove")
    @Log(title = "主播赛季", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long[] ids) {
        yunSeasonService.delete(ids);
        return Result.ok();
    }

    @PostMapping("/{id}/copy")
    @SaCheckPermission("yun:season:copy")
    @Log(title = "主播赛季复制", businessType = BusinessType.INSERT)
    public Result<YunSeason> copy(@PathVariable Long id, @RequestBody(required = false) YunSeasonCopyRequest request) {
        return Result.ok(yunSeasonService.copy(id, request));
    }

    @GetMapping("/{id}/members")
    @SaCheckPermission("yun:season:list")
    public Result<PageResult<YunSeasonAnchorPageRow>> memberPage(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer eliminated,
            @RequestParam(required = false) Integer captainFlag) {
        var result = yunSeasonService.memberPage(id, page, pageSize, keyword, eliminated, captainFlag);
        return Result.ok(PageResult.of(result));
    }

    @GetMapping("/{id}/candidate-anchors")
    @SaCheckPermission("yun:season:list")
    public Result<PageResult<YunAnchorPageRow>> candidateAnchorPage(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String anchorId,
            @RequestParam(required = false) String anchorName,
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) String guildName,
            @RequestParam(required = false) Integer status) {
        var result = yunSeasonService.candidateAnchorPage(id, page, pageSize, anchorId, anchorName, roomId, guildName, status);
        return Result.ok(PageResult.of(result));
    }

    @PostMapping("/{id}/members")
    @SaCheckPermission("yun:season:edit")
    @Log(title = "赛季成员批量加入", businessType = BusinessType.INSERT)
    public Result<YunSeasonMemberBatchResult> addMembers(@PathVariable Long id, @RequestBody YunSeasonMemberBatchRequest request) {
        return Result.ok(yunSeasonService.addMembers(id, request));
    }

    @PutMapping("/members")
    @SaCheckPermission("yun:season:edit")
    @Log(title = "赛季成员", businessType = BusinessType.UPDATE)
    public Result<Void> updateMember(@RequestBody YunSeasonAnchor member) {
        yunSeasonService.updateMember(member);
        return Result.ok();
    }

    @DeleteMapping("/{id}/members/{memberIds}")
    @SaCheckPermission("yun:season:edit")
    @Log(title = "赛季成员", businessType = BusinessType.DELETE)
    public Result<Void> deleteMembers(@PathVariable Long id, @PathVariable Long[] memberIds) {
        yunSeasonService.deleteMembers(id, memberIds);
        return Result.ok();
    }

    @PostMapping("/{id}/members/reset")
    @SaCheckPermission("yun:season:edit")
    @Log(title = "赛季成员重置", businessType = BusinessType.UPDATE)
    public Result<Void> resetMembers(@PathVariable Long id) {
        yunSeasonService.resetMembers(id);
        return Result.ok();
    }
}
