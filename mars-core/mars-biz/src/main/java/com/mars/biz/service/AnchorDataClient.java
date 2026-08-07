package com.mars.biz.service;

import com.mars.biz.dto.BojiangAnchorInfo;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 主播数据源客户端统一接口。
 */
public interface AnchorDataClient {

    String sourceCode();

    BojiangAnchorInfo fetchAnchorProfile(String anchorId);

    BojiangAnchorInfo fetchDailyAnchor(String anchorId, LocalDate date);

    BojiangAnchorInfo fetchMonthAnchor(String anchorId, YearMonth month);
}
