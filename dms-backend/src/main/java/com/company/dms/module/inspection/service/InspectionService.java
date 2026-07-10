package com.company.dms.module.inspection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.dict.entity.DictItem;
import com.company.dms.module.dict.service.DictService;
import com.company.dms.module.inspection.entity.InspectionPlan;
import com.company.dms.module.inspection.entity.InspectionTask;
import com.company.dms.module.inspection.mapper.InspectionPlanMapper;
import com.company.dms.module.inspection.mapper.InspectionTaskMapper;
import com.company.dms.module.inspection.model.InspectionModels.*;
import com.company.dms.module.resource.entity.Building;
import com.company.dms.module.resource.entity.Floor;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.service.BuildingService;
import com.company.dms.module.resource.service.FloorService;
import com.company.dms.module.resource.service.RoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InspectionService {
    private static final Set<Integer> CYCLE_TYPES = Set.of(1, 2, 3, 4);
    private static final Set<Integer> TARGET_TYPES = Set.of(1, 2, 3);

    private final InspectionPlanMapper planMapper;
    private final InspectionTaskMapper taskMapper;
    private final BuildingService buildingService;
    private final FloorService floorService;
    private final RoomService roomService;
    private final DictService dictService;
    private final ObjectMapper objectMapper;

    public InspectionService(InspectionPlanMapper planMapper, InspectionTaskMapper taskMapper,
                             BuildingService buildingService, FloorService floorService, RoomService roomService,
                             DictService dictService, ObjectMapper objectMapper) {
        this.planMapper = planMapper;
        this.taskMapper = taskMapper;
        this.buildingService = buildingService;
        this.floorService = floorService;
        this.roomService = roomService;
        this.dictService = dictService;
        this.objectMapper = objectMapper;
    }

    public PageResult<PlanVO> pagePlans(PlanQuery query) {
        Page<InspectionPlan> page = planMapper.selectPage(Page.of(query.getPage(), query.getSize()),
                Wrappers.<InspectionPlan>lambdaQuery()
                        .eq(query.getStatus() != null, InspectionPlan::getStatus, query.getStatus())
                        .eq(query.getCycleType() != null, InspectionPlan::getCycleType, query.getCycleType())
                        .eq(query.getTargetType() != null, InspectionPlan::getTargetType, query.getTargetType())
                        .orderByDesc(InspectionPlan::getId));
        Page<PlanVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toPlanVO).toList());
        return PageResult.of(result);
    }

    @Transactional
    public Long createPlan(PlanSave dto) {
        ValidatedPlan validated = validatePlan(dto);
        InspectionPlan plan = new InspectionPlan();
        BeanUtils.copyProperties(dto, plan, "items");
        plan.setPlanName(dto.getPlanName().trim());
        plan.setInspector(dto.getInspector().trim());
        plan.setTargetName(validated.targetName());
        plan.setItemsJson(writeJson(validated.items()));
        plan.setStatus(1);
        planMapper.insert(plan);
        return plan.getId();
    }

    @Transactional
    public void updatePlan(Long id, PlanSave dto) {
        InspectionPlan plan = getPlan(id);
        ValidatedPlan validated = validatePlan(dto);
        BeanUtils.copyProperties(dto, plan, "items", "status");
        plan.setPlanName(dto.getPlanName().trim());
        plan.setInspector(dto.getInspector().trim());
        plan.setTargetName(validated.targetName());
        plan.setItemsJson(writeJson(validated.items()));
        planMapper.updateById(plan);
    }

    @Transactional
    public void changePlanStatus(Long id, StatusChange dto) {
        if (dto.getStatus() == null || (dto.getStatus() != 0 && dto.getStatus() != 1)) {
            throw new BizException("巡检计划状态只能是启用或停用");
        }
        InspectionPlan plan = getPlan(id);
        plan.setStatus(dto.getStatus());
        planMapper.updateById(plan);
    }

    @Transactional
    public Long generateTask(Long planId, TaskGenerate dto) {
        InspectionPlan plan = getPlan(planId);
        if (plan.getStatus() != 1) throw new BizException("停用的巡检计划不能派发任务");
        Long duplicate = taskMapper.selectCount(Wrappers.<InspectionTask>lambdaQuery()
                .eq(InspectionTask::getPlanId, planId)
                .eq(InspectionTask::getPlannedDate, dto.getPlannedDate()));
        if (duplicate > 0) throw new BizException("该计划在所选日期已派发任务");

        InspectionTask task = new InspectionTask();
        task.setTaskNo("IT-" + dto.getPlannedDate().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + planId);
        task.setPlanId(planId);
        task.setPlanName(plan.getPlanName());
        task.setTargetType(plan.getTargetType());
        task.setTargetId(plan.getTargetId());
        task.setTargetName(plan.getTargetName());
        task.setInspector(trimToNull(dto.getInspector()) == null ? plan.getInspector() : dto.getInspector().trim());
        task.setPlannedDate(dto.getPlannedDate());
        task.setItemsJson(plan.getItemsJson());
        task.setStatus(1);
        try {
            taskMapper.insert(task);
        } catch (DuplicateKeyException e) {
            throw new BizException("该计划在所选日期已派发任务");
        }
        return task.getId();
    }

    public PageResult<TaskVO> pageTasks(TaskQuery query) {
        Page<InspectionTask> page = taskMapper.selectPage(Page.of(query.getPage(), query.getSize()),
                Wrappers.<InspectionTask>lambdaQuery()
                        .eq(query.getStatus() != null, InspectionTask::getStatus, query.getStatus())
                        .eq(query.getPlanId() != null, InspectionTask::getPlanId, query.getPlanId())
                        .eq(query.getTargetType() != null, InspectionTask::getTargetType, query.getTargetType())
                        .eq(query.getPlannedDate() != null, InspectionTask::getPlannedDate, query.getPlannedDate())
                        .orderByDesc(InspectionTask::getPlannedDate)
                        .orderByDesc(InspectionTask::getId));
        Page<TaskVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toTaskVO).toList());
        return PageResult.of(result);
    }

    public TaskVO taskDetail(Long id) {
        return toTaskVO(getTask(id));
    }

    @Transactional
    public void executeTask(Long id, TaskExecute dto) {
        InspectionTask task = getTask(id);
        if (task.getStatus() != 1) throw new BizException("只有待执行任务可以提交巡检结果");
        List<String> expected = readItems(task.getItemsJson());
        if (dto.getItems() == null || dto.getItems().size() != expected.size()) {
            throw new BizException("必须提交全部巡检项");
        }

        Map<String, ItemResult> submitted = new LinkedHashMap<>();
        for (ItemResult item : dto.getItems()) {
            String name = item == null ? null : trimToNull(item.getItem());
            if (name == null || item.getPassed() == null || submitted.put(name, item) != null) {
                throw new BizException("巡检结果包含无效或重复项目");
            }
        }

        List<ItemResult> normalized = expected.stream().map(name -> {
            ItemResult item = submitted.get(name);
            if (item == null) throw new BizException("巡检结果与计划项目不一致");
            String note = trimToNull(item.getNote());
            if (!item.getPassed() && note == null) throw new BizException("异常巡检项必须填写说明");
            ItemResult result = new ItemResult();
            result.setItem(name);
            result.setPassed(item.getPassed());
            result.setNote(note);
            return result;
        }).toList();

        task.setResultsJson(writeJson(normalized));
        task.setStatus(normalized.stream().allMatch(ItemResult::getPassed) ? 2 : 3);
        task.setCompletedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    @Transactional
    public void rectifyTask(Long id, TaskRectify dto) {
        InspectionTask task = getTask(id);
        if (task.getStatus() != 3) throw new BizException("只有待整改任务可以确认整改");
        task.setRectificationNote(dto.getNote().trim());
        task.setRectifiedAt(LocalDateTime.now());
        task.setStatus(4);
        taskMapper.updateById(task);
    }

    @Transactional
    public void cancelTask(Long id) {
        InspectionTask task = getTask(id);
        if (task.getStatus() != 1) throw new BizException("只有待执行任务可以取消");
        task.setStatus(5);
        taskMapper.updateById(task);
    }

    private ValidatedPlan validatePlan(PlanSave dto) {
        if (!CYCLE_TYPES.contains(dto.getCycleType())) throw new BizException("巡检周期无效");
        if (!TARGET_TYPES.contains(dto.getTargetType())) throw new BizException("巡检对象类型无效");
        LinkedHashSet<String> items = new LinkedHashSet<>();
        if (dto.getItems() != null) dto.getItems().stream().map(this::trimToNull).forEach(items::add);
        items.remove(null);
        if (items.isEmpty()) throw new BizException("至少选择一个巡检项");
        Set<String> allowed = dictService.listItems("INSPECTION_ITEM", true).stream()
                .map(DictItem::getDictLabel).collect(Collectors.toSet());
        if (!allowed.containsAll(items)) throw new BizException("巡检项必须来自启用的数据字典");
        return new ValidatedPlan(List.copyOf(items), resolveTargetName(dto.getTargetType(), dto.getTargetId()));
    }

    private String resolveTargetName(Integer targetType, Long targetId) {
        if (targetType == 1) {
            Building building = buildingService.getById(targetId);
            return building.getBuildingName();
        }
        if (targetType == 2) {
            Floor floor = floorService.getById(targetId);
            return trimToNull(floor.getFloorName()) == null ? floor.getFloorNumber() + "层" : floor.getFloorName();
        }
        Room room = roomService.getById(targetId);
        return room.getRoomNumber();
    }

    private InspectionPlan getPlan(Long id) {
        InspectionPlan plan = planMapper.selectById(id);
        if (plan == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "巡检计划不存在");
        return plan;
    }

    private InspectionTask getTask(Long id) {
        InspectionTask task = taskMapper.selectById(id);
        if (task == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "巡检任务不存在");
        return task;
    }

    private PlanVO toPlanVO(InspectionPlan plan) {
        PlanVO vo = new PlanVO();
        BeanUtils.copyProperties(plan, vo);
        vo.setItems(readItems(plan.getItemsJson()));
        return vo;
    }

    private TaskVO toTaskVO(InspectionTask task) {
        TaskVO vo = new TaskVO();
        BeanUtils.copyProperties(task, vo);
        vo.setItems(readItems(task.getItemsJson()));
        vo.setResults(readResults(task.getResultsJson()));
        return vo;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("巡检数据序列化失败", e);
        }
    }

    private List<String> readItems(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("巡检项目数据损坏", e);
        }
    }

    private List<ItemResult> readResults(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("巡检结果数据损坏", e);
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }

    private record ValidatedPlan(List<String> items, String targetName) {}
}
