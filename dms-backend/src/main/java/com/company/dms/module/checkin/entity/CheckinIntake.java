package com.company.dms.module.checkin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_checkin_intake")
public class CheckinIntake extends BaseEntity {
    private Long id;
    private String bizNo;
    private Long residentId;
    private Integer source;
    private LocalDate expectCheckinDate;
    private Integer genderLimitReq;
    private Integer roomTypeReq;
    private Long buildingIdReq;
    private String remark;
    private Integer status;
    private String rawPayload;
}
