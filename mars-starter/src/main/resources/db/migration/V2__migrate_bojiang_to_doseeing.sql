-- 将已有 BOJIANG 数据源记录迁移为 DOSEEING（播酱平台已下线，统一使用在看）
UPDATE yun_anchor SET data_source = 'DOSEEING' WHERE data_source = 'BOJIANG';
