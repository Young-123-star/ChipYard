package com.company.dms.module.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RoomSaveDTO {
    @NotNull(message = "所属楼栋不能为空")
    private Long buildingId;
    @NotNull(message = "所属楼层不能为空")
    private Long floorId;
    @NotBlank(message = "房间号不能为空")
    private String roomNumber;
    @NotNull(message = "房型不能为空")
    private Integer roomType;
    private BigDecimal area;
    private String orientation;
    @NotNull(message = "床位数不能为空")
    private Integer bedCount;
    private String facilities;
    private Integer genderLimit = 0;
    private Integer status = 1;
    private String remark;
}
