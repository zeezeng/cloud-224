package com.mars.biz.dto;

import lombok.Data;

/**
 * 赛季复制请求
 */
@Data
public class YunSeasonCopyRequest {

    private String seasonCode;

    private String seasonName;

    private String coverImageUrl;

    private Integer status;

    private Boolean copyMembers;

    private String remark;
}
