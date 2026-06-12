package com.company.dms.common.exception;

import com.company.dms.common.result.ResultCode;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final int code;

    public BizException(String message) {
        super(message);
        this.code = ResultCode.BIZ_ERROR.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
