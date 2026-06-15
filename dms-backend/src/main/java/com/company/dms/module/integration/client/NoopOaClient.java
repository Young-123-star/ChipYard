package com.company.dms.module.integration.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoopOaClient implements OaClient {
    private static final Logger log = LoggerFactory.getLogger(NoopOaClient.class);

    @Override
    public void notifyCheckinCompleted(String bizNo) {
        log.info("[OA-预留] 入住完成回写占位，bizNo={}", bizNo);
    }
}
