package com.company.dms.module.resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_room")
public class Room extends BaseEntity {
    private Long id;
    private Long buildingId;
    private Long floorId;
    private String roomNumber;
    private Integer roomType;
    private BigDecimal area;
    private String orientation;
    private Integer bedCount;
    private Integer occupiedBeds;
    private String facilities;
    private Integer genderLimit;
    private Integer status;
    private String remark;
}
