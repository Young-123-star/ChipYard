package com.company.dms.module.fee.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BuildingSummaryVO {
    private Long buildingId;
    private String buildingName;
    private BigDecimal total;
    private BigDecimal paid;
    private BigDecimal unpaid;
    private BigDecimal collectRate;
}
