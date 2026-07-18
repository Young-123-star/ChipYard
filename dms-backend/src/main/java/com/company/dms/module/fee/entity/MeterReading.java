package com.company.dms.module.fee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_meter_reading")
public class MeterReading extends BaseEntity {
    private Long id;
    private Long buildingId;
    private String accountCode;
    private Integer targetType;
    private Long roomId;
    private String period;
    private Integer meterType;
    private BigDecimal prevReading;
    private BigDecimal currentReading;
    private BigDecimal consumption;
    private BigDecimal unitPrice;
    private BigDecimal amount;
}
