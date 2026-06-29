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
    deleted_at    DATETIME
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
    remark        VARCHAR(500),
    created_at    DATETIME,
    updated_at    DATETIME,
    deleted_at    DATETIME
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
    deleted_at      DATETIME
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
    raw_payload         CLOB,
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
    raw_payload          CLOB,
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
    created_at        DATETIME,
    updated_at        DATETIME,
    deleted_at        DATETIME
);

DROP TABLE IF EXISTS dms_meter_reading;
CREATE TABLE dms_meter_reading (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id         BIGINT        NOT NULL,
    period          VARCHAR(7)    NOT NULL,
    meter_type      TINYINT       NOT NULL,
    prev_reading    DECIMAL(12,2) DEFAULT 0,
    current_reading DECIMAL(12,2) NOT NULL,
    consumption     DECIMAL(12,2) DEFAULT 0,
    unit_price      DECIMAL(10,2) DEFAULT 0,
    amount          DECIMAL(10,2) DEFAULT 0,
    created_at      DATETIME,
    updated_at      DATETIME,
    deleted_at      DATETIME
);

DROP TABLE IF EXISTS dms_utility_rate;
CREATE TABLE dms_utility_rate (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    electricity_price DECIMAL(10,2) NOT NULL,
    water_price       DECIMAL(10,2) NOT NULL,
    created_at        DATETIME,
    updated_at        DATETIME,
    deleted_at        DATETIME
);

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
