package com.company.dms.module.integration.oa;

import com.company.dms.common.result.R;
import com.company.dms.module.integration.support.IntegrationTokenGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "集成-OA")
@RestController
@RequestMapping("/api/integration/oa")
public class OaWebhookController {

    private final IntegrationTokenGuard tokenGuard;
    private final OaApplicationAdapter adapter;

    public OaWebhookController(IntegrationTokenGuard tokenGuard, OaApplicationAdapter adapter) {
        this.tokenGuard = tokenGuard;
        this.adapter = adapter;
    }

    @Operation(summary = "OA 入住申请单推送（需 X-Integration-Token）")
    @PostMapping("/checkin-application")
    public R<Long> checkinApplication(@RequestHeader(value = "X-Integration-Token", required = false) String token,
                                      @RequestBody OaCheckinApplicationDTO dto) {
        tokenGuard.verify(token);
        return R.ok(adapter.handle(dto));
    }
}
