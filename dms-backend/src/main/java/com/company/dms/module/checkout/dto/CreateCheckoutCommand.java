package com.company.dms.module.checkout.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateCheckoutCommand {
    private String bizNo;
    private Integer source;            // 1退宿申请 2离职 3手工
    private Long residentId;
    private Long checkinRecordId;
    private String reason;
    private LocalDate expectCheckoutDate;
    private String rawPayload;
}
