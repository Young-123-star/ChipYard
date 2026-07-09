package com.company.dms.module.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class DictType extends BaseEntity {
    private Long id;
    private String dictType;
    private String dictName;
    private Integer sortOrder;
    private Integer status;
    private Integer systemFlag;
    private String remark;
}
