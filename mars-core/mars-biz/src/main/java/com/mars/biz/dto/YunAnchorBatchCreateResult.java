package com.mars.biz.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 云224主播批量新增结果
 */
@Data
public class YunAnchorBatchCreateResult {

    private Integer totalCount = 0;

    private Integer successCount = 0;

    private Integer failCount = 0;

    private List<String> successAnchorIds = new ArrayList<>();

    private List<String> errors = new ArrayList<>();
}
