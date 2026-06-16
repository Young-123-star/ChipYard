package com.company.dms.module.fee.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateBillsDTO {
    @NotBlank(message = "账期不能为空")
    private String period;   // YYYY-MM
}
