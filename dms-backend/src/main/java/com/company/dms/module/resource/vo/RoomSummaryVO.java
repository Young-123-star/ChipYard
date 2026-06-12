package com.company.dms.module.resource.vo;

import lombok.Data;

/** 房间列表按当前筛选条件的汇总（不分页，覆盖全部匹配项） */
@Data
public class RoomSummaryVO {
    private long total;        // 房间数
    private int totalBeds;     // 床位总数
    private int occupiedBeds;  // 已入住床位
    private int freeBeds;      // 空闲床位
}
