package com.company.dms.module.fee.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FeeBillVO {
    private Long id;
    private String billNo;
    private Long checkinRecordId;
    private Long residentId;
    private String residentName;
    private String employeeNo;
    private Long roomId;
    private String roomNumber;
    private Integer roomType;
    private String period;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime paidAt;
    private Integer payMethod;
    private Integer billType;
    private String remark;
}
