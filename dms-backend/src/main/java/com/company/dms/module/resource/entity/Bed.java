package com.company.dms.module.resource.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.dms.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dms_bed")
public class Bed extends BaseEntity {
    private Long id;
    private Long roomId;
    private String bedNumber;
    private Integer bedType;
    @TableField(value = "current_user_id", updateStrategy = FieldStrategy.ALWAYS)
    private Long currentUserId;
    private Integer status;
}
