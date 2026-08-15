-- 云224主播表：增加平台字段，区分斗鱼/虎牙，唯一索引改为 (platform, anchor_id)
ALTER TABLE `yun_anchor`
  ADD COLUMN `platform` varchar(16) NOT NULL DEFAULT 'DOUYU' COMMENT '平台(DOUYU-斗鱼 HUYA-虎牙)' AFTER `anchor_id`;

DROP INDEX `uk_anchor_id` ON `yun_anchor`;
ALTER TABLE `yun_anchor`
  ADD UNIQUE INDEX `uk_platform_anchor_id`(`platform`, `anchor_id`);
