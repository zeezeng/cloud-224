-- 将 yun_season_anchor.elimination_times 重命名为 fail_times（开条次数）
-- 兼容已手动执行过重命名的环境：列已存在则跳过
SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'yun_season_anchor'
    AND COLUMN_NAME = 'elimination_times'
);

SET @ddl = IF(
  @column_exists > 0,
  'ALTER TABLE `yun_season_anchor` CHANGE COLUMN `elimination_times` `fail_times` int DEFAULT ''0'' COMMENT ''开条次数''',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;