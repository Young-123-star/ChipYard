package com.company.dms.module.fee.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MeterReadingVO {
    private Long id;
    private Long roomId;
    private String roomNumber;
    private String period;
    private Integer meterType;
    private BigDecimal prevReading;
    private BigDecimal currentReading;
    private BigDecimal consumption;
    private BigDecimal unitPrice;
    private BigDecimal amount;
}
