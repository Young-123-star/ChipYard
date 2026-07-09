package com.company.dms.module.dict.controller;

import com.company.dms.common.result.R;
import com.company.dms.module.dict.dto.DictItemSaveDTO;
import com.company.dms.module.dict.dto.DictTypeSaveDTO;
import com.company.dms.module.dict.entity.DictItem;
import com.company.dms.module.dict.entity.DictType;
import com.company.dms.module.dict.service.DictService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("/types")
    public R<List<DictType>> listTypes() {
        return R.ok(dictService.listTypes());
    }

    @PostMapping("/types")
    public R<Long> createType(@Valid @RequestBody DictTypeSaveDTO dto) {
        return R.ok(dictService.createType(dto));
    }

    @PutMapping("/types/{id}")
    public R<Void> updateType(@PathVariable Long id, @Valid @RequestBody DictTypeSaveDTO dto) {
        dictService.updateType(id, dto);
        return R.ok();
    }

    @DeleteMapping("/types/{id}")
    public R<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return R.ok();
    }

    @GetMapping("/items")
    public R<List<DictItem>> listItems(@RequestParam String dictType,
                                       @RequestParam(defaultValue = "true") boolean activeOnly) {
        return R.ok(dictService.listItems(dictType, activeOnly));
    }

    @PostMapping("/items")
    public R<Long> createItem(@Valid @RequestBody DictItemSaveDTO dto) {
        return R.ok(dictService.createItem(dto));
    }

    @PutMapping("/items/{id}")
    public R<Void> updateItem(@PathVariable Long id, @Valid @RequestBody DictItemSaveDTO dto) {
        dictService.updateItem(id, dto);
        return R.ok();
    }

    @DeleteMapping("/items/{id}")
    public R<Void> deleteItem(@PathVariable Long id) {
        dictService.deleteItem(id);
        return R.ok();
    }
}
