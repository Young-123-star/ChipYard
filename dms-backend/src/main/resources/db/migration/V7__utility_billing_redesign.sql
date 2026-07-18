ALTER TABLE dms_room ADD COLUMN settlement_mode TINYINT;
ALTER TABLE dms_room ADD COLUMN utility_account_code VARCHAR(50);
ALTER TABLE dms_room ADD COLUMN electricity_rule TINYINT DEFAULT 0;
ALTER TABLE dms_room ADD COLUMN water_rule TINYINT DEFAULT 0;

ALTER TABLE dms_meter_reading MODIFY COLUMN room_id BIGINT NULL;
ALTER TABLE dms_meter_reading ADD COLUMN building_id BIGINT;
ALTER TABLE dms_meter_reading ADD COLUMN account_code VARCHAR(50);
ALTER TABLE dms_meter_reading ADD COLUMN target_type TINYINT DEFAULT 2;
ALTER TABLE dms_meter_reading MODIFY COLUMN unit_price DECIMAL(10,4) DEFAULT 0;

ALTER TABLE dms_utility_rate MODIFY COLUMN electricity_price DECIMAL(10,4) NOT NULL;
ALTER TABLE dms_utility_rate MODIFY COLUMN water_price DECIMAL(10,4) NOT NULL;
UPDATE dms_utility_rate SET electricity_price = 0.5383, water_price = 4.1500 WHERE id = 1;

ALTER TABLE dms_fee_bill ADD COLUMN utility_result_id BIGINT;

CREATE TABLE dms_utility_settlement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_id BIGINT NOT NULL,
    account_code VARCHAR(50) NOT NULL,
    period VARCHAR(7) NOT NULL,
    cycle_start DATE NOT NULL,
    cycle_end DATE NOT NULL,
    electricity_price DECIMAL(10,4) NOT NULL,
    water_price DECIMAL(10,4) NOT NULL,
    electricity_usage DECIMAL(14,4) DEFAULT 0,
    water_usage DECIMAL(14,4) DEFAULT 0,
    total_cost DECIMAL(12,2) DEFAULT 0,
    employee_amount DECIMAL(12,2) DEFAULT 0,
    company_amount DECIMAL(12,2) DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME,
    deleted_at DATETIME,
    active_unique_key TINYINT GENERATED ALWAYS AS (CASE WHEN status = 1 AND deleted_at IS NULL THEN 1 ELSE NULL END),
    CONSTRAINT uk_utility_settlement_active UNIQUE (building_id, account_code, period, active_unique_key)
);

CREATE TABLE dms_utility_room_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    settlement_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    utility_type TINYINT NOT NULL,
    actual_usage DECIMAL(14,4) DEFAULT 0,
    allowance_usage DECIMAL(14,4) DEFAULT 0,
    excess_usage DECIMAL(14,4) DEFAULT 0,
    total_cost DECIMAL(12,2) DEFAULT 0,
    employee_amount DECIMAL(12,2) DEFAULT 0,
    company_amount DECIMAL(12,2) DEFAULT 0,
    occupant_count INT DEFAULT 0,
    calculation_note VARCHAR(500),
    created_at DATETIME,
    updated_at DATETIME,
    deleted_at DATETIME
);

CREATE UNIQUE INDEX uk_meter_target_period
    ON dms_meter_reading (building_id, account_code, target_type, room_id, period, meter_type);
CREATE INDEX idx_utility_result_settlement ON dms_utility_room_result (settlement_id);
CREATE INDEX idx_fee_bill_utility_result ON dms_fee_bill (utility_result_id);
