package com.company.dms.module.fee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_utility_rate")
public class UtilityRate extends BaseEntity {
    private Long id;
    private BigDecimal electricityPrice;
    private BigDecimal waterPrice;
    /** 电免额(度/间/月) */
    private BigDecimal electricAllowance;
    /** 户级水免额(吨/户/月) */
    private BigDecimal householdWaterAllowance;
    /** 房间水免额(吨/间/月) */
    private BigDecimal roomWaterAllowance;
    /** 结算周期起日(上月) */
    private Integer cycleStartDay;
    /** 结算周期止日(本月) */
    private Integer cycleEndDay;
}
