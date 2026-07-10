package com.company.dms.module.inspection.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class InspectionModels {
    private InspectionModels() {}

    @Data public static class PlanQuery {
        private Integer status;
        private Integer cycleType;
        private Integer targetType;
        private long page = 1;
        private long size = 10;
    }

    @Data public static class TaskQuery {
        private Integer status;
        private Long planId;
        private Integer targetType;
        private LocalDate plannedDate;
        private long page = 1;
        private long size = 10;
    }

    @Data public static class PlanSave {
        @NotBlank private String planName;
        @NotNull private Integer cycleType;
        @NotNull private Integer targetType;
        @NotNull private Long targetId;
        @NotBlank private String inspector;
        @NotEmpty private List<@NotBlank String> items;
        private String remark;
    }

    @Data public static class StatusChange {
        @NotNull private Integer status;
    }

    @Data public static class TaskGenerate {
        @NotNull private LocalDate plannedDate;
        private String inspector;
    }

    @Data public static class ItemResult {
        @NotBlank private String item;
        @NotNull private Boolean passed;
        private String note;
    }

    @Data public static class TaskExecute {
        @NotEmpty private List<@Valid ItemResult> items;
    }

    @Data public static class TaskRectify {
        @NotBlank private String note;
    }

    @Data public static class PlanVO {
        private Long id;
        private String planName;
        private Integer cycleType;
        private Integer targetType;
        private Long targetId;
        private String targetName;
        private String inspector;
        private List<String> items;
        private Integer status;
        private String remark;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data public static class TaskVO {
        private Long id;
        private String taskNo;
        private Long planId;
        private String planName;
        private Integer targetType;
        private Long targetId;
        private String targetName;
        private String inspector;
        private LocalDate plannedDate;
        private List<String> items;
        private List<ItemResult> results;
        private Integer status;
        private LocalDateTime completedAt;
        private String rectificationNote;
        private LocalDateTime rectifiedAt;
        private LocalDateTime createdAt;
    }
}
