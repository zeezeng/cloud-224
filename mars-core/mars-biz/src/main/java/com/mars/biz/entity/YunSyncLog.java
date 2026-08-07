package com.mars.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mars.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 云224数据同步日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("yun_sync_log")
public class YunSyncLog extends BaseEntity {

    private String syncType;

    private String periodType;

    private String periodKey;

    private String triggerType;

    private String status;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;
}
