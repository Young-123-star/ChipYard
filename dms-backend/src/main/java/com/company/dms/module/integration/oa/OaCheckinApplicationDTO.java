package com.company.dms.module.integration.oa;

import lombok.Data;

/** OA 入住申请单推送报文（字段名贴 OA 侧；真实对接时按实际调整）。 */
@Data
public class OaCheckinApplicationDTO {
    private String applicationNo;
    private String employeeNo;
    private String employeeName;
    private Integer gender;
    private String expectDate;
    private Integer genderLimit;
    private Integer roomType;
    private Long buildingId;
    private String note;
}
