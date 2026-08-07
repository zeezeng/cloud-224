package com.mars.biz.service;

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

    YunSyncResult syncYesterday(String triggerType);

    YunSyncResult syncYesterday(String triggerType, String dataSource);
}
