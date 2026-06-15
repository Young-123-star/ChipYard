package com.company.dms.module.resource.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.resource.dto.BedSaveDTO;
import com.company.dms.module.resource.entity.Bed;
import com.company.dms.module.resource.mapper.BedMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BedServiceImpl implements BedService {

    private final BedMapper bedMapper;

    public BedServiceImpl(BedMapper bedMapper) {
        this.bedMapper = bedMapper;
    }

    @Override
    public List<Bed> listByRoom(Long roomId) {
        return bedMapper.selectList(Wrappers.<Bed>lambdaQuery()
                .eq(Bed::getRoomId, roomId)
                .orderByAsc(Bed::getBedNumber));
    }

    @Override
    public Long create(BedSaveDTO dto) {
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
        Bed b = new Bed();
        BeanUtils.copyProperties(dto, b);
        b.setId(id);
        bedMapper.updateById(b);
    }

    @Override
    public void delete(Long id) {
        if (bedMapper.selectById(id) == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "床位不存在");
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
        Bed b = getById(bedId);
        if (b.getStatus() != null && b.getStatus() == 2) throw new BizException("该床位已被占用");
        b.setStatus(2);
        b.setCurrentUserId(residentId);
        bedMapper.updateById(b);
    }

    @Override
    public void release(Long bedId) {
        Bed b = getById(bedId);
        b.setStatus(1);
        b.setCurrentUserId(null);
        bedMapper.updateById(b);
    }
}
