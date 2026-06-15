package com.company.dms.module.resident.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_resident")
public class Resident extends BaseEntity {
    private Long id;
    private String employeeNo;
    private String realName;
    private Integer gender;
    private Integer residentType;
    private String deptName;
    private String phone;
    private String idCard;
    private Integer source;
    private Integer status;
}
