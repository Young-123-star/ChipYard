package com.company.dms.module.fee.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ArrearsRankVO {
    private Long residentId;
    private String residentName;
    private String employeeNo;
    private BigDecimal unpaidAmount;
    private int unpaidCount;
}
