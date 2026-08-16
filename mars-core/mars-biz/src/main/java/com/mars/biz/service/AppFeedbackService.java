package com.mars.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mars.biz.entity.AppFeedback;

/**
 * App 用户反馈 Service
 */
public interface AppFeedbackService extends IService<AppFeedback> {

    /**
     * 提交反馈
     */
    Long submit(AppFeedback feedback);

    /**
     * 后台分页查询
     */
    Page<AppFeedback> page(Integer page, Integer pageSize, Integer feedbackType, Integer status, String keyword,
                           String beginTime, String endTime);

    /**
     * 详情
     */
    AppFeedback detail(Long id);

    /**
     * 更新处理状态
     */
    void updateStatus(Long id, Integer status, String handleRemark);

    /**
     * 删除
     */
    void delete(Long[] ids);
}
