package com.company.dms.module.fee.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UtilityRateDTO {
    @NotNull(message = "电价不能为空")
    @DecimalMin(value = "0", message = "电价不能为负")
    private BigDecimal electricityPrice;
    @NotNull(message = "水价不能为空")
    @DecimalMin(value = "0", message = "水价不能为负")
    private BigDecimal waterPrice;
    /** 电免额(度/间/月)，为空表示不修改 */
    private BigDecimal electricAllowance;
    /** 户级水免额(吨/户/月)，为空表示不修改 */
    private BigDecimal householdWaterAllowance;
    /** 房间水免额(吨/间/月)，为空表示不修改 */
    private BigDecimal roomWaterAllowance;
    /** 结算周期起日(上月)，为空表示不修改 */
    private Integer cycleStartDay;
    /** 结算周期止日(本月)，为空表示不修改 */
    private Integer cycleEndDay;
}
