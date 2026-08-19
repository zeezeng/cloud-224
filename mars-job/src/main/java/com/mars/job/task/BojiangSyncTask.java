package com.mars.job.task;

import com.mars.biz.service.YunAnchorGiftSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 主播数据同步定时任务（使用在看数据源）。
 */
@Slf4j
@Component("bojiangSyncTask")
@RequiredArgsConstructor
public class BojiangSyncTask {

    private static final String DATA_SOURCE_DOSEEING = "DOSEEING";

    private final YunAnchorGiftSyncService yunAnchorGiftSyncService;

    /**
     * 同步今日和本月主播礼物数据
     */
    public void syncTodayAndMonth() {
        var result = yunAnchorGiftSyncService.syncTodayAndMonth("AUTO", DATA_SOURCE_DOSEEING);
        log.info("主播今日/本月同步完成: total={}, success={}, fail={}",
                result.getTotalCount(), result.getSuccessCount(), result.getFailCount());
    }

    /**
     * 补全昨日完整数据：昨日结束后执行一次，把日末(23:59:59)数据补齐，
     * 修复昨日记录停留在跨天前最后一次刷新的问题。
     */
    public void syncYesterday() {
        var result = yunAnchorGiftSyncService.syncYesterday("AUTO", DATA_SOURCE_DOSEEING);
        log.info("主播昨日补全完成: total={}, success={}, fail={}",
                result.getTotalCount(), result.getSuccessCount(), result.getFailCount());
    }

}
