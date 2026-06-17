package com.company.dms.module.fee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MeterReadingDTO {
    @NotNull(message = "房间不能为空")
    private Long roomId;
    @NotNull(message = "账期不能为空")
    private String period;       // YYYY-MM
    @NotNull(message = "表类型不能为空")
    private Integer meterType;   // 1电 2水
    @NotNull(message = "本期读数不能为空")
    private BigDecimal currentReading;
}
