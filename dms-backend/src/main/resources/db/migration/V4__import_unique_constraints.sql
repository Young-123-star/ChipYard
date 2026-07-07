ALTER TABLE dms_floor
    ADD COLUMN active_unique_key TINYINT
        GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END) STORED;

CREATE UNIQUE INDEX uk_dms_floor_building_number_active
    ON dms_floor (building_id, floor_number, active_unique_key);

ALTER TABLE dms_resident
    ADD COLUMN active_unique_key TINYINT
        GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END) STORED;

CREATE UNIQUE INDEX uk_dms_resident_employee_active
    ON dms_resident (employee_no, active_unique_key);
