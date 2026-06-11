package com.company.dms.module.resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_building")
public class Building extends BaseEntity {
    private Long id;
    private String buildingCode;
    private String buildingName;
    private String address;
    private Integer floorCount;
    private Integer hasElevator;
    private Integer totalRooms;
    private Integer totalBeds;
    private Integer status;
    private String remark;
}
