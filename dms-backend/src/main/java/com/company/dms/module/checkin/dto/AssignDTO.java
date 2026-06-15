package com.company.dms.module.checkin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AssignDTO {
    @NotNull(message = "床位不能为空")
    private Long bedId;
    private LocalDate checkinDate;
}
