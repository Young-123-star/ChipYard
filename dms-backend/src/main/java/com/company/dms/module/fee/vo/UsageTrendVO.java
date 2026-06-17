package com.company.dms.module.fee.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UsageTrendVO {
    private String period;
    private BigDecimal electricity;
    private BigDecimal water;
}
