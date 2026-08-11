-- ============================================================
-- Flyway 初始化脚本（V1）—— 建表 + 初始数据
-- ------------------------------------------------------------
-- 全新数据库启动时 Flyway 执行本脚本，一步完成：
--   1. 创建所有表结构（50 张表）
--   2. 写入初始数据（管理员/角色/菜单/字典/岗位/配置等）
--
-- 初始管理员账号：admin / admin123
--
-- 后续数据库结构变更规范：
--   新增 V2__xxx.sql、V3__xxx.sql ... 放入本目录，
--   后端启动时 Flyway 会按版本号顺序自动执行未应用的脚本。
--   脚本命名：V<版本号>__<简短描述>.sql（双下划线分隔）
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE `app_banner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标题',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '描述',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片地址',
  `jump_type` tinyint DEFAULT '0' COMMENT '跳转类型(0-不跳转 1-小程序页面 2-网页URL)',
  `jump_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '跳转目标',
  `sort` int DEFAULT '0' COMMENT '排序值，越小越靠前',
  `status` tinyint DEFAULT '1' COMMENT '状态(0-禁用 1-启用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标识(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status_sort` (`status`,`sort`,`id` DESC) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='App首页轮播图'
;

CREATE TABLE `app_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(120) DEFAULT NULL COMMENT 'notice title, nullable for marquee',
  `content` text NOT NULL,
  `notice_type` tinyint NOT NULL DEFAULT '1' COMMENT 'notice type: 1 marquee 2 popup',
  `sort` int DEFAULT '0',
  `status` tinyint DEFAULT '0',
  `published_at` datetime DEFAULT NULL,
  `valid_from` datetime DEFAULT NULL COMMENT 'popup valid from',
  `valid_to` datetime DEFAULT NULL COMMENT 'popup valid to',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort_publish` (`status`,`sort`,`published_at` DESC,`id` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
;

CREATE TABLE `coder_banner` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `link` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `tag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `btn_text` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `sort` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `position` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC
;

CREATE TABLE `gen_table` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '???',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '???',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '?????',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '?????',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '?????',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '?????',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '?????',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '??????',
  `gen_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'crud' COMMENT '?????crud?? tree???',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '/' COMMENT '????',
  `front_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'naive-ui' COMMENT '??????',
  `form_layout` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'vertical' COMMENT '?????vertical-???? grid-?????',
  `parent_menu_id` bigint DEFAULT NULL COMMENT '????ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???????'
;

CREATE TABLE `gen_table_column` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??',
  `table_id` bigint DEFAULT NULL COMMENT '?????',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '???',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '???',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '???',
  `java_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'Java??',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'Java???',
  `is_pk` tinyint DEFAULT '0' COMMENT '?????1??',
  `is_increment` tinyint DEFAULT '0' COMMENT '?????1??',
  `is_required` tinyint DEFAULT '0' COMMENT '?????1??',
  `is_insert` tinyint DEFAULT '0' COMMENT '????????1??',
  `is_edit` tinyint DEFAULT '0' COMMENT '???????1??',
  `is_list` tinyint DEFAULT '0' COMMENT '???????1??',
  `is_query` tinyint DEFAULT '0' COMMENT '???????1??',
  `query_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'EQ' COMMENT '????',
  `html_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '????',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '????',
  `sort` int DEFAULT '0' COMMENT '??',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_table_id` (`table_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???????'
;

CREATE TABLE `qrtz_blob_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers?trigger_name???',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers?trigger_group???',
  `blob_data` blob COMMENT '?????Trigger??',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Blob???????'
;

CREATE TABLE `qrtz_calendars` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `calendar` blob NOT NULL COMMENT '?????calendar??',
  PRIMARY KEY (`sched_name`,`calendar_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?????'
;

CREATE TABLE `qrtz_cron_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers?trigger_name???',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers?trigger_group???',
  `cron_expression` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'cron???',
  `time_zone_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Cron???????'
;

CREATE TABLE `qrtz_fired_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `entry_id` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '?????id',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers?trigger_name???',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers?trigger_group???',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '??????',
  `fired_time` bigint NOT NULL COMMENT '?????',
  `sched_time` bigint NOT NULL COMMENT '????????',
  `priority` int NOT NULL COMMENT '???',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '??',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????????',
  PRIMARY KEY (`sched_name`,`entry_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='????????'
;

CREATE TABLE `qrtz_job_details` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `job_class_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '???????',
  `is_durable` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '?????',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `is_update_data` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '??????',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????????',
  `job_data` blob COMMENT '?????job??',
  PRIMARY KEY (`sched_name`,`job_name`,`job_group`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???????'
;

CREATE TABLE `qrtz_locks` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `lock_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '?????',
  PRIMARY KEY (`sched_name`,`lock_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?????????'
;

CREATE TABLE `qrtz_paused_trigger_grps` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers?trigger_group???',
  PRIMARY KEY (`sched_name`,`trigger_group`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???????'
;

CREATE TABLE `qrtz_scheduler_state` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `last_checkin_time` bigint NOT NULL COMMENT '??????',
  `checkin_interval` bigint NOT NULL COMMENT '??????',
  PRIMARY KEY (`sched_name`,`instance_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='??????'
;

CREATE TABLE `qrtz_simple_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers?trigger_name???',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers?trigger_group???',
  `repeat_count` bigint NOT NULL COMMENT '???????',
  `repeat_interval` bigint NOT NULL COMMENT '???????',
  `times_triggered` bigint NOT NULL COMMENT '???????',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?????????'
;

CREATE TABLE `qrtz_simprop_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers?trigger_name???',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers?trigger_group???',
  `str_prop_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'String???trigger??????',
  `str_prop_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'String???trigger??????',
  `str_prop_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'String???trigger??????',
  `int_prop_1` int DEFAULT NULL COMMENT 'int???trigger??????',
  `int_prop_2` int DEFAULT NULL COMMENT 'int???trigger??????',
  `long_prop_1` bigint DEFAULT NULL COMMENT 'long???trigger??????',
  `long_prop_2` bigint DEFAULT NULL COMMENT 'long???trigger??????',
  `dec_prop_1` decimal(13,4) DEFAULT NULL COMMENT 'decimal???trigger??????',
  `dec_prop_2` decimal(13,4) DEFAULT NULL COMMENT 'decimal???trigger??????',
  `bool_prop_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Boolean???trigger??????',
  `bool_prop_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Boolean???trigger??????',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='????????'
;

CREATE TABLE `qrtz_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '??????',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '?????????',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_job_details?job_name???',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_job_details?job_group???',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `next_fire_time` bigint DEFAULT NULL COMMENT '???????????',
  `prev_fire_time` bigint DEFAULT NULL COMMENT '???????????-1??????',
  `priority` int DEFAULT NULL COMMENT '???',
  `trigger_state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '?????',
  `trigger_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '??????',
  `start_time` bigint NOT NULL COMMENT '????',
  `end_time` bigint DEFAULT NULL COMMENT '????',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????',
  `misfire_instr` smallint DEFAULT NULL COMMENT '???????',
  `job_data` blob COMMENT '?????job??',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  KEY `sched_name` (`sched_name`,`job_name`,`job_group`) USING BTREE,
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='????????'
;

CREATE TABLE `student` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '??',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '??',
  `gender` tinyint DEFAULT NULL COMMENT '?? 1? 2?',
  `birthday` date DEFAULT NULL COMMENT '????',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `class_id` bigint DEFAULT NULL COMMENT '??ID',
  `status` tinyint DEFAULT '1' COMMENT '??',
  `deleted` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???'
;

CREATE TABLE `sys_api_access_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `start_time` datetime DEFAULT NULL COMMENT '??????',
  `end_time` datetime DEFAULT NULL COMMENT '??????',
  `api_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'API??',
  `method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'HTTP??',
  `status_code` int DEFAULT NULL COMMENT 'HTTP???',
  `success` tinyint DEFAULT '1' COMMENT '????(0? 1?)',
  `cost_time` bigint DEFAULT NULL COMMENT '??(??)',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???IP',
  `user_id` bigint DEFAULT NULL COMMENT '??ID(?????)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_start_time` (`start_time`) USING BTREE,
  KEY `idx_api_path` (`api_path`(100)) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4010 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='API??????'
;

CREATE TABLE `sys_chat_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '?ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '???',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???',
  `owner_id` bigint NOT NULL COMMENT '??ID',
  `announcement` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???',
  `max_members` int DEFAULT '200' COMMENT '?????',
  `status` tinyint DEFAULT '1' COMMENT '???0-?? 1-??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_owner_id` (`owner_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???'
;

CREATE TABLE `sys_chat_group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `group_id` bigint NOT NULL COMMENT '?ID',
  `user_id` bigint NOT NULL COMMENT '??ID',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `role` tinyint DEFAULT '0' COMMENT '???0-???? 1-??? 2-??',
  `muted` tinyint DEFAULT '0' COMMENT '?????0-? 1-?',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_group_user` (`group_id`,`user_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='????'
;

CREATE TABLE `sys_chat_group_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `group_id` bigint NOT NULL COMMENT '?ID',
  `sender_id` bigint NOT NULL COMMENT '???ID',
  `sender_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????',
  `sender_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `msg_type` tinyint DEFAULT '1' COMMENT '?????1-?? 2-?? 3-?? 4-????',
  `send_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_group_id` (`group_id`) USING BTREE,
  KEY `idx_send_time` (`send_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='????'
;

CREATE TABLE `sys_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `sender_id` bigint NOT NULL COMMENT '???ID',
  `sender_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????',
  `sender_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????',
  `receiver_id` bigint NOT NULL COMMENT '???ID(0????)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '????',
  `msg_type` tinyint DEFAULT '1' COMMENT '????(1?? 2?? 3??)',
  `is_read` tinyint DEFAULT '0' COMMENT '????(0?? 1??)',
  `send_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sender_id` (`sender_id`) USING BTREE,
  KEY `idx_receiver_id` (`receiver_id`) USING BTREE,
  KEY `idx_send_time` (`send_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=504 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?????'
;

CREATE TABLE `sys_config_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `group_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `group_icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '???(JSON??)',
  `sort` int DEFAULT '0' COMMENT '??',
  `status` tinyint DEFAULT '1' COMMENT '??(0-?? 1-??)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_group_code` (`group_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???????'
;

CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `parent_id` bigint DEFAULT '0' COMMENT '???ID',
  `ancestors` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '????',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `sort` int DEFAULT '0' COMMENT '????',
  `leader` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `status` tinyint DEFAULT '1' COMMENT '??(0-?? 1-??)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `create_by` bigint DEFAULT NULL COMMENT '???',
  `update_by` bigint DEFAULT NULL COMMENT '???',
  `deleted` tinyint DEFAULT '0' COMMENT '????(0-??? 1-???)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???'
;

CREATE TABLE `sys_dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `sort` int DEFAULT '0' COMMENT '????',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??????',
  `is_default` tinyint DEFAULT '0' COMMENT '????(0-? 1-?)',
  `status` tinyint DEFAULT '1' COMMENT '??(0-?? 1-??)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `create_by` bigint DEFAULT NULL COMMENT '???',
  `update_by` bigint DEFAULT NULL COMMENT '???',
  `deleted` tinyint DEFAULT '0' COMMENT '????(0-??? 1-???)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?????'
;

CREATE TABLE `sys_dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `status` tinyint DEFAULT '1' COMMENT '??(0-?? 1-??)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `create_by` bigint DEFAULT NULL COMMENT '???',
  `update_by` bigint DEFAULT NULL COMMENT '???',
  `deleted` tinyint DEFAULT '0' COMMENT '????(0-??? 1-???)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_dict_type` (`dict_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?????'
;

CREATE TABLE `sys_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `original_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '?????',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '?????',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '????URL',
  `file_size` bigint DEFAULT '0' COMMENT '????????',
  `file_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '?????MIME???',
  `file_suffix` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '????',
  `storage_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '????',
  `bucket_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '?????',
  `group_id` bigint DEFAULT NULL COMMENT '??ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '??',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '???',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_file_path` (`file_path`(191)) USING BTREE,
  KEY `idx_storage_type` (`storage_type`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_group_id` (`group_id`) USING BTREE,
  KEY `idx_file_type` (`file_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=89 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?????'
;

CREATE TABLE `sys_file_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `storage_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????(local/minio/aliyun)',
  `master` tinyint DEFAULT '0' COMMENT '??????(0? 1?)',
  `domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `base_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????(????)',
  `bucket_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????',
  `access_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `secret_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `region` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `status` tinyint DEFAULT '1' COMMENT '??(0?? 1??)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_storage_type` (`storage_type`) USING BTREE,
  KEY `idx_master` (`master`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???????'
;

CREATE TABLE `sys_file_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `sort` int DEFAULT '0' COMMENT '??',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?????'
;

CREATE TABLE `sys_job` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'DEFAULT' COMMENT '????',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '???????',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'cron?????',
  `misfire_policy` tinyint DEFAULT '3' COMMENT '????????(1-???? 2-???? 3-????)',
  `concurrent` tinyint DEFAULT '1' COMMENT '??????(0-?? 1-??)',
  `status` tinyint DEFAULT '0' COMMENT '??(0-?? 1-??)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `create_by` bigint DEFAULT NULL COMMENT '???',
  `update_by` bigint DEFAULT NULL COMMENT '???',
  `deleted` tinyint DEFAULT '0' COMMENT '????(0-??? 1-???)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?????'
;

CREATE TABLE `sys_job_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???????',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `status` tinyint DEFAULT '0' COMMENT '????(0-?? 1-??)',
  `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `start_time` datetime DEFAULT NULL COMMENT '????',
  `stop_time` datetime DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=206 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???????'
;

CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??IP??',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `status` tinyint DEFAULT '0' COMMENT '????(0-?? 1-??)',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `login_time` datetime DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=318 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?????'
;

CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `parent_id` bigint DEFAULT '0' COMMENT '??ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `type` tinyint NOT NULL COMMENT '????(1-?? 2-?? 3-??)',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `component` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `sort` int DEFAULT '0' COMMENT '??',
  `visible` tinyint DEFAULT '1' COMMENT '????(0-?? 1-??)',
  `status` tinyint DEFAULT '1' COMMENT '??(0-?? 1-??)',
  `is_frame` tinyint DEFAULT '0' COMMENT '????(0-? 1-?)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `create_by` bigint DEFAULT NULL COMMENT '???',
  `update_by` bigint DEFAULT NULL COMMENT '???',
  `deleted` tinyint DEFAULT '0' COMMENT '????(0-??? 1-???)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=353 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???'
;

CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '????',
  `notice_type` tinyint DEFAULT '1' COMMENT '????(1?? 2??)',
  `channels` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '["station"]' COMMENT '????(JSON): station???,email??,sms??,webhook??/??/????',
  `target_type` tinyint DEFAULT '3' COMMENT '??????(1???? 2??? 3??)',
  `target_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????ID(JSON): ??ID???ID??',
  `status` tinyint DEFAULT '0' COMMENT '??(0?? 1??)',
  `create_by` bigint DEFAULT NULL COMMENT '???ID',
  `create_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `deleted` tinyint DEFAULT '0' COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_notice_type` (`notice_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?????'
;

CREATE TABLE `sys_notice_send_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??',
  `notice_id` bigint NOT NULL COMMENT '??ID',
  `channel` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????: station???,email??,dingtalk??,feishu??,wechat_work????',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '??: 1?? 2??',
  `target_count` int DEFAULT '0' COMMENT '??????',
  `success_count` int DEFAULT '0' COMMENT '????(??/???)',
  `error_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `send_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_notice_id` (`notice_id`) USING BTREE,
  KEY `idx_send_time` (`send_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???????'
;

CREATE TABLE `sys_oper_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `business_type` int DEFAULT '0' COMMENT '????(0-?? 1-?? 2-?? 3-??)',
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `status` int DEFAULT '0' COMMENT '????(0-?? 1-??)',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `oper_time` datetime DEFAULT NULL COMMENT '????',
  `cost_time` bigint DEFAULT '0' COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=985 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?????'
;

CREATE TABLE `sys_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `parent_id` bigint DEFAULT '0' COMMENT '???ID',
  `post_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `sort` int DEFAULT '0' COMMENT '????',
  `status` tinyint DEFAULT '1' COMMENT '??(0-?? 1-??)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `create_by` bigint DEFAULT NULL COMMENT '???',
  `update_by` bigint DEFAULT NULL COMMENT '???',
  `deleted` tinyint DEFAULT '0' COMMENT '????(0-??? 1-???)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_post_code` (`post_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???'
;

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????',
  `sort` int DEFAULT '0' COMMENT '??',
  `status` tinyint DEFAULT '1' COMMENT '??(0-?? 1-??)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `create_by` bigint DEFAULT NULL COMMENT '???',
  `update_by` bigint DEFAULT NULL COMMENT '???',
  `deleted` tinyint DEFAULT '0' COMMENT '????(0-??? 1-???)',
  `data_scope` tinyint NOT NULL DEFAULT '1' COMMENT '????(1?? 2??? 3??? 4?????? 5???)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_code` (`code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???'
;

CREATE TABLE `sys_role_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `dept_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_dept` (`role_id`,`dept_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='??-?? ??????'
;

CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `role_id` bigint NOT NULL COMMENT '??ID',
  `menu_id` bigint NOT NULL COMMENT '??ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_role_id` (`role_id`) USING BTREE,
  KEY `idx_menu_id` (`menu_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8865 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???????'
;

CREATE TABLE `sys_server` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '?????',
  `host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '?????',
  `port` int NOT NULL DEFAULT '22' COMMENT 'SSH??',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '???',
  `auth_type` tinyint NOT NULL DEFAULT '1' COMMENT '?????1-?? 2-??',
  `password` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????????',
  `private_key` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '????',
  `passphrase` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??????????',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '???0-?? 1-??',
  `sort` int NOT NULL DEFAULT '0' COMMENT '??',
  `last_connect_time` datetime DEFAULT NULL COMMENT '??????',
  `create_by` bigint DEFAULT NULL COMMENT '???',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_by` bigint DEFAULT NULL COMMENT '???',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '?????0-? 1-?',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='??????'
;

CREATE TABLE `sys_sms_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '???',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????/???',
  `sms_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'verify_code' COMMENT '?????verify_code-??? notice-?? marketing-??',
  `template_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??ID',
  `template_params` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????JSON???',
  `provider` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????aliyun-??? tencent-??? console-???',
  `status` tinyint DEFAULT '0' COMMENT '?????0-??? 1-?? 2-??',
  `result_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??????',
  `biz_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????????ID',
  `send_time` datetime DEFAULT NULL COMMENT '????',
  `user_id` bigint DEFAULT NULL COMMENT '??ID',
  `biz_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????login-?? register-?? reset_password-???? bind_phone-????',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'IP??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_phone` (`phone`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_send_time` (`send_time`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???????'
;

CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `dept_id` bigint DEFAULT NULL COMMENT '??id',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '???',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '??',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???',
  `gender` tinyint DEFAULT '0' COMMENT '??(0-?? 1-? 2-?)',
  `status` tinyint DEFAULT '1' COMMENT '??(0-?? 1-??)',
  `is_quit` tinyint DEFAULT '0' COMMENT '????(0-? 1-?)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `create_by` bigint DEFAULT NULL COMMENT '???',
  `update_by` bigint DEFAULT NULL COMMENT '???',
  `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'admin' COMMENT '????(admin-????? pc-PC???? app-App/?????)',
  `open_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??openId(?????????)',
  `deleted` tinyint DEFAULT '0' COMMENT '????(0-??? 1-???)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE,
  KEY `idx_open_id` (`open_id`) USING BTREE,
  KEY `idx_user_type` (`user_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???'
;

CREATE TABLE `sys_user_blacklist` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '??ID',
  `blocked_user_id` bigint NOT NULL COMMENT '??????ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_blocked` (`user_id`,`blocked_user_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_blocked_user_id` (`blocked_user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='??????'
;

CREATE TABLE `sys_user_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '??ID',
  `notice_id` bigint NOT NULL COMMENT '??ID',
  `is_read` tinyint DEFAULT '0' COMMENT '????(0?? 1??)',
  `read_time` datetime DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_notice` (`user_id`,`notice_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_notice_id` (`notice_id`) USING BTREE,
  KEY `idx_is_read` (`is_read`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???????'
;

CREATE TABLE `sys_user_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `user_id` bigint NOT NULL COMMENT '??ID',
  `post_id` bigint NOT NULL COMMENT '??ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_post_id` (`post_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???????'
;

CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `user_id` bigint NOT NULL COMMENT '??ID',
  `role_id` bigint NOT NULL COMMENT '??ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_role_id` (`role_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='???????'
;

CREATE TABLE `yun_anchor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `anchor_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '??????ID???????/?????',
  `room_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `anchor_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `room_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????',
  `category_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??ID',
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `guild_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `guild_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `bio` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `room_status` int DEFAULT NULL COMMENT '????',
  `last_start_time` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??????',
  `status` tinyint DEFAULT '1' COMMENT '??(0-?? 1-??)',
  `show_rank` tinyint DEFAULT '1' COMMENT '???????(0-? 1-?)',
  `sort` int DEFAULT '0' COMMENT '?????????',
  `data_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'MANUAL' COMMENT '????(MANUAL/BOJIANG)',
  `auto_update_profile` tinyint DEFAULT '1' COMMENT '?????????????(0-? 1-?)',
  `last_profile_sync_time` datetime DEFAULT NULL COMMENT '????????',
  `last_gift_sync_time` datetime DEFAULT NULL COMMENT '????????',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `create_by` bigint DEFAULT NULL COMMENT '???',
  `update_by` bigint DEFAULT NULL COMMENT '???',
  `deleted` tinyint DEFAULT '0' COMMENT '????(0-??? 1-???)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_anchor_id` (`anchor_id`) USING BTREE,
  KEY `idx_status_rank_sort` (`status`,`show_rank`,`sort`,`id` DESC) USING BTREE,
  KEY `idx_guild_name` (`guild_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?224????'
;

CREATE TABLE `yun_anchor_gift_stat` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `anchor_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '??????ID',
  `room_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `period_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????(DAY/MONTH)',
  `period_key` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '?????',
  `external_rank_no` int DEFAULT NULL COMMENT '??????',
  `gift_total_value` decimal(14,2) DEFAULT '0.00' COMMENT '????',
  `paid_gift_value` decimal(14,2) DEFAULT '0.00' COMMENT '??????',
  `bag_gift_value` decimal(14,2) DEFAULT '0.00' COMMENT '??????',
  `fishball_gift_count` decimal(18,2) DEFAULT '0.00' COMMENT '??????',
  `gift_user_count` int DEFAULT NULL COMMENT '????',
  `active_audience_count` int DEFAULT NULL COMMENT '????',
  `danmu_count` int DEFAULT NULL COMMENT '????',
  `danmu_user_count` int DEFAULT NULL COMMENT '????',
  `duration_text` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??????',
  `room_status` int DEFAULT NULL COMMENT '????',
  `lived` tinyint DEFAULT NULL COMMENT '?????',
  `last_start_time` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??????',
  `source_update_time` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????????',
  `raw_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '?????JSON',
  `synced_at` datetime DEFAULT NULL COMMENT '????',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `create_by` bigint DEFAULT NULL COMMENT '???',
  `update_by` bigint DEFAULT NULL COMMENT '???',
  `deleted` tinyint DEFAULT '0' COMMENT '????(0-??? 1-???)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_anchor_period` (`anchor_id`,`period_type`,`period_key`) USING BTREE,
  KEY `idx_period_rank` (`period_type`,`period_key`,`gift_total_value` DESC,`anchor_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=531 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?224????????'
;

CREATE TABLE `yun_sync_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `sync_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `period_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `period_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????',
  `trigger_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????(MANUAL/AUTO)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `total_count` int DEFAULT '0' COMMENT '???',
  `success_count` int DEFAULT '0' COMMENT '????',
  `fail_count` int DEFAULT '0' COMMENT '????',
  `error_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `started_at` datetime DEFAULT NULL COMMENT '????',
  `ended_at` datetime DEFAULT NULL COMMENT '????',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `create_by` bigint DEFAULT NULL COMMENT '???',
  `update_by` bigint DEFAULT NULL COMMENT '???',
  `deleted` tinyint DEFAULT '0' COMMENT '????(0-??? 1-???)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sync_time` (`sync_type`,`started_at` DESC) USING BTREE,
  KEY `idx_period` (`period_type`,`period_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=208 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='?224????'
;


-- ============================================================
-- 初始数据
-- ============================================================

INSERT INTO `sys_dept` VALUES (1, 0, '0', '火星网络科技', -2, '管理员', NULL, NULL, 1, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, 1, 0);
INSERT INTO `sys_dept` VALUES (2, 1, '0,1', '技术部', 0, '张三', NULL, NULL, 1, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, 1, 0);
INSERT INTO `sys_dept` VALUES (3, 1, '0,1', '产品部', 1, '李四', NULL, NULL, 1, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, 1, 0);
INSERT INTO `sys_dept` VALUES (4, 1, '0,1', '运营部', 2, '王五', NULL, NULL, 1, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, 1, 0);
INSERT INTO `sys_dept` VALUES (5, 0, '0', '测试', -1, 'lisi', '19999999999', '', 1, '2026-02-10 15:59:43', '2026-02-10 15:59:43', 1, 1, 0);
INSERT INTO `sys_dept` VALUES (6, 5, '0,5', '测试01部门', 1, '11', '22', '2', 1, '2026-02-10 16:10:49', '2026-02-10 16:10:49', 1, 1, 0);
INSERT INTO `sys_dept` VALUES (7, 5, '0,5', '测试02部门', 0, '01', '202', '2', 1, '2026-02-10 16:11:02', '2026-02-10 16:11:02', 1, 1, 0);
INSERT INTO `sys_dept` VALUES (8, 5, '0,5', '测试001', 2, '001', '1', '2', 1, '2026-02-10 16:11:15', '2026-02-10 16:11:15', 1, 1, 0);

-- ------------------------------------------------------------
-- sys_user  用户表（仅 admin）  (1 条)
-- ------------------------------------------------------------
INSERT INTO `sys_user` VALUES (1, 1, 'admin', '$2a$10$zqQcoU.zIWJQy42DAzxj5usRR9RLNx.HZ729ocAzSgnB90/sI3s5u', '超级管理员', '', '850994281@qq.com', '18888888888', 1, 1, 0, '111', '2026-01-29 22:42:08', '2026-02-24 22:02:00', NULL, 1, 'admin', NULL, 0);

-- ------------------------------------------------------------
-- sys_role  角色表  (2 条)
-- ------------------------------------------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, 1, '拥有所有权限', '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, 1, 0, 1);
INSERT INTO `sys_role` VALUES (2, '普通用户', 'user', 2, 1, '普通用户角色', '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, 1, 0, 3);

-- ------------------------------------------------------------
-- sys_menu  菜单表  (176 条)
-- ------------------------------------------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '系统管理', 1, '/system', NULL, NULL, 'SettingsOutline', 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (2, 1, '用户管理', 2, '/system/user', '/system/user/index', 'sys:user:list', 'PersonOutline', 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (3, 2, '新增用户', 3, NULL, NULL, 'sys:user:add', NULL, 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (4, 2, '编辑用户', 3, NULL, NULL, 'sys:user:edit', NULL, 2, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (5, 2, '删除用户', 3, NULL, NULL, 'sys:user:delete', NULL, 3, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (6, 1, '角色管理', 2, '/system/role', '/system/role/index', 'sys:role:list', 'PeopleOutline', 2, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (7, 6, '新增角色', 3, NULL, NULL, 'sys:role:add', NULL, 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (8, 6, '编辑角色', 3, NULL, NULL, 'sys:role:edit', NULL, 2, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (9, 6, '删除角色', 3, NULL, NULL, 'sys:role:delete', NULL, 3, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (10, 1, '菜单管理', 2, '/system/menu', '/system/menu/index', 'sys:menu:list', 'MenuOutline', 3, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (11, 10, '新增菜单', 3, NULL, NULL, 'sys:menu:add', NULL, 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (12, 10, '编辑菜单', 3, NULL, NULL, 'sys:menu:edit', NULL, 2, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (13, 10, '删除菜单', 3, NULL, NULL, 'sys:menu:delete', NULL, 3, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (14, 1, '字典管理', 2, '/system/dict', '/system/dict/index', 'sys:dict:list', 'BookOutline', 4, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (15, 14, '新增字典', 3, NULL, NULL, 'sys:dict:add', NULL, 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (16, 14, '编辑字典', 3, NULL, NULL, 'sys:dict:edit', NULL, 2, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (17, 14, '删除字典', 3, NULL, NULL, 'sys:dict:delete', NULL, 3, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (18, 1, '系统配置', 2, '/system/config', '/system/config/index', 'sys:config:list', 'SettingsSharp', 5, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (19, 18, '新增配置', 3, NULL, NULL, 'sys:config:add', NULL, 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (20, 18, '编辑配置', 3, NULL, NULL, 'sys:config:edit', NULL, 2, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (21, 18, '删除配置', 3, NULL, NULL, 'sys:config:delete', NULL, 3, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (22, 0, '组织管理', 1, '/org', NULL, NULL, 'BusinessOutline', 2, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (23, 22, '部门管理', 2, '/org/dept', '/org/dept/index', 'sys:dept:list', 'GitNetworkOutline', 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (24, 23, '新增部门', 3, NULL, NULL, 'sys:dept:add', NULL, 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (25, 23, '编辑部门', 3, NULL, NULL, 'sys:dept:edit', NULL, 2, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (26, 23, '删除部门', 3, NULL, NULL, 'sys:dept:delete', NULL, 3, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (27, 22, '岗位管理', 2, '/org/post', '/org/post/index', 'sys:post:list', 'IdCardOutline', 2, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (28, 27, '新增岗位', 3, NULL, NULL, 'sys:post:add', NULL, 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (29, 27, '编辑岗位', 3, NULL, NULL, 'sys:post:edit', NULL, 2, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (30, 27, '删除岗位', 3, NULL, NULL, 'sys:post:delete', NULL, 3, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (31, 0, '系统日志', 1, '/log', NULL, NULL, 'DocumentTextOutline', 3, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (32, 31, '操作日志', 2, '/log/operlog', '/log/operlog/index', 'monitor:operlog:list', 'ListOutline', 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (33, 32, '删除日志', 3, NULL, NULL, 'monitor:operlog:delete', NULL, 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (34, 31, '登录日志', 2, '/log/loginlog', '/log/loginlog/index', 'monitor:loginlog:list', 'LogInOutline', 2, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (35, 34, '删除日志', 3, NULL, NULL, 'monitor:loginlog:delete', NULL, 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (36, 0, '系统监控', 1, '/monitor', NULL, NULL, 'PulseOutline', 4, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (37, 36, '在线用户', 2, '/monitor/online', '/monitor/online/index', 'monitor:online:list', 'PeopleCircleOutline', 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (38, 37, '强退用户', 3, NULL, NULL, 'monitor:online:forceLogout', NULL, 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (39, 36, '定时任务', 2, '/monitor/job', '/monitor/job/index', 'monitor:job:list', 'TimerOutline', 2, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (40, 39, '新增任务', 3, NULL, NULL, 'monitor:job:add', NULL, 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (41, 39, '编辑任务', 3, NULL, NULL, 'monitor:job:edit', NULL, 2, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (42, 39, '删除任务', 3, NULL, NULL, 'monitor:job:delete', NULL, 3, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (43, 36, '缓存监控', 2, '/monitor/cache', '/monitor/cache/index', 'monitor:cache:list', 'ServerOutline', 3, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (44, 43, '删除缓存', 3, NULL, NULL, 'monitor:cache:delete', NULL, 1, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (45, 36, '服务监控', 2, '/monitor/server', '/monitor/server/index', 'monitor:server:list', 'DesktopOutline', 4, 1, 1, 0, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (126, 0, '文件管理', 1, '/file', NULL, NULL, 'FolderOpenOutline', 5, 1, 1, 0, '2026-01-30 23:40:01', '2026-01-30 23:40:01', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (127, 126, '文件列表', 2, '/system/file', '/system/file/index', 'sys:file:list', 'DocumentOutline', 1, 1, 1, 0, '2026-01-30 23:40:01', '2026-01-30 23:40:01', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (128, 127, '上传文件', 3, NULL, NULL, 'sys:file:upload', NULL, 1, 1, 1, 0, '2026-01-30 23:40:01', '2026-01-30 23:40:01', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (129, 127, '删除文件', 3, NULL, NULL, 'sys:file:delete', NULL, 2, 1, 1, 0, '2026-01-30 23:40:01', '2026-01-30 23:40:01', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (130, 126, '文件配置', 2, '/system/file-config', '/system/file-config/index', 'sys:fileConfig:list', 'CloudOutline', 2, 1, 1, 0, '2026-01-30 23:40:01', '2026-01-31 14:43:52', NULL, 1, 1);
INSERT INTO `sys_menu` VALUES (131, 130, '新增配置', 3, NULL, NULL, 'sys:fileConfig:add', NULL, 1, 1, 1, 0, '2026-01-30 23:40:01', '2026-01-31 14:43:45', NULL, 1, 1);
INSERT INTO `sys_menu` VALUES (132, 130, '编辑配置', 3, NULL, NULL, 'sys:fileConfig:edit', NULL, 2, 1, 1, 0, '2026-01-30 23:40:01', '2026-01-31 14:43:47', NULL, 1, 1);
INSERT INTO `sys_menu` VALUES (133, 130, '删除配置', 3, NULL, NULL, 'sys:fileConfig:delete', NULL, 3, 1, 1, 0, '2026-01-30 23:40:01', '2026-01-31 14:43:50', NULL, 1, 1);
INSERT INTO `sys_menu` VALUES (134, 0, '消息中心', 1, '/message', NULL, NULL, 'NotificationsOutline', 6, 1, 1, 0, '2026-01-30 23:53:55', '2026-01-30 23:53:55', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (135, 134, '系统通知', 2, '/message/notice', '/message/notice/index', 'sys:notice:list', 'NotificationsOutline', 1, 1, 1, 0, '2026-01-30 23:53:55', '2026-01-30 23:53:55', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (136, 135, '新增通知', 3, NULL, NULL, 'sys:notice:add', NULL, 1, 1, 1, 0, '2026-01-30 23:53:55', '2026-01-30 23:53:55', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (137, 135, '编辑通知', 3, NULL, NULL, 'sys:notice:edit', NULL, 2, 1, 1, 0, '2026-01-30 23:53:55', '2026-01-30 23:53:55', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (138, 135, '删除通知', 3, NULL, NULL, 'sys:notice:delete', NULL, 3, 1, 1, 0, '2026-01-30 23:53:55', '2026-01-30 23:53:55', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (139, 134, '即时聊天', 2, '/message/chat', '/message/chat/index', 'sys:chat:list', 'ChatbubbleOutline', 2, 1, 1, 0, '2026-01-30 23:53:55', '2026-01-30 23:53:55', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (140, 0, '测试菜单', 1, '/test', '', '', 'StarOutline', 7, 0, 0, 0, '2026-01-31 20:17:37', '2026-01-31 20:17:37', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (141, 140, '测试菜单', 2, '/test/test', '/test/test/index', '', 'SearchOutline', 0, 1, 1, 0, '2026-01-31 20:23:37', '2026-01-31 20:23:37', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (142, 2, '用户列表', 3, '', '', 'sys:user:list', '', 0, 1, 1, 0, '2026-01-31 20:51:50', '2026-01-31 20:51:50', 4, 4, 0);
INSERT INTO `sys_menu` VALUES (143, 6, '角色列表', 3, '', '', 'sys:role:list', '', 0, 1, 1, 0, '2026-01-31 20:52:13', '2026-01-31 20:52:13', 4, 4, 0);
INSERT INTO `sys_menu` VALUES (144, 10, '菜单列表', 3, '', '', 'sys:menu:list', '', 0, 1, 1, 0, '2026-01-31 20:52:32', '2026-01-31 20:52:32', 4, 4, 0);
INSERT INTO `sys_menu` VALUES (145, 14, '字典列表', 3, '', '', 'sys:dict:list', '', 0, 1, 1, 0, '2026-01-31 20:52:52', '2026-01-31 20:52:52', 4, 4, 0);
INSERT INTO `sys_menu` VALUES (146, 23, '部门列表', 3, '', '', 'sys:dept:list', '', 0, 1, 1, 0, '2026-01-31 20:54:26', '2026-01-31 20:54:26', 4, 4, 0);
INSERT INTO `sys_menu` VALUES (147, 27, '岗位列表', 3, '', '', 'sys:post:list', '', 0, 1, 1, 0, '2026-01-31 20:54:45', '2026-01-31 20:54:45', 4, 4, 0);
INSERT INTO `sys_menu` VALUES (148, 32, '查询日志', 3, '', '', 'monitor:operlog:list', '', 0, 1, 1, 0, '2026-01-31 20:55:14', '2026-01-31 20:55:14', 4, 4, 0);
INSERT INTO `sys_menu` VALUES (149, 34, '日志列表', 3, '', '', 'monitor:loginlog:list', '', 0, 1, 1, 0, '2026-01-31 20:55:32', '2026-01-31 20:55:32', 4, 4, 0);
INSERT INTO `sys_menu` VALUES (150, 37, '在线用户列表', 3, '', '', 'monitor:online:list', '', 0, 1, 1, 0, '2026-01-31 20:56:04', '2026-01-31 20:56:04', 4, 4, 0);
INSERT INTO `sys_menu` VALUES (151, 39, '任务列表', 3, '', '', 'monitor:job:list', '', 0, 1, 1, 0, '2026-01-31 20:56:20', '2026-01-31 20:56:20', 4, 4, 0);
INSERT INTO `sys_menu` VALUES (152, 127, '文件列表', 3, '', '', 'sys:file:list', '', 0, 1, 1, 0, '2026-01-31 20:57:18', '2026-01-31 20:57:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (153, 135, '系统通知', 3, '', '', 'sys:notice:list', '', 0, 1, 1, 0, '2026-01-31 20:57:55', '2026-01-31 20:57:55', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (154, 36, '服务器管理', 2, '/monitor/server-manager', '/monitor/server-manager/index', NULL, 'ServerOutline', 5, 1, 1, 0, '2026-01-31 23:37:21', '2026-01-31 23:37:21', NULL, 1, 0);
INSERT INTO `sys_menu` VALUES (155, 154, '服务器列表', 3, NULL, NULL, 'monitor:server:list', NULL, 1, 1, 1, 0, '2026-01-31 23:37:21', '2026-01-31 23:37:21', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (156, 154, '服务器详情', 3, NULL, NULL, 'monitor:server:query', NULL, 2, 1, 1, 0, '2026-01-31 23:37:21', '2026-01-31 23:37:21', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (157, 154, '新增服务器', 3, NULL, NULL, 'monitor:server:add', NULL, 3, 1, 1, 0, '2026-01-31 23:37:21', '2026-01-31 23:37:21', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (158, 154, '编辑服务器', 3, NULL, NULL, 'monitor:server:edit', NULL, 4, 1, 1, 0, '2026-01-31 23:37:21', '2026-01-31 23:37:21', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (159, 154, '删除服务器', 3, NULL, NULL, 'monitor:server:remove', NULL, 5, 1, 1, 0, '2026-01-31 23:37:21', '2026-01-31 23:37:21', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (160, 154, '测试连接', 3, NULL, NULL, 'monitor:server:test', NULL, 6, 1, 1, 0, '2026-01-31 23:37:21', '2026-01-31 23:37:21', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (161, 0, '开发工具', 1, '', '', '', 'HammerOutline', 99, 1, 1, 0, '2026-02-02 19:50:38', '2026-02-02 19:52:36', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (162, 161, '代码生成', 2, '/tool/gen', '/tool/gen/index', '', 'CodeSlashOutline', 1, 1, 1, 0, '2026-02-02 19:50:38', '2026-02-02 19:53:12', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (163, 162, '查询', 3, '', '', 'tool:gen:list', '', 1, 1, 1, 0, '2026-02-02 19:50:38', '2026-02-02 19:52:40', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (164, 162, '详情', 3, '', '', 'tool:gen:query', '', 2, 1, 1, 0, '2026-02-02 19:50:38', '2026-02-02 19:52:40', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (165, 162, '导入', 3, '', '', 'tool:gen:import', '', 3, 1, 1, 0, '2026-02-02 19:50:38', '2026-02-02 19:52:40', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (166, 162, '编辑', 3, '', '', 'tool:gen:edit', '', 4, 1, 1, 0, '2026-02-02 19:50:38', '2026-02-02 19:52:40', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (167, 162, '删除', 3, '', '', 'tool:gen:remove', '', 5, 1, 1, 0, '2026-02-02 19:50:38', '2026-02-02 19:52:40', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (168, 162, '预览', 3, '', '', 'tool:gen:preview', '', 6, 1, 1, 0, '2026-02-02 19:50:38', '2026-02-02 19:52:40', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (169, 162, '生成代码', 3, '', '', 'tool:gen:code', '', 7, 1, 1, 0, '2026-02-02 19:50:38', '2026-02-02 19:52:40', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (170, 1, '客户表', 2, '/system/customer', '/system/customer/index', '', 'ListOutline', 1, 0, 0, 0, '2026-02-02 19:57:54', '2026-02-02 19:57:54', NULL, 1, 0);
INSERT INTO `sys_menu` VALUES (171, 170, '客户表查询', 3, '', '', 'system:customer:list', '', 1, 1, 1, 0, '2026-02-02 19:57:54', '2026-02-02 19:57:54', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (172, 170, '客户表详情', 3, '', '', 'system:customer:query', '', 2, 1, 1, 0, '2026-02-02 19:57:54', '2026-02-02 19:57:54', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (173, 170, '客户表新增', 3, '', '', 'system:customer:add', '', 3, 1, 1, 0, '2026-02-02 19:57:54', '2026-02-02 19:57:54', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (174, 170, '客户表修改', 3, '', '', 'system:customer:edit', '', 4, 1, 1, 0, '2026-02-02 19:57:54', '2026-02-02 19:57:54', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (175, 170, '客户表删除', 3, '', '', 'system:customer:remove', '', 5, 1, 1, 0, '2026-02-02 19:57:54', '2026-02-02 19:57:54', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (272, 161, '学生管理', 2, 'system/student', 'system/student/index', 'system:student:list', 'ListOutline', 1, 1, 1, 0, '2026-02-03 08:58:09', '2026-02-09 12:55:57', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (273, 272, '学生管理查询', 3, NULL, NULL, 'system:student:list', NULL, 1, 1, 1, 0, '2026-02-03 08:58:09', '2026-02-09 12:55:57', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (274, 272, '学生管理详情', 3, NULL, NULL, 'system:student:query', NULL, 2, 1, 1, 0, '2026-02-03 08:58:09', '2026-02-09 12:55:57', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (275, 272, '学生管理新增', 3, NULL, NULL, 'system:student:add', NULL, 3, 1, 1, 0, '2026-02-03 08:58:09', '2026-02-09 12:55:57', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (276, 272, '学生管理修改', 3, NULL, NULL, 'system:student:edit', NULL, 4, 1, 1, 0, '2026-02-03 08:58:09', '2026-02-09 12:55:57', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (277, 272, '学生管理删除', 3, NULL, NULL, 'system:student:remove', NULL, 5, 1, 1, 0, '2026-02-03 08:58:09', '2026-02-09 12:55:57', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (278, 161, '接口文档', 2, '/tool/api-doc', 'https://s.apifox.cn/73166c1b-50fb-47b6-a015-50111f2fbf9e/417351170e0', '', 'DocumentOutline', 0, 1, 1, 1, '2026-02-13 15:18:16', '2026-02-13 15:18:16', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (279, 0, '系统官网', 1, '', 'https://mars-coder.cn', '', 'PlanetOutline', 100, 1, 1, 1, '2026-02-13 15:52:55', '2026-02-13 15:52:55', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (280, 2, '用户导入', 3, '', '', 'sys:user:import', '', 0, 1, 1, 0, '2026-02-25 15:56:46', '2026-02-25 15:56:46', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (281, 2, '用户导出', 3, '', '', 'sys:user:export', '', 0, 1, 1, 0, '2026-02-25 15:57:01', '2026-02-25 15:57:01', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (282, 36, 'SQL监控', 2, '/monitor/druid', '/monitor/druid/index', 'monitor:druid:list', 'PieChartOutline', 4, 1, 1, 0, '2026-02-28 22:30:41', '2026-02-28 22:30:41', NULL, 1, 0);
INSERT INTO `sys_menu` VALUES (283, 36, 'API访问统计', 2, '/monitor/api-access', '/monitor/api-access/index', 'monitor:apiAccess:list', 'StatsChartOutline', 7, 1, 1, 0, '2026-02-28 22:56:30', '2026-02-28 22:56:30', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (284, 283, '访问统计列表', 3, '', '', 'monitor:apiAccess:list', '', 0, 1, 1, 0, '2026-02-28 22:58:30', '2026-02-28 22:58:30', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (285, 161, '学生表', 2, 'system/student', 'system/student/index', 'system:student:list', 'ListOutline', 1, 1, 1, 0, '2026-03-01 11:14:06', '2026-03-01 11:16:47', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (286, 285, '学生表查询', 3, NULL, NULL, 'system:student:list', NULL, 1, 1, 1, 0, '2026-03-01 11:14:06', '2026-03-01 11:16:47', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (287, 285, '学生表详情', 3, NULL, NULL, 'system:student:query', NULL, 2, 1, 1, 0, '2026-03-01 11:14:06', '2026-03-01 11:16:47', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (288, 285, '学生表新增', 3, NULL, NULL, 'system:student:add', NULL, 3, 1, 1, 0, '2026-03-01 11:14:06', '2026-03-01 11:16:47', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (289, 285, '学生表修改', 3, NULL, NULL, 'system:student:edit', NULL, 4, 1, 1, 0, '2026-03-01 11:14:06', '2026-03-01 11:16:47', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (290, 285, '学生表删除', 3, NULL, NULL, 'system:student:remove', NULL, 5, 1, 1, 0, '2026-03-01 11:14:06', '2026-03-01 11:16:47', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (291, 161, '学生表', 2, 'system/student', 'system/student/index', 'system:student:list', 'ListOutline', 1, 1, 1, 0, '2026-03-01 11:24:24', '2026-03-01 11:27:48', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (292, 291, '学生表查询', 3, NULL, NULL, 'system:student:list', NULL, 1, 1, 1, 0, '2026-03-01 11:24:24', '2026-03-01 11:27:48', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (293, 291, '学生表详情', 3, NULL, NULL, 'system:student:query', NULL, 2, 1, 1, 0, '2026-03-01 11:24:24', '2026-03-01 11:27:48', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (294, 291, '学生表新增', 3, NULL, NULL, 'system:student:add', NULL, 3, 1, 1, 0, '2026-03-01 11:24:24', '2026-03-01 11:27:48', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (295, 291, '学生表修改', 3, NULL, NULL, 'system:student:edit', NULL, 4, 1, 1, 0, '2026-03-01 11:24:24', '2026-03-01 11:27:48', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (296, 291, '学生表删除', 3, NULL, NULL, 'system:student:remove', NULL, 5, 1, 1, 0, '2026-03-01 11:24:24', '2026-03-01 11:27:48', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (297, 161, '学生表', 2, 'system/student', 'system/student/index', 'system:student:list', 'ListOutline', 1, 1, 1, 0, '2026-03-01 11:27:57', '2026-03-01 11:46:03', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (298, 297, '学生表查询', 3, NULL, NULL, 'system:student:list', NULL, 1, 1, 1, 0, '2026-03-01 11:27:57', '2026-03-01 11:46:03', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (299, 297, '学生表详情', 3, NULL, NULL, 'system:student:query', NULL, 2, 1, 1, 0, '2026-03-01 11:27:57', '2026-03-01 11:46:03', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (300, 297, '学生表新增', 3, NULL, NULL, 'system:student:add', NULL, 3, 1, 1, 0, '2026-03-01 11:27:57', '2026-03-01 11:46:03', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (301, 297, '学生表修改', 3, NULL, NULL, 'system:student:edit', NULL, 4, 1, 1, 0, '2026-03-01 11:27:57', '2026-03-01 11:46:03', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (302, 297, '学生表删除', 3, NULL, NULL, 'system:student:remove', NULL, 5, 1, 1, 0, '2026-03-01 11:27:57', '2026-03-01 11:46:03', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (303, 161, '学生表', 2, 'system/student', 'system/student/index', 'system:student:list', 'ListOutline', 1, 1, 1, 0, '2026-03-01 12:04:47', '2026-03-01 12:06:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (304, 303, '学生表查询', 3, NULL, NULL, 'system:student:list', NULL, 1, 1, 1, 0, '2026-03-01 12:04:47', '2026-03-01 12:06:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (305, 303, '学生表详情', 3, NULL, NULL, 'system:student:query', NULL, 2, 1, 1, 0, '2026-03-01 12:04:47', '2026-03-01 12:06:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (306, 303, '学生表新增', 3, NULL, NULL, 'system:student:add', NULL, 3, 1, 1, 0, '2026-03-01 12:04:47', '2026-03-01 12:06:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (307, 303, '学生表修改', 3, NULL, NULL, 'system:student:edit', NULL, 4, 1, 1, 0, '2026-03-01 12:04:47', '2026-03-01 12:06:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (308, 303, '学生表删除', 3, NULL, NULL, 'system:student:remove', NULL, 5, 1, 1, 0, '2026-03-01 12:04:47', '2026-03-01 12:06:19', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (309, 161, '学生表', 2, 'system/student', 'system/student/index', 'system:student:list', 'ListOutline', 1, 1, 1, 0, '2026-03-01 12:07:02', '2026-03-01 12:09:32', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (310, 309, '学生表查询', 3, NULL, NULL, 'system:student:list', NULL, 1, 1, 1, 0, '2026-03-01 12:07:02', '2026-03-01 12:09:32', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (311, 309, '学生表详情', 3, NULL, NULL, 'system:student:query', NULL, 2, 1, 1, 0, '2026-03-01 12:07:02', '2026-03-01 12:09:32', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (312, 309, '学生表新增', 3, NULL, NULL, 'system:student:add', NULL, 3, 1, 1, 0, '2026-03-01 12:07:02', '2026-03-01 12:09:32', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (313, 309, '学生表修改', 3, NULL, NULL, 'system:student:edit', NULL, 4, 1, 1, 0, '2026-03-01 12:07:02', '2026-03-01 12:09:32', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (314, 309, '学生表删除', 3, NULL, NULL, 'system:student:remove', NULL, 5, 1, 1, 0, '2026-03-01 12:07:02', '2026-03-01 12:09:32', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (315, 161, '学生表', 2, 'system/student', 'system/student/index', 'system:student:list', 'ListOutline', 1, 1, 1, 0, '2026-03-01 12:09:45', '2026-03-01 12:14:37', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (316, 315, '学生表查询', 3, NULL, NULL, 'system:student:list', NULL, 1, 1, 1, 0, '2026-03-01 12:09:45', '2026-03-01 12:14:37', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (317, 315, '学生表详情', 3, NULL, NULL, 'system:student:query', NULL, 2, 1, 1, 0, '2026-03-01 12:09:45', '2026-03-01 12:14:37', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (318, 315, '学生表新增', 3, NULL, NULL, 'system:student:add', NULL, 3, 1, 1, 0, '2026-03-01 12:09:45', '2026-03-01 12:14:37', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (319, 315, '学生表修改', 3, NULL, NULL, 'system:student:edit', NULL, 4, 1, 1, 0, '2026-03-01 12:09:45', '2026-03-01 12:14:37', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (320, 315, '学生表删除', 3, NULL, NULL, 'system:student:remove', NULL, 5, 1, 1, 0, '2026-03-01 12:09:45', '2026-03-01 12:14:37', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (321, 161, '学生表', 2, 'system/student', 'system/student/index', 'system:student:list', 'ListOutline', 1, 1, 1, 0, '2026-03-01 12:15:15', '2026-03-01 12:20:10', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (322, 321, '学生表查询', 3, NULL, NULL, 'system:student:list', NULL, 1, 1, 1, 0, '2026-03-01 12:15:15', '2026-03-01 12:20:10', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (323, 321, '学生表详情', 3, NULL, NULL, 'system:student:query', NULL, 2, 1, 1, 0, '2026-03-01 12:15:15', '2026-03-01 12:20:10', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (324, 321, '学生表新增', 3, NULL, NULL, 'system:student:add', NULL, 3, 1, 1, 0, '2026-03-01 12:15:15', '2026-03-01 12:20:10', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (325, 321, '学生表修改', 3, NULL, NULL, 'system:student:edit', NULL, 4, 1, 1, 0, '2026-03-01 12:15:15', '2026-03-01 12:20:10', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (326, 321, '学生表删除', 3, NULL, NULL, 'system:student:remove', NULL, 5, 1, 1, 0, '2026-03-01 12:15:15', '2026-03-01 12:20:10', 1, 1, 1);
INSERT INTO `sys_menu` VALUES (327, 161, '学生表', 2, 'system/student', 'system/student/index', 'system:student:list', 'ListOutline', 1, 1, 1, 0, '2026-03-01 12:20:17', '2026-03-01 12:20:17', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (328, 327, '学生表查询', 3, NULL, NULL, 'system:student:list', NULL, 1, 1, 1, 0, '2026-03-01 12:20:17', '2026-03-01 12:20:17', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (329, 327, '学生表详情', 3, NULL, NULL, 'system:student:query', NULL, 2, 1, 1, 0, '2026-03-01 12:20:17', '2026-03-01 12:20:17', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (330, 327, '学生表新增', 3, NULL, NULL, 'system:student:add', NULL, 3, 1, 1, 0, '2026-03-01 12:20:17', '2026-03-01 12:20:17', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (331, 327, '学生表修改', 3, NULL, NULL, 'system:student:edit', NULL, 4, 1, 1, 0, '2026-03-01 12:20:17', '2026-03-01 12:20:17', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (332, 327, '学生表删除', 3, NULL, NULL, 'system:student:remove', NULL, 5, 1, 1, 0, '2026-03-01 12:20:17', '2026-03-01 12:20:17', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (333, 0, '云224管理', 1, '/yun', NULL, NULL, 'CloudOutline', 8, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (334, 333, '首页轮播图', 2, '/yun/banner', '/yun/banner/index', 'yun:banner:list', 'ImageOutline', 1, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (335, 334, '轮播图列表', 3, NULL, NULL, 'yun:banner:list', NULL, 1, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (336, 334, '轮播图详情', 3, NULL, NULL, 'yun:banner:query', NULL, 2, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (337, 334, '轮播图新增', 3, NULL, NULL, 'yun:banner:add', NULL, 3, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (338, 334, '轮播图修改', 3, NULL, NULL, 'yun:banner:edit', NULL, 4, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (339, 334, '轮播图删除', 3, NULL, NULL, 'yun:banner:remove', NULL, 5, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (340, 333, '主播管理', 2, '/yun/anchor', '/yun/anchor/index', 'yun:anchor:list', 'PersonOutline', 2, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (341, 340, '主播列表', 3, NULL, NULL, 'yun:anchor:list', NULL, 1, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (342, 340, '主播详情', 3, NULL, NULL, 'yun:anchor:query', NULL, 2, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (343, 340, '主播新增', 3, NULL, NULL, 'yun:anchor:add', NULL, 3, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (344, 340, '主播修改', 3, NULL, NULL, 'yun:anchor:edit', NULL, 4, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (345, 340, '主播删除', 3, NULL, NULL, 'yun:anchor:remove', NULL, 5, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (346, 340, '主播同步', 3, NULL, NULL, 'yun:anchor:sync', NULL, 6, 1, 1, 0, '2026-08-07 00:00:00', '2026-08-07 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (347, 333, '首页公告', 2, '/yun/notice', '/yun/notice/index', 'yun:notice:list', 'NotificationsOutline', 2, 1, 1, 0, '2026-08-08 00:00:00', '2026-08-08 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (348, 347, '公告列表', 3, NULL, NULL, 'yun:notice:list', NULL, 1, 1, 1, 0, '2026-08-08 00:00:00', '2026-08-08 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (349, 347, '公告详情', 3, NULL, NULL, 'yun:notice:query', NULL, 2, 1, 1, 0, '2026-08-08 00:00:00', '2026-08-08 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (350, 347, '公告新增', 3, NULL, NULL, 'yun:notice:add', NULL, 3, 1, 1, 0, '2026-08-08 00:00:00', '2026-08-08 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (351, 347, '公告修改', 3, NULL, NULL, 'yun:notice:edit', NULL, 4, 1, 1, 0, '2026-08-08 00:00:00', '2026-08-08 00:00:00', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (352, 347, '公告删除', 3, NULL, NULL, 'yun:notice:remove', NULL, 5, 1, 1, 0, '2026-08-08 00:00:00', '2026-08-08 00:00:00', 1, 1, 0);

-- ------------------------------------------------------------
-- sys_dict_type  字典类型表  (7 条)
-- ------------------------------------------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', 1, '用户性别列表', '2026-01-29 22:42:08', '2026-03-01 11:18:09', NULL, 1, 1);
INSERT INTO `sys_dict_type` VALUES (2, '系统状态', 'sys_status', 1, '系统通用状态', '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_dict_type` VALUES (3, '是否', 'sys_yes_no', 1, '是否选项', '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_dict_type` VALUES (4, '性别', 'sex', 1, '', '2026-01-29 23:21:29', '2026-02-07 15:33:54', 1, 1, 1);
INSERT INTO `sys_dict_type` VALUES (5, '111', '222', 1, '', '2026-02-07 19:42:53', '2026-02-07 23:26:19', 1, 1, 1);
INSERT INTO `sys_dict_type` VALUES (6, '222', '333', 1, '', '2026-02-07 19:43:46', '2026-02-07 23:26:17', 1, 1, 1);
INSERT INTO `sys_dict_type` VALUES (7, '用户性别', 'gender', 1, '', '2026-01-29 22:42:08', '2026-01-29 22:42:08', 1, 1, 0);

-- ------------------------------------------------------------
-- sys_dict_data  字典数据表  (11 条)
-- ------------------------------------------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '1', 'sys_user_sex', NULL, 'default', 0, 1, NULL, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '2', 'sys_user_sex', NULL, 'default', 0, 1, NULL, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '0', 'sys_user_sex', NULL, 'default', 0, 1, NULL, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_dict_data` VALUES (4, 1, '正常', '1', 'sys_status', NULL, 'success', 0, 1, NULL, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_dict_data` VALUES (5, 2, '停用', '0', 'sys_status', NULL, 'error', 0, 1, NULL, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_dict_data` VALUES (6, 1, '是', '1', 'sys_yes_no', NULL, 'success', 0, 1, NULL, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_dict_data` VALUES (7, 2, '否', '0', 'sys_yes_no', NULL, 'error', 0, 1, NULL, '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_dict_data` VALUES (8, 0, 'sex', '1', 'sex', NULL, 'default', 0, 1, '', '2026-01-29 23:21:38', '2026-01-29 23:21:38', 1, 1, 0);
INSERT INTO `sys_dict_data` VALUES (9, 0, '女', '0', 'sex', NULL, 'default', 0, 1, '', '2026-01-29 23:21:58', '2026-01-29 23:21:58', 1, 1, 0);
INSERT INTO `sys_dict_data` VALUES (10, 0, '男', '1', 'gender', NULL, 'default', 0, 1, '', '2026-03-01 11:18:33', '2026-03-01 11:18:33', 1, 1, 0);
INSERT INTO `sys_dict_data` VALUES (11, 0, '女', '2', 'gender', NULL, 'default', 0, 1, '', '2026-03-01 11:18:39', '2026-03-01 11:18:39', 1, 1, 0);

-- ------------------------------------------------------------
-- sys_post  岗位表  (7 条)
-- ------------------------------------------------------------
INSERT INTO `sys_post` VALUES (1, 0, 'ceo', '董事长', 1, 1, '公司董事长', '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, NULL, 0);
INSERT INTO `sys_post` VALUES (2, 6, 'cto', '技术总监', 2, 1, '技术总监', '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, 1, 0);
INSERT INTO `sys_post` VALUES (3, 2, 'pm', '产品经理', 3, 1, '产品经理', '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, 1, 0);
INSERT INTO `sys_post` VALUES (4, 6, 'dev', '开发工程师', 4, 1, '开发工程师', '2026-01-29 22:42:08', '2026-01-29 22:42:08', NULL, 1, 0);
INSERT INTO `sys_post` VALUES (6, 1, 'manager', '总经理', 2, 1, '', '2026-01-29 22:42:08', '2026-01-29 22:42:08', 1, 1, 0);
INSERT INTO `sys_post` VALUES (7, 2, 'test_coder', '测试工程师', 0, 1, '', '2026-02-09 15:47:35', '2026-02-09 17:03:40', 1, 1, 1);
INSERT INTO `sys_post` VALUES (8, 2, 'test001', '测试', 0, 1, '', '2026-02-10 16:42:47', '2026-02-10 16:42:47', 1, 1, 0);

-- ------------------------------------------------------------
-- sys_user_role  用户角色关联表（仅 user_id=1）  (1 条)
-- ------------------------------------------------------------
INSERT INTO `sys_user_role` VALUES (45, 1, 1);

-- ------------------------------------------------------------
-- sys_role_menu  角色菜单关联表  (234 条)
-- ------------------------------------------------------------
INSERT INTO `sys_role_menu` VALUES (7457, 2, 139);
INSERT INTO `sys_role_menu` VALUES (7458, 2, 153);
INSERT INTO `sys_role_menu` VALUES (7459, 2, 136);
INSERT INTO `sys_role_menu` VALUES (7460, 2, 137);
INSERT INTO `sys_role_menu` VALUES (7461, 2, 138);
INSERT INTO `sys_role_menu` VALUES (7462, 2, 150);
INSERT INTO `sys_role_menu` VALUES (7463, 2, 38);
INSERT INTO `sys_role_menu` VALUES (7464, 2, 151);
INSERT INTO `sys_role_menu` VALUES (7465, 2, 40);
INSERT INTO `sys_role_menu` VALUES (7466, 2, 41);
INSERT INTO `sys_role_menu` VALUES (7467, 2, 42);
INSERT INTO `sys_role_menu` VALUES (7468, 2, 44);
INSERT INTO `sys_role_menu` VALUES (7469, 2, 45);
INSERT INTO `sys_role_menu` VALUES (7470, 2, 155);
INSERT INTO `sys_role_menu` VALUES (7471, 2, 156);
INSERT INTO `sys_role_menu` VALUES (7472, 2, 157);
INSERT INTO `sys_role_menu` VALUES (7473, 2, 158);
INSERT INTO `sys_role_menu` VALUES (7474, 2, 159);
INSERT INTO `sys_role_menu` VALUES (7475, 2, 160);
INSERT INTO `sys_role_menu` VALUES (7476, 2, 148);
INSERT INTO `sys_role_menu` VALUES (7477, 2, 33);
INSERT INTO `sys_role_menu` VALUES (7478, 2, 149);
INSERT INTO `sys_role_menu` VALUES (7479, 2, 35);
INSERT INTO `sys_role_menu` VALUES (7480, 2, 142);
INSERT INTO `sys_role_menu` VALUES (7481, 2, 3);
INSERT INTO `sys_role_menu` VALUES (7482, 2, 4);
INSERT INTO `sys_role_menu` VALUES (7483, 2, 5);
INSERT INTO `sys_role_menu` VALUES (7484, 2, 171);
INSERT INTO `sys_role_menu` VALUES (7485, 2, 172);
INSERT INTO `sys_role_menu` VALUES (7486, 2, 173);
INSERT INTO `sys_role_menu` VALUES (7487, 2, 174);
INSERT INTO `sys_role_menu` VALUES (7488, 2, 175);
INSERT INTO `sys_role_menu` VALUES (7489, 2, 143);
INSERT INTO `sys_role_menu` VALUES (7490, 2, 7);
INSERT INTO `sys_role_menu` VALUES (7491, 2, 8);
INSERT INTO `sys_role_menu` VALUES (7492, 2, 9);
INSERT INTO `sys_role_menu` VALUES (7493, 2, 144);
INSERT INTO `sys_role_menu` VALUES (7494, 2, 11);
INSERT INTO `sys_role_menu` VALUES (7495, 2, 12);
INSERT INTO `sys_role_menu` VALUES (7496, 2, 13);
INSERT INTO `sys_role_menu` VALUES (7497, 2, 145);
INSERT INTO `sys_role_menu` VALUES (7498, 2, 15);
INSERT INTO `sys_role_menu` VALUES (7499, 2, 16);
INSERT INTO `sys_role_menu` VALUES (7500, 2, 17);
INSERT INTO `sys_role_menu` VALUES (7501, 2, 19);
INSERT INTO `sys_role_menu` VALUES (7502, 2, 20);
INSERT INTO `sys_role_menu` VALUES (7503, 2, 21);
INSERT INTO `sys_role_menu` VALUES (7504, 2, 146);
INSERT INTO `sys_role_menu` VALUES (7505, 2, 24);
INSERT INTO `sys_role_menu` VALUES (7506, 2, 25);
INSERT INTO `sys_role_menu` VALUES (7507, 2, 26);
INSERT INTO `sys_role_menu` VALUES (7508, 2, 147);
INSERT INTO `sys_role_menu` VALUES (7509, 2, 28);
INSERT INTO `sys_role_menu` VALUES (7510, 2, 29);
INSERT INTO `sys_role_menu` VALUES (7511, 2, 30);
INSERT INTO `sys_role_menu` VALUES (7512, 2, 152);
INSERT INTO `sys_role_menu` VALUES (7513, 2, 128);
INSERT INTO `sys_role_menu` VALUES (7514, 2, 129);
INSERT INTO `sys_role_menu` VALUES (7515, 2, 141);
INSERT INTO `sys_role_menu` VALUES (7516, 2, 163);
INSERT INTO `sys_role_menu` VALUES (7517, 2, 164);
INSERT INTO `sys_role_menu` VALUES (7518, 2, 165);
INSERT INTO `sys_role_menu` VALUES (7519, 2, 166);
INSERT INTO `sys_role_menu` VALUES (7520, 2, 167);
INSERT INTO `sys_role_menu` VALUES (7521, 2, 168);
INSERT INTO `sys_role_menu` VALUES (7522, 2, 169);
INSERT INTO `sys_role_menu` VALUES (8416, 2, 282);
INSERT INTO `sys_role_menu` VALUES (8418, 2, 283);
INSERT INTO `sys_role_menu` VALUES (8698, 1, 7);
INSERT INTO `sys_role_menu` VALUES (8699, 1, 8);
INSERT INTO `sys_role_menu` VALUES (8700, 1, 9);
INSERT INTO `sys_role_menu` VALUES (8701, 1, 143);
INSERT INTO `sys_role_menu` VALUES (8702, 1, 142);
INSERT INTO `sys_role_menu` VALUES (8703, 1, 3);
INSERT INTO `sys_role_menu` VALUES (8704, 1, 4);
INSERT INTO `sys_role_menu` VALUES (8705, 1, 5);
INSERT INTO `sys_role_menu` VALUES (8706, 1, 144);
INSERT INTO `sys_role_menu` VALUES (8707, 1, 11);
INSERT INTO `sys_role_menu` VALUES (8708, 1, 12);
INSERT INTO `sys_role_menu` VALUES (8709, 1, 13);
INSERT INTO `sys_role_menu` VALUES (8710, 1, 145);
INSERT INTO `sys_role_menu` VALUES (8711, 1, 15);
INSERT INTO `sys_role_menu` VALUES (8712, 1, 16);
INSERT INTO `sys_role_menu` VALUES (8713, 1, 17);
INSERT INTO `sys_role_menu` VALUES (8714, 1, 19);
INSERT INTO `sys_role_menu` VALUES (8715, 1, 20);
INSERT INTO `sys_role_menu` VALUES (8716, 1, 21);
INSERT INTO `sys_role_menu` VALUES (8717, 1, 146);
INSERT INTO `sys_role_menu` VALUES (8718, 1, 24);
INSERT INTO `sys_role_menu` VALUES (8719, 1, 25);
INSERT INTO `sys_role_menu` VALUES (8720, 1, 26);
INSERT INTO `sys_role_menu` VALUES (8721, 1, 147);
INSERT INTO `sys_role_menu` VALUES (8722, 1, 28);
INSERT INTO `sys_role_menu` VALUES (8723, 1, 29);
INSERT INTO `sys_role_menu` VALUES (8724, 1, 30);
INSERT INTO `sys_role_menu` VALUES (8725, 1, 148);
INSERT INTO `sys_role_menu` VALUES (8726, 1, 33);
INSERT INTO `sys_role_menu` VALUES (8727, 1, 149);
INSERT INTO `sys_role_menu` VALUES (8728, 1, 35);
INSERT INTO `sys_role_menu` VALUES (8729, 1, 150);
INSERT INTO `sys_role_menu` VALUES (8730, 1, 38);
INSERT INTO `sys_role_menu` VALUES (8731, 1, 151);
INSERT INTO `sys_role_menu` VALUES (8732, 1, 40);
INSERT INTO `sys_role_menu` VALUES (8733, 1, 41);
INSERT INTO `sys_role_menu` VALUES (8734, 1, 42);
INSERT INTO `sys_role_menu` VALUES (8735, 1, 44);
INSERT INTO `sys_role_menu` VALUES (8736, 1, 45);
INSERT INTO `sys_role_menu` VALUES (8737, 1, 153);
INSERT INTO `sys_role_menu` VALUES (8738, 1, 136);
INSERT INTO `sys_role_menu` VALUES (8739, 1, 137);
INSERT INTO `sys_role_menu` VALUES (8740, 1, 138);
INSERT INTO `sys_role_menu` VALUES (8741, 1, 139);
INSERT INTO `sys_role_menu` VALUES (8742, 1, 141);
INSERT INTO `sys_role_menu` VALUES (8743, 1, 155);
INSERT INTO `sys_role_menu` VALUES (8744, 1, 156);
INSERT INTO `sys_role_menu` VALUES (8745, 1, 157);
INSERT INTO `sys_role_menu` VALUES (8746, 1, 158);
INSERT INTO `sys_role_menu` VALUES (8747, 1, 159);
INSERT INTO `sys_role_menu` VALUES (8748, 1, 160);
INSERT INTO `sys_role_menu` VALUES (8749, 1, 171);
INSERT INTO `sys_role_menu` VALUES (8750, 1, 172);
INSERT INTO `sys_role_menu` VALUES (8751, 1, 173);
INSERT INTO `sys_role_menu` VALUES (8752, 1, 174);
INSERT INTO `sys_role_menu` VALUES (8753, 1, 175);
INSERT INTO `sys_role_menu` VALUES (8754, 1, 152);
INSERT INTO `sys_role_menu` VALUES (8755, 1, 128);
INSERT INTO `sys_role_menu` VALUES (8756, 1, 129);
INSERT INTO `sys_role_menu` VALUES (8757, 1, 278);
INSERT INTO `sys_role_menu` VALUES (8758, 1, 163);
INSERT INTO `sys_role_menu` VALUES (8759, 1, 164);
INSERT INTO `sys_role_menu` VALUES (8760, 1, 165);
INSERT INTO `sys_role_menu` VALUES (8761, 1, 166);
INSERT INTO `sys_role_menu` VALUES (8762, 1, 167);
INSERT INTO `sys_role_menu` VALUES (8763, 1, 168);
INSERT INTO `sys_role_menu` VALUES (8764, 1, 169);
INSERT INTO `sys_role_menu` VALUES (8765, 1, 279);
INSERT INTO `sys_role_menu` VALUES (8766, 1, 280);
INSERT INTO `sys_role_menu` VALUES (8767, 1, 281);
INSERT INTO `sys_role_menu` VALUES (8768, 1, 282);
INSERT INTO `sys_role_menu` VALUES (8769, 1, 284);
INSERT INTO `sys_role_menu` VALUES (8770, 1, 2);
INSERT INTO `sys_role_menu` VALUES (8771, 1, 170);
INSERT INTO `sys_role_menu` VALUES (8772, 1, 6);
INSERT INTO `sys_role_menu` VALUES (8773, 1, 10);
INSERT INTO `sys_role_menu` VALUES (8774, 1, 14);
INSERT INTO `sys_role_menu` VALUES (8775, 1, 18);
INSERT INTO `sys_role_menu` VALUES (8776, 1, 23);
INSERT INTO `sys_role_menu` VALUES (8777, 1, 27);
INSERT INTO `sys_role_menu` VALUES (8778, 1, 32);
INSERT INTO `sys_role_menu` VALUES (8779, 1, 34);
INSERT INTO `sys_role_menu` VALUES (8780, 1, 37);
INSERT INTO `sys_role_menu` VALUES (8781, 1, 39);
INSERT INTO `sys_role_menu` VALUES (8782, 1, 43);
INSERT INTO `sys_role_menu` VALUES (8783, 1, 154);
INSERT INTO `sys_role_menu` VALUES (8784, 1, 283);
INSERT INTO `sys_role_menu` VALUES (8785, 1, 127);
INSERT INTO `sys_role_menu` VALUES (8786, 1, 135);
INSERT INTO `sys_role_menu` VALUES (8787, 1, 162);
INSERT INTO `sys_role_menu` VALUES (8788, 1, 1);
INSERT INTO `sys_role_menu` VALUES (8789, 1, 22);
INSERT INTO `sys_role_menu` VALUES (8790, 1, 31);
INSERT INTO `sys_role_menu` VALUES (8791, 1, 36);
INSERT INTO `sys_role_menu` VALUES (8792, 1, 126);
INSERT INTO `sys_role_menu` VALUES (8793, 1, 134);
INSERT INTO `sys_role_menu` VALUES (8794, 1, 140);
INSERT INTO `sys_role_menu` VALUES (8795, 1, 285);
INSERT INTO `sys_role_menu` VALUES (8796, 1, 286);
INSERT INTO `sys_role_menu` VALUES (8797, 1, 287);
INSERT INTO `sys_role_menu` VALUES (8798, 1, 288);
INSERT INTO `sys_role_menu` VALUES (8799, 1, 289);
INSERT INTO `sys_role_menu` VALUES (8800, 1, 290);
INSERT INTO `sys_role_menu` VALUES (8801, 1, 161);
INSERT INTO `sys_role_menu` VALUES (8802, 1, 291);
INSERT INTO `sys_role_menu` VALUES (8803, 1, 292);
INSERT INTO `sys_role_menu` VALUES (8804, 1, 293);
INSERT INTO `sys_role_menu` VALUES (8805, 1, 294);
INSERT INTO `sys_role_menu` VALUES (8806, 1, 295);
INSERT INTO `sys_role_menu` VALUES (8807, 1, 296);
INSERT INTO `sys_role_menu` VALUES (8808, 1, 297);
INSERT INTO `sys_role_menu` VALUES (8809, 1, 298);
INSERT INTO `sys_role_menu` VALUES (8810, 1, 299);
INSERT INTO `sys_role_menu` VALUES (8811, 1, 300);
INSERT INTO `sys_role_menu` VALUES (8812, 1, 301);
INSERT INTO `sys_role_menu` VALUES (8813, 1, 302);
INSERT INTO `sys_role_menu` VALUES (8814, 1, 303);
INSERT INTO `sys_role_menu` VALUES (8815, 1, 304);
INSERT INTO `sys_role_menu` VALUES (8816, 1, 305);
INSERT INTO `sys_role_menu` VALUES (8817, 1, 306);
INSERT INTO `sys_role_menu` VALUES (8818, 1, 307);
INSERT INTO `sys_role_menu` VALUES (8819, 1, 308);
INSERT INTO `sys_role_menu` VALUES (8820, 1, 309);
INSERT INTO `sys_role_menu` VALUES (8821, 1, 310);
INSERT INTO `sys_role_menu` VALUES (8822, 1, 311);
INSERT INTO `sys_role_menu` VALUES (8823, 1, 312);
INSERT INTO `sys_role_menu` VALUES (8824, 1, 313);
INSERT INTO `sys_role_menu` VALUES (8825, 1, 314);
INSERT INTO `sys_role_menu` VALUES (8826, 1, 315);
INSERT INTO `sys_role_menu` VALUES (8827, 1, 316);
INSERT INTO `sys_role_menu` VALUES (8828, 1, 317);
INSERT INTO `sys_role_menu` VALUES (8829, 1, 318);
INSERT INTO `sys_role_menu` VALUES (8830, 1, 319);
INSERT INTO `sys_role_menu` VALUES (8831, 1, 320);
INSERT INTO `sys_role_menu` VALUES (8832, 1, 321);
INSERT INTO `sys_role_menu` VALUES (8833, 1, 322);
INSERT INTO `sys_role_menu` VALUES (8834, 1, 323);
INSERT INTO `sys_role_menu` VALUES (8835, 1, 324);
INSERT INTO `sys_role_menu` VALUES (8836, 1, 325);
INSERT INTO `sys_role_menu` VALUES (8837, 1, 326);
INSERT INTO `sys_role_menu` VALUES (8838, 1, 327);
INSERT INTO `sys_role_menu` VALUES (8839, 1, 328);
INSERT INTO `sys_role_menu` VALUES (8840, 1, 329);
INSERT INTO `sys_role_menu` VALUES (8841, 1, 330);
INSERT INTO `sys_role_menu` VALUES (8842, 1, 331);
INSERT INTO `sys_role_menu` VALUES (8843, 1, 332);
INSERT INTO `sys_role_menu` VALUES (8844, 1, 333);
INSERT INTO `sys_role_menu` VALUES (8845, 1, 334);
INSERT INTO `sys_role_menu` VALUES (8846, 1, 335);
INSERT INTO `sys_role_menu` VALUES (8847, 1, 336);
INSERT INTO `sys_role_menu` VALUES (8848, 1, 337);
INSERT INTO `sys_role_menu` VALUES (8849, 1, 338);
INSERT INTO `sys_role_menu` VALUES (8850, 1, 339);
INSERT INTO `sys_role_menu` VALUES (8851, 1, 340);
INSERT INTO `sys_role_menu` VALUES (8852, 1, 341);
INSERT INTO `sys_role_menu` VALUES (8853, 1, 342);
INSERT INTO `sys_role_menu` VALUES (8854, 1, 343);
INSERT INTO `sys_role_menu` VALUES (8855, 1, 344);
INSERT INTO `sys_role_menu` VALUES (8856, 1, 345);
INSERT INTO `sys_role_menu` VALUES (8857, 1, 346);
INSERT INTO `sys_role_menu` VALUES (8858, 1, 347);
INSERT INTO `sys_role_menu` VALUES (8859, 1, 348);
INSERT INTO `sys_role_menu` VALUES (8860, 1, 349);
INSERT INTO `sys_role_menu` VALUES (8861, 1, 350);
INSERT INTO `sys_role_menu` VALUES (8862, 1, 351);
INSERT INTO `sys_role_menu` VALUES (8863, 1, 352);

-- ------------------------------------------------------------
-- sys_user_post  用户岗位关联表（仅 user_id=1）  (2 条)
-- ------------------------------------------------------------
INSERT INTO `sys_user_post` VALUES (15, 1, 1);
INSERT INTO `sys_user_post` VALUES (16, 1, 2);

-- ------------------------------------------------------------
-- sys_config_group  系统配置组表  (14 条)
-- ------------------------------------------------------------
INSERT INTO `sys_config_group` VALUES (1, 'system', '系统配置', NULL, '{\"siteName\":\"Mars-Admin\",\"siteDescription\":\"现代化企业级管理系统\",\"siteLogo\":\"\",\"copyright\":\"版权所有 © 成都火星网络科技有限公司 2025-2030\",\"icp\":\"\",\"watermarkEnabled\":true,\"watermarkType\":\"username\",\"watermarkCustomText\":\"\",\"watermarkOpacity\":0.05}', 1, 1, NULL, '2026-01-31 14:38:29', '2026-01-31 14:38:29');
INSERT INTO `sys_config_group` VALUES (2, 'register', '注册配置', NULL, '{\"enabled\":true,\"verifyEmail\":false,\"verifyPhone\":false,\"defaultRole\":\"user\",\"needAudit\":true}', 2, 1, NULL, '2026-01-31 14:38:29', '2026-01-31 14:38:29');
INSERT INTO `sys_config_group` VALUES (3, 'login', '登录配置', NULL, '{\"captchaEnabled\":true,\"captchaType\":\"image\",\"maxRetryCount\":5,\"lockTime\":30,\"rememberMe\":true,\"singleLogin\":false}', 3, 1, NULL, '2026-01-31 14:38:29', '2026-01-31 15:38:39');
INSERT INTO `sys_config_group` VALUES (4, 'password', '密码配置', NULL, '{\"minLength\":6,\"maxLength\":20,\"requireUppercase\":false,\"requireLowercase\":false,\"requireNumber\":false,\"requireSpecial\":false,\"expireDays\":0}', 4, 1, NULL, '2026-01-31 14:38:29', '2026-01-31 14:38:29');
INSERT INTO `sys_config_group` VALUES (5, 'email', '邮件配置', NULL, '{\"host\":\"smtp.qq.com\",\"port\":465,\"username\":\"850994281@qq.com\",\"password\":\"pbfbulghhkqmbedj\",\"fromName\":\"Mars管理系统\",\"ssl\":true,\"enabled\":true}', 5, 1, NULL, '2026-01-31 14:38:29', '2026-01-31 22:13:14');
INSERT INTO `sys_config_group` VALUES (6, 'emailTemplate', '邮件模板', NULL, '{\"verifyCode\":\"您的验证码是：{code}，有效期{expire}分钟。\",\"resetPassword\":\"您正在重置密码，验证码：{code}，有效期{expire}分钟。\",\"welcome\":\"欢迎注册{siteName}，您的账号已创建成功。\"}', 6, 1, NULL, '2026-01-31 14:38:29', '2026-01-31 14:38:29');
INSERT INTO `sys_config_group` VALUES (7, 'sms', '短信配置', NULL, '{\"provider\":\"aliyun\",\"accessKeyId\":\"LTAI5tEPLV6eFkXgFiRcN111\",\"accessKeySecret\":\"rOOOUyxCxFsxfWru1NojIycWMOvCWJ\",\"signName\":\"测试签名\",\"tencentAppId\":\"\",\"templateVerifyCode\":\"SMS_225366344\",\"templateResetPassword\":\"\",\"templateNotice\":\"\",\"enabled\":true}', 7, 1, NULL, '2026-01-31 14:38:29', '2026-02-13 13:52:21');
INSERT INTO `sys_config_group` VALUES (9, 'storage', '文件配置', NULL, '{\"provider\":\"local\",\"domain\":\"https://mars-1259757313.cos.ap-guangzhou.myqcloud.com\",\"localPath\":\"./uploads\",\"maxSize\":100,\"allowTypes\":\"jpg,jpeg,png,gif,webp,bmp,ico,svg,pdf,doc,docx,xls,xlsx,ppt,pptx,txt,md,csv,xml,json,yaml,yml,html,htm,css,js,ts,vue,java,py,go,sql,sh,bat,mp4,avi,mov,wmv,flv,mkv,webm,mp3,wav,ogg,flac,aac,zip,rar,7z,tar,gz,apk,exe,dmg\",\"minioEndpoint\":\"\",\"minioAccessKey\":\"test\",\"minioSecretKey\":\"123456\",\"minioBucket\":\"\",\"aliyunEndpoint\":\"https://oss-cn-beijing.aliyuncs.com\",\"aliyunAccessKey\":\"LTAI5tEPLV6eFkXgFiRcNA11\",\"aliyunSecretKey\":\"rOOOUyxCxFsxfWru1NojIycWMOvCWJ\",\"aliyunBucket\":\"test\",\"tencentSecretId\":\"AKIDESjxznOs9D3f5II6hDXeZIn5BC8sRq45\",\"tencentSecretKey\":\"qSs54sTt8Azc2EDyxufDYhgS0LTO5a11\",\"tencentBucket\":\"111\",\"tencentRegion\":\"ap-guangzhou\",\"rustfsEndpoint\":\"\",\"rustfsAccessKey\":\"test\",\"rustfsSecretKey\":\"123456\",\"rustfsBucket\":\"\"}', 9, 1, NULL, '2026-01-31 14:38:29', '2026-02-02 17:02:01');
INSERT INTO `sys_config_group` VALUES (10, 'push', '推送配置', NULL, '{\"dingtalk\":{\"signName\":\"SECc676d2aebcd1866a9a07688c682e71397cca6c489bf2106c9e8b72138828ebcd\",\"tokenId\":\"https://oapi.dingtalk.com/robot/send?access_token=bd9c580de40dd39508598779c4fc0cd88df6830a52870edc1ad95815bbe48af4\"},\"feishu\":{\"signName\":\"测试机器人\",\"tokenId\":\"https://open.feishu.cn/open-apis/bot/v2/hook/12ad7a09-2e8f-4447-8cfa-9a3d96ed273d\"},\"wechat_work\":{\"signName\":\"\",\"tokenId\":\"\"}}', 10, 1, NULL, '2026-01-31 14:38:29', '2026-02-13 13:53:13');
INSERT INTO `sys_config_group` VALUES (11, 'thirdParty', '第三方配置', NULL, '{\"wechat\":{\"enabled\":false,\"appId\":\"\",\"appSecret\":\"\"},\"alipay\":{\"enabled\":false,\"appId\":\"\",\"privateKey\":\"\",\"publicKey\":\"\"},\"github\":{\"enabled\":false,\"clientId\":\"\",\"clientSecret\":\"\"}}', 11, 1, NULL, '2026-01-31 14:38:29', '2026-01-31 14:38:29');
INSERT INTO `sys_config_group` VALUES (12, 'payment', '支付配置', NULL, '{\r\n    \"wechatPay\": {\r\n        \"enabled\": true,\r\n        \"mchId\": \"1627500294\",\r\n        \"appId\": \"wxe97894ad8c7ef7e0\",\r\n        \"apiKey\": \"\",\r\n        \"apiV3Key\": \"lxpvkwojpnxafnoutgqowbecdwdsmpwq\",\r\n        \"privateKey\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDlGsA4SciJOYYq\\nTL+/hYlaRLkkJ060c+2MrOl7egozzwddhNLHRC0wasgGdQdbDI39mAm34I7mLdMV\\nlv10dgtKXgpQBHc9QPKy3bPFcgFrz7rxS0YcFrqmzzB69a0LVVfAsZE2SD/4yKc3\\nVFW8cLKQZKRYYm3gZGwN0rsJFVU3dfWgOaoNlBkc5bNIbY7j4aHeW7tJXOQCiig6\\nKj+Dh7r1/POzTciCfqVB1Vjf+VkFMuF6oyKLxMBzFzxvXCGw3PySL6HuY1g5xI7j\\nbNi+xfqtzxZEQAv1QjbfBjzygQXeLCpsuYGVFRRVdyNYxkV90FDVI8swLXpMh65b\\nYNgBGtn1AgMBAAECggEBAKlIx+mPk07aI2mUBkcU+7WofAjbxosN8eP1TBxBw9Ie\\nUnnmj/xPQvi4ng4vYP0E3NIaCmxE0DICgCs+ww7Pvm336LTRZ+3p1KsXqCLnp2cr\\nOh3bGfXdUZO6Gj9w0qlCKTInwn2SizpfwTbf6O3xc++/fbQVHs0kRrc8E5mVmr77\\n01aGIJvXxtQPfdn/R2TMBwqiN8pO5igILlDzNAEusXnfSDOp3rYsXwcnCxJqgnVm\\nydlo7JMU2iqRKSD09qeKFgb+Hbr9aJIQdcvjGBSNmF3MsCFgs/XIb47B4xvy2HBN\\nvIBRwBy08fFeih0GE+0IKr0LyAQ8naMjRTD8A6SbPE0CgYEA9HJ+qigfUPsh/Q+u\\nyyoZeIrsR1xoNVcwANwpWnChsic+B3V/D/pWMJxPv9wKRsVt/dc4kVht//j69tS8\\ny3BFoUxSfUuoK5hdhI8osk3wdVFOnrPPs57s2bMcPPF3Rd5iMvcRNqM1IENCpDAR\\n4zlrEqcMpGSNfaSVhFEyo0fvsV8CgYEA7+6gxxkZJD7DwoVUk8w0BJoq7pNUZc43\\nC0uI8EIRCWxSkd5ahruJjreJuFM1IQUmmqFgewdhEIdUjyORwgVQlo469uwqYPQ5\\n8RWMJcQVK8+QEWV/TdywO3P7oEFgFmVlII/h7Janz/ZlOFZ1X8ANVvpenqgeB2j+\\nl1JMVfjnUSsCgYAHmvRT6PGofFe/XtiKW6H1PSVCxx464p6MuEzVEoIFX/EvHDm6\\nzogV9RcKGhd7wjK83hBVfVHWz/FG8rF5BuIztYMvgMYXrSLjt+yFN6WOkNwIVgHV\\nTdGCqG7tennCg7u8aDFx6LwDZ/RP1WsJDcVGDEp5ZuN8ED3SoxAXQmqzswKBgCk3\\nOtM40oLRbVtq//5ro7vup9VX5bWfWQFNtnZfQwH1Y7G/GpnueVDU4omRcZz8f4cs\\nlaBMwjXOqY31NEK6Gv/h6usj4pvJGHL7mpmaN3DRNRRn9RhxAq0T3XPIBzORs2+G\\nh+7WanllADpPT9Zk7WW1mK90fcQUGzfvYUGbglEFAoGARffpCUANEp1oedOyUVRN\\nSKIvSRggxxqMuzSdnm7eGKDmm+kbA8Iig2C0jgcn4vQZpngbhlNsGrb26Bvdh8wE\\nTBtkcxSBjzsFBdE4kSdVqxnZeVezouWixvkxL4ax1xwczS9hyJlunDljsUb2PkwZ\\nBE39glMdpIqGYrpSTM3p6mI=\\n-----END PRIVATE KEY-----\",\r\n        \"certSerialNo\": \"2FD947564972A8536BDD750944C4796CDF3265EB\",\r\n        \"notifyUrl\": \"http://q668fd96.natappfree.cc/api/pay/notify/wechat\"\r\n    },\r\n    \"alipay\": {\r\n        \"enabled\": true,\r\n        \"appId\": \"2021005192689177\",\r\n        \"privateKey\": \"MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCc1KF+8tqemN/+k99xwIxtfrZ8Py7ZtSv6zoDa0Z9pW1IWpYmz+Zm1wtF3CTX5xApyyBTWQgK8Pb/oEz4u4zKzuwcZFX/IdfP2mWaMHRvWnDLunWqH8rQO+JOaWDvzA68lL02AfubuafQldXY+hdeOpt+t5Kj/OnC+o4z0qTAcM9uTpeX9Z8lttlcW9JtCATRP+klr52bRaOcACgh3MIJrQ3OteeiikvbVtZtw4u3X2h5tdRlCl2/youKO6/iZXGmAmtTGRU8Iy8iBAMI6Ow8K6XH5xHccTzTOx8xv1PZ2IszVvMVhLJDXaUg4DyVbhN8hrmKFmu1i9eBbdSZixkNpAgMBAAECggEAXDjFBqu0VxK6lS9Lc86wRSsAECvvVuIsjH2mVAZ0YTXsHZkWUpjyBGodVow6Czd2lWyGpD+I8Dy3frbiGBxOElZmpB96VtzVqyslnDr5xcdwQ9SZcnwL2cnesiI0joCaG5mnT2pQTd5MTUK3V6jIyv/iBJWzsvIgnln6Z1yeB9ai/3c5Lvu0/ZnhC7trqD73BB1x49E0AV90y0/C/IA+FLEKio9/xjgYweSvTiaYTCBKzQv74Oco54HDtd93rlavZUu7F1qdpOWAj903N1xf8A/fepcL8/qPdSZNoRbPr2NgPMZa70hLvnWDfIXRWoaOZ+lFnPtewI8FAaVX4mI3AQKBgQDO8GxdEunrmRuOXbv/JqTj0dG2lXT7kUvDdJ6QVr3HIsmyxkZXQsp/7QdXh/FdRBNFwkOirmClUqrvYq3CbytgjNdxmYdZQ2A/YXqDdTs8J2Li36hbkOPIFNyMZsjtYF39eosf2oF0/ydRSlMqW5B6jpUh5qCYVWkVtUjLuXaM2QKBgQDCAwMqd6Um9X50dKNIqY1X2ImLiRdLVaqn4/pTwylxxIrRO9f5jF7PnenDci809+Sc+yCcZarvdh1QbUE+YGhYOjj2WGaB9sS2TGDFzOguGs7m7hCIQPa6VEyP2I07kaZcpb+r5GqnT9U47mPRcLJe3zop+w3B7cW5JcdtOSCREQKBgQCbpbALzWcOIoncado2Dk3lYPJ4fy+O6/jtWTDOZb+2IQ9OHN3ZUk5XK+PizUgYm1RXmscefEQK9QPGrBT/cnhQ1X5SXmS0Gf4xjdMFP06/buxsskbCIFeDLVW5cLHeASaQufQckE/gvO1IsjudV2NzGv1Gk13lVhCFGGZZfPSS+QKBgG4y2dSAWx1y6d3p9mkqbW9NPms0djfDNAji9GgpfVvyoErSbA2BzsSs1H/AVtIGUCNefRp4oQwdEe+B70In7nzWrU43zhnZ+cf2QC16AxNVBNqktF1AUSRrB4XZIfeI9m6/csyHFJFuRhVtSuNG2PoMX3RC9oCFtv5AWDNQ9I+RAoGAZF1dQs826kCeptQHXnlgTGNNIX9jLGyfO2qysBOCcqwFIrcJpsb11Q1xLrQmju6EHzr4kAINp32Qd5fo/oCM25JuSiw+fK6CgkAEYjSr/9dD4KpGicHmsib3GyfPj850K2RwFz2RckwX+If/NgI3dIecMTgTJ0tfytaaeqFH4PY=\",\r\n        \"publicKey\": \"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiwci3m6eLWeR1kgfeWm/F0V1e68VWUyRB4N5mhnxryTHqiLeN8ilxN9Kn/Ute1C9cL3b4hfx3NYk7zt60QWP9ly8QJQOlqd1H7XsG16AlEpsIaN1SrMYWq16nAD6uwvMmK0nTdzhuNIKOfdC2YWyv3AJTWh0nCTddYV2D+eSH/Ui6xkfgK8pFn/X1Q0xjXvuZrsXxF+WTk5mymEy2u4Kp7/rD/lClfNAv68kOHe92iKj1VzhtROrSp5//xuvL2PA7FLMqo5olZpBmda3eMWgnvHNwvaJvHJENN2ubANwMPNkwMkQ7MKLCBI33fzEERxJBACrJCc6lo8t+wq3zDo/uwIDAQAB\",\r\n        \"signType\": \"RSA2\",\r\n        \"charset\": \"UTF-8\",\r\n        \"gatewayUrl\": \"https://openapi.alipay.com/gateway.do\",\r\n        \"notifyUrl\": \"http://q668fd96.natappfree.cc/api/pay/notify/alipay\",\r\n        \"returnUrl\": \"\"\r\n    }\r\n}', 12, 1, NULL, '2026-01-31 14:38:29', '2026-01-31 16:13:21');
INSERT INTO `sys_config_group` VALUES (13, 'security', '安全配置', NULL, '{\"encryptEnabled\":true,\"encryptScope\":\"global\",\"encryptPublicKey\":\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxwcKZj5Wdozt6+8i9H2WW2eNaNUvI9iyU7Ot2P5XW9MSfTqRTCbv/aVEUOm60SHm7OXFAbuwUeuo6Pu2P7qPffXiqCXBdC1joo7VywNlapnmkwXP6jhuP+oHM31BvG2uInv40LHocUIRbMhREavnw+By7kT3Cq2SmgLBGsRkoIrpAuMBe47n8DjRGq2cvFde/EoChO0uO0AxlTUpfNXatUDGH0NtCEJeECoMBkg4nI0JAPnZETkimurbryPFoAVk5ld/GJg5WruQ1piicy9YgbOhjWnmb6gJ1RUU9xypNeHI/jLQCdjBn4NGQFtD73v36/WFnv4MgFAZV6iKr5kSdQIDAQAB\",\"encryptPrivateKey\":\"MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDHBwpmPlZ2jO3r7yL0fZZbZ41o1S8j2LJTs63Y/ldb0xJ9OpFMJu/9pURQ6brRIebs5cUBu7BR66jo+7Y/uo999eKoJcF0LWOijtXLA2VqmeaTBc/qOG4/6gczfUG8ba4ie/jQsehxQhFsyFERq+fD4HLuRPcKrZKaAsEaxGSgiukC4wF7jufwONEarZy8V178SgKE7S47QDGVNSl81dq1QMYfQ20IQl4QKgwGSDicjQkA+dkROSKa6tuvI8WgBWTmV38YmDlau5DWmKJzL1iBs6GNaeZvqAnVFRT3HKk14cj+MtAJ2MGfg0ZAW0Pve/fr9YWe/gyAUBlXqIqvmRJ1AgMBAAECggEAIOYACRCK2EPJXDOGMqXDwc4nKMn8Zc9/AqjztqesJwiHyN1ygQT6rJGx7jIEaGdTNZtxaiztI01x+TkKUhRzfZ20XpkHFj4edxNnMYyZKfrQi0LtsEitqLD1icRNpmj23MpjQZP22SnTmYivJd2ljNJADTSnJUO1tPF5nAQUohipaHm9ikipKzT+Qa605nj1TvG1NF1a0y/IElBGb5FFyQGISgUoiPh8/aZXeO5pS6YMJTTQul/9Q7f9fwJFrzPl3qqc3kDxYjagJcPtV5VmX/nSrMpeLnaTvRIg78ocwNF+XYJ5L1Sr9wxYEADykw4P8E0ijGYynSeZlo0u+Q7U7QKBgQDZaG5ITWYmt+4KQrR0r1HHGFWJPtFVKcwjC+EIm9I1S+gTOjZ/6SG45upDqlHtmCOMf1drRFhSehdD6UHUFL4xN/fAxkP3F+iKU/KfJy6yclCuhW+k0Efi6W4mKR9ZkhINJvVibsNdA0vXQa603bbr7hfHVeJl1xI761htsnEFuwKBgQDqW1s5f67gXowzjmK6a40Z+/DIoHBTd267zOIEknhUg6oaMtW1v/yPjwWrf6wJmpUFO3Mq3xTDd/k1iXBOke2vHmZG2AplNVScreRx20lRBmzuGe+9sSDozTfFJO25oPhH86wmIAmqMB5nu1L1TJjbKRAU+hcdC+v22NWMQ48tjwKBgALF9kIt2pO73Ol8mFi0s9JaWRz7FCiF8/iuehxmAHR1l2xHXdKb4rY9G9fpIEprmmh8Z10S7h1/OTTAkPpnmVV/ZUWsQcmxIGJDV+D32vyjwKu5QAdWMNSQLbuG4sN9vYU1bgPnbc6N8DW6vMPJ4D96Ngtw6QZri+v/wI0FrbNpAoGAcpvuxvXMXemfAu+VFLnYLWbqYBMmG4uC2dDej4HZ2urw2xMVNGcJamN1UGOFjMTOL9rc/ZBPJTCc7TOjeqke5c8mEWtB2jD0ihL4bz3gYwGTb/W7Krde8rq5lW3z3B3+jaF7BMISN+qEVBJmBZRKBJPWS4vqlcfow7VS6d94O70CgYBTLo2LdYZV9rn7FGmgC9/fuJOgWEfeqmunNx8SsYUjaXSyy+Vb+dlgH/YRfypb37rxxsNwWQKggZww6gSO1/TkFoV73W035XBKbMB3XLEFHp2v75qYBYEHvVpW1YEl2QGlUzOUWXrP5G/3v8O0/+5yJwjKcmkWDjPGIIKj8GPZsQ==\",\"xssFilter\":true,\"sqlInject\":true,\"disableDevtool\":false,\"tokenName\":\"Authorization\",\"tokenTimeout\":3600,\"tokenActiveTimeout\":86400,\"tokenIsConcurrent\":true,\"tokenIsShare\":true,\"tokenStyle\":\"uuid\",\"tokenIsLog\":false,\"tokenIsReadBody\":false,\"tokenIsReadCookie\":false,\"tokenIsReadHeader\":true,\"tokenIsPrint\":true,\"tokenIsWriteHeader\":false}', 13, 1, NULL, '2026-01-31 14:38:29', '2026-02-07 16:04:06');
INSERT INTO `sys_config_group` VALUES (15, 'wechatMiniProgram', '小程序配置', NULL, '{\"enabled\":true,\"appId\":\"wxe97894ad8c7ef7e0\",\"appSecret\":\"ef498f4264b2271eac752b36433aca63\"}', 14, 1, '微信小程序登录配置', '2026-02-03 10:48:41', '2026-02-03 10:48:41');
INSERT INTO `sys_config_group` VALUES (16, 'wechatMp', '公众号配置', NULL, '{\"enabled\":true,\"appId\":\"wx12721c4ea1370b36\",\"appSecret\":\"f860891f96df4fffe78f0424b913aedd\",\"token\":\"mars_coding_wechat_token1\",\"aesKey\":\"zBtP7b8qZKCSW2eU7Ozm6Jyapv5PCQu2Vxpj1v72qBP\",\"callbackUrl\":\"https://api.mars-coder.cn/api/wechat/callback\",\"oauthRedirectUrl\":\"http://localhost:3001/login\",\"menuConfig\":\"\"}', 15, 1, '微信公众号配置', '2026-02-03 10:48:41', '2026-02-03 10:48:41');
INSERT INTO `sys_config_group` (`group_code`, `group_name`, `config_value`, `sort`, `status`, `remark`) VALUES ('yunDataSource', '云224数据源', '{"doseeingCookie":""}', 20, 1, '在看数据源配置，包含会员Cookie等');

SET FOREIGN_KEY_CHECKS=1;
