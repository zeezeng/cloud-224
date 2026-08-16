package com.mars.app.controller;

import com.mars.biz.entity.AppFeedback;
import com.mars.biz.service.AppFeedbackService;
import com.mars.common.exception.BusinessException;
import com.mars.common.result.Result;
import com.mars.system.helper.SystemConfigHelper;
import com.mars.system.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * App 用户反馈公开接口
 */
@RestController
@RequestMapping("/app/feedback")
@RequiredArgsConstructor
public class AppFeedbackController {

    private static final String RATE_LIMIT_KEY_PREFIX = "app:feedback:limit:";

    private final AppFeedbackService appFeedbackService;
    private final StringRedisTemplate redisTemplate;
    private final SystemConfigHelper configHelper;

    @GetMapping("/contact")
    public Result<FeedbackContactResponse> contact() {
        FeedbackContactResponse response = new FeedbackContactResponse();
        response.setWechatId(configHelper.getString(SystemConfigHelper.GROUP_FEEDBACK_CONTACT, "wechatId", ""));
        response.setQrcodeUrl(configHelper.getString(SystemConfigHelper.GROUP_FEEDBACK_CONTACT, "qrcodeUrl", ""));
        response.setRemark(configHelper.getString(SystemConfigHelper.GROUP_FEEDBACK_CONTACT, "remark", ""));
        return Result.ok(response);
    }

    @PostMapping
    public Result<Long> submit(@RequestBody FeedbackSubmitRequest request, HttpServletRequest servletRequest) {
        String ip = IpUtils.getIpAddr(servletRequest);
        String rateKey = RATE_LIMIT_KEY_PREFIX + ip;
        Boolean allowed = redisTemplate.opsForValue().setIfAbsent(rateKey, "1", 60, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(allowed)) {
            throw new BusinessException("提交太频繁，请稍后再试");
        }

        AppFeedback feedback = new AppFeedback();
        feedback.setFeedbackType(request.getFeedbackType());
        feedback.setContent(request.getContent());
        feedback.setPagePath(request.getPagePath());
        feedback.setClientInfo(request.getClientInfo());
        return Result.ok(appFeedbackService.submit(feedback));
    }

    @Data
    public static class FeedbackSubmitRequest {
        private Integer feedbackType;
        private String content;
        private String pagePath;
        private String clientInfo;
    }

    @Data
    public static class FeedbackContactResponse {
        private String wechatId;
        private String qrcodeUrl;
        private String remark;
    }
}
