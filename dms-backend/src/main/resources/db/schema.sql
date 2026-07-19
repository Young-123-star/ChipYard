DROP TABLE IF EXISTS dms_inspection_task;
DROP TABLE IF EXISTS dms_inspection_plan;
DROP TABLE IF EXISTS sys_dict_item;
DROP TABLE IF EXISTS sys_dict_type;

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    real_name   VARCHAR(50)  NOT NULL,
    gender      TINYINT      DEFAULT 0,
    status      TINYINT      DEFAULT 1,
    created_at  DATETIME,
    updated_at  DATETIME,
    deleted_at  DATETIME
);

DROP TABLE IF EXISTS dms_building;
CREATE TABLE dms_building (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_code VARCHAR(50)  NOT NULL,
    building_name VARCHAR(100) NOT NULL,
    address       VARCHAR(255),
    floor_count   INT  NOT NULL DEFAULT 1,
    has_elevator  TINYINT DEFAULT 0,
    total_rooms   INT DEFAULT 0,
    total_beds    INT DEFAULT 0,
    status        TINYINT DEFAULT 1,
    remark        VARCHAR(500),
    created_at    DATETIME,
    updated_at    DATETIME,
    deleted_at    DATETIME,
    active_unique_key TINYINT GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END)
);

DROP TABLE IF EXISTS dms_floor;
CREATE TABLE dms_floor (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_id  BIGINT NOT NULL,
    floor_number INT    NOT NULL,
    floor_name   VARCHAR(50),
    room_count   INT DEFAULT 0,
    bed_count    INT DEFAULT 0,
    status       TINYINT DEFAULT 1,
    created_at   DATETIME,
    updated_at   DATETIME,
    deleted_at   DATETIME
);

DROP TABLE IF EXISTS dms_room;
CREATE TABLE dms_room (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_id   BIGINT NOT NULL,
    floor_id      BIGINT NOT NULL,
    room_number   VARCHAR(20) NOT NULL,
    room_type     TINYINT NOT NULL,
    area          DECIMAL(6,2),
    orientation   VARCHAR(20),
    bed_count     INT NOT NULL DEFAULT 1,
    occupied_beds INT DEFAULT 0,
    facilities    VARCHAR(1000),
    gender_limit  TINYINT DEFAULT 0,
    status        TINYINT DEFAULT 1,
    settlement_mode TINYINT,
    utility_account_code VARCHAR(50),
    electricity_rule TINYINT DEFAULT 0,
    water_rule TINYINT DEFAULT 0,
    remark        VARCHAR(500),
    created_at    DATETIME,
    updated_at    DATETIME,
    deleted_at    DATETIME,
    active_unique_key TINYINT GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END)
);

DROP TABLE IF EXISTS dms_bed;
CREATE TABLE dms_bed (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id         BIGINT NOT NULL,
    bed_number      VARCHAR(20) NOT NULL,
    bed_type        TINYINT DEFAULT 1,
    current_user_id BIGINT,
    status          TINYINT DEFAULT 1,
    created_at      DATETIME,
    updated_at      DATETIME,
    deleted_at      DATETIME,
    active_unique_key TINYINT GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END)
);

DROP TABLE IF EXISTS dms_resident;
CREATE TABLE dms_resident (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_no   VARCHAR(50)  NOT NULL,
    real_name     VARCHAR(50)  NOT NULL,
    gender        TINYINT      DEFAULT 0,
    resident_type TINYINT      DEFAULT 1,
    dept_name     VARCHAR(100),
    phone         VARCHAR(20),
    id_card       VARCHAR(32),
    source        TINYINT      DEFAULT 2,
    status        TINYINT      DEFAULT 1,
    created_at    DATETIME,
    updated_at    DATETIME,
    deleted_at    DATETIME
);

DROP TABLE IF EXISTS dms_checkin_intake;
CREATE TABLE dms_checkin_intake (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    biz_no              VARCHAR(64) NOT NULL,
    resident_id         BIGINT      NOT NULL,
    source              TINYINT     DEFAULT 3,
    expect_checkin_date DATE,
    gender_limit_req    TINYINT     DEFAULT 0,
    room_type_req       TINYINT,
    building_id_req     BIGINT,
    remark              VARCHAR(500),
    status              TINYINT     DEFAULT 1,
    raw_payload         TEXT,
    created_at          DATETIME,
    updated_at          DATETIME,
    deleted_at          DATETIME
);

DROP TABLE IF EXISTS dms_checkin_record;
CREATE TABLE dms_checkin_record (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    intake_id    BIGINT NOT NULL,
    resident_id  BIGINT NOT NULL,
    building_id  BIGINT,
    floor_id     BIGINT,
    room_id      BIGINT,
    bed_id       BIGINT,
    checkin_date DATE,
    status       TINYINT DEFAULT 1,
    created_at   DATETIME,
    updated_at   DATETIME,
    deleted_at   DATETIME
);

ALTER TABLE dms_checkin_record ADD COLUMN checkout_date DATE;

DROP TABLE IF EXISTS dms_checkout_order;
CREATE TABLE dms_checkout_order (
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    biz_no               VARCHAR(64) NOT NULL,
    resident_id          BIGINT      NOT NULL,
    checkin_record_id    BIGINT,
    source               TINYINT     DEFAULT 3,
    reason               VARCHAR(500),
    expect_checkout_date DATE,
    status               TINYINT     DEFAULT 1,
    raw_payload          TEXT,
    arrears_amount       DECIMAL(10,2) DEFAULT 0,
    created_at           DATETIME,
    updated_at           DATETIME,
    deleted_at           DATETIME
);

DROP TABLE IF EXISTS dms_fee_standard;
CREATE TABLE dms_fee_standard (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type     TINYINT        NOT NULL,
    monthly_price DECIMAL(10,2)  NOT NULL,
    remark        VARCHAR(200),
    created_at    DATETIME,
    updated_at    DATETIME,
    deleted_at    DATETIME
);

DROP TABLE IF EXISTS dms_fee_bill;
CREATE TABLE dms_fee_bill (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    bill_no           VARCHAR(64)   NOT NULL,
    checkin_record_id BIGINT        NOT NULL,
    resident_id       BIGINT        NOT NULL,
    room_id           BIGINT,
    period            VARCHAR(7)    NOT NULL,
    amount            DECIMAL(10,2) NOT NULL,
    status            TINYINT       DEFAULT 1,
    paid_at           DATETIME,
    pay_method        TINYINT,
    bill_type         TINYINT       DEFAULT 1,
    remark            VARCHAR(200),
    utility_result_id BIGINT,
    created_at        DATETIME,
    updated_at        DATETIME,
    deleted_at        DATETIME
);

DROP TABLE IF EXISTS dms_meter_reading;
CREATE TABLE dms_meter_reading (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_id BIGINT,
    account_code VARCHAR(50),
    target_type TINYINT DEFAULT 2,
    room_id         BIGINT,
    period          VARCHAR(7)    NOT NULL,
    meter_type      TINYINT       NOT NULL,
    prev_reading    DECIMAL(12,2) DEFAULT 0,
    current_reading DECIMAL(12,2) NOT NULL,
    consumption     DECIMAL(12,2) DEFAULT 0,
    unit_price      DECIMAL(10,4) DEFAULT 0,
    amount          DECIMAL(10,2) DEFAULT 0,
    created_at      DATETIME,
    updated_at      DATETIME,
    deleted_at      DATETIME
);

DROP TABLE IF EXISTS dms_utility_rate;
CREATE TABLE dms_utility_rate (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    electricity_price DECIMAL(10,4) NOT NULL,
    water_price       DECIMAL(10,4) NOT NULL,
    created_at        DATETIME,
    updated_at        DATETIME,
    deleted_at        DATETIME
);

DROP TABLE IF EXISTS dms_utility_room_result;
DROP TABLE IF EXISTS dms_utility_settlement;
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
    active_unique_key TINYINT GENERATED ALWAYS AS (CASE WHEN status = 1 AND deleted_at IS NULL THEN 1 ELSE NULL END)
);
CREATE UNIQUE INDEX uk_utility_settlement_active ON dms_utility_settlement (building_id, account_code, period, active_unique_key);

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
CREATE INDEX idx_utility_result_settlement ON dms_utility_room_result (settlement_id);
DROP TABLE IF EXISTS dms_repair_order;
CREATE TABLE dms_repair_order (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no     VARCHAR(64)  NOT NULL,
    room_id      BIGINT       NOT NULL,
    resident_id  BIGINT,
    title        VARCHAR(100) NOT NULL,
    description  VARCHAR(500),
    priority     TINYINT      DEFAULT 1,
    status       TINYINT      DEFAULT 1,
    handler      VARCHAR(50),
    accepted_at  DATETIME,
    result       VARCHAR(500),
    completed_at DATETIME,
    remark       VARCHAR(200),
    created_at   DATETIME,
    updated_at   DATETIME,
    deleted_at   DATETIME
);

CREATE TABLE dms_inspection_plan (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_name   VARCHAR(100) NOT NULL,
    cycle_type  TINYINT NOT NULL,
    target_type TINYINT NOT NULL,
    target_id   BIGINT NOT NULL,
    target_name VARCHAR(100) NOT NULL,
    inspector   VARCHAR(50) NOT NULL,
    items_json  TEXT NOT NULL,
    status      TINYINT DEFAULT 1,
    remark      VARCHAR(500),
    created_at  DATETIME,
    updated_at  DATETIME,
    deleted_at  DATETIME
);

CREATE TABLE dms_inspection_task (
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_no              VARCHAR(64) NOT NULL,
    plan_id              BIGINT NOT NULL,
    plan_name            VARCHAR(100) NOT NULL,
    target_type          TINYINT NOT NULL,
    target_id            BIGINT NOT NULL,
    target_name          VARCHAR(100) NOT NULL,
    inspector            VARCHAR(50) NOT NULL,
    planned_date         DATE NOT NULL,
    items_json           TEXT NOT NULL,
    results_json         TEXT,
    status               TINYINT DEFAULT 1,
    completed_at         DATETIME,
    rectification_note   VARCHAR(500),
    rectified_at         DATETIME,
    created_at           DATETIME,
    updated_at           DATETIME,
    deleted_at           DATETIME,
    CONSTRAINT uk_inspection_task_plan_date UNIQUE (plan_id, planned_date)
);
CREATE TABLE sys_dict_type (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_type   VARCHAR(100) NOT NULL,
    dict_name   VARCHAR(100) NOT NULL,
    sort_order  INT DEFAULT 0,
    status      TINYINT DEFAULT 1,
    system_flag TINYINT DEFAULT 0,
    remark      VARCHAR(500),
    created_at  DATETIME,
    updated_at  DATETIME,
    deleted_at  DATETIME
);

CREATE TABLE sys_dict_item (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_type   VARCHAR(100) NOT NULL,
    dict_value  VARCHAR(100) NOT NULL,
    dict_label  VARCHAR(100) NOT NULL,
    sort_order  INT DEFAULT 0,
    tag_type    VARCHAR(30),
    status      TINYINT DEFAULT 1,
    system_flag TINYINT DEFAULT 0,
    remark      VARCHAR(500),
    created_at  DATETIME,
    updated_at  DATETIME,
    deleted_at  DATETIME
);
CREATE UNIQUE INDEX uk_dms_building_code_active ON dms_building (building_code, active_unique_key);
CREATE UNIQUE INDEX uk_dms_room_building_number_active ON dms_room (building_id, room_number, active_unique_key);
CREATE UNIQUE INDEX uk_dms_bed_room_number_active ON dms_bed (room_id, bed_number, active_unique_key);
CREATE UNIQUE INDEX uk_meter_target_period ON dms_meter_reading (building_id, account_code, target_type, room_id, period, meter_type);
