UPDATE sys_dict_item
SET dict_label = '电费', tag_type = 'warning', updated_at = NOW()
WHERE dict_type = 'BILL_TYPE' AND dict_value = '2' AND deleted_at IS NULL;

INSERT INTO sys_dict_item
    (dict_type, dict_value, dict_label, sort_order, tag_type, status, system_flag, created_at, updated_at)
SELECT 'BILL_TYPE', '3', '水费', 3, 'primary', 1, 1, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_item
    WHERE dict_type = 'BILL_TYPE' AND dict_value = '3' AND deleted_at IS NULL
);
