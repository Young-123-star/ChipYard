package com.company.dms.module.resource.dto;

import lombok.Data;

@Data
public class RoomQuery {
    private Long buildingId;
    private Long floorId;
    private Integer roomType;
    private Integer status;
    private long page = 1;
    private long size = 10;
}
