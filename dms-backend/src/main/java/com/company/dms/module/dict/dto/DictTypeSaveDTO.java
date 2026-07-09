package com.company.dms.module.dict.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictTypeSaveDTO {
    @NotBlank
    private String dictType;
    @NotBlank
    private String dictName;
    private Integer sortOrder = 0;
    private Integer status = 1;
    private String remark;
}
