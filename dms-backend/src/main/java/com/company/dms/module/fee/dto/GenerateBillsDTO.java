package com.company.dms.module.fee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GenerateBillsDTO {
    @NotBlank(message = "账期不能为空")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "账期格式应为 yyyy-MM")
    private String period;   // YYYY-MM
}
