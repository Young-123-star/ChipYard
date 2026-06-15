package com.company.dms.module.integration.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoopHcpClient implements HcpClient {
    private static final Logger log = LoggerFactory.getLogger(NoopHcpClient.class);

    @Override
    public void ping() {
        log.info("[HCP-预留] ping 占位");
    }
}
