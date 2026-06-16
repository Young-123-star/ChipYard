package com.company.dms.module.checkout.dto;

import lombok.Data;

@Data
public class CheckoutQuery {
    private Integer status;
    private Integer source;
    private long page = 1;
    private long size = 10;
}
