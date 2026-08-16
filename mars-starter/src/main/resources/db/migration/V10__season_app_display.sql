ALTER TABLE `yun_season`
  ADD COLUMN `app_display` tinyint DEFAULT '0' COMMENT '客户端显示(0-否 1-是)' AFTER `status`;

CREATE INDEX `idx_app_display` ON `yun_season` (`app_display`, `status`, `sort`, `id`);

UPDATE `yun_season`
SET `app_display` = 1
WHERE `deleted` = 0
  AND `status` = 1
ORDER BY `sort` ASC, `id` DESC
LIMIT 1;
