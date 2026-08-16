package com.mars.biz.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mars.biz.entity.AppFeedback;
import com.mars.biz.mapper.AppFeedbackMapper;
import com.mars.biz.service.AppFeedbackService;
import com.mars.common.exception.BusinessException;
import com.mars.system.entity.SysUser;
import com.mars.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * App 用户反馈 Service 实现
 */
@Service
@RequiredArgsConstructor
public class AppFeedbackServiceImpl extends ServiceImpl<AppFeedbackMapper, AppFeedback> implements AppFeedbackService {

    private static final int TYPE_SUGGESTION = 1;
    private static final int TYPE_BUG = 2;
    private static final int TYPE_CONTENT = 3;
    private static final int TYPE_OTHER = 4;
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_PROCESSING = 1;
    private static final int STATUS_DONE = 2;
    private static final int STATUS_IGNORED = 3;
    private static final int CONTENT_MIN_LENGTH = 5;
    private static final int CONTENT_MAX_LENGTH = 1000;

    private final SysUserService sysUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(AppFeedback feedback) {
        normalizeAndValidateSubmit(feedback);
        feedback.setStatus(STATUS_PENDING);
        this.save(feedback);
        return feedback.getId();
    }

    @Override
    public Page<AppFeedback> page(Integer page, Integer pageSize, Integer feedbackType, Integer status, String keyword,
                                  String beginTime, String endTime) {
        Page<AppFeedback> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AppFeedback> wrapper = new LambdaQueryWrapper<>();
        if (feedbackType != null) {
            wrapper.eq(AppFeedback::getFeedbackType, feedbackType);
        }
        if (status != null) {
            wrapper.eq(AppFeedback::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            wrapper.like(AppFeedback::getContent, trimmed);
        }
        if (StringUtils.hasText(beginTime)) {
            wrapper.ge(AppFeedback::getCreateTime, beginTime.trim());
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(AppFeedback::getCreateTime, endTime.trim());
        }
        wrapper.orderByAsc(AppFeedback::getStatus)
                .orderByDesc(AppFeedback::getCreateTime)
                .orderByDesc(AppFeedback::getId);
        Page<AppFeedback> result = this.page(pageParam, wrapper);
        result.getRecords().forEach(this::fillDisplayFields);
        return result;
    }

    @Override
    public AppFeedback detail(Long id) {
        AppFeedback feedback = this.getById(id);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        fillDisplayFields(feedback);
        return feedback;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status, String handleRemark) {
        AppFeedback feedback = this.getById(id);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        if (!isValidStatus(status)) {
            throw new BusinessException("反馈状态不正确");
        }
        feedback.setStatus(status);
        feedback.setHandleRemark(normalizeOptional(handleRemark, 500));
        try {
            if (StpUtil.isLogin()) {
                feedback.setHandlerId(StpUtil.getLoginIdAsLong());
            }
        } catch (Exception ignored) {
        }
        feedback.setHandledAt(LocalDateTime.now());
        this.updateById(feedback);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
        this.removeByIds(Arrays.asList(ids));
    }

    private void normalizeAndValidateSubmit(AppFeedback feedback) {
        if (feedback == null) {
            throw new BusinessException("反馈信息不能为空");
        }
        if (feedback.getFeedbackType() == null) {
            feedback.setFeedbackType(TYPE_SUGGESTION);
        }
        if (!isValidType(feedback.getFeedbackType())) {
            throw new BusinessException("反馈类型不正确");
        }
        if (!StringUtils.hasText(feedback.getContent())) {
            throw new BusinessException("反馈内容不能为空");
        }
        String content = feedback.getContent().trim();
        if (content.length() < CONTENT_MIN_LENGTH) {
            throw new BusinessException("反馈内容至少5个字");
        }
        if (content.length() > CONTENT_MAX_LENGTH) {
            throw new BusinessException("反馈内容不能超过1000个字");
        }
        feedback.setContent(content);
        feedback.setPagePath(normalizeOptional(feedback.getPagePath(), 200));
        feedback.setClientInfo(normalizeOptional(feedback.getClientInfo(), 500));
        feedback.setHandlerId(null);
        feedback.setHandledAt(null);
        feedback.setHandleRemark(null);
    }

    private String normalizeOptional(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            return trimmed.substring(0, maxLength);
        }
        return trimmed;
    }

    private boolean isValidType(Integer type) {
        return type == TYPE_SUGGESTION || type == TYPE_BUG || type == TYPE_CONTENT || type == TYPE_OTHER;
    }

    private boolean isValidStatus(Integer status) {
        return status != null && (status == STATUS_PENDING || status == STATUS_PROCESSING
                || status == STATUS_DONE || status == STATUS_IGNORED);
    }

    private void fillDisplayFields(AppFeedback feedback) {
        if (feedback == null) {
            return;
        }
        String content = feedback.getContent();
        if (!StringUtils.hasText(content)) {
            feedback.setContentPreview("");
        } else {
            String normalized = content.replaceAll("\\s+", " ").trim();
            feedback.setContentPreview(normalized.length() > 60 ? normalized.substring(0, 60) + "..." : normalized);
        }
        if (feedback.getHandlerId() != null) {
            SysUser handler = sysUserService.getById(feedback.getHandlerId());
            if (handler != null) {
                feedback.setHandlerName(StringUtils.hasText(handler.getNickname()) ? handler.getNickname() : handler.getUsername());
            }
        }
    }
}
