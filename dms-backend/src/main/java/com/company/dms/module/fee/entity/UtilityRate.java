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
}
