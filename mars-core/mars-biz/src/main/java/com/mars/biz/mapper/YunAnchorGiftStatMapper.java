package com.mars.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mars.biz.entity.YunAnchorGiftStat;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 云224主播礼物统计 Mapper
 */
@Mapper
public interface YunAnchorGiftStatMapper extends BaseMapper<YunAnchorGiftStat> {

    /**
     * 基于唯一键 (anchor_id, period_type, period_key) 的原子写入：
     * 记录不存在则插入，存在则更新。
     * 相比“先查再插”(selectOne + insert/updateById)，本条单语句原子执行，
     * 避免在高并发/长事务下对唯一索引产生间隙锁而导致的锁等待超时。
     */
    @Insert("INSERT INTO yun_anchor_gift_stat ("
            + "anchor_id, room_id, period_type, period_key, external_rank_no, gift_total_value,"
            + "paid_gift_value, bag_gift_value, fishball_gift_count, gift_user_count, paid_gift_user_count,"
            + "stream_hours, active_audience_count, danmu_count, danmu_user_count, duration_text,"
            + "room_status, lived, last_start_time, source_update_time, raw_json, synced_at, update_time"
            + ") VALUES ("
            + "#{anchorId}, #{roomId}, #{periodType}, #{periodKey}, #{externalRankNo}, #{giftTotalValue},"
            + "#{paidGiftValue}, #{bagGiftValue}, #{fishballGiftCount}, #{giftUserCount}, #{paidGiftUserCount},"
            + "#{streamHours}, #{activeAudienceCount}, #{danmuCount}, #{danmuUserCount}, #{durationText},"
            + "#{roomStatus}, #{lived}, #{lastStartTime}, #{sourceUpdateTime}, #{rawJson}, #{syncedAt}, NOW()"
            + ") ON DUPLICATE KEY UPDATE"
            + " room_id = VALUES(room_id), external_rank_no = VALUES(external_rank_no),"
            + " gift_total_value = VALUES(gift_total_value), paid_gift_value = VALUES(paid_gift_value),"
            + " bag_gift_value = VALUES(bag_gift_value), fishball_gift_count = VALUES(fishball_gift_count),"
            + " gift_user_count = VALUES(gift_user_count), paid_gift_user_count = VALUES(paid_gift_user_count),"
            + " stream_hours = VALUES(stream_hours), active_audience_count = VALUES(active_audience_count),"
            + " danmu_count = VALUES(danmu_count), danmu_user_count = VALUES(danmu_user_count),"
            + " duration_text = VALUES(duration_text), room_status = VALUES(room_status),"
            + " lived = VALUES(lived), last_start_time = VALUES(last_start_time),"
            + " source_update_time = VALUES(source_update_time), raw_json = VALUES(raw_json),"
            + " synced_at = VALUES(synced_at), update_time = NOW()")
    int upsert(YunAnchorGiftStat stat);
}