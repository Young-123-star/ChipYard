-- 仅用于独立测试数据库。脚本会清空宿舍业务数据，严禁在生产或共享数据库执行。
-- 用途：准备 2026-08 水电费完整黄金矩阵；脚本可重复执行以恢复基线。
-- 预期：7 条结算、14 张个人账单；实际费用 1733.54，员工承担 301.69，公司承担 1431.85。

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
    (id, building_code, building_name, address, floor_count, has_elevator,
     total_rooms, total_beds, status, created_at, updated_at)
VALUES
    (9101, 'TEST-124', 'TEST-1/2/4号楼', '水电测试区', 1, 0, 2, 3, 1, NOW(), NOW()),
    (9102, 'TEST-36',  'TEST-3/6号楼',   '水电测试区', 1, 0, 2, 2, 1, NOW(), NOW()),
    (9103, 'TEST-5',   'TEST-5号楼',     '水电测试区', 1, 0, 4, 5, 1, NOW(), NOW());

INSERT INTO dms_floor
    (id, building_id, floor_number, floor_name, room_count, bed_count, status, created_at, updated_at)
VALUES
    (9111, 9101, 1, '测试层', 2, 3, 1, NOW(), NOW()),
    (9112, 9102, 1, '测试层', 2, 2, 1, NOW(), NOW()),
    (9113, 9103, 1, '测试层', 4, 5, 1, NOW(), NOW());

-- settlement_mode: 1=户级，2=房间级
-- electricity_rule/water_rule: 1=户级补贴，2=房间补贴，3=夫妻平摊，4=公司承担
INSERT INTO dms_room
    (id, building_id, floor_id, room_number, room_type, bed_count, occupied_beds,
     gender_limit, status, settlement_mode, utility_account_code, electricity_rule, water_rule,
     created_at, updated_at)
VALUES
    (9201, 9101, 9111, 'H124-A',      2, 2, 2, 0, 2, 1, 'H124',        1, 1, NOW(), NOW()),
    (9202, 9101, 9111, 'H124-B',      2, 1, 1, 0, 2, 1, 'H124',        1, 1, NOW(), NOW()),
    (9203, 9102, 9112, 'R36-EDGE',    3, 1, 1, 0, 2, 2, 'R36-EDGE',    2, 2, NOW(), NOW()),
    (9204, 9102, 9112, 'R36-OVER',    3, 1, 1, 0, 2, 2, 'R36-OVER',    2, 2, NOW(), NOW()),
    (9205, 9103, 9113, 'B5-COUPLE',   2, 2, 2, 0, 2, 2, 'B5-COUPLE',   3, 3, NOW(), NOW()),
    (9206, 9103, 9113, 'B5-SUP',      2, 1, 1, 0, 2, 2, 'B5-SUP',      2, 2, NOW(), NOW()),
    (9207, 9103, 9113, 'B5-STANDARD', 2, 1, 1, 0, 2, 2, 'B5-STANDARD', 4, 4, NOW(), NOW()),
    (9208, 9103, 9113, 'B5-EMPTY',    2, 1, 0, 0, 1, 2, 'B5-EMPTY',    2, 2, NOW(), NOW());

INSERT INTO dms_resident
    (id, employee_no, real_name, gender, resident_type, dept_name, source, status, created_at, updated_at)
VALUES
    (9301, 'TEST-E001', '测试住户01', 1, 1, '测试部', 1, 1, NOW(), NOW()),
    (9302, 'TEST-E002', '测试住户02', 1, 1, '测试部', 1, 1, NOW(), NOW()),
    (9303, 'TEST-E003', '测试住户03', 1, 1, '测试部', 1, 1, NOW(), NOW()),
    (9304, 'TEST-E004', '测试住户04', 1, 1, '测试部', 1, 1, NOW(), NOW()),
    (9305, 'TEST-E005', '测试住户05', 1, 1, '测试部', 1, 1, NOW(), NOW()),
    (9306, 'TEST-E006', '测试住户06', 1, 1, '测试部', 1, 1, NOW(), NOW()),
    (9307, 'TEST-E007', '测试住户07', 2, 1, '测试部', 1, 1, NOW(), NOW()),
    (9308, 'TEST-E008', '测试住户08', 1, 1, '测试部', 1, 1, NOW(), NOW()),
    (9309, 'TEST-E009', '测试住户09', 1, 1, '测试部', 1, 1, NOW(), NOW());

INSERT INTO dms_bed
    (id, room_id, bed_number, bed_type, current_user_id, status, created_at, updated_at)
VALUES
    (9601, 9201, 'A', 3, 9301, 2, NOW(), NOW()),
    (9602, 9201, 'B', 3, 9302, 2, NOW(), NOW()),
    (9603, 9202, 'A', 3, 9303, 2, NOW(), NOW()),
    (9604, 9203, 'A', 3, 9304, 2, NOW(), NOW()),
    (9605, 9204, 'A', 3, 9305, 2, NOW(), NOW()),
    (9606, 9205, 'A', 3, 9306, 2, NOW(), NOW()),
    (9607, 9205, 'B', 3, 9307, 2, NOW(), NOW()),
    (9608, 9206, 'A', 3, 9308, 2, NOW(), NOW()),
    (9609, 9207, 'A', 3, 9309, 2, NOW(), NOW()),
    (9610, 9208, 'A', 3, NULL, 1, NOW(), NOW());

INSERT INTO dms_checkin_intake
    (id, biz_no, resident_id, source, expect_checkin_date, room_type_req, building_id_req,
     remark, status, created_at, updated_at)
VALUES
    (9401, 'TEST-IN-001', 9301, 1, '2026-08-01', 2, 9101, '水电矩阵', 2, NOW(), NOW()),
    (9402, 'TEST-IN-002', 9302, 1, '2026-08-01', 2, 9101, '水电矩阵', 2, NOW(), NOW()),
    (9403, 'TEST-IN-003', 9303, 1, '2026-08-01', 2, 9101, '水电矩阵', 2, NOW(), NOW()),
    (9404, 'TEST-IN-004', 9304, 1, '2026-08-01', 3, 9102, '水电矩阵', 2, NOW(), NOW()),
    (9405, 'TEST-IN-005', 9305, 1, '2026-08-01', 3, 9102, '水电矩阵', 2, NOW(), NOW()),
    (9406, 'TEST-IN-006', 9306, 1, '2026-08-01', 2, 9103, '水电矩阵', 2, NOW(), NOW()),
    (9407, 'TEST-IN-007', 9307, 1, '2026-08-01', 2, 9103, '水电矩阵', 2, NOW(), NOW()),
    (9408, 'TEST-IN-008', 9308, 1, '2026-08-01', 2, 9103, '水电矩阵', 2, NOW(), NOW()),
    (9409, 'TEST-IN-009', 9309, 1, '2026-08-01', 2, 9103, '水电矩阵', 2, NOW(), NOW());

INSERT INTO dms_checkin_record
    (id, intake_id, resident_id, building_id, floor_id, room_id, bed_id,
     checkin_date, checkout_date, status, created_at, updated_at)
VALUES
    (9501, 9401, 9301, 9101, 9111, 9201, 9601, '2026-08-01', NULL, 1, NOW(), NOW()),
    (9502, 9402, 9302, 9101, 9111, 9201, 9602, '2026-08-01', NULL, 1, NOW(), NOW()),
    (9503, 9403, 9303, 9101, 9111, 9202, 9603, '2026-08-01', NULL, 1, NOW(), NOW()),
    (9504, 9404, 9304, 9102, 9112, 9203, 9604, '2026-08-01', NULL, 1, NOW(), NOW()),
    (9505, 9405, 9305, 9102, 9112, 9204, 9605, '2026-08-01', NULL, 1, NOW(), NOW()),
    (9506, 9406, 9306, 9103, 9113, 9205, 9606, '2026-08-01', NULL, 1, NOW(), NOW()),
    (9507, 9407, 9307, 9103, 9113, 9205, 9607, '2026-08-01', NULL, 1, NOW(), NOW()),
    (9508, 9408, 9308, 9103, 9113, 9206, 9608, '2026-08-01', NULL, 1, NOW(), NOW()),
    (9509, 9409, 9309, 9103, 9113, 9207, 9609, '2026-08-01', NULL, 1, NOW(), NOW());

-- target_type: 1=户总表，2=房间表；meter_type: 1=电，2=冷水，3=热水。
INSERT INTO dms_meter_reading
    (id, building_id, account_code, target_type, room_id, period, meter_type,
     prev_reading, current_reading, consumption, unit_price, amount, created_at, updated_at)
VALUES
    (9701, 9101, 'H124', 1, 9201, '2026-08', 1, 1000, 1700, 700, 0.5383, 376.81, NOW(), NOW()),
    (9702, 9101, 'H124', 2, 9201, '2026-08', 1,  500,  760, 260, 0.5383, 139.96, NOW(), NOW()),
    (9703, 9101, 'H124', 2, 9202, '2026-08', 1,  600,  940, 340, 0.5383, 183.02, NOW(), NOW()),
    (9704, 9101, 'H124', 1, 9201, '2026-08', 2,  200,  240,  40, 4.1500, 166.00, NOW(), NOW()),
    (9705, 9101, 'H124', 1, 9201, '2026-08', 3,  100,  120,  20, 4.1500,  83.00, NOW(), NOW()),

    (9710, 9102, 'R36-EDGE', 2, 9203, '2026-08', 1, 100, 350, 250, 0.5383, 134.58, NOW(), NOW()),
    (9711, 9102, 'R36-EDGE', 2, 9203, '2026-08', 2,   0,  10,  10, 4.1500,  41.50, NOW(), NOW()),
    (9712, 9102, 'R36-EDGE', 2, 9203, '2026-08', 3,   0,   7,   7, 4.1500,  29.05, NOW(), NOW()),

    (9720, 9102, 'R36-OVER', 2, 9204, '2026-08', 1, 100, 400, 300, 0.5383, 161.49, NOW(), NOW()),
    (9721, 9102, 'R36-OVER', 2, 9204, '2026-08', 2,   0,  12,  12, 4.1500,  49.80, NOW(), NOW()),
    (9722, 9102, 'R36-OVER', 2, 9204, '2026-08', 3,   0,   8,   8, 4.1500,  33.20, NOW(), NOW()),

    (9730, 9103, 'B5-COUPLE', 2, 9205, '2026-08', 1, 0, 100, 100, 0.5383, 53.83, NOW(), NOW()),
    (9731, 9103, 'B5-COUPLE', 2, 9205, '2026-08', 2, 0,   6,   6, 4.1500, 24.90, NOW(), NOW()),
    (9732, 9103, 'B5-COUPLE', 2, 9205, '2026-08', 3, 0,   4,   4, 4.1500, 16.60, NOW(), NOW()),

    (9740, 9103, 'B5-SUP', 2, 9206, '2026-08', 1, 0, 260, 260, 0.5383, 139.96, NOW(), NOW()),
    (9741, 9103, 'B5-SUP', 2, 9206, '2026-08', 2, 0,  12,  12, 4.1500,  49.80, NOW(), NOW()),
    (9742, 9103, 'B5-SUP', 2, 9206, '2026-08', 3, 0,   8,   8, 4.1500,  33.20, NOW(), NOW()),

    (9750, 9103, 'B5-STANDARD', 2, 9207, '2026-08', 1, 0, 100, 100, 0.5383, 53.83, NOW(), NOW()),
    (9751, 9103, 'B5-STANDARD', 2, 9207, '2026-08', 2, 0,   6,   6, 4.1500, 24.90, NOW(), NOW()),
    (9752, 9103, 'B5-STANDARD', 2, 9207, '2026-08', 3, 0,   4,   4, 4.1500, 16.60, NOW(), NOW()),

    (9760, 9103, 'B5-EMPTY', 2, 9208, '2026-08', 1, 0, 300, 300, 0.5383, 161.49, NOW(), NOW()),
    (9761, 9103, 'B5-EMPTY', 2, 9208, '2026-08', 2, 0,  12,  12, 4.1500,  49.80, NOW(), NOW()),
    (9762, 9103, 'B5-EMPTY', 2, 9208, '2026-08', 3, 0,   8,   8, 4.1500,  33.20, NOW(), NOW());
