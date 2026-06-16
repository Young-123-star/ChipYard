package com.company.dms.module.fee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_fee_standard")
public class FeeStandard extends BaseEntity {
    private Long id;
    private Integer roomType;
    private BigDecimal monthlyPrice;
    private String remark;
}
