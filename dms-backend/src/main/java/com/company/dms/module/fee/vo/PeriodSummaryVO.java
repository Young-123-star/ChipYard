package com.company.dms.module.fee.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PeriodSummaryVO {
    private String period;
    private BigDecimal rentTotal;
    private BigDecimal elecTotal;
    private BigDecimal waterTotal;
    private BigDecimal total;
    private BigDecimal paid;
    private BigDecimal unpaid;
    private BigDecimal collectRate;
}
