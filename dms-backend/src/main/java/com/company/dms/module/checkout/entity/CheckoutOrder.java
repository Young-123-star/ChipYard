package com.company.dms.module.checkout.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_checkout_order")
public class CheckoutOrder extends BaseEntity {
    private Long id;
    private String bizNo;
    private Long residentId;
    private Long checkinRecordId;
    private Integer source;
    private String reason;
    private LocalDate expectCheckoutDate;
    private Integer status;
    private String rawPayload;
}
