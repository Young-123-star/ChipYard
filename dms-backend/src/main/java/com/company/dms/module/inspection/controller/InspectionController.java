package com.company.dms.module.inspection.controller;

import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.R;
import com.company.dms.module.inspection.model.InspectionModels.*;
import com.company.dms.module.inspection.service.InspectionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inspection")
public class InspectionController {
    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @GetMapping("/plans")
    public R<PageResult<PlanVO>> plans(PlanQuery query) {
        return R.ok(inspectionService.pagePlans(query));
    }

    @PostMapping("/plans")
    public R<Long> createPlan(@Valid @RequestBody PlanSave dto) {
        return R.ok(inspectionService.createPlan(dto));
    }

    @PutMapping("/plans/{id}")
    public R<Void> updatePlan(@PathVariable Long id, @Valid @RequestBody PlanSave dto) {
        inspectionService.updatePlan(id, dto);
        return R.ok();
    }

    @PostMapping("/plans/{id}/status")
    public R<Void> changePlanStatus(@PathVariable Long id, @Valid @RequestBody StatusChange dto) {
        inspectionService.changePlanStatus(id, dto);
        return R.ok();
    }

    @PostMapping("/plans/{id}/tasks")
    public R<Long> generateTask(@PathVariable Long id, @Valid @RequestBody TaskGenerate dto) {
        return R.ok(inspectionService.generateTask(id, dto));
    }

    @GetMapping("/tasks")
    public R<PageResult<TaskVO>> tasks(TaskQuery query) {
        return R.ok(inspectionService.pageTasks(query));
    }

    @GetMapping("/tasks/{id}")
    public R<TaskVO> taskDetail(@PathVariable Long id) {
        return R.ok(inspectionService.taskDetail(id));
    }

    @PostMapping("/tasks/{id}/execute")
    public R<Void> executeTask(@PathVariable Long id, @Valid @RequestBody TaskExecute dto) {
        inspectionService.executeTask(id, dto);
        return R.ok();
    }

    @PostMapping("/tasks/{id}/rectify")
    public R<Void> rectifyTask(@PathVariable Long id, @Valid @RequestBody TaskRectify dto) {
        inspectionService.rectifyTask(id, dto);
        return R.ok();
    }

    @PostMapping("/tasks/{id}/cancel")
    public R<Void> cancelTask(@PathVariable Long id) {
        inspectionService.cancelTask(id);
        return R.ok();
    }
}
