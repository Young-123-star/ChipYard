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

INSERT INTO dms_fee_standard (room_type, monthly_price, remark, created_at, updated_at) VALUES
    (1, 1200.00, '单人间', NOW(), NOW()),
    (2, 800.00,  '双人间', NOW(), NOW()),
    (3, 500.00,  '四人间', NOW(), NOW());

INSERT INTO dms_fee_bill (bill_no, checkin_record_id, resident_id, room_id, period, amount, status, created_at, updated_at)
VALUES ('BILL-1-202606', 1, 1, 2, '2026-06', 800.00, 1, NOW(), NOW());

INSERT INTO dms_utility_rate (id, electricity_price, water_price, created_at, updated_at)
VALUES (1, 0.5383, 4.1500, NOW(), NOW());

INSERT INTO dms_meter_reading (room_id, period, meter_type, prev_reading, current_reading, consumption, unit_price, amount, created_at, updated_at) VALUES
    (2, '2026-06', 1, 100.00, 130.00, 30.00, 1.00, 30.00, NOW(), NOW()),
    (2, '2026-06', 2, 50.00,  58.00,  8.00,  5.00, 40.00, NOW(), NOW());

INSERT INTO dms_repair_order (order_no, room_id, resident_id, title, description, priority, status, handler, accepted_at, result, completed_at, remark, created_at, updated_at)
VALUES ('SEED-RO-1', 2, 1, '空调漏水', 'A102 空调内机滴水', 2, 1, NULL, NULL, NULL, NULL, '种子-待受理', NOW(), NOW()),
       ('SEED-RO-2', 1, NULL, '门锁松动', 'A101 门锁松动需检修', 1, 2, '维修班张工', NOW(), NULL, NULL, '种子-处理中', NOW(), NOW()),
       ('SEED-RO-3', 2, 1, '灯管损坏', '已更换灯管', 1, 3, '维修班李工', NOW(), '已更换灯管', NOW(), '种子-已完成', NOW(), NOW());

INSERT INTO dms_inspection_plan (plan_name, cycle_type, target_type, target_id, target_name, inspector, items_json, status, remark, created_at, updated_at)
VALUES (STRINGDECODE('A\u680b\u65e5\u5e38\u5b89\u5168\u5de1\u68c0'), 1, 1, 1, STRINGDECODE('A\u680b\u5458\u5de5\u5bbf\u820d'), STRINGDECODE('\u5bbf\u820d\u7ba1\u7406\u5458'), STRINGDECODE('["\u6d88\u9632\u8bbe\u65bd","\u7528\u7535\u5b89\u5168","\u536b\u751f\u72b6\u51b5","\u8fdd\u89c4\u7535\u5668"]'), 1, STRINGDECODE('\u79cd\u5b50-\u542f\u7528\u8ba1\u5212'), NOW(), NOW());

INSERT INTO dms_inspection_task (task_no, plan_id, plan_name, target_type, target_id, target_name, inspector, planned_date, items_json, status, created_at, updated_at)
VALUES ('IT-20260710-1', 1, STRINGDECODE('A\u680b\u65e5\u5e38\u5b89\u5168\u5de1\u68c0'), 1, 1, STRINGDECODE('A\u680b\u5458\u5de5\u5bbf\u820d'), STRINGDECODE('\u5bbf\u820d\u7ba1\u7406\u5458'), '2026-07-10', STRINGDECODE('["\u6d88\u9632\u8bbe\u65bd","\u7528\u7535\u5b89\u5168","\u536b\u751f\u72b6\u51b5","\u8fdd\u89c4\u7535\u5668"]'), 1, NOW(), NOW());

INSERT INTO sys_dict_type (dict_type, dict_name, sort_order, status, system_flag, created_at, updated_at) VALUES
('ROOM_TYPE', '房间类型', 10, 1, 1, NOW(), NOW()),
('ROOM_STATUS', '房间状态', 20, 1, 1, NOW(), NOW()),
('BED_STATUS', '床位状态', 30, 1, 1, NOW(), NOW()),
('BED_TYPE', '床位类型', 40, 1, 1, NOW(), NOW()),
('GENDER_LIMIT', '性别限制', 50, 1, 1, NOW(), NOW()),
('BUILDING_STATUS', '楼栋状态', 60, 1, 1, NOW(), NOW()),
('RESIDENT_TYPE', '居住人类型', 70, 1, 1, NOW(), NOW()),
('RESIDENT_STATUS', '居住人状态', 80, 1, 1, NOW(), NOW()),
('INTAKE_STATUS', '入住申请状态', 90, 1, 1, NOW(), NOW()),
('INTAKE_SOURCE', '入住来源', 100, 1, 1, NOW(), NOW()),
('CHECKOUT_STATUS', '退宿状态', 110, 1, 1, NOW(), NOW()),
('CHECKOUT_SOURCE', '退宿来源', 120, 1, 1, NOW(), NOW()),
('BILL_STATUS', '账单状态', 130, 1, 1, NOW(), NOW()),
('BILL_TYPE', '账单类型', 140, 1, 1, NOW(), NOW()),
('PAY_METHOD', '支付方式', 150, 1, 1, NOW(), NOW()),
('METER_TYPE', '抄表类型', 160, 1, 1, NOW(), NOW()),
('REPAIR_STATUS', '维修状态', 170, 1, 1, NOW(), NOW()),
('REPAIR_PRIORITY', '维修优先级', 180, 1, 1, NOW(), NOW()),
('ROOM_FACILITY', '房间设施', 190, 1, 1, NOW(), NOW());

INSERT INTO sys_dict_item (dict_type, dict_value, dict_label, sort_order, tag_type, status, system_flag, created_at, updated_at) VALUES
('ROOM_TYPE', '1', '单人间', 1, 'success', 1, 1, NOW(), NOW()),
('ROOM_TYPE', '2', '双人间', 2, 'primary', 1, 1, NOW(), NOW()),
('ROOM_TYPE', '3', '四人间', 3, 'warning', 1, 1, NOW(), NOW()),
('ROOM_STATUS', '1', '空闲', 1, 'success', 1, 1, NOW(), NOW()),
('ROOM_STATUS', '2', '已满', 2, 'danger', 1, 1, NOW(), NOW()),
('ROOM_STATUS', '3', '维修中', 3, 'warning', 1, 1, NOW(), NOW()),
('BED_STATUS', '1', '空闲', 1, 'success', 1, 1, NOW(), NOW()),
('BED_STATUS', '2', '已入住', 2, 'primary', 1, 1, NOW(), NOW()),
('BED_STATUS', '3', '维修中', 3, 'warning', 1, 1, NOW(), NOW()),
('BED_TYPE', '1', '上铺', 1, 'info', 1, 1, NOW(), NOW()),
('BED_TYPE', '2', '下铺', 2, 'info', 1, 1, NOW(), NOW()),
('BED_TYPE', '3', '独立床', 3, 'success', 1, 1, NOW(), NOW()),
('GENDER_LIMIT', '0', '不限', 0, 'info', 1, 1, NOW(), NOW()),
('GENDER_LIMIT', '1', '男', 1, 'primary', 1, 1, NOW(), NOW()),
('GENDER_LIMIT', '2', '女', 2, 'danger', 1, 1, NOW(), NOW()),
('BUILDING_STATUS', '1', '启用', 1, 'success', 1, 1, NOW(), NOW()),
('BUILDING_STATUS', '0', '停用', 2, 'info', 1, 1, NOW(), NOW()),
('RESIDENT_TYPE', '1', '员工', 1, 'primary', 1, 1, NOW(), NOW()),
('RESIDENT_TYPE', '2', '外包', 2, 'warning', 1, 1, NOW(), NOW()),
('RESIDENT_STATUS', '1', '在住', 1, 'success', 1, 1, NOW(), NOW()),
('RESIDENT_STATUS', '2', '退宿', 2, 'info', 1, 1, NOW(), NOW()),
('INTAKE_STATUS', '1', '待分配', 1, 'warning', 1, 1, NOW(), NOW()),
('INTAKE_STATUS', '2', '已入住', 2, 'success', 1, 1, NOW(), NOW()),
('INTAKE_STATUS', '3', '已取消', 3, 'info', 1, 1, NOW(), NOW()),
('INTAKE_SOURCE', '1', '手工录入', 1, 'primary', 1, 1, NOW(), NOW()),
('INTAKE_SOURCE', '2', 'Excel导入', 2, 'success', 1, 1, NOW(), NOW()),
('INTAKE_SOURCE', '3', 'OA同步', 3, 'warning', 1, 1, NOW(), NOW()),
('CHECKOUT_STATUS', '1', '待确认', 1, 'warning', 1, 1, NOW(), NOW()),
('CHECKOUT_STATUS', '2', '已退宿', 2, 'success', 1, 1, NOW(), NOW()),
('CHECKOUT_STATUS', '3', '已取消', 3, 'info', 1, 1, NOW(), NOW()),
('CHECKOUT_SOURCE', '1', '手工录入', 1, 'primary', 1, 1, NOW(), NOW()),
('CHECKOUT_SOURCE', '2', 'Excel导入', 2, 'success', 1, 1, NOW(), NOW()),
('CHECKOUT_SOURCE', '3', 'OA同步', 3, 'warning', 1, 1, NOW(), NOW()),
('BILL_STATUS', '1', '未支付', 1, 'warning', 1, 1, NOW(), NOW()),
('BILL_STATUS', '2', '已支付', 2, 'success', 1, 1, NOW(), NOW()),
('BILL_STATUS', '3', '已作废', 3, 'info', 1, 1, NOW(), NOW()),
('BILL_TYPE', '1', '住宿费', 1, 'primary', 1, 1, NOW(), NOW()),
('BILL_TYPE', '2', '电费', 2, 'warning', 1, 1, NOW(), NOW()),
('BILL_TYPE', '3', '水费', 3, 'primary', 1, 1, NOW(), NOW()),
('PAY_METHOD', '1', '现金', 1, 'info', 1, 1, NOW(), NOW()),
('PAY_METHOD', '2', '转账', 2, 'primary', 1, 1, NOW(), NOW()),
('PAY_METHOD', '3', '代扣', 3, 'success', 1, 1, NOW(), NOW()),
('METER_TYPE', '1', '电表', 1, 'warning', 1, 1, NOW(), NOW()),
('METER_TYPE', '2', '水表', 2, 'primary', 1, 1, NOW(), NOW()),
('REPAIR_STATUS', '1', '待受理', 1, 'warning', 1, 1, NOW(), NOW()),
('REPAIR_STATUS', '2', '处理中', 2, 'primary', 1, 1, NOW(), NOW()),
('REPAIR_STATUS', '3', '已完成', 3, 'success', 1, 1, NOW(), NOW()),
('REPAIR_PRIORITY', '1', '普通', 1, 'info', 1, 1, NOW(), NOW()),
('REPAIR_PRIORITY', '2', '紧急', 2, 'danger', 1, 1, NOW(), NOW()),
('ROOM_FACILITY', '空调', '空调', 1, 'primary', 1, 1, NOW(), NOW()),
('ROOM_FACILITY', '热水器', '热水器', 2, 'success', 1, 1, NOW(), NOW()),
('ROOM_FACILITY', '衣柜', '衣柜', 3, 'warning', 1, 1, NOW(), NOW()),
('ROOM_FACILITY', '书桌', '书桌', 4, 'info', 1, 1, NOW(), NOW());

INSERT INTO sys_dict_type (dict_type, dict_name, sort_order, status, system_flag, created_at, updated_at)
VALUES ('INSPECTION_ITEM', STRINGDECODE('\u5de1\u68c0\u9879\u76ee'), 200, 1, 0, NOW(), NOW());

INSERT INTO sys_dict_item (dict_type, dict_value, dict_label, sort_order, tag_type, status, system_flag, created_at, updated_at) VALUES
('INSPECTION_ITEM', 'FIRE', STRINGDECODE('\u6d88\u9632\u8bbe\u65bd'), 1, 'danger', 1, 0, NOW(), NOW()),
('INSPECTION_ITEM', 'ELECTRIC', STRINGDECODE('\u7528\u7535\u5b89\u5168'), 2, 'warning', 1, 0, NOW(), NOW()),
('INSPECTION_ITEM', 'HYGIENE', STRINGDECODE('\u536b\u751f\u72b6\u51b5'), 3, 'success', 1, 0, NOW(), NOW()),
('INSPECTION_ITEM', 'APPLIANCE', STRINGDECODE('\u8fdd\u89c4\u7535\u5668'), 4, 'info', 1, 0, NOW(), NOW());
