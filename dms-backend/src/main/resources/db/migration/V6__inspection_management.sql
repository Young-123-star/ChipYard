CREATE TABLE IF NOT EXISTS dms_inspection_plan (
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

CREATE TABLE IF NOT EXISTS dms_inspection_task (
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
    UNIQUE KEY uk_inspection_task_plan_date (plan_id, planned_date)
);

INSERT INTO sys_dict_type (dict_type, dict_name, sort_order, status, system_flag, created_at, updated_at)
SELECT 'INSPECTION_ITEM', '巡检项目', 200, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'INSPECTION_ITEM' AND deleted_at IS NULL);

INSERT INTO sys_dict_item (dict_type, dict_value, dict_label, sort_order, tag_type, status, system_flag, created_at, updated_at)
SELECT 'INSPECTION_ITEM', '消防设施', '消防设施', 1, 'danger', 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_item WHERE dict_type = 'INSPECTION_ITEM' AND dict_value = '消防设施' AND deleted_at IS NULL);
INSERT INTO sys_dict_item (dict_type, dict_value, dict_label, sort_order, tag_type, status, system_flag, created_at, updated_at)
SELECT 'INSPECTION_ITEM', '用电安全', '用电安全', 2, 'warning', 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_item WHERE dict_type = 'INSPECTION_ITEM' AND dict_value = '用电安全' AND deleted_at IS NULL);
INSERT INTO sys_dict_item (dict_type, dict_value, dict_label, sort_order, tag_type, status, system_flag, created_at, updated_at)
SELECT 'INSPECTION_ITEM', '卫生状况', '卫生状况', 3, 'success', 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_item WHERE dict_type = 'INSPECTION_ITEM' AND dict_value = '卫生状况' AND deleted_at IS NULL);
INSERT INTO sys_dict_item (dict_type, dict_value, dict_label, sort_order, tag_type, status, system_flag, created_at, updated_at)
SELECT 'INSPECTION_ITEM', '违规电器', '违规电器', 4, 'info', 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_item WHERE dict_type = 'INSPECTION_ITEM' AND dict_value = '违规电器' AND deleted_at IS NULL);
