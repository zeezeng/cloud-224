package com.mars.biz.dto;

import com.mars.biz.entity.YunSeasonAnchor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 赛季成员分页行
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class YunSeasonAnchorPageRow extends YunSeasonAnchor {

    private Integer anchorStatus;

    private Integer showRank;

    private String guildName;

    private Integer roomStatus;

    private String lastStartTime;
}
