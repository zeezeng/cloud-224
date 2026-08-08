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
     * 备注
     */
    private String remark;

    /**
     * 内容预览
     */
    @TableField(exist = false)
    private String contentPreview;
}
