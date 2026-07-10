package com.company.dms.module.inspection;

import com.company.dms.common.exception.BizException;
import com.company.dms.module.inspection.model.InspectionModels.*;
import com.company.dms.module.inspection.service.InspectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class InspectionServiceTest {
    @Autowired InspectionService inspectionService;

    @Test
    void plan_validates_target_and_dictionary_items() {
        PlanSave missingTarget = plan("invalid target", List.of("消防设施"));
        missingTarget.setTargetId(999L);
        assertThrows(BizException.class, () -> inspectionService.createPlan(missingTarget));

        PlanSave invalidItem = plan("invalid item", List.of("自定义非法项"));
        assertThrows(BizException.class, () -> inspectionService.createPlan(invalidItem));
    }

    @Test
    void disabled_plan_cannot_generate_and_date_is_idempotent() {
        Long planId = inspectionService.createPlan(plan("dispatch guards", List.of("消防设施")));
        StatusChange disabled = new StatusChange();
        disabled.setStatus(0);
        inspectionService.changePlanStatus(planId, disabled);
        assertThrows(BizException.class, () -> inspectionService.generateTask(planId, generate("2026-08-01")));

        disabled.setStatus(1);
        inspectionService.changePlanStatus(planId, disabled);
        inspectionService.generateTask(planId, generate("2026-08-01"));
        assertThrows(BizException.class, () -> inspectionService.generateTask(planId, generate("2026-08-01")));
    }

    @Test
    void generated_task_keeps_plan_snapshot() {
        Long planId = inspectionService.createPlan(plan("snapshot before", List.of("消防设施", "用电安全")));
        Long taskId = inspectionService.generateTask(planId, generate("2026-08-02"));

        inspectionService.updatePlan(planId, plan("snapshot after", List.of("卫生状况")));

        TaskVO task = inspectionService.taskDetail(taskId);
        assertEquals("snapshot before", task.getPlanName());
        assertEquals(List.of("消防设施", "用电安全"), task.getItems());
    }

    @Test
    void execution_requires_all_items_and_abnormal_note_then_rectifies() {
        Long taskId = newTask("execution", List.of("消防设施", "用电安全"), "2026-08-03");

        TaskExecute incomplete = execute(result("消防设施", true, null));
        assertThrows(BizException.class, () -> inspectionService.executeTask(taskId, incomplete));

        TaskExecute missingNote = execute(result("消防设施", true, null), result("用电安全", false, " "));
        assertThrows(BizException.class, () -> inspectionService.executeTask(taskId, missingNote));

        inspectionService.executeTask(taskId,
                execute(result("用电安全", false, "插座过载"), result("消防设施", true, null)));
        TaskVO pending = inspectionService.taskDetail(taskId);
        assertEquals(3, pending.getStatus());
        assertEquals(List.of("消防设施", "用电安全"), pending.getResults().stream().map(ItemResult::getItem).toList());
        assertThrows(BizException.class, () -> inspectionService.cancelTask(taskId));

        TaskRectify rectify = new TaskRectify();
        rectify.setNote("已更换插排并移除过载设备");
        inspectionService.rectifyTask(taskId, rectify);
        TaskVO done = inspectionService.taskDetail(taskId);
        assertEquals(4, done.getStatus());
        assertNotNull(done.getRectifiedAt());
        assertThrows(BizException.class, () -> inspectionService.rectifyTask(taskId, rectify));
    }

    @Test
    void all_passed_task_completes_without_rectification() {
        Long taskId = newTask("all passed", List.of("卫生状况"), "2026-08-04");
        inspectionService.executeTask(taskId, execute(result("卫生状况", true, null)));
        assertEquals(2, inspectionService.taskDetail(taskId).getStatus());
    }

    private Long newTask(String name, List<String> items, String date) {
        return inspectionService.generateTask(inspectionService.createPlan(plan(name, items)), generate(date));
    }

    private PlanSave plan(String name, List<String> items) {
        PlanSave dto = new PlanSave();
        dto.setPlanName(name);
        dto.setCycleType(1);
        dto.setTargetType(1);
        dto.setTargetId(1L);
        dto.setInspector("宿舍管理员");
        dto.setItems(items);
        return dto;
    }

    private TaskGenerate generate(String date) {
        TaskGenerate dto = new TaskGenerate();
        dto.setPlannedDate(LocalDate.parse(date));
        return dto;
    }

    private TaskExecute execute(ItemResult... items) {
        TaskExecute dto = new TaskExecute();
        dto.setItems(List.of(items));
        return dto;
    }

    private ItemResult result(String item, boolean passed, String note) {
        ItemResult result = new ItemResult();
        result.setItem(item);
        result.setPassed(passed);
        result.setNote(note);
        return result;
    }
}
