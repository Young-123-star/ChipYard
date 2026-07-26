package com.company.dms.module.integration.oa;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** OA 入住申请单推送报文（字段名贴 OA 侧；真实对接时按实际调整）。 */
@Data
public class OaCheckinApplicationDTO {
    @NotBlank(message = "申请单号不能为空")
    private String applicationNo;
    @NotBlank(message = "工号不能为空")
    private String employeeNo;
    @NotBlank(message = "姓名不能为空")
    private String employeeName;
    private Integer gender;
    private String expectDate;
    private Integer genderLimit;
    private Integer roomType;
    private Long buildingId;
    private String note;
}
