package com.company.dms.module.resource.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.resource.dto.BedSaveDTO;
import com.company.dms.module.resource.entity.Bed;
import com.company.dms.module.resource.mapper.BedMapper;
import com.company.dms.module.resource.mapper.RoomMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BedServiceImpl implements BedService {

    private final BedMapper bedMapper;
    private final RoomMapper roomMapper;

    public BedServiceImpl(BedMapper bedMapper, RoomMapper roomMapper) {
        this.bedMapper = bedMapper;
        this.roomMapper = roomMapper;
    }

    @Override
    public List<Bed> listByRoom(Long roomId) {
        return bedMapper.selectList(Wrappers.<Bed>lambdaQuery()
                .eq(Bed::getRoomId, roomId)
                .orderByAsc(Bed::getBedNumber));
    }

    @Override
    public Long create(BedSaveDTO dto) {
        checkRoomExists(dto.getRoomId());
        Long count = bedMapper.selectCount(Wrappers.<Bed>lambdaQuery()
                .eq(Bed::getRoomId, dto.getRoomId())
                .eq(Bed::getBedNumber, dto.getBedNumber()));
        if (count > 0) throw new BizException("该房间已存在相同床位编号");
        Bed b = new Bed();
        BeanUtils.copyProperties(dto, b);
        bedMapper.insert(b);
        return b.getId();
    }

    @Override
    public void update(Long id, BedSaveDTO dto) {
        if (bedMapper.selectById(id) == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "床位不存在");
        checkRoomExists(dto.getRoomId());
        Long count = bedMapper.selectCount(Wrappers.<Bed>lambdaQuery()
                .eq(Bed::getRoomId, dto.getRoomId())
                .eq(Bed::getBedNumber, dto.getBedNumber())
                .ne(Bed::getId, id));
        if (count > 0) throw new BizException("该房间已存在相同床位编号");
        Bed b = new Bed();
        BeanUtils.copyProperties(dto, b);
        b.setId(id);
        bedMapper.updateById(b);
    }

    @Override
    public void delete(Long id) {
        Bed b = bedMapper.selectById(id);
        if (b == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "床位不存在");
        if (b.getStatus() != null && b.getStatus() == 2) throw new BizException("床位处于在住状态，不能删除");
        bedMapper.deleteById(id);
    }

    @Override
    public Bed getById(Long id) {
        Bed b = bedMapper.selectById(id);
        if (b == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "床位不存在");
        return b;
    }

    @Override
    public void occupy(Long bedId, Long residentId) {
        getById(bedId);
        // 条件更新：仅空闲(1)的床位允许占位，避免并发重复占用
        int updated = bedMapper.update(null, Wrappers.<Bed>lambdaUpdate()
                .set(Bed::getStatus, 2)
                .set(Bed::getCurrentUserId, residentId)
                .eq(Bed::getId, bedId)
                .eq(Bed::getStatus, 1));
        if (updated == 0) throw new BizException("床位已被占用");
    }

    @Override
    public void release(Long bedId) {
        Bed b = getById(bedId);
        if (b.getStatus() == null || b.getStatus() != 2) throw new BizException("床位不在占用状态");
        b.setStatus(1);
        b.setCurrentUserId(null);
        bedMapper.updateById(b);
    }

    private void checkRoomExists(Long roomId) {
        if (roomMapper.selectById(roomId) == null) throw new BizException("所属房间不存在");
    }
}
