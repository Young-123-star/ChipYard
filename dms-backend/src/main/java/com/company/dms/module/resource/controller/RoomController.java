package com.company.dms.module.resource.controller;

import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.R;
import com.company.dms.module.resource.dto.BoardQuery;
import com.company.dms.module.resource.dto.RoomQuery;
import com.company.dms.module.resource.dto.RoomSaveDTO;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.service.RoomService;
import com.company.dms.module.resource.vo.RoomBoardVO;
import com.company.dms.module.resource.vo.RoomSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "房间管理")
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Operation(summary = "分页列表")
    @GetMapping
    public R<PageResult<Room>> page(RoomQuery query) {
        return R.ok(roomService.page(query));
    }

    @Operation(summary = "按筛选条件汇总（不分页）")
    @GetMapping("/summary")
    public R<RoomSummaryVO> summary(RoomQuery query) {
        return R.ok(roomService.summary(query));
    }

    @Operation(summary = "房间状态看板")
    @GetMapping("/board")
    public R<List<RoomBoardVO>> board(BoardQuery query) {
        return R.ok(roomService.board(query));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<Room> get(@PathVariable Long id) {
        return R.ok(roomService.getById(id));
    }

    @Operation(summary = "新增")
    @PostMapping
    public R<Long> create(@Valid @RequestBody RoomSaveDTO dto) {
        return R.ok(roomService.create(dto));
    }

    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody RoomSaveDTO dto) {
        roomService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return R.ok();
    }
}
