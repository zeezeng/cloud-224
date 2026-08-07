package com.mars.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mars.biz.entity.AppBanner;

import java.util.List;

/**
 * App 首页轮播图 Service
 */
public interface AppBannerService extends IService<AppBanner> {

    /**
     * 后台分页查询
     */
    Page<AppBanner> page(Integer page, Integer pageSize, String title, Integer jumpType, Integer status);

    /**
     * 新增
     */
    void create(AppBanner banner);

    /**
     * 修改
     */
    void update(AppBanner banner);

    /**
     * 删除
     */
    void delete(Long[] ids);

    /**
     * App 首页启用轮播图列表
     */
    List<AppBanner> listEnabled();
}
