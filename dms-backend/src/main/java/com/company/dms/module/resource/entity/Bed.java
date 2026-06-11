package com.company.dms.module.resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_bed")
public class Bed extends BaseEntity {
    private Long id;
    private Long roomId;
    private String bedNumber;
    private Integer bedType;
    private Long currentUserId;
    private Integer status;
}
