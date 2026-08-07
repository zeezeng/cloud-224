package com.mars.job.task;

import com.mars.biz.service.YunAnchorGiftSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 主播数据同步定时任务（按主播 dataSource 使用播酱/在看）。
 */
@Slf4j
@Component("bojiangSyncTask")
@RequiredArgsConstructor
public class BojiangSyncTask {

    private final YunAnchorGiftSyncService yunAnchorGiftSyncService;

    /**
     * 同步今日和本月主播礼物数据
     */
    public void syncTodayAndMonth() {
        var result = yunAnchorGiftSyncService.syncTodayAndMonth("AUTO");
        log.info("主播今日/本月同步完成: total={}, success={}, fail={}",
                result.getTotalCount(), result.getSuccessCount(), result.getFailCount());
    }

    /**
     * 同步昨日主播礼物数据
     */
    public void syncYesterday() {
        var result = yunAnchorGiftSyncService.syncYesterday("AUTO");
        log.info("主播昨日同步完成: total={}, success={}, fail={}",
                result.getTotalCount(), result.getSuccessCount(), result.getFailCount());
    }
}
