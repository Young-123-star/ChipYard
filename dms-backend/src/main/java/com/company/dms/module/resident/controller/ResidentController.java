package com.company.dms.module.resident.controller;

import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.R;
import com.company.dms.module.resident.dto.ResidentQuery;
import com.company.dms.module.resident.dto.ResidentSaveDTO;
import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.service.ResidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "居住人管理")
@RestController
@RequestMapping("/api/residents")
public class ResidentController {

    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @Operation(summary = "分页列表")
    @GetMapping
    public R<PageResult<Resident>> page(ResidentQuery query) {
        return R.ok(residentService.page(query));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<Resident> get(@PathVariable Long id) {
        return R.ok(residentService.getById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    public R<Long> create(@Valid @RequestBody ResidentSaveDTO dto) {
        return R.ok(residentService.create(dto));
    }

    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ResidentSaveDTO dto) {
        residentService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        residentService.delete(id);
        return R.ok();
    }
}
