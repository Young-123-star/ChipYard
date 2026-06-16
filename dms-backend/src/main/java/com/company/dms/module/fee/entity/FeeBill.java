package com.company.dms.module.fee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_fee_bill")
public class FeeBill extends BaseEntity {
    private Long id;
    private String billNo;
    private Long checkinRecordId;
    private Long residentId;
    private Long roomId;
    private String period;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime paidAt;
    private Integer payMethod;
}
