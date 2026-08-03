package com.company.dms.module.fee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UtilityBatchApplyDTO {
    /** 楼栋过滤，为空表示全部楼栋 */
    private Long buildingId;
    /** 房型过滤，为空表示全部房型 */
    private Integer roomType;
    @NotNull(message = "结算方式不能为空")
    private Integer settlementMode;
    @NotNull(message = "用电规则不能为空")
    private Integer electricityRule;
    @NotNull(message = "用水规则不能为空")
    private Integer waterRule;
}
