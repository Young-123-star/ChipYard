package com.company.dms.module.integration.hcp;

import lombok.Data;

/** HCP 新员工推送报文（字段名贴 HCP 侧）。 */
@Data
public class HcpEmployeeDTO {
    private String employeeNo;
    private String name;
    private Integer sex;
    private Integer empType;
    private String department;
    private String mobile;
    private String idCard;
    private String entryDate;
}
