package com.company.dms.module.integration.hcp;

import com.company.dms.common.result.R;
import com.company.dms.module.integration.support.IntegrationTokenGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "集成-HCP")
@RestController
@RequestMapping("/api/integration/hcp")
public class HcpWebhookController {

    private final IntegrationTokenGuard tokenGuard;
    private final HcpEmployeeAdapter adapter;

    public HcpWebhookController(IntegrationTokenGuard tokenGuard, HcpEmployeeAdapter adapter) {
        this.tokenGuard = tokenGuard;
        this.adapter = adapter;
    }

    @Operation(summary = "HCP 新员工推送（需 X-Integration-Token）")
    @PostMapping("/employee")
    public R<Long> employee(@RequestHeader(value = "X-Integration-Token", required = false) String token,
                            @RequestBody HcpEmployeeDTO dto) {
        tokenGuard.verify(token);
        return R.ok(adapter.handle(dto));
    }
}
