package com.company.dms.module.integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "integration")
public class IntegrationProperties {
    private String token;
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
