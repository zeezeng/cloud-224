package com.mars.biz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mars.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * App 用户反馈
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_feedback")
public class AppFeedback extends BaseEntity {

    /**
     * 反馈类型（1想法建议 2Bug问题 3内容错误 4其他）
     */
    private Integer feedbackType;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 处理状态（0待处理 1处理中 2已完成 3已忽略）
     */
    private Integer status;

    /**
     * 提交时所在页面
     */
    private String pagePath;

    /**
     * 客户端信息
     */
    private String clientInfo;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理时间
     */
    private LocalDateTime handledAt;

    /**
     * 内部处理备注
     */
    private String handleRemark;

    /**
     * 内容摘要
     */
    @TableField(exist = false)
    private String contentPreview;

    /**
     * 处理人名称
     */
    @TableField(exist = false)
    private String handlerName;
}
