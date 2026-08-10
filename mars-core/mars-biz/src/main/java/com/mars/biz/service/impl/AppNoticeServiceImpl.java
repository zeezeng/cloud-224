package com.mars.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mars.biz.entity.AppNotice;
import com.mars.biz.mapper.AppNoticeMapper;
import com.mars.biz.service.AppNoticeService;
import com.mars.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * App 首页公告 Service 实现
 */
@Service
public class AppNoticeServiceImpl extends ServiceImpl<AppNoticeMapper, AppNotice> implements AppNoticeService {

    private static final int STATUS_OFFLINE = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;
    private static final int TYPE_MARQUEE = 1;
    private static final int TYPE_POPUP = 2;

    @Override
    public Page<AppNotice> page(Integer page, Integer pageSize, String title, Integer status, Integer noticeType) {
        Page<AppNotice> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AppNotice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(AppNotice::getTitle, title.trim());
        }
        if (status != null) {
            wrapper.eq(AppNotice::getStatus, status);
        }
        if (noticeType != null) {
            wrapper.eq(AppNotice::getNoticeType, noticeType);
        }
        wrapper.orderByAsc(AppNotice::getSort)
                .orderByDesc(AppNotice::getPublishedAt)
                .orderByDesc(AppNotice::getId);
        Page<AppNotice> result = this.page(pageParam, wrapper);
        result.getRecords().forEach(this::fillPreview);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(AppNotice notice) {
        normalizeAndValidate(notice);
        if (notice.getStatus() == null) {
            notice.setStatus(STATUS_OFFLINE);
        }
        if (notice.getSort() == null) {
            notice.setSort(0);
        }
        if (notice.getStatus() == STATUS_PUBLISHED && notice.getPublishedAt() == null) {
            notice.setPublishedAt(LocalDateTime.now());
        }
        this.save(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AppNotice notice) {
        if (notice.getId() == null) {
            throw new BusinessException("公告ID不能为空");
        }
        AppNotice existing = this.getById(notice.getId());
        if (existing == null) {
            throw new BusinessException("公告不存在");
        }
        // 编辑时未传类型则保留原类型，避免被误改为跑马灯
        if (notice.getNoticeType() == null) {
            notice.setNoticeType(existing.getNoticeType());
        }
        normalizeAndValidate(notice);
        if (notice.getSort() == null) {
            notice.setSort(existing.getSort());
        }
        if (notice.getStatus() == null) {
            notice.setStatus(existing.getStatus());
        }
        if (notice.getStatus() == STATUS_PUBLISHED) {
            notice.setPublishedAt(existing.getPublishedAt() == null ? LocalDateTime.now() : existing.getPublishedAt());
        } else {
            notice.setPublishedAt(existing.getPublishedAt());
        }
        this.updateById(notice);
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
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        AppNotice notice = requireNotice(id);
        notice.setStatus(STATUS_PUBLISHED);
        notice.setPublishedAt(LocalDateTime.now());
        this.updateById(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offline(Long id) {
        AppNotice notice = requireNotice(id);
        notice.setStatus(STATUS_OFFLINE);
        this.updateById(notice);
    }

    @Override
    public List<AppNotice> listPublished(Integer limit) {
        int finalLimit = normalizeLimit(limit);
        LambdaQueryWrapper<AppNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppNotice::getStatus, STATUS_PUBLISHED)
                .and(w -> w.eq(AppNotice::getNoticeType, TYPE_MARQUEE).or().isNull(AppNotice::getNoticeType))
                .orderByAsc(AppNotice::getSort)
                .orderByDesc(AppNotice::getPublishedAt)
                .orderByDesc(AppNotice::getId)
                .last("limit " + finalLimit);
        List<AppNotice> list = this.list(wrapper);
        list.forEach(this::fillPreview);
        return list;
    }

    @Override
    public List<AppNotice> listActivePopup() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<AppNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppNotice::getNoticeType, TYPE_POPUP)
                .eq(AppNotice::getStatus, STATUS_PUBLISHED)
                .and(w -> w.isNull(AppNotice::getValidFrom).or().le(AppNotice::getValidFrom, now))
                .and(w -> w.isNull(AppNotice::getValidTo).or().ge(AppNotice::getValidTo, now))
                .orderByAsc(AppNotice::getSort)
                .orderByDesc(AppNotice::getPublishedAt)
                .orderByDesc(AppNotice::getId);
        List<AppNotice> list = this.list(wrapper);
        list.forEach(this::fillPreview);
        return list;
    }

    private AppNotice requireNotice(Long id) {
        AppNotice notice = this.getById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        return notice;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private void normalizeAndValidate(AppNotice notice) {
        if (notice == null) {
            throw new BusinessException("公告信息不能为空");
        }
        if (!StringUtils.hasText(notice.getContent())) {
            throw new BusinessException("公告内容不能为空");
        }

        // 类型默认跑马灯，校验取值
        if (notice.getNoticeType() == null) {
            notice.setNoticeType(TYPE_MARQUEE);
        }
        if (notice.getNoticeType() != TYPE_MARQUEE && notice.getNoticeType() != TYPE_POPUP) {
            throw new BusinessException("公告类型不正确");
        }
        // 弹窗类型必须有标题
        if (notice.getNoticeType() == TYPE_POPUP && !StringUtils.hasText(notice.getTitle())) {
            throw new BusinessException("弹窗公告标题不能为空");
        }

        // 标题可选：有值则 trim，无值置 null
        if (StringUtils.hasText(notice.getTitle())) {
            notice.setTitle(notice.getTitle().trim());
        } else {
            notice.setTitle(null);
        }
        notice.setContent(notice.getContent().trim());
        if (StringUtils.hasText(notice.getRemark())) {
            notice.setRemark(notice.getRemark().trim());
        }
        if (notice.getStatus() == null) {
            notice.setStatus(STATUS_OFFLINE);
        }
        if (notice.getStatus() != STATUS_OFFLINE && notice.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException("公告状态不正确");
        }
        if (notice.getSort() == null) {
            notice.setSort(0);
        }
    }

    private void fillPreview(AppNotice notice) {
        if (notice == null) {
            return;
        }
        String content = notice.getContent();
        if (!StringUtils.hasText(content)) {
            notice.setContentPreview("");
            return;
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() > 60) {
            notice.setContentPreview(normalized.substring(0, 60) + "...");
            return;
        }
        notice.setContentPreview(normalized);
    }
}
