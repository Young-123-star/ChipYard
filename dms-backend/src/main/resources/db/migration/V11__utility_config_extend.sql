-- 水电结算参数配置化：免额与结算周期日纳入 dms_utility_rate 单行配置
ALTER TABLE dms_utility_rate ADD COLUMN electric_allowance DECIMAL(10,2) NOT NULL DEFAULT 250 COMMENT '电免额(度/间/月)';
ALTER TABLE dms_utility_rate ADD COLUMN household_water_allowance DECIMAL(10,2) NOT NULL DEFAULT 50 COMMENT '户级水免额(吨/户/月)';
ALTER TABLE dms_utility_rate ADD COLUMN room_water_allowance DECIMAL(10,2) NOT NULL DEFAULT 17 COMMENT '房间水免额(吨/间/月)';
ALTER TABLE dms_utility_rate ADD COLUMN cycle_start_day TINYINT NOT NULL DEFAULT 25 COMMENT '结算周期起日(上月)';
ALTER TABLE dms_utility_rate ADD COLUMN cycle_end_day TINYINT NOT NULL DEFAULT 24 COMMENT '结算周期止日(本月)';
