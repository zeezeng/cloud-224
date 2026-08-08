-- Fix App home notice permissions for an existing database.
-- Run this on the mars-system database, then log out and log back in.

INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `is_frame`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`)
VALUES
  (347, 333, '首页公告', 2, '/yun/notice', '/yun/notice/index', 'yun:notice:list', 'NotificationsOutline', 2, 1, 1, 0, '2026-08-08 00:00:00', '2026-08-08 00:00:00', 1, 1, 0),
  (348, 347, '公告列表', 3, NULL, NULL, 'yun:notice:list', NULL, 1, 1, 1, 0, '2026-08-08 00:00:00', '2026-08-08 00:00:00', 1, 1, 0),
  (349, 347, '公告详情', 3, NULL, NULL, 'yun:notice:query', NULL, 2, 1, 1, 0, '2026-08-08 00:00:00', '2026-08-08 00:00:00', 1, 1, 0),
  (350, 347, '公告新增', 3, NULL, NULL, 'yun:notice:add', NULL, 3, 1, 1, 0, '2026-08-08 00:00:00', '2026-08-08 00:00:00', 1, 1, 0),
  (351, 347, '公告修改', 3, NULL, NULL, 'yun:notice:edit', NULL, 4, 1, 1, 0, '2026-08-08 00:00:00', '2026-08-08 00:00:00', 1, 1, 0),
  (352, 347, '公告删除', 3, NULL, NULL, 'yun:notice:remove', NULL, 5, 1, 1, 0, '2026-08-08 00:00:00', '2026-08-08 00:00:00', 1, 1, 0)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `permission` = VALUES(`permission`),
  `icon` = VALUES(`icon`),
  `sort` = VALUES(`sort`),
  `visible` = VALUES(`visible`),
  `status` = VALUES(`status`),
  `is_frame` = VALUES(`is_frame`),
  `update_time` = VALUES(`update_time`),
  `update_by` = VALUES(`update_by`),
  `deleted` = VALUES(`deleted`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.id
FROM `sys_menu` m
WHERE m.id IN (347, 348, 349, 350, 351, 352)
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` rm
    WHERE rm.role_id = 1 AND rm.menu_id = m.id
  );
