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
