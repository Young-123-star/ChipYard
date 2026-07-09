package com.company.dms.module.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_item")
public class DictItem extends BaseEntity {
    private Long id;
    private String dictType;
    private String dictValue;
    private String dictLabel;
    private Integer sortOrder;
    private String tagType;
    private Integer status;
    private Integer systemFlag;
    private String remark;
}
