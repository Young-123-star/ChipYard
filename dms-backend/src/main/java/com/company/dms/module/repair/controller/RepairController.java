package com.company.dms.module.repair.controller;

import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.R;
import com.company.dms.module.repair.dto.RepairAcceptDTO;
import com.company.dms.module.repair.dto.RepairCompleteDTO;
import com.company.dms.module.repair.dto.RepairCreateDTO;
import com.company.dms.module.repair.dto.RepairQuery;
import com.company.dms.module.repair.service.RepairService;
import com.company.dms.module.repair.vo.RepairOrderVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repair")
public class RepairController {

    private final RepairService repairService;

    public RepairController(RepairService repairService) {
        this.repairService = repairService;
    }

    @GetMapping("/orders")
    public R<PageResult<RepairOrderVO>> orders(RepairQuery query) {
        return R.ok(repairService.pageOrders(query));
    }

    @GetMapping("/orders/{id}")
    public R<RepairOrderVO> detail(@PathVariable Long id) {
        return R.ok(repairService.getDetail(id));
    }

    @PostMapping("/orders")
    public R<Long> create(@Valid @RequestBody RepairCreateDTO dto) {
        return R.ok(repairService.create(dto));
    }

    @PostMapping("/orders/{id}/accept")
    public R<Void> accept(@PathVariable Long id, @Valid @RequestBody RepairAcceptDTO dto) {
        repairService.accept(id, dto);
        return R.ok();
    }

    @PostMapping("/orders/{id}/complete")
    public R<Void> complete(@PathVariable Long id, @Valid @RequestBody RepairCompleteDTO dto) {
        repairService.complete(id, dto);
        return R.ok();
    }

    @PostMapping("/orders/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        repairService.cancel(id);
        return R.ok();
    }
}
