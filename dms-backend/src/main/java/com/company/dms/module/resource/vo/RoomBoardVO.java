package com.company.dms.module.resource.vo;

import lombok.Data;

@Data
public class RoomBoardVO {
    private Long id;
    private Long buildingId;
    private Long floorId;
    private Integer floorNumber;
    private String roomNumber;
    private Integer roomType;
    private Integer bedCount;
    private Integer occupiedBeds;
    private Integer genderLimit; // 0不限 1男 2女
    private Integer status;   // 0停用 1空闲 2已满 3维修中 4预留
    private java.math.BigDecimal area;
    private String orientation;
    private String facilities;
}
