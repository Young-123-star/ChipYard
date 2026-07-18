package com.company.dms.module.resource.vo;

import com.company.dms.module.resource.entity.Building;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 楼栋 + 实时入住统计（用于列表/卡片展示） */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildingVO extends Building {
    /** 实时统计：房间数 */
    private Integer realRoomCount;
    /** 实时统计：床位数 */
    private Integer realBedCount;
    /** 实时统计：已入住床位 */
    private Integer occupiedBeds;
    private Integer utilityConfiguredRooms;
    private Integer utilityUnconfiguredRooms;
}
