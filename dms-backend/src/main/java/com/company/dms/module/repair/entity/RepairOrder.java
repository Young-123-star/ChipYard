package com.company.dms.module.repair.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_repair_order")
public class RepairOrder extends BaseEntity {
    private Long id;
    private String orderNo;
    private Long roomId;
    private Long residentId;
    private String title;
    private String description;
    private Integer priority;
    private Integer status;
    private String handler;
    private LocalDateTime acceptedAt;
    private String result;
    private LocalDateTime completedAt;
    private String remark;
}
