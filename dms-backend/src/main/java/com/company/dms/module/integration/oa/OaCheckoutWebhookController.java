package com.company.dms.module.integration.oa;

import com.company.dms.common.result.R;
import com.company.dms.module.checkout.vo.CheckoutResultVO;
import com.company.dms.module.integration.support.IntegrationTokenGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "集成-OA退宿")
@RestController
@RequestMapping("/api/integration/oa")
public class OaCheckoutWebhookController {

    private final IntegrationTokenGuard tokenGuard;
    private final CheckoutApplicationAdapter checkoutAdapter;
    private final ResignationAdapter resignationAdapter;

    public OaCheckoutWebhookController(IntegrationTokenGuard tokenGuard,
                                       CheckoutApplicationAdapter checkoutAdapter,
                                       ResignationAdapter resignationAdapter) {
        this.tokenGuard = tokenGuard;
        this.checkoutAdapter = checkoutAdapter;
        this.resignationAdapter = resignationAdapter;
    }

    @Operation(summary = "OA 退宿申请单推送（需 X-Integration-Token）")
    @PostMapping("/checkout-application")
    public R<CheckoutResultVO> checkoutApplication(@RequestHeader(value = "X-Integration-Token", required = false) String token,
                                                   @RequestBody OaCheckoutApplicationDTO dto) {
        tokenGuard.verify(token);
        return R.ok(checkoutAdapter.handle(dto));
    }

    @Operation(summary = "OA 离职单推送（需 X-Integration-Token）")
    @PostMapping("/resignation")
    public R<CheckoutResultVO> resignation(@RequestHeader(value = "X-Integration-Token", required = false) String token,
                                           @RequestBody OaResignationDTO dto) {
        tokenGuard.verify(token);
        return R.ok(resignationAdapter.handle(dto));
    }
}
