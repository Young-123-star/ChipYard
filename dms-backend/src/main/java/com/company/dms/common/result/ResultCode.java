package com.company.dms.common.result;

public enum ResultCode {
    SUCCESS(0, "success"),
    PARAM_ERROR(40000, "参数错误"),
    UNAUTHORIZED(40100, "未认证或登录已过期"),
    FORBIDDEN(40300, "无权限"),
    NOT_FOUND(40400, "资源不存在"),
    BIZ_ERROR(50000, "业务处理失败"),
    SYSTEM_ERROR(50001, "系统异常");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
