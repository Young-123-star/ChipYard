package com.company.dms.module.checkin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_checkin_record")
public class CheckinRecord extends BaseEntity {
    private Long id;
    private Long intakeId;
    private Long residentId;
    private Long buildingId;
    private Long floorId;
    private Long roomId;
    private Long bedId;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private Integer status;
}
