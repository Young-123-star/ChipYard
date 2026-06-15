package com.company.dms.module.checkin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class IntakeCreateDTO {
    @NotNull(message = "居住人不能为空")
    private Long residentId;
    private LocalDate expectCheckinDate;
    private Integer genderLimitReq;
    private Integer roomTypeReq;
    private Long buildingIdReq;
    private String remark;
}
