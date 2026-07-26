package com.company.dms.module.integration.hcp;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** HCP 新员工推送报文（字段名贴 HCP 侧）。 */
@Data
public class HcpEmployeeDTO {
    @NotBlank(message = "工号不能为空")
    private String employeeNo;
    @NotBlank(message = "姓名不能为空")
    private String name;
    private Integer sex;
    private Integer empType;
    private String department;
    private String mobile;
    private String idCard;
    private String entryDate;
}
