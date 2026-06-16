package com.company.dms.module.checkout.vo;

import lombok.Data;

@Data
public class CheckoutResultVO {
    private String outcome;          // NO_RESIDENT / NO_ACTIVE_CHECKIN / RESIGNED_NO_CHECKIN / ORDER_CREATED
    private Long orderId;
    private boolean residentResigned;
    private String message;

    public static CheckoutResultVO of(String outcome, Long orderId, boolean residentResigned, String message) {
        CheckoutResultVO vo = new CheckoutResultVO();
        vo.setOutcome(outcome);
        vo.setOrderId(orderId);
        vo.setResidentResigned(residentResigned);
        vo.setMessage(message);
        return vo;
    }
}
