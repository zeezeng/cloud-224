package com.mars.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mars.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App 首页轮播图
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_banner")
public class AppBanner extends BaseEntity {

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片地址
     */
    private String imageUrl;

    /**
     * 跳转类型(0-不跳转 1-小程序页面 2-网页URL)
     */
    private Integer jumpType;

    /**
     * 跳转目标
     */
    private String jumpTarget;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;

    /**
     * 状态(0-禁用 1-启用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
