package com.company.dms.module.fee.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UtilityAccountSaveDTO {
    @NotNull(message = "楼栋不能为空")
    private Long buildingId;
    /** 水电账户编号；房间账户(模式2)可留空，默认用房间号 */
    private String accountCode;
    @NotNull(message = "结算方式不能为空")
    private Integer settlementMode;
    private Integer electricityRule = 0;
    private Integer waterRule = 0;
    @NotEmpty(message = "房间列表不能为空")
    private List<Long> roomIds;
}
