package com.company.dms.module.repair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RepairCreateDTO {
    private Long roomId;
    private Long residentId;
    /** 按房号（roomCode）报修时必传，用于在同楼栋内定位房间。 */
    private Long buildingId;
    private String roomCode;
    private String residentCode;
    @NotBlank
    private String title;
    private String description;
    private Integer priority;
    private String remark;
}
