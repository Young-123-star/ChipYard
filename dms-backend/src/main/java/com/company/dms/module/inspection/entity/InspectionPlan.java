package com.company.dms.module.inspection.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_inspection_plan")
public class InspectionPlan extends BaseEntity {
    private Long id;
    private String planName;
    private Integer cycleType;
    private Integer targetType;
    private Long targetId;
    private String targetName;
    private String inspector;
    private String itemsJson;
    private Integer status;
    private String remark;
}
