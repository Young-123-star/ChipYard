package com.company.dms.module.repair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RepairAcceptDTO {
    @NotBlank
    private String handler;
}
