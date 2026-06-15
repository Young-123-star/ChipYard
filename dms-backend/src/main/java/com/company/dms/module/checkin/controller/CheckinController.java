package com.company.dms.module.checkin.controller;

import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.R;
import com.company.dms.module.checkin.dto.*;
import com.company.dms.module.checkin.service.CheckinService;
import com.company.dms.module.checkin.vo.IntakeVO;
import com.company.dms.module.checkin.vo.RecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "入住管理")
@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @Operation(summary = "意向单分页列表")
    @GetMapping("/intakes")
    public R<PageResult<IntakeVO>> intakes(IntakeQuery query) {
        return R.ok(checkinService.pageIntakes(query));
    }

    @Operation(summary = "手工新建意向单")
    @PostMapping("/intakes")
    public R<Long> createIntake(@Valid @RequestBody IntakeCreateDTO dto) {
        return R.ok(checkinService.createManualIntake(dto));
    }

    @Operation(summary = "选床确认入住")
    @PostMapping("/intakes/{id}/assign")
    public R<Long> assign(@PathVariable Long id, @Valid @RequestBody AssignDTO dto) {
        return R.ok(checkinService.assign(id, dto));
    }

    @Operation(summary = "取消意向单")
    @PostMapping("/intakes/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        checkinService.cancel(id);
        return R.ok();
    }

    @Operation(summary = "入住档案分页列表")
    @GetMapping("/records")
    public R<PageResult<RecordVO>> records(RecordQuery query) {
        return R.ok(checkinService.pageRecords(query));
    }
}
