package com.company.dms.module.integration.support;

import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.integration.config.IntegrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Component
public class IntegrationTokenGuard {

    private final IntegrationProperties properties;

    public IntegrationTokenGuard(IntegrationProperties properties) {
        this.properties = properties;
    }

    public void verify(String token) {
        String expected = properties.getToken();
        boolean ok = expected != null && token != null
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                                         token.getBytes(StandardCharsets.UTF_8));
        if (!ok) {
            log.warn("集成令牌校验失败，请求路径：{}", currentPath());
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "集成令牌校验失败");
        }
    }

    private String currentPath() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? "unknown" : attrs.getRequest().getRequestURI();
    }
}
