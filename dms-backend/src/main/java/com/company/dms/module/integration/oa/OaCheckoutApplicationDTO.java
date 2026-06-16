package com.company.dms.module.integration.oa;

import lombok.Data;

/** OA 退宿申请单推送报文。 */
@Data
public class OaCheckoutApplicationDTO {
    private String applicationNo;   // 申请单号 → biz_no
    private String employeeNo;      // 工号
    private String reason;          // 退宿原因
    private String expectDate;      // 预计退宿日 yyyy-MM-dd
}
