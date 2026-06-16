package com.company.dms.module.checkout.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CheckoutCreateDTO {
    @NotNull(message = "居住人不能为空")
    private Long residentId;
    private String reason;
    private LocalDate expectCheckoutDate;
}
