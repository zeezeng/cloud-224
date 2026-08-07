package com.mars.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mars.biz.entity.AppBanner;
import com.mars.biz.mapper.AppBannerMapper;
import com.mars.biz.service.AppBannerService;
import com.mars.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * App 首页轮播图 Service 实现
 */
@Service
public class AppBannerServiceImpl extends ServiceImpl<AppBannerMapper, AppBanner> implements AppBannerService {

    private static final int JUMP_TYPE_NONE = 0;
    private static final int JUMP_TYPE_PAGE = 1;
    private static final int JUMP_TYPE_WEB = 2;
    private static final int STATUS_ENABLED = 1;

    @Override
    public Page<AppBanner> page(Integer page, Integer pageSize, String title, Integer jumpType, Integer status) {
        Page<AppBanner> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AppBanner> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(AppBanner::getTitle, title.trim());
        }
        if (jumpType != null) {
            wrapper.eq(AppBanner::getJumpType, jumpType);
        }
        if (status != null) {
            wrapper.eq(AppBanner::getStatus, status);
        }
        wrapper.orderByAsc(AppBanner::getSort).orderByDesc(AppBanner::getId);
        return this.page(pageParam, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(AppBanner banner) {
        normalizeAndValidate(banner);
        if (banner.getStatus() == null) {
            banner.setStatus(STATUS_ENABLED);
        }
        if (banner.getSort() == null) {
            banner.setSort(0);
        }
        this.save(banner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AppBanner banner) {
        if (banner.getId() == null) {
            throw new BusinessException("轮播图ID不能为空");
        }
        normalizeAndValidate(banner);
        this.updateById(banner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
        this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public List<AppBanner> listEnabled() {
        LambdaQueryWrapper<AppBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppBanner::getStatus, STATUS_ENABLED)
                .orderByAsc(AppBanner::getSort)
                .orderByDesc(AppBanner::getId);
        return this.list(wrapper);
    }

    private void normalizeAndValidate(AppBanner banner) {
        if (banner == null) {
            throw new BusinessException("轮播图信息不能为空");
        }
        if (!StringUtils.hasText(banner.getImageUrl())) {
            throw new BusinessException("轮播图图片不能为空");
        }

        banner.setImageUrl(banner.getImageUrl().trim());
        if (StringUtils.hasText(banner.getTitle())) {
            banner.setTitle(banner.getTitle().trim());
        }
        if (StringUtils.hasText(banner.getDescription())) {
            banner.setDescription(banner.getDescription().trim());
        }
        if (banner.getJumpType() == null) {
            banner.setJumpType(JUMP_TYPE_NONE);
        }

        if (banner.getJumpType() == JUMP_TYPE_NONE) {
            banner.setJumpTarget(null);
            return;
        }

        if (!StringUtils.hasText(banner.getJumpTarget())) {
            throw new BusinessException("跳转目标不能为空");
        }

        String jumpTarget = banner.getJumpTarget().trim();
        if (banner.getJumpType() == JUMP_TYPE_PAGE) {
            if (!(jumpTarget.startsWith("/pages/") || jumpTarget.startsWith("pages/"))) {
                throw new BusinessException("小程序页面路径必须以 /pages/ 或 pages/ 开头");
            }
            banner.setJumpTarget(jumpTarget);
            return;
        }

        if (banner.getJumpType() == JUMP_TYPE_WEB) {
            if (!(jumpTarget.startsWith("http://") || jumpTarget.startsWith("https://"))) {
                throw new BusinessException("网页URL必须以 http:// 或 https:// 开头");
            }
            banner.setJumpTarget(jumpTarget);
            return;
        }

        throw new BusinessException("不支持的跳转类型");
    }
}
