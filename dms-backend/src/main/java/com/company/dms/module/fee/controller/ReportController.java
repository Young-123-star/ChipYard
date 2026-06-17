package com.company.dms.module.fee.controller;

import com.company.dms.common.result.R;
import com.company.dms.module.fee.service.ReportService;
import com.company.dms.module.fee.vo.ArrearsRankVO;
import com.company.dms.module.fee.vo.BuildingSummaryVO;
import com.company.dms.module.fee.vo.PeriodSummaryVO;
import com.company.dms.module.fee.vo.UsageTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "统计报表")
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "按账期汇总")
    @GetMapping("/period-summary")
    public R<List<PeriodSummaryVO>> periodSummary() {
        return R.ok(reportService.getPeriodSummary());
    }

    @Operation(summary = "按楼栋汇总")
    @GetMapping("/building-summary")
    public R<List<BuildingSummaryVO>> buildingSummary() {
        return R.ok(reportService.getBuildingSummary());
    }

    @Operation(summary = "欠费排行")
    @GetMapping("/arrears-ranking")
    public R<List<ArrearsRankVO>> arrearsRanking(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(reportService.getArrearsRanking(limit));
    }

    @Operation(summary = "水电用量趋势")
    @GetMapping("/usage-trend")
    public R<List<UsageTrendVO>> usageTrend() {
        return R.ok(reportService.getUsageTrend());
    }
}
