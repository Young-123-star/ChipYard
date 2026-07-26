-- 业务单号唯一约束与常用查询索引；同步修正来源字典编码

-- 入住意向单 / 退宿单 biz_no 唯一（webhook 幂等回查依赖该字段）
CREATE UNIQUE INDEX uk_dms_checkin_intake_biz_no ON dms_checkin_intake (biz_no);
CREATE UNIQUE INDEX uk_dms_checkout_order_biz_no ON dms_checkout_order (biz_no);

-- 维修工单 order_no 唯一
CREATE UNIQUE INDEX uk_dms_repair_order_no ON dms_repair_order (order_no);

-- 账单 / 入住档案 / 抄表常用查询索引
CREATE INDEX idx_fee_bill_period_status ON dms_fee_bill (period, status);
CREATE INDEX idx_fee_bill_checkin_status ON dms_fee_bill (checkin_record_id, status);
CREATE INDEX idx_checkin_record_resident_status ON dms_checkin_record (resident_id, status);
CREATE INDEX idx_checkin_record_room_status ON dms_checkin_record (room_id, status);
CREATE INDEX idx_meter_room_period_type ON dms_meter_reading (room_id, period, meter_type);

-- 来源字典统一编码：1=OA申请、2=HCP同步、3=手工录入、4=Excel导入
UPDATE sys_dict_item SET dict_label = 'OA申请', sort_order = 1, tag_type = 'warning', updated_at = NOW()
WHERE dict_type = 'INTAKE_SOURCE' AND dict_value = '1' AND deleted_at IS NULL;
UPDATE sys_dict_item SET dict_label = 'HCP同步', sort_order = 2, tag_type = 'info', updated_at = NOW()
WHERE dict_type = 'INTAKE_SOURCE' AND dict_value = '2' AND deleted_at IS NULL;
UPDATE sys_dict_item SET dict_label = '手工录入', sort_order = 3, tag_type = 'primary', updated_at = NOW()
WHERE dict_type = 'INTAKE_SOURCE' AND dict_value = '3' AND deleted_at IS NULL;
INSERT INTO sys_dict_item (dict_type, dict_value, dict_label, sort_order, tag_type, status, system_flag, created_at, updated_at)
SELECT 'INTAKE_SOURCE', '4', 'Excel导入', 4, 'success', 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_item WHERE dict_type = 'INTAKE_SOURCE' AND dict_value = '4' AND deleted_at IS NULL);

UPDATE sys_dict_item SET dict_label = 'OA申请', sort_order = 1, tag_type = 'warning', updated_at = NOW()
WHERE dict_type = 'CHECKOUT_SOURCE' AND dict_value = '1' AND deleted_at IS NULL;
UPDATE sys_dict_item SET dict_label = 'HCP同步', sort_order = 2, tag_type = 'info', updated_at = NOW()
WHERE dict_type = 'CHECKOUT_SOURCE' AND dict_value = '2' AND deleted_at IS NULL;
UPDATE sys_dict_item SET dict_label = '手工录入', sort_order = 3, tag_type = 'primary', updated_at = NOW()
WHERE dict_type = 'CHECKOUT_SOURCE' AND dict_value = '3' AND deleted_at IS NULL;
INSERT INTO sys_dict_item (dict_type, dict_value, dict_label, sort_order, tag_type, status, system_flag, created_at, updated_at)
SELECT 'CHECKOUT_SOURCE', '4', 'Excel导入', 4, 'success', 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_item WHERE dict_type = 'CHECKOUT_SOURCE' AND dict_value = '4' AND deleted_at IS NULL);
