package com.mars.biz.dto;

import lombok.Data;

/**
 * 云224在看Cookie状态
 */
@Data
public class YunCookieStatus {

    /** 是否已配置Cookie */
    private Boolean configured = false;

    /**
     * 状态：
     * NOT_CONFIGURED 未配置；OK 有效；EXPIRED 已失效；ERROR 校验请求失败
     */
    private String status = "NOT_CONFIGURED";

    /** 状态说明 */
    private String message = "";
}
