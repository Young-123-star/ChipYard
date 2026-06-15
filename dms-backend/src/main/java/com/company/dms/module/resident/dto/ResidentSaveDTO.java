package com.company.dms.module.resident.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResidentSaveDTO {
    @NotBlank(message = "工号不能为空")
    private String employeeNo;
    @NotBlank(message = "姓名不能为空")
    private String realName;
    private Integer gender;
    private Integer residentType;
    private String deptName;
    private String phone;
    private String idCard;
    private Integer status;
}
