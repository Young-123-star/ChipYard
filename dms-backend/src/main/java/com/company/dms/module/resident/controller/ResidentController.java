package com.company.dms.module.resident.controller;

import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.R;
import com.company.dms.module.resident.dto.ResidentQuery;
import com.company.dms.module.resident.dto.ResidentSaveDTO;
import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.service.ResidentService;
import com.company.dms.module.resident.vo.ResidentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

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
    public R<PageResult<ResidentVO>> page(ResidentQuery query) {
        PageResult<Resident> p = residentService.page(query);
        PageResult<ResidentVO> vo = new PageResult<>();
        vo.setRecords(p.getRecords().stream().map(ResidentVO::of).collect(Collectors.toList()));
        vo.setTotal(p.getTotal());
        vo.setPage(p.getPage());
        vo.setSize(p.getSize());
        return R.ok(vo);
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<ResidentVO> get(@PathVariable Long id) {
        return R.ok(ResidentVO.of(residentService.getById(id)));
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
