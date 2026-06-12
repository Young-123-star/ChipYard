package com.company.dms.module.resource.controller;

import com.company.dms.common.result.R;
import com.company.dms.module.resource.dto.FloorSaveDTO;
import com.company.dms.module.resource.service.FloorService;
import com.company.dms.module.resource.vo.FloorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "楼层管理")
@RestController
@RequestMapping("/api/floors")
public class FloorController {

    private final FloorService floorService;

    public FloorController(FloorService floorService) {
        this.floorService = floorService;
    }

    @Operation(summary = "按楼栋查楼层（含实时统计）")
    @GetMapping
    public R<List<FloorVO>> list(@RequestParam Long buildingId) {
        return R.ok(floorService.listByBuilding(buildingId));
    }

    @Operation(summary = "新增")
    @PostMapping
    public R<Long> create(@Valid @RequestBody FloorSaveDTO dto) {
        return R.ok(floorService.create(dto));
    }

    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody FloorSaveDTO dto) {
        floorService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        floorService.delete(id);
        return R.ok();
    }
}
