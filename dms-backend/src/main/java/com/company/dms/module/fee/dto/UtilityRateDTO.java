package com.company.dms.module.fee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UtilityRateDTO {
    @NotNull(message = "电价不能为空")
    private BigDecimal electricityPrice;
    @NotNull(message = "水价不能为空")
    private BigDecimal waterPrice;
}
