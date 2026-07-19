package com.company.dms.module.fee.controller;

import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.R;
import com.company.dms.module.fee.dto.BillQuery;
import com.company.dms.module.fee.dto.FeeStandardDTO;
import com.company.dms.module.fee.dto.GenerateBillsDTO;
import com.company.dms.module.fee.dto.MeterQuery;
import com.company.dms.module.fee.dto.MeterReadingDTO;
import com.company.dms.module.fee.dto.PayBillDTO;
import com.company.dms.module.fee.dto.UtilityRateDTO;
import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.entity.FeeStandard;
import com.company.dms.module.fee.entity.UtilityRate;
import com.company.dms.module.fee.service.FeeBillService;
import com.company.dms.module.fee.service.FeeStandardService;
import com.company.dms.module.fee.service.MeterService;
import com.company.dms.module.fee.service.UtilityBillingService;
import com.company.dms.module.fee.vo.ArrearsVO;
import com.company.dms.module.fee.vo.FeeBillVO;
import com.company.dms.module.fee.vo.GenerateResultVO;
import com.company.dms.module.fee.vo.MeterReadingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

@Tag(name = "费用管理")
@RestController
@RequestMapping("/api/fee")
public class FeeController {

    private final FeeStandardService standardService;
    private final FeeBillService billService;
    private final MeterService meterService;
    private final UtilityBillingService utilityBillingService;

    public FeeController(FeeStandardService standardService, FeeBillService billService, MeterService meterService,
                         UtilityBillingService utilityBillingService) {
        this.standardService = standardService;
        this.billService = billService;
        this.meterService = meterService;
        this.utilityBillingService = utilityBillingService;
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
    public R<Void> pay(@PathVariable Long id, @Valid @RequestBody PayBillDTO dto) {
        billService.pay(id, dto.getPayMethod());
        return R.ok();
    }

    @Operation(summary = "作废账单")
    @PostMapping("/bills/{id}/void")
    public R<Void> voidBill(@PathVariable Long id) {
        billService.voidBill(id);
        return R.ok();
    }

    @Operation(summary = "查在住档案待结算欠费（预览）")
    @GetMapping("/arrears")
    public R<ArrearsVO> arrears(@RequestParam Long checkinRecordId) {
        List<FeeBill> unpaid = billService.listUnpaidByRecord(checkinRecordId);
        BigDecimal total = unpaid.stream().map(FeeBill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return R.ok(ArrearsVO.of(unpaid.size(), total));
    }

    // ---- 水电抄表 ----
    @Operation(summary = "查水电单价")
    @GetMapping("/utility-rate")
    public R<UtilityRate> utilityRate() {
        return R.ok(meterService.getRate());
    }

    @Operation(summary = "改水电单价")
    @PutMapping("/utility-rate")
    public R<Void> updateRate(@Valid @RequestBody UtilityRateDTO dto) {
        meterService.updateRate(dto);
        return R.ok();
    }

    @Operation(summary = "抄表台账分页")
    @GetMapping("/meter-readings")
    public R<PageResult<MeterReadingVO>> meterReadings(MeterQuery query) {
        return R.ok(meterService.pageReadings(query));
    }

    @Operation(summary = "录入/更新抄表读数")
    @PostMapping("/meter-readings")
    public R<Long> saveReading(@Valid @RequestBody MeterReadingDTO dto) {
        return R.ok(meterService.saveReading(dto));
    }

    @Operation(summary = "按账期生成水电账单")
    @PostMapping("/utility-bills/generate")
    public R<GenerateResultVO> generateUtility(@Valid @RequestBody GenerateBillsDTO dto) {
        return R.ok(meterService.generateUtilityBills(dto.getPeriod()));
    }

    @GetMapping("/utility/accounts")
    public R<List<Map<String, Object>>> utilityAccounts(@RequestParam(required = false) Long buildingId) {
        return R.ok(utilityBillingService.listAccounts(buildingId));
    }

    @GetMapping("/utility/readings")
    public R<List<com.company.dms.module.fee.entity.MeterReading>> utilityReadings(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String accountCode) {
        return R.ok(utilityBillingService.listReadings(period, buildingId, accountCode));
    }

    @PostMapping("/utility/readings")
    public R<Long> saveUtilityReading(@Valid @RequestBody MeterReadingDTO dto) {
        return R.ok(utilityBillingService.saveReading(dto));
    }

    @PostMapping("/utility/settlements/preview")
    public R<Map<String, Object>> previewUtilitySettlement(@Valid @RequestBody GenerateBillsDTO dto) {
        return R.ok(utilityBillingService.preview(dto.getPeriod()));
    }

    @PostMapping("/utility/settlements/generate")
    public R<Map<String, Object>> generateUtilitySettlement(@Valid @RequestBody GenerateBillsDTO dto) {
        return R.ok(utilityBillingService.generate(dto.getPeriod()));
    }

    @GetMapping("/utility/settlements")
    public R<List<com.company.dms.module.fee.entity.UtilitySettlement>> utilitySettlements(
            @RequestParam(required = false) String period) {
        return R.ok(utilityBillingService.listSettlements(period));
    }

    @PostMapping("/utility/settlements/{id}/void")
    public R<Void> voidUtilitySettlement(@PathVariable Long id) {
        utilityBillingService.voidSettlement(id);
        return R.ok();
    }
}
