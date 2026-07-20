-- 仅用于独立测试数据库。脚本会清空宿舍业务数据，严禁在生产或共享数据库执行。
-- 用途：准备 2026-08 水电费 30 分钟冒烟测试数据。
-- 恢复方式：重启本地 H2 服务后重新执行本脚本。

DELETE FROM dms_fee_bill;
DELETE FROM dms_utility_room_result;
DELETE FROM dms_utility_settlement;
DELETE FROM dms_meter_reading;
DELETE FROM dms_utility_rate;
DELETE FROM dms_checkout_order;
DELETE FROM dms_checkin_record;
DELETE FROM dms_checkin_intake;
DELETE FROM dms_bed;
DELETE FROM dms_room;
DELETE FROM dms_floor;
DELETE FROM dms_building;
DELETE FROM dms_resident;

INSERT INTO dms_utility_rate
    (id, electricity_price, water_price, created_at, updated_at)
VALUES
    (1, 0.5383, 4.1500, NOW(), NOW());

INSERT INTO dms_building
    (id, building_code, building_name, address, floor_count, has_elevator, total_rooms, total_beds, status, created_at, updated_at)
VALUES
    (9102, 'TEST-36', 'TEST-3/6号楼', '水电测试区', 1, 0, 1, 1, 1, NOW(), NOW());

INSERT INTO dms_floor
    (id, building_id, floor_number, floor_name, room_count, bed_count, status, created_at, updated_at)
VALUES
    (9112, 9102, 1, '测试层', 1, 1, 1, NOW(), NOW());

-- 水电配置故意留空，用于验证房间级账户自动使用房间号。
INSERT INTO dms_room
    (id, building_id, floor_id, room_number, room_type, bed_count, occupied_beds,
     gender_limit, status, settlement_mode, utility_account_code, electricity_rule, water_rule,
     created_at, updated_at)
VALUES
    (9204, 9102, 9112, 'T36-01', 3, 1, 1,
     0, 2, NULL, NULL, 0, 0, NOW(), NOW());

INSERT INTO dms_resident
    (id, employee_no, real_name, gender, resident_type, dept_name, source, status, created_at, updated_at)
VALUES
    (9305, 'TEST-E005', '测试住户05', 1, 1, '测试部', 1, 1, NOW(), NOW());

INSERT INTO dms_bed
    (id, room_id, bed_number, bed_type, current_user_id, status, created_at, updated_at)
VALUES
    (9605, 9204, 'A', 3, 9305, 2, NOW(), NOW());

INSERT INTO dms_checkin_intake
    (id, biz_no, resident_id, source, expect_checkin_date, room_type_req, building_id_req,
     remark, status, created_at, updated_at)
VALUES
    (9405, 'TEST-IN-005', 9305, 1, '2026-08-01', 3, 9102,
     '水电冒烟测试', 2, NOW(), NOW());

INSERT INTO dms_checkin_record
    (id, intake_id, resident_id, building_id, floor_id, room_id, bed_id,
     checkin_date, checkout_date, status, created_at, updated_at)
VALUES
    (9505, 9405, 9305, 9102, 9112, 9204, 9605,
     '2026-08-01', NULL, 1, NOW(), NOW());
