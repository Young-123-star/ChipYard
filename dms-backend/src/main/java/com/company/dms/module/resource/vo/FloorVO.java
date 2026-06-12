package com.company.dms.module.resource.vo;

import lombok.Data;

@Data
public class FloorVO {
    private Long id;
    private Long buildingId;
    private Integer floorNumber;
    private String floorName;
    private Integer status;
    /** 实时统计：该层房间数 */
    private Integer roomCount;
    /** 实时统计：该层床位数（房间床位数之和） */
    private Integer bedCount;
    /** 实时统计：该层已入住床位数 */
    private Integer occupiedBeds;
}
