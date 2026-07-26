package com.company.dms.module.fee.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UtilityRateDTO {
    @NotNull(message = "电价不能为空")
    @DecimalMin(value = "0", message = "电价不能为负")
    private BigDecimal electricityPrice;
    @NotNull(message = "水价不能为空")
    @DecimalMin(value = "0", message = "水价不能为负")
    private BigDecimal waterPrice;
}
