package com.company.dms.module.checkout.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CheckoutConfirmDTO {
    /** 为空时后端默认当天 */
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "退宿日期格式须为 YYYY-MM-DD")
    private String checkoutDate;
}
