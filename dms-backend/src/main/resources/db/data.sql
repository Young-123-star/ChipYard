-- admin / admin123
INSERT INTO sys_user (username, password, real_name, gender, status, created_at, updated_at)
VALUES ('admin', '$2a$10$UskK5C9E/NMafS6Gc6KOqe1OIFR6d/qnzpZTteoE2qNyylT9/ggB.', '系统管理员', 1, 1, NOW(), NOW());

INSERT INTO dms_building (building_code, building_name, address, floor_count, has_elevator, total_rooms, total_beds, status, created_at, updated_at)
VALUES ('A', 'A栋员工宿舍', '园区东路1号', 6, 1, 4, 8, 1, NOW(), NOW());

INSERT INTO dms_floor (building_id, floor_number, floor_name, room_count, bed_count, status, created_at, updated_at)
VALUES (1, 1, '1层', 2, 4, 1, NOW(), NOW()),
       (1, 2, '2层', 2, 4, 1, NOW(), NOW());

INSERT INTO dms_room (building_id, floor_id, room_number, room_type, area, orientation, bed_count, occupied_beds, facilities, gender_limit, status, created_at, updated_at)
VALUES (1, 1, 'A101', 2, 18.50, '南', 2, 0, '{"air_conditioner":1,"water_heater":1,"wardrobe":2,"desk":2}', 1, 1, NOW(), NOW()),
       (1, 1, 'A102', 2, 18.50, '南', 2, 1, '{"air_conditioner":1,"water_heater":1,"wardrobe":2,"desk":2}', 1, 2, NOW(), NOW()),
       (1, 2, 'A201', 3, 26.00, '北', 4, 0, '{"air_conditioner":1,"water_heater":1,"wardrobe":4,"desk":4}', 2, 3, NOW(), NOW()),
       (1, 2, 'A202', 2, 18.50, '南', 2, 0, '{"air_conditioner":1}', 1, 1, NOW(), NOW());

INSERT INTO dms_bed (room_id, bed_number, bed_type, current_user_id, status, created_at, updated_at)
VALUES (1, 'A', 3, NULL, 1, NOW(), NOW()),
       (1, 'B', 3, NULL, 1, NOW(), NOW()),
       (2, 'A', 3, 1, 2, NOW(), NOW()),
       (2, 'B', 3, NULL, 1, NOW(), NOW());

INSERT INTO dms_resident (employee_no, real_name, gender, resident_type, dept_name, phone, id_card, source, status, created_at, updated_at)
VALUES ('E1001', '张三', 1, 1, '研发部', '13800000001', '110101199001011234', 1, 1, NOW(), NOW()),
       ('E1002', '李四', 2, 1, '财务部', '13800000002', '110101199202022345', 1, 1, NOW(), NOW()),
       ('E2001', '王五', 1, 2, '外包-保洁', '13800000003', '110101199303033456', 1, 1, NOW(), NOW());

-- 让 A102 已占床位指向居住人 1（张三）
UPDATE dms_bed SET current_user_id = 1 WHERE room_id = 2 AND bed_number = 'A';

-- 张三已入住档案（对应种子里 A102-A 床）
INSERT INTO dms_checkin_intake (biz_no, resident_id, source, expect_checkin_date, gender_limit_req, room_type_req, building_id_req, remark, status, raw_payload, created_at, updated_at)
VALUES ('SEED-IN-1', 1, 3, '2026-06-01', 1, 2, 1, '种子-已入住', 2, NULL, NOW(), NOW());
INSERT INTO dms_checkin_record (intake_id, resident_id, building_id, floor_id, room_id, bed_id, checkin_date, status, created_at, updated_at)
VALUES (1, 1, 1, 1, 2, 3, '2026-06-01', 1, NOW(), NOW());

-- 李四：一条待分配意向单（演示用）
INSERT INTO dms_checkin_intake (biz_no, resident_id, source, expect_checkin_date, gender_limit_req, room_type_req, building_id_req, remark, status, raw_payload, created_at, updated_at)
VALUES ('SEED-IN-2', 2, 1, '2026-06-20', 2, 2, 1, '种子-待分配', 1, NULL, NOW(), NOW());

INSERT INTO dms_checkout_order (biz_no, resident_id, checkin_record_id, source, reason, expect_checkout_date, status, raw_payload, created_at, updated_at)
VALUES ('SEED-CO-1', 1, 1, 1, '种子-退宿申请', '2026-07-01', 1, NULL, NOW(), NOW());
