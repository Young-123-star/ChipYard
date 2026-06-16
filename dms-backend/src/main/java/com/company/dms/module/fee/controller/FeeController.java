package com.company.dms.module.fee.controller;

import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.R;
import com.company.dms.module.fee.dto.BillQuery;
import com.company.dms.module.fee.dto.FeeStandardDTO;
import com.company.dms.module.fee.dto.GenerateBillsDTO;
import com.company.dms.module.fee.entity.FeeStandard;
import com.company.dms.module.fee.service.FeeBillService;
import com.company.dms.module.fee.service.FeeStandardService;
import com.company.dms.module.fee.vo.FeeBillVO;
import com.company.dms.module.fee.vo.GenerateResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "费用管理")
@RestController
@RequestMapping("/api/fee")
public class FeeController {

    private final FeeStandardService standardService;
    private final FeeBillService billService;

    public FeeController(FeeStandardService standardService, FeeBillService billService) {
        this.standardService = standardService;
        this.billService = billService;
    }

    // ---- 收费标准 ----
    @Operation(summary = "收费标准列表")
    @GetMapping("/standards")
    public R<List<FeeStandard>> standards() {
        return R.ok(standardService.list());
    }

    @Operation(summary = "新增收费标准")
    @PostMapping("/standards")
    public R<Long> createStandard(@Valid @RequestBody FeeStandardDTO dto) {
        return R.ok(standardService.create(dto));
    }

    @Operation(summary = "修改收费标准")
    @PutMapping("/standards/{id}")
    public R<Void> updateStandard(@PathVariable Long id, @Valid @RequestBody FeeStandardDTO dto) {
        standardService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除收费标准")
    @DeleteMapping("/standards/{id}")
    public R<Void> deleteStandard(@PathVariable Long id) {
        standardService.delete(id);
        return R.ok();
    }

    // ---- 账单 ----
    @Operation(summary = "账单分页列表")
    @GetMapping("/bills")
    public R<PageResult<FeeBillVO>> bills(BillQuery query) {
        return R.ok(billService.pageBills(query));
    }

    @Operation(summary = "按账期生成账单")
    @PostMapping("/bills/generate")
    public R<GenerateResultVO> generate(@Valid @RequestBody GenerateBillsDTO dto) {
        return R.ok(billService.generate(dto.getPeriod()));
    }

    @Operation(summary = "缴费")
    @PostMapping("/bills/{id}/pay")
    public R<Void> pay(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        billService.pay(id, body.get("payMethod"));
        return R.ok();
    }

    @Operation(summary = "作废账单")
    @PostMapping("/bills/{id}/void")
    public R<Void> voidBill(@PathVariable Long id) {
        billService.voidBill(id);
        return R.ok();
    }
}
