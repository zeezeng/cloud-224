CREATE TABLE IF NOT EXISTS `app_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `feedback_type` tinyint NOT NULL DEFAULT '1' COMMENT '反馈类型(1-想法建议 2-Bug问题 3-内容错误 4-其他)',
  `content` varchar(1000) NOT NULL COMMENT '反馈内容',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态(0-待处理 1-处理中 2-已完成 3-已忽略)',
  `page_path` varchar(200) DEFAULT NULL COMMENT '提交时所在页面',
  `client_info` varchar(500) DEFAULT NULL COMMENT '客户端信息',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(500) DEFAULT NULL COMMENT '内部处理备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标识(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status_create_time` (`status`,`create_time` DESC,`id` DESC) USING BTREE,
  KEY `idx_type_status` (`feedback_type`,`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='App用户反馈';

INSERT INTO `sys_menu` (`parent_id`, `name`, `type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `is_frame`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`)
SELECT 333, '用户反馈', 2, '/yun/feedback', '/yun/feedback/index', 'yun:feedback:list', 'HelpOutline', 3, 1, 1, 0, NOW(), NOW(), 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'yun:feedback:list' AND `type` = 2);

INSERT INTO `sys_menu` (`parent_id`, `name`, `type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `is_frame`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`)
SELECT parent.id, child.name, 3, NULL, NULL, child.permission, NULL, child.sort, 1, 1, 0, NOW(), NOW(), 1, 1, 0
FROM `sys_menu` parent
JOIN (
  SELECT '反馈列表' AS name, 'yun:feedback:list' AS permission, 1 AS sort
  UNION ALL SELECT '反馈详情', 'yun:feedback:query', 2
  UNION ALL SELECT '反馈处理', 'yun:feedback:handle', 3
  UNION ALL SELECT '反馈删除', 'yun:feedback:remove', 4
) child ON parent.permission = 'yun:feedback:list' AND parent.type = 2
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` exists_menu WHERE exists_menu.parent_id = parent.id AND exists_menu.permission = child.permission AND exists_menu.type = 3);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu.id
FROM `sys_menu` menu
WHERE menu.permission IN ('yun:feedback:list', 'yun:feedback:query', 'yun:feedback:handle', 'yun:feedback:remove')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` role_menu
    WHERE role_menu.role_id = 1 AND role_menu.menu_id = menu.id
  );

INSERT INTO `sys_config_group` (`group_code`, `group_name`, `config_value`, `sort`, `status`, `remark`, `create_time`, `update_time`)
SELECT 'feedbackContact', '反馈联系设置', '{"wechatId":"","qrcodeUrl":"","remark":"也可以添加微信直接沟通"}', 21, 1, '小程序用户反馈页展示的微信号与二维码，由反馈管理页维护', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_config_group` WHERE `group_code` = 'feedbackContact');
