-- 添加云224数据源配置组（用于存储在看会员Cookie等配置）
INSERT INTO `sys_config_group` (`group_code`, `group_name`, `config_value`, `sort`, `status`, `remark`)
VALUES ('yunDataSource', '云224数据源', '{"doseeingCookie":""}', 20, 1, '在看数据源配置，包含会员Cookie等');
