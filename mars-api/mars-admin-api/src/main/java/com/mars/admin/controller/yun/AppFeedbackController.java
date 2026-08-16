package com.mars.admin.controller.yun;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mars.biz.entity.AppFeedback;
import com.mars.biz.service.AppFeedbackService;
import com.mars.common.result.PageResult;
import com.mars.common.result.Result;
import com.mars.system.annotation.Log;
import com.mars.system.annotation.Log.BusinessType;
import com.mars.system.helper.SystemConfigHelper;
import com.mars.system.service.SysConfigGroupService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * App 用户反馈管理
 */
@RestController("adminAppFeedbackController")
@RequestMapping("/yun/feedback")
@RequiredArgsConstructor
public class AppFeedbackController {

    private final AppFeedbackService appFeedbackService;
    private final SystemConfigHelper configHelper;
    private final SysConfigGroupService configGroupService;

    @GetMapping("/page")
    @SaCheckPermission("yun:feedback:list")
    public Result<PageResult<AppFeedback>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer feedbackType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        var result = appFeedbackService.page(page, pageSize, feedbackType, status, keyword, beginTime, endTime);
        return Result.ok(PageResult.of(result));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("yun:feedback:query")
    public Result<AppFeedback> getInfo(@PathVariable Long id) {
        return Result.ok(appFeedbackService.detail(id));
    }

    @GetMapping("/contact")
    @SaCheckPermission("yun:feedback:list")
    public Result<FeedbackContactRequest> getContact() {
        FeedbackContactRequest response = new FeedbackContactRequest();
        response.setWechatId(configHelper.getString(SystemConfigHelper.GROUP_FEEDBACK_CONTACT, "wechatId", ""));
        response.setQrcodeUrl(configHelper.getString(SystemConfigHelper.GROUP_FEEDBACK_CONTACT, "qrcodeUrl", ""));
        response.setRemark(configHelper.getString(SystemConfigHelper.GROUP_FEEDBACK_CONTACT, "remark", ""));
        return Result.ok(response);
    }

    @PutMapping("/contact")
    @SaCheckPermission("yun:feedback:handle")
    @Log(title = "App用户反馈联系设置", businessType = BusinessType.UPDATE)
    public Result<Void> updateContact(@RequestBody FeedbackContactRequest request) {
        Map<String, Object> config = new HashMap<>();
        config.put("wechatId", trimToEmpty(request.getWechatId()));
        config.put("qrcodeUrl", trimToEmpty(request.getQrcodeUrl()));
        config.put("remark", trimToEmpty(request.getRemark()));
        try {
            String configValue = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(config);
            configGroupService.saveOrCreateConfig(
                    SystemConfigHelper.GROUP_FEEDBACK_CONTACT,
                    "反馈联系设置",
                    configValue,
                    21,
                    "小程序用户反馈页展示的微信号与二维码，由反馈管理页维护"
            );
        } catch (Exception e) {
            return Result.fail("保存联系设置失败: " + e.getMessage());
        }
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @SaCheckPermission("yun:feedback:handle")
    @Log(title = "App用户反馈处理", businessType = BusinessType.UPDATE)
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody FeedbackStatusRequest request) {
        appFeedbackService.updateStatus(id, request.getStatus(), request.getHandleRemark());
        return Result.ok();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("yun:feedback:remove")
    @Log(title = "App用户反馈", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long[] ids) {
        appFeedbackService.delete(ids);
        return Result.ok();
    }

    @Data
    public static class FeedbackStatusRequest {
        private Integer status;
        private String handleRemark;
    }

    @Data
    public static class FeedbackContactRequest {
        private String wechatId;
        private String qrcodeUrl;
        private String remark;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
