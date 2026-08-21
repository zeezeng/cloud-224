-- 为 yun_season 增加总奖金字段（管理端设置，客户端展示用，仅供参考）
-- 兼容已手动执行过该 DDL 的环境：列已存在则跳过
SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'yun_season'
    AND COLUMN_NAME = 'total_bonus'
);

SET @ddl = IF(
  @column_exists > 0,
  'SELECT 1',
  'ALTER TABLE `yun_season` ADD COLUMN `total_bonus` DECIMAL(18, 2) NULL DEFAULT 0.00 COMMENT ''赛季总奖金(元)，客户端展示用，仅供参考'' AFTER `sort`'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
