package com.company.dms.module.resource.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FloorSaveDTO {
    @NotNull(message = "所属楼栋不能为空")
    private Long buildingId;
    @NotNull(message = "楼层号不能为空")
    private Integer floorNumber;
    private String floorName;
    private Integer status = 1;
}
