package com.company.dms.module.repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepairCreateDTO {
    @NotNull
    private Long roomId;
    private Long residentId;
    @NotBlank
    private String title;
    private String description;
    private Integer priority;
    private String remark;
}
