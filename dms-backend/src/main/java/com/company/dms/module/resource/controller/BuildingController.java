package com.company.dms.module.resource.controller;

import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.R;
import com.company.dms.module.resource.dto.BuildingQuery;
import com.company.dms.module.resource.dto.BuildingSaveDTO;
import com.company.dms.module.resource.entity.Building;
import com.company.dms.module.resource.service.BuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "楼栋管理")
@RestController
@RequestMapping("/api/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @Operation(summary = "分页列表")
    @GetMapping
    public R<PageResult<Building>> page(BuildingQuery query) {
        return R.ok(buildingService.page(query));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<Building> get(@PathVariable Long id) {
        return R.ok(buildingService.getById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    public R<Long> create(@Valid @RequestBody BuildingSaveDTO dto) {
        return R.ok(buildingService.create(dto));
    }

    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody BuildingSaveDTO dto) {
        buildingService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        buildingService.delete(id);
        return R.ok();
    }
}
