package com.company.dms.module.dict.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictItemSaveDTO {
    @NotBlank
    private String dictType;
    @NotBlank
    private String dictValue;
    @NotBlank
    private String dictLabel;
    private Integer sortOrder = 0;
    private String tagType;
    private Integer status = 1;
    private String remark;
}
