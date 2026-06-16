package com.company.dms.module.fee.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ArrearsVO {
    private int count;
    private BigDecimal totalAmount;

    public static ArrearsVO of(int count, BigDecimal totalAmount) {
        ArrearsVO vo = new ArrearsVO();
        vo.setCount(count);
        vo.setTotalAmount(totalAmount);
        return vo;
    }
}
