package com.mars.biz.service;

import com.mars.biz.dto.YunSyncProgress;
import com.mars.biz.dto.YunSyncResult;

/**
 * 云224主播礼物同步 Service
 */
public interface YunAnchorGiftSyncService {

    YunSyncResult syncAnchor(Long id, String triggerType);

    YunSyncResult syncAnchor(Long id, String triggerType, String dataSource);

    YunSyncResult syncAll(String triggerType);

    YunSyncResult syncAll(String triggerType, String dataSource);

    YunSyncResult syncTodayAndMonth(String triggerType);

    YunSyncResult syncTodayAndMonth(String triggerType, String dataSource);

    /**
     * 补全昨日完整数据。昨日结束后执行一次，把日末(23:59:59)数据写入，
     * 修复昨日记录停留在跨天前最后一次刷新的问题。
     */
    YunSyncResult syncYesterday(String triggerType);

    YunSyncResult syncYesterday(String triggerType, String dataSource);

    /**
     * 异步启动全部主播同步，立即返回任务进度。
     */
    YunSyncProgress startSyncAll(String triggerType, String dataSource);

    /**
     * 查询异步同步任务进度。
     */
    YunSyncProgress getSyncAllProgress(String taskId);
}
