ALTER TABLE `yun_anchor_gift_stat`
  ADD COLUMN `paid_gift_user_count` int DEFAULT NULL COMMENT '付费送礼人数(SR人数)' AFTER `gift_user_count`,
  ADD COLUMN `stream_hours` decimal(10,2) DEFAULT NULL COMMENT '开播小时' AFTER `paid_gift_user_count`;