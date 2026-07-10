package com.company.dms.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SchemaConstraintTest {

    private Connection connection;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:h2:mem:schema_constraints_" + System.nanoTime()
                        + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
                "sa",
                ""
        );
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource(connection, true);
        new ResourceDatabasePopulator(new ClassPathResource("db/schema.sql")).execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void active_resource_names_are_unique_within_their_scope() {
        jdbcTemplate.update(
                "INSERT INTO dms_building (building_code, building_name, floor_count) VALUES (?, ?, ?)",
                "T1",
                "Test Building",
                1
        );
        assertThrows(DuplicateKeyException.class, () -> jdbcTemplate.update(
                "INSERT INTO dms_building (building_code, building_name, floor_count) VALUES (?, ?, ?)",
                "T1",
                "Duplicate Building",
                1
        ));

        jdbcTemplate.update(
                "INSERT INTO dms_room (building_id, floor_id, room_number, room_type) VALUES (?, ?, ?, ?)",
                1L,
                1L,
                "101",
                1
        );
        assertThrows(DuplicateKeyException.class, () -> jdbcTemplate.update(
                "INSERT INTO dms_room (building_id, floor_id, room_number, room_type) VALUES (?, ?, ?, ?)",
                1L,
                2L,
                "101",
                2
        ));

        jdbcTemplate.update(
                "INSERT INTO dms_bed (room_id, bed_number) VALUES (?, ?)",
                1L,
                "A"
        );
        assertThrows(DuplicateKeyException.class, () -> jdbcTemplate.update(
                "INSERT INTO dms_bed (room_id, bed_number) VALUES (?, ?)",
                1L,
                "A"
        ));
    }

    @Test
    void soft_deleted_resource_names_can_be_reused() {
        jdbcTemplate.update(
                "INSERT INTO dms_building (id, building_code, building_name, floor_count, deleted_at) VALUES (?, ?, ?, ?, NOW())",
                10L,
                "T2",
                "Deleted Building",
                1
        );
        assertDoesNotThrow(() -> jdbcTemplate.update(
                "INSERT INTO dms_building (building_code, building_name, floor_count) VALUES (?, ?, ?)",
                "T2",
                "Active Building",
                1
        ));

        jdbcTemplate.update(
                "INSERT INTO dms_room (id, building_id, floor_id, room_number, room_type, deleted_at) VALUES (?, ?, ?, ?, ?, NOW())",
                10L,
                1L,
                1L,
                "102",
                1
        );
        assertDoesNotThrow(() -> jdbcTemplate.update(
                "INSERT INTO dms_room (building_id, floor_id, room_number, room_type) VALUES (?, ?, ?, ?)",
                1L,
                2L,
                "102",
                2
        ));

        jdbcTemplate.update(
                "INSERT INTO dms_bed (id, room_id, bed_number, deleted_at) VALUES (?, ?, ?, NOW())",
                10L,
                1L,
                "B"
        );
        assertDoesNotThrow(() -> jdbcTemplate.update(
                "INSERT INTO dms_bed (room_id, bed_number) VALUES (?, ?)",
                1L,
                "B"
        ));
    }

    @Test
    void inspection_plan_date_is_unique() {
        jdbcTemplate.update(
                "INSERT INTO dms_inspection_plan (id, plan_name, cycle_type, target_type, target_id, target_name, inspector, items_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                100L, "Plan", 1, 1, 1L, "Building", "Admin", "[]"
        );
        jdbcTemplate.update(
                "INSERT INTO dms_inspection_task (task_no, plan_id, plan_name, target_type, target_id, target_name, inspector, planned_date, items_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "IT-1", 100L, "Plan", 1, 1L, "Building", "Admin", "2026-08-01", "[]"
        );
        assertThrows(DuplicateKeyException.class, () -> jdbcTemplate.update(
                "INSERT INTO dms_inspection_task (task_no, plan_id, plan_name, target_type, target_id, target_name, inspector, planned_date, items_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "IT-2", 100L, "Plan", 1, 1L, "Building", "Admin", "2026-08-01", "[]"
        ));
    }
}