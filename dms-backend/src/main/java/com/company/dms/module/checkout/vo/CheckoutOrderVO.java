package com.company.dms.module.checkout.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CheckoutOrderVO {
    private Long id;
    private String bizNo;
    private Long residentId;
    private String residentName;
    private String employeeNo;
    private Long checkinRecordId;
    private Long roomId;
    private Long bedId;
    private Integer source;
    private String reason;
    private LocalDate expectCheckoutDate;
    private Integer status;
}
