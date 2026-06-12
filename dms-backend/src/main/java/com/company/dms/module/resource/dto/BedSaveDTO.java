package com.company.dms.module.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BedSaveDTO {
    @NotNull(message = "所属房间不能为空")
    private Long roomId;
    @NotBlank(message = "床位编号不能为空")
    private String bedNumber;
    private Integer bedType = 3;
    private Integer status = 1;
}
