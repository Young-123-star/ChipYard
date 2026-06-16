package com.company.dms.module.fee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayBillDTO {
    @NotNull(message = "缴费方式不能为空")
    private Integer payMethod;   // 1现金 2转账
}
