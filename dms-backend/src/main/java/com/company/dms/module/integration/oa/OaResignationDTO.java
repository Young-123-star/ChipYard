package com.company.dms.module.integration.oa;

import lombok.Data;

/** OA 离职单推送报文。 */
@Data
public class OaResignationDTO {
    private String resignationNo;   // 离职单号 → biz_no
    private String employeeNo;      // 工号
    private String lastWorkDate;    // 最后工作日 yyyy-MM-dd → 预计退宿日
}
