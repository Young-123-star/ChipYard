package com.company.dms.module.integration.support;

import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.integration.config.IntegrationProperties;
import org.springframework.stereotype.Component;

@Component
public class IntegrationTokenGuard {

    private final IntegrationProperties properties;

    public IntegrationTokenGuard(IntegrationProperties properties) {
        this.properties = properties;
    }

    public void verify(String token) {
        if (properties.getToken() == null || !properties.getToken().equals(token)) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "集成令牌校验失败");
        }
    }
}
