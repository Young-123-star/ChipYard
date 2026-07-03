INSERT INTO sys_user (id, username, password, real_name, gender, status, created_at, updated_at)
SELECT 1, 'admin', '$2a$10$UskK5C9E/NMafS6Gc6KOqe1OIFR6d/qnzpZTteoE2qNyylT9/ggB.', '系统管理员', 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 1 OR username = 'admin');

INSERT INTO dms_building (id, building_code, building_name, address, floor_count, has_elevator, total_rooms, total_beds, status, created_at, updated_at)
SELECT 1, 'A', 'A栋员工宿舍', '园区东路1号', 6, 1, 4, 8, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_building WHERE id = 1 OR building_code = 'A');

INSERT INTO dms_floor (id, building_id, floor_number, floor_name, room_count, bed_count, status, created_at, updated_at)
SELECT 1, 1, 1, '1层', 2, 4, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_floor WHERE id = 1);
INSERT INTO dms_floor (id, building_id, floor_number, floor_name, room_count, bed_count, status, created_at, updated_at)
SELECT 2, 1, 2, '2层', 2, 4, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_floor WHERE id = 2);

INSERT INTO dms_room (id, building_id, floor_id, room_number, room_type, area, orientation, bed_count, occupied_beds, facilities, gender_limit, status, created_at, updated_at)
SELECT 1, 1, 1, 'A101', 2, 18.50, '南', 2, 0, '{"air_conditioner":1,"water_heater":1,"wardrobe":2,"desk":2}', 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_room WHERE id = 1);
INSERT INTO dms_room (id, building_id, floor_id, room_number, room_type, area, orientation, bed_count, occupied_beds, facilities, gender_limit, status, created_at, updated_at)
SELECT 2, 1, 1, 'A102', 2, 18.50, '南', 2, 1, '{"air_conditioner":1,"water_heater":1,"wardrobe":2,"desk":2}', 1, 2, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_room WHERE id = 2);
INSERT INTO dms_room (id, building_id, floor_id, room_number, room_type, area, orientation, bed_count, occupied_beds, facilities, gender_limit, status, created_at, updated_at)
SELECT 3, 1, 2, 'A201', 3, 26.00, '北', 4, 0, '{"air_conditioner":1,"water_heater":1,"wardrobe":4,"desk":4}', 2, 3, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_room WHERE id = 3);
INSERT INTO dms_room (id, building_id, floor_id, room_number, room_type, area, orientation, bed_count, occupied_beds, facilities, gender_limit, status, created_at, updated_at)
SELECT 4, 1, 2, 'A202', 2, 18.50, '南', 2, 0, '{"air_conditioner":1}', 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_room WHERE id = 4);

INSERT INTO dms_bed (id, room_id, bed_number, bed_type, current_user_id, status, created_at, updated_at)
SELECT 1, 1, 'A', 3, NULL, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM dms_bed WHERE id = 1);
INSERT INTO dms_bed (id, room_id, bed_number, bed_type, current_user_id, status, created_at, updated_at)
SELECT 2, 1, 'B', 3, NULL, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM dms_bed WHERE id = 2);
INSERT INTO dms_bed (id, room_id, bed_number, bed_type, current_user_id, status, created_at, updated_at)
SELECT 3, 2, 'A', 3, 1, 2, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM dms_bed WHERE id = 3);
INSERT INTO dms_bed (id, room_id, bed_number, bed_type, current_user_id, status, created_at, updated_at)
SELECT 4, 2, 'B', 3, NULL, 1, NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM dms_bed WHERE id = 4);

INSERT INTO dms_resident (id, employee_no, real_name, gender, resident_type, dept_name, phone, id_card, source, status, created_at, updated_at)
SELECT 1, 'E1001', '张三', 1, 1, '研发部', '13800000001', '110101199001011234', 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_resident WHERE id = 1 OR employee_no = 'E1001');
INSERT INTO dms_resident (id, employee_no, real_name, gender, resident_type, dept_name, phone, id_card, source, status, created_at, updated_at)
SELECT 2, 'E1002', '李四', 2, 1, '财务部', '13800000002', '110101199202022345', 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_resident WHERE id = 2 OR employee_no = 'E1002');
INSERT INTO dms_resident (id, employee_no, real_name, gender, resident_type, dept_name, phone, id_card, source, status, created_at, updated_at)
SELECT 3, 'E2001', '王五', 1, 2, '外包-保洁', '13800000003', '110101199303033456', 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_resident WHERE id = 3 OR employee_no = 'E2001');

INSERT INTO dms_checkin_intake (id, biz_no, resident_id, source, expect_checkin_date, gender_limit_req, room_type_req, building_id_req, remark, status, raw_payload, created_at, updated_at)
SELECT 1, 'SEED-IN-1', 1, 3, '2026-06-01', 1, 2, 1, '种子-已入住', 2, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_checkin_intake WHERE id = 1 OR biz_no = 'SEED-IN-1');
INSERT INTO dms_checkin_record (id, intake_id, resident_id, building_id, floor_id, room_id, bed_id, checkin_date, status, created_at, updated_at)
SELECT 1, 1, 1, 1, 1, 2, 3, '2026-06-01', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_checkin_record WHERE id = 1);
INSERT INTO dms_checkin_intake (id, biz_no, resident_id, source, expect_checkin_date, gender_limit_req, room_type_req, building_id_req, remark, status, raw_payload, created_at, updated_at)
SELECT 2, 'SEED-IN-2', 2, 1, '2026-06-20', 2, 2, 1, '种子-待分配', 1, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_checkin_intake WHERE id = 2 OR biz_no = 'SEED-IN-2');

INSERT INTO dms_checkout_order (id, biz_no, resident_id, checkin_record_id, source, reason, expect_checkout_date, status, raw_payload, created_at, updated_at)
SELECT 1, 'SEED-CO-1', 1, 1, 1, '种子-退宿申请', '2026-07-01', 1, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_checkout_order WHERE id = 1 OR biz_no = 'SEED-CO-1');

INSERT INTO dms_fee_standard (id, room_type, monthly_price, remark, created_at, updated_at)
SELECT 1, 1, 1200.00, '单人间', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM dms_fee_standard WHERE id = 1 OR room_type = 1);
INSERT INTO dms_fee_standard (id, room_type, monthly_price, remark, created_at, updated_at)
SELECT 2, 2, 800.00, '双人间', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM dms_fee_standard WHERE id = 2 OR room_type = 2);
INSERT INTO dms_fee_standard (id, room_type, monthly_price, remark, created_at, updated_at)
SELECT 3, 3, 500.00, '四人间', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM dms_fee_standard WHERE id = 3 OR room_type = 3);

INSERT INTO dms_fee_bill (id, bill_no, checkin_record_id, resident_id, room_id, period, amount, status, created_at, updated_at)
SELECT 1, 'BILL-1-202606', 1, 1, 2, '2026-06', 800.00, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_fee_bill WHERE id = 1 OR bill_no = 'BILL-1-202606');

INSERT INTO dms_utility_rate (id, electricity_price, water_price, created_at, updated_at)
SELECT 1, 1.00, 5.00, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_utility_rate WHERE id = 1);

INSERT INTO dms_meter_reading (id, room_id, period, meter_type, prev_reading, current_reading, consumption, unit_price, amount, created_at, updated_at)
SELECT 1, 2, '2026-06', 1, 100.00, 130.00, 30.00, 1.00, 30.00, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_meter_reading WHERE id = 1);
INSERT INTO dms_meter_reading (id, room_id, period, meter_type, prev_reading, current_reading, consumption, unit_price, amount, created_at, updated_at)
SELECT 2, 2, '2026-06', 2, 50.00, 58.00, 8.00, 5.00, 40.00, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_meter_reading WHERE id = 2);

INSERT INTO dms_repair_order (id, order_no, room_id, resident_id, title, description, priority, status, handler, accepted_at, result, completed_at, remark, created_at, updated_at)
SELECT 1, 'SEED-RO-1', 2, 1, '空调漏水', 'A102 空调内机滴水', 2, 1, NULL, NULL, NULL, NULL, '种子-待受理', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_repair_order WHERE id = 1 OR order_no = 'SEED-RO-1');
INSERT INTO dms_repair_order (id, order_no, room_id, resident_id, title, description, priority, status, handler, accepted_at, result, completed_at, remark, created_at, updated_at)
SELECT 2, 'SEED-RO-2', 1, NULL, '门锁松动', 'A101 门锁松动需检修', 1, 2, '维修班张工', NOW(), NULL, NULL, '种子-处理中', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_repair_order WHERE id = 2 OR order_no = 'SEED-RO-2');
INSERT INTO dms_repair_order (id, order_no, room_id, resident_id, title, description, priority, status, handler, accepted_at, result, completed_at, remark, created_at, updated_at)
SELECT 3, 'SEED-RO-3', 2, 1, '灯管损坏', '已更换灯管', 1, 3, '维修班李工', NOW(), '已更换灯管', NOW(), '种子-已完成', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM dms_repair_order WHERE id = 3 OR order_no = 'SEED-RO-3');