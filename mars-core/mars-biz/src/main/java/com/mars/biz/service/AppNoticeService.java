package com.mars.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mars.biz.entity.AppNotice;

import java.util.List;

/**
 * App 首页公告 Service
 */
public interface AppNoticeService extends IService<AppNotice> {

    /**
     * 后台分页查询
     */
    Page<AppNotice> page(Integer page, Integer pageSize, String title, Integer status, Integer noticeType);

    /**
     * 新增
     */
    void create(AppNotice notice);

    /**
     * 修改
     */
    void update(AppNotice notice);

    /**
     * 删除
     */
    void delete(Long[] ids);

    /**
     * 发布
     */
    void publish(Long id);

    /**
     * 下线
     */
    void offline(Long id);

    /**
     * App 首页跑马灯公告列表（noticeType=1）
     */
    List<AppNotice> listPublished(Integer limit);

    /**
     * App 当前有效的弹窗公告列表（noticeType=2，发布状态，在有效期内）
     */
    List<AppNotice> listActivePopup();
}
