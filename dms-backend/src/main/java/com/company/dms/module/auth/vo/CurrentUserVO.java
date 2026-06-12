package com.company.dms.module.auth.vo;

import lombok.Data;

@Data
public class CurrentUserVO {
    private Long userId;
    private String username;
    private String realName;
    private Integer gender;
}
