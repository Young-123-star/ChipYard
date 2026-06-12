package com.company.dms.module.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuildingSaveDTO {
    @NotBlank(message = "楼栋编码不能为空")
    private String buildingCode;
    @NotBlank(message = "楼栋名称不能为空")
    private String buildingName;
    private String address;
    @NotNull(message = "楼层数不能为空")
    private Integer floorCount;
    private Integer hasElevator = 0;
    private Integer status = 1;
    private String remark;
}
