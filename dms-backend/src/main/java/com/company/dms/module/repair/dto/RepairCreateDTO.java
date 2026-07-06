package com.company.dms.module.repair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RepairCreateDTO {
    private Long roomId;
    private Long residentId;
    private String roomCode;
    private String residentCode;
    @NotBlank
    private String title;
    private String description;
    private Integer priority;
    private String remark;
}
