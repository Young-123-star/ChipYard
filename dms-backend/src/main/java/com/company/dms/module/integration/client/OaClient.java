package com.company.dms.module.integration.client;

public interface OaClient {
    /** 入住完成后回写 OA（预留，真实对接时实现）。 */
    void notifyCheckinCompleted(String bizNo);

    /** 退宿完成后回写 OA（预留）。 */
    void notifyCheckoutCompleted(String bizNo);
}
