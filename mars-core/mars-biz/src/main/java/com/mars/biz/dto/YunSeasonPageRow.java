package com.mars.biz.dto;

import com.mars.biz.entity.YunSeason;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 赛季分页行
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class YunSeasonPageRow extends YunSeason {

    private Integer memberCount;

    private Integer activeCount;

    private Integer eliminatedCount;

    private Integer captainCount;
}
