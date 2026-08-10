package com.mars.biz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mars.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * App 首页公告
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_notice")
public class AppNotice extends BaseEntity {

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告类型（1跑马灯 2弹窗）
     */
    private Integer noticeType;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;

    /**
     * 状态(0-下线 1-发布)
     */
    private Integer status;

    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;

    /**
     * 弹窗生效时间（为空表示立即生效，仅弹窗类型有效）
     */
    private LocalDateTime validFrom;

    /**
     * 弹窗失效时间（为空表示长期有效，仅弹窗类型有效）
     */
    private LocalDateTime validTo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 内容预览
     */
    @TableField(exist = false)
    private String contentPreview;
}
