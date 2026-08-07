package com.mars.biz.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 云224同步结果
 */
@Data
public class YunSyncResult {

    private Integer totalCount = 0;

    private Integer successCount = 0;

    private Integer failCount = 0;

    private List<String> errors = new ArrayList<>();

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;
}
