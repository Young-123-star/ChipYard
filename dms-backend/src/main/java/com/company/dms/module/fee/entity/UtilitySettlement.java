package com.company.dms.module.fee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_utility_settlement")
public class UtilitySettlement extends BaseEntity {
    private Long id;
    private Long buildingId;
    private String accountCode;
    private String period;
    private LocalDate cycleStart;
    private LocalDate cycleEnd;
    private BigDecimal electricityPrice;
    private BigDecimal waterPrice;
    private BigDecimal electricityUsage;
    private BigDecimal waterUsage;
    private BigDecimal totalCost;
    private BigDecimal employeeAmount;
    private BigDecimal companyAmount;
    private Integer status;
}
