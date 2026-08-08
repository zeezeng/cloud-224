package com.mars.biz.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 云224主播礼物同步进度（异步任务）
 */
@Data
public class YunSyncProgress {

    private String taskId;

    private Boolean running = false;

    private Integer totalCount = 0;

    private Integer completedCount = 0;

    private Integer successCount = 0;

    private Integer failCount = 0;

    private String currentAnchorId;

    private String currentPeriodKey;

    private List<String> errors = new ArrayList<>();

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;
}
