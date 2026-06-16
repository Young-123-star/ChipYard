package com.company.dms.module.fee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class FeeStandardDTO {
    @NotNull(message = "房型不能为空")
    private Integer roomType;
    @NotNull(message = "月单价不能为空")
    private BigDecimal monthlyPrice;
    private String remark;
}
