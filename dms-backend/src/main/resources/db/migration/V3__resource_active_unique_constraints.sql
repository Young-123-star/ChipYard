ALTER TABLE dms_building
    ADD COLUMN active_unique_key TINYINT
        GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END) STORED;

CREATE UNIQUE INDEX uk_dms_building_code_active
    ON dms_building (building_code, active_unique_key);

ALTER TABLE dms_room
    ADD COLUMN active_unique_key TINYINT
        GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END) STORED;

CREATE UNIQUE INDEX uk_dms_room_building_number_active
    ON dms_room (building_id, room_number, active_unique_key);

ALTER TABLE dms_bed
    ADD COLUMN active_unique_key TINYINT
        GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN 1 ELSE NULL END) STORED;

CREATE UNIQUE INDEX uk_dms_bed_room_number_active
    ON dms_bed (room_id, bed_number, active_unique_key);
