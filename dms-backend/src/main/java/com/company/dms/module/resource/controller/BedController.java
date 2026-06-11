package com.company.dms.module.resource.controller;

import com.company.dms.common.result.R;
import com.company.dms.module.resource.dto.BedSaveDTO;
import com.company.dms.module.resource.entity.Bed;
import com.company.dms.module.resource.service.BedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "床位管理")
@RestController
@RequestMapping("/api/beds")
public class BedController {

    private final BedService bedService;

    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @Operation(summary = "按房间查床位")
    @GetMapping
    public R<List<Bed>> list(@RequestParam Long roomId) {
        return R.ok(bedService.listByRoom(roomId));
    }

    @Operation(summary = "新增")
    @PostMapping
    public R<Long> create(@Valid @RequestBody BedSaveDTO dto) {
        return R.ok(bedService.create(dto));
    }

    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody BedSaveDTO dto) {
        bedService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        bedService.delete(id);
        return R.ok();
    }
}
