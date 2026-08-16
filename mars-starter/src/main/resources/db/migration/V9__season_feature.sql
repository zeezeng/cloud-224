ALTER TABLE `yun_anchor`
  ADD COLUMN `big_image_url` varchar(500) DEFAULT NULL COMMENT '主播大图' AFTER `avatar_url`;

CREATE TABLE `yun_season` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `season_code` varchar(32) NOT NULL COMMENT '赛季编号，如 S10.5',
  `season_name` varchar(100) DEFAULT NULL COMMENT '赛季名称',
  `cover_image_url` varchar(500) DEFAULT NULL COMMENT '赛季封面图',
  `status` tinyint DEFAULT '1' COMMENT '状态(0-停用 1-启用)',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `sort` int DEFAULT '0' COMMENT '排序值，越小越靠前',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标识(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_season_code` (`season_code`) USING BTREE,
  KEY `idx_status_sort` (`status`, `sort`, `id` DESC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='主播赛季';

CREATE TABLE `yun_season_anchor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `season_id` bigint NOT NULL COMMENT '赛季ID',
  `anchor_ref_id` bigint DEFAULT NULL COMMENT '主播主表ID',
  `anchor_id` varchar(64) DEFAULT NULL COMMENT '主播业务ID',
  `platform` varchar(16) DEFAULT NULL COMMENT '平台(DOUYU-斗鱼 HUYA-虎牙)',
  `room_id` varchar(64) DEFAULT NULL COMMENT '房间号',
  `anchor_name` varchar(100) DEFAULT NULL COMMENT '主播名称',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '主播头像',
  `big_image_url` varchar(500) DEFAULT NULL COMMENT '主播大图',
  `team_name` varchar(100) DEFAULT NULL COMMENT '队伍名称',
  `captain_flag` tinyint DEFAULT '0' COMMENT '是否队长(0-否 1-是)',
  `eliminated` tinyint DEFAULT '0' COMMENT '是否淘汰(0-否 1-是)',
  `elimination_times` int DEFAULT '0' COMMENT '淘汰次数',
  `elimination_round` int DEFAULT '0' COMMENT '淘汰轮次',
  `next_elimination_amount` decimal(14,2) DEFAULT '0.00' COMMENT '下次淘汰金额',
  `sort` int DEFAULT '0' COMMENT '排序值，越小越靠前',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标识(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_season_anchor` (`season_id`, `anchor_ref_id`) USING BTREE,
  KEY `idx_season_sort` (`season_id`, `eliminated`, `captain_flag`, `sort`, `id` DESC) USING BTREE,
  KEY `idx_anchor_ref_id` (`anchor_ref_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='主播赛季成员';

INSERT INTO `sys_menu` (`parent_id`, `name`, `type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `is_frame`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`)
SELECT parent.id, '主播赛季', 2, '/yun/season', '/yun/season/index', 'yun:season:list', 'TrophyOutline', 4, 1, 1, 0, NOW(), NOW(), 1, 1, 0
FROM `sys_menu` parent
WHERE parent.path = '/yun'
  AND parent.type = 1
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'yun:season:list' AND `type` = 2);

INSERT INTO `sys_menu` (`parent_id`, `name`, `type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `is_frame`, `create_time`, `update_time`, `create_by`, `update_by`, `deleted`)
SELECT parent.id, child.name, 3, NULL, NULL, child.permission, NULL, child.sort, 1, 1, 0, NOW(), NOW(), 1, 1, 0
FROM `sys_menu` parent
JOIN (
  SELECT '赛季列表' AS name, 'yun:season:list' AS permission, 1 AS sort
  UNION ALL SELECT '赛季详情', 'yun:season:query', 2
  UNION ALL SELECT '赛季新增', 'yun:season:add', 3
  UNION ALL SELECT '赛季修改', 'yun:season:edit', 4
  UNION ALL SELECT '赛季删除', 'yun:season:remove', 5
  UNION ALL SELECT '赛季复制', 'yun:season:copy', 6
) child ON parent.permission = 'yun:season:list' AND parent.type = 2
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_menu` exists_menu
  WHERE exists_menu.parent_id = parent.id
    AND exists_menu.permission = child.permission
    AND exists_menu.type = 3
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu.id
FROM `sys_menu` menu
WHERE menu.permission IN ('yun:season:list', 'yun:season:query', 'yun:season:add', 'yun:season:edit', 'yun:season:remove', 'yun:season:copy')
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` role_menu
    WHERE role_menu.role_id = 1 AND role_menu.menu_id = menu.id
  );
