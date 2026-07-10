package com.company.dms.module.inspection.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_inspection_task")
public class InspectionTask extends BaseEntity {
    private Long id;
    private String taskNo;
    private Long planId;
    private String planName;
    private Integer targetType;
    private Long targetId;
    private String targetName;
    private String inspector;
    private LocalDate plannedDate;
    private String itemsJson;
    private String resultsJson;
    private Integer status;
    private LocalDateTime completedAt;
    private String rectificationNote;
    private LocalDateTime rectifiedAt;
}
