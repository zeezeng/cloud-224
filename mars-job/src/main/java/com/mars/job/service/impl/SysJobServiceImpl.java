package com.mars.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mars.common.exception.BusinessException;
import com.mars.common.result.PageResult;
import com.mars.job.entity.SysJob;
import com.mars.job.mapper.SysJobMapper;
import com.mars.job.service.SysJobService;
import com.mars.job.util.ScheduleUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 定时任务服务实现
 */
@Service
@RequiredArgsConstructor
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements SysJobService {

    private static final String JOB_NAME = "在看今日及本月同步";
    private static final String JOB_GROUP = "YUN224";
    private static final String INVOKE_TARGET = "bojiangSyncTask.syncTodayAndMonth";
    private static final String CRON_EXPRESSION = "0 0/10 * * * ?";
    private static final String REMARK = "同步在看今日和本月礼物榜，每10分钟执行一次";

    private final Scheduler scheduler;

    /**
     * 项目启动时，初始化定时任务
     */
    @PostConstruct
    public void init() throws SchedulerException {
        ensureDoseeingSyncJob();
        removeLegacyYesterdaySyncJob();
        scheduler.clear();
        List<SysJob> jobList = this.list();
        for (SysJob job : jobList) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
    }

    private void ensureDoseeingSyncJob() {
        LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysJob::getJobGroup, JOB_GROUP)
                .eq(SysJob::getInvokeTarget, INVOKE_TARGET);
        SysJob job = this.list(wrapper).stream().findFirst().orElse(null);
        if (job == null) {
            job = new SysJob();
            job.setJobName(JOB_NAME);
            job.setJobGroup(JOB_GROUP);
            job.setInvokeTarget(INVOKE_TARGET);
            job.setCronExpression(CRON_EXPRESSION);
            job.setMisfirePolicy(3);
            job.setConcurrent(1);
            job.setStatus(1);
            job.setRemark(REMARK);
            this.save(job);
            return;
        }

        boolean changed = false;
        if (!JOB_NAME.equals(job.getJobName())) {
            job.setJobName(JOB_NAME);
            changed = true;
        }
        if (!CRON_EXPRESSION.equals(job.getCronExpression())) {
            job.setCronExpression(CRON_EXPRESSION);
            changed = true;
        }
        if (!Integer.valueOf(3).equals(job.getMisfirePolicy())) {
            job.setMisfirePolicy(3);
            changed = true;
        }
        if (!Integer.valueOf(1).equals(job.getConcurrent())) {
            job.setConcurrent(1);
            changed = true;
        }
        if (!Integer.valueOf(1).equals(job.getStatus())) {
            job.setStatus(1);
            changed = true;
        }
        if (!REMARK.equals(job.getRemark())) {
            job.setRemark(REMARK);
            changed = true;
        }
        if (changed) {
            this.updateById(job);
        }
    }

    private void removeLegacyYesterdaySyncJob() {
        LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysJob::getId, 5L);
        this.remove(wrapper);
    }

    @Override
    public PageResult<SysJob> page(Integer page, Integer pageSize, String jobName, String jobGroup, Integer status) {
        Page<SysJob> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(jobName), SysJob::getJobName, jobName)
                .eq(StringUtils.hasText(jobGroup), SysJob::getJobGroup, jobGroup)
                .eq(status != null, SysJob::getStatus, status)
                .orderByDesc(SysJob::getCreateTime);
        return PageResult.of(this.page(pageParam, wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysJob job) {
        job.setStatus(0); // 默认暂停
        this.save(job);
        try {
            ScheduleUtils.createScheduleJob(scheduler, job);
        } catch (SchedulerException e) {
            throw new BusinessException("创建定时任务失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysJob job) {
        SysJob existJob = this.getById(job.getId());
        if (existJob == null) {
            throw new BusinessException("任务不存在");
        }
        this.updateById(job);
        try {
            ScheduleUtils.updateScheduleJob(scheduler, job);
        } catch (SchedulerException e) {
            throw new BusinessException("更新定时任务失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysJob job = this.getById(id);
        if (job != null) {
            this.removeById(id);
            try {
                ScheduleUtils.deleteScheduleJob(scheduler, job);
            } catch (SchedulerException e) {
                throw new BusinessException("删除定时任务失败：" + e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        SysJob job = this.getById(id);
        if (job == null) {
            throw new BusinessException("任务不存在");
        }
        job.setStatus(status);
        this.updateById(job);
        try {
            if (status == 1) {
                ScheduleUtils.resumeJob(scheduler, job);
            } else {
                ScheduleUtils.pauseJob(scheduler, job);
            }
        } catch (SchedulerException e) {
            throw new BusinessException("修改任务状态失败：" + e.getMessage());
        }
    }

    @Override
    public void run(Long id) {
        SysJob job = this.getById(id);
        if (job == null) {
            throw new BusinessException("任务不存在");
        }
        try {
            ScheduleUtils.run(scheduler, job);
        } catch (SchedulerException e) {
            throw new BusinessException("执行任务失败：" + e.getMessage());
        }
    }
}
