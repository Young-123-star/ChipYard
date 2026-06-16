package com.company.dms.module.checkout.controller;

import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.R;
import com.company.dms.module.checkout.dto.CheckoutCreateDTO;
import com.company.dms.module.checkout.dto.CheckoutQuery;
import com.company.dms.module.checkout.service.CheckoutService;
import com.company.dms.module.checkout.vo.CheckoutOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "退宿管理")
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @Operation(summary = "退宿单分页列表")
    @GetMapping("/orders")
    public R<PageResult<CheckoutOrderVO>> orders(CheckoutQuery query) {
        return R.ok(checkoutService.pageOrders(query));
    }

    @Operation(summary = "手工新建退宿单")
    @PostMapping("/orders")
    public R<Long> create(@Valid @RequestBody CheckoutCreateDTO dto) {
        return R.ok(checkoutService.createManual(dto));
    }

    @Operation(summary = "办理退宿")
    @PostMapping("/orders/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        LocalDate date = (body != null && body.get("checkoutDate") != null && !body.get("checkoutDate").isBlank())
                ? LocalDate.parse(body.get("checkoutDate")) : null;
        checkoutService.confirm(id, date);
        return R.ok();
    }

    @Operation(summary = "取消退宿单")
    @PostMapping("/orders/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        checkoutService.cancel(id);
        return R.ok();
    }
}
