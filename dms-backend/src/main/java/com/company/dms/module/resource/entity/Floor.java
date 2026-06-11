package com.company.dms.module.resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_floor")
public class Floor extends BaseEntity {
    private Long id;
    private Long buildingId;
    private Integer floorNumber;
    private String floorName;
    private Integer roomCount;
    private Integer bedCount;
    private Integer status;
}
