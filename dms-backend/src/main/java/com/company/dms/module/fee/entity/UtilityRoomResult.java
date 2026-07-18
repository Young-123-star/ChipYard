package com.company.dms.module.fee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_utility_room_result")
public class UtilityRoomResult extends BaseEntity {
    private Long id;
    private Long settlementId;
    private Long roomId;
    private Integer utilityType;
    private BigDecimal actualUsage;
    private BigDecimal allowanceUsage;
    private BigDecimal excessUsage;
    private BigDecimal totalCost;
    private BigDecimal employeeAmount;
    private BigDecimal companyAmount;
    private Integer occupantCount;
    private String calculationNote;
}
