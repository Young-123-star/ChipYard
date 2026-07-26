package com.company.dms.module.checkin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.checkin.dto.*;
import com.company.dms.module.checkin.entity.CheckinIntake;
import com.company.dms.module.checkin.entity.CheckinRecord;
import com.company.dms.module.checkin.mapper.CheckinIntakeMapper;
import com.company.dms.module.checkin.mapper.CheckinRecordMapper;
import com.company.dms.module.checkin.vo.IntakeVO;
import com.company.dms.module.checkin.vo.RecordVO;
import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.service.ResidentService;
import com.company.dms.module.resource.entity.Bed;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.service.BedService;
import com.company.dms.module.resource.service.RoomService;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CheckinServiceImpl implements CheckinService {

    private final CheckinIntakeMapper intakeMapper;
    private final CheckinRecordMapper recordMapper;
    private final ResidentService residentService;
    private final BedService bedService;
    private final RoomService roomService;

    public CheckinServiceImpl(CheckinIntakeMapper intakeMapper, CheckinRecordMapper recordMapper,
                              ResidentService residentService, BedService bedService, RoomService roomService) {
        this.intakeMapper = intakeMapper;
        this.recordMapper = recordMapper;
        this.residentService = residentService;
        this.bedService = bedService;
        this.roomService = roomService;
    }

    @Override
    public PageResult<IntakeVO> pageIntakes(IntakeQuery query) {
        Page<CheckinIntake> p = intakeMapper.selectPage(
                Page.of(query.getPage(), query.getSize()),
                Wrappers.<CheckinIntake>lambdaQuery()
                        .eq(query.getStatus() != null, CheckinIntake::getStatus, query.getStatus())
                        .eq(query.getSource() != null, CheckinIntake::getSource, query.getSource())
                        .orderByDesc(CheckinIntake::getId));
        List<Long> residentIds = p.getRecords().stream().map(CheckinIntake::getResidentId).distinct().collect(Collectors.toList());
        Map<Long, Resident> residents = residentIds.isEmpty() ? Map.of()
                : residentService.listByIds(residentIds).stream()
                    .collect(Collectors.toMap(Resident::getId, Function.identity(), (a, b) -> a));
        Page<IntakeVO> voPage = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(i -> toIntakeVO(i, residents.get(i.getResidentId()))).collect(Collectors.toList()));
        return PageResult.of(voPage);
    }

    private IntakeVO toIntakeVO(CheckinIntake i, Resident r) {
        IntakeVO vo = new IntakeVO();
        BeanUtils.copyProperties(i, vo);
        if (r != null) {
            vo.setResidentName(r.getRealName());
            vo.setEmployeeNo(r.getEmployeeNo());
            vo.setResidentGender(r.getGender());
        }
        return vo;
    }

    @Override
    public CheckinIntake getIntake(Long id) {
        CheckinIntake i = intakeMapper.selectById(id);
        if (i == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "入住意向单不存在");
        return i;
    }

    @Override
    public Long createManualIntake(IntakeCreateDTO dto) {
        residentService.getById(dto.getResidentId()); // 校验居住人存在
        CheckinIntake i = new CheckinIntake();
        BeanUtils.copyProperties(dto, i);
        i.setSource(3);
        i.setStatus(1);
        i.setBizNo("MANUAL-" + dto.getResidentId() + "-" + System.nanoTime());
        intakeMapper.insert(i);
        return i.getId();
    }

    @Override
    public Long createIntakeFromCommand(CreateIntakeCommand cmd) {
        CheckinIntake existing = intakeMapper.selectOne(Wrappers.<CheckinIntake>lambdaQuery()
                .eq(CheckinIntake::getBizNo, cmd.getBizNo()).last("limit 1"));
        if (existing != null) return existing.getId(); // 幂等
        residentService.getById(cmd.getResidentId());
        CheckinIntake i = new CheckinIntake();
        BeanUtils.copyProperties(cmd, i);
        i.setStatus(1);
        try {
            intakeMapper.insert(i);
        } catch (DuplicateKeyException e) {
            // 并发下唯一索引兜底：按 bizNo 回查返回已有记录
            CheckinIntake existed = intakeMapper.selectOne(Wrappers.<CheckinIntake>lambdaQuery()
                    .eq(CheckinIntake::getBizNo, cmd.getBizNo()).last("limit 1"));
            if (existed != null) return existed.getId();
            throw e;
        }
        return i.getId();
    }

    @Override
    public void cancel(Long intakeId) {
        getIntake(intakeId); // 存在性校验
        int affected = intakeMapper.update(null, Wrappers.<CheckinIntake>lambdaUpdate()
                .set(CheckinIntake::getStatus, 3)
                .eq(CheckinIntake::getId, intakeId)
                .eq(CheckinIntake::getStatus, 1));
        if (affected == 0) throw new BizException("单据状态已变化，请刷新后重试");
    }

    @Override
    public PageResult<RecordVO> pageRecords(RecordQuery query) {
        Page<CheckinRecord> p = recordMapper.selectPage(
                Page.of(query.getPage(), query.getSize()),
                Wrappers.<CheckinRecord>lambdaQuery()
                        .eq(query.getBuildingId() != null, CheckinRecord::getBuildingId, query.getBuildingId())
                        .eq(query.getRoomId() != null, CheckinRecord::getRoomId, query.getRoomId())
                        .eq(query.getStatus() != null, CheckinRecord::getStatus, query.getStatus())
                        .orderByDesc(CheckinRecord::getId));
        List<Long> residentIds = p.getRecords().stream().map(CheckinRecord::getResidentId).distinct().collect(Collectors.toList());
        Map<Long, Resident> residents = residentIds.isEmpty() ? Map.of()
                : residentService.listByIds(residentIds).stream()
                    .collect(Collectors.toMap(Resident::getId, Function.identity(), (a, b) -> a));
        Page<RecordVO> voPage = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(rec -> {
            RecordVO vo = new RecordVO();
            BeanUtils.copyProperties(rec, vo);
            Resident r = residents.get(rec.getResidentId());
            if (r != null) { vo.setResidentName(r.getRealName()); vo.setEmployeeNo(r.getEmployeeNo()); }
            return vo;
        }).collect(Collectors.toList()));
        return PageResult.of(voPage);
    }

    @Override
    @Transactional
    public Long assign(Long intakeId, AssignDTO dto) {
        CheckinIntake intake = getIntake(intakeId);
        if (intake.getStatus() != 1) throw new BizException("仅待分配的意向单可办理入住");
        if (findActiveRecordByResident(intake.getResidentId()) != null) throw new BizException("该居住人已有在住记录");

        Bed bed = bedService.getById(dto.getBedId());
        if (bed.getStatus() == null || bed.getStatus() != 1) throw new BizException("所选床位不可用（非空闲）");

        Room room = roomService.getById(bed.getRoomId());
        if (room.getStatus() != null && room.getStatus() == 3) throw new BizException("该房间维修中，不可入住");
        Resident resident = residentService.getById(intake.getResidentId());

        // 性别校验：房间限性别时，居住人性别须非空且匹配
        if (room.getGenderLimit() != null && room.getGenderLimit() != 0
                && (resident.getGender() == null || resident.getGender() == 0
                        || !resident.getGender().equals(room.getGenderLimit()))) {
            throw new BizException("居住人性别与房间性别限制不符");
        }

        // 占床 + 刷新房间统计
        bedService.occupy(bed.getId(), resident.getId());
        roomService.refreshOccupancy(room.getId());

        // 生成入住档案
        CheckinRecord rec = new CheckinRecord();
        rec.setIntakeId(intake.getId());
        rec.setResidentId(resident.getId());
        rec.setBuildingId(room.getBuildingId());
        rec.setFloorId(room.getFloorId());
        rec.setRoomId(room.getId());
        rec.setBedId(bed.getId());
        rec.setCheckinDate(dto.getCheckinDate() != null ? dto.getCheckinDate() : LocalDate.now());
        rec.setStatus(1);
        recordMapper.insert(rec);

        // 意向单置已入住
        intake.setStatus(2);
        intakeMapper.updateById(intake);

        return rec.getId();
    }

    @Override
    public CheckinRecord getRecord(Long id) {
        CheckinRecord r = recordMapper.selectById(id);
        if (r == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "入住档案不存在");
        return r;
    }

    @Override
    public CheckinRecord findActiveRecordByResident(Long residentId) {
        return recordMapper.selectOne(Wrappers.<CheckinRecord>lambdaQuery()
                .eq(CheckinRecord::getResidentId, residentId)
                .eq(CheckinRecord::getStatus, 1)
                .last("limit 1"));
    }

    @Override
    public void markCheckedOut(Long recordId, LocalDate checkoutDate) {
        CheckinRecord r = getRecord(recordId);
        if (r.getStatus() != 1) throw new BizException("该入住档案非在住状态");
        r.setStatus(2);
        r.setCheckoutDate(checkoutDate);
        recordMapper.updateById(r);
    }

    @Override
    public List<CheckinRecord> listActiveRecords() {
        return recordMapper.selectList(Wrappers
                .<CheckinRecord>lambdaQuery().eq(CheckinRecord::getStatus, 1));
    }

    @Override
    public List<CheckinRecord> listActiveRecordsByRoom(Long roomId) {
        return recordMapper.selectList(Wrappers
                .<CheckinRecord>lambdaQuery()
                .eq(CheckinRecord::getStatus, 1)
                .eq(CheckinRecord::getRoomId, roomId));
    }

    @Override
    public List<CheckinRecord> listRecordsByRoomAt(Long roomId, LocalDate date) {
        return recordMapper.selectList(Wrappers.<CheckinRecord>lambdaQuery()
                .eq(CheckinRecord::getRoomId, roomId)
                .le(CheckinRecord::getCheckinDate, date)
                .and(q -> q.isNull(CheckinRecord::getCheckoutDate)
                        .or().gt(CheckinRecord::getCheckoutDate, date))
                .orderByAsc(CheckinRecord::getId));
    }

    @Override
    public List<CheckinRecord> listRecordsByIds(java.util.Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return recordMapper.selectBatchIds(ids);
    }
}
