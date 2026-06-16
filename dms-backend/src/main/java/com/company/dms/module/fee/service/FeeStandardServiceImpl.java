package com.company.dms.module.fee.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.fee.dto.FeeStandardDTO;
import com.company.dms.module.fee.entity.FeeStandard;
import com.company.dms.module.fee.mapper.FeeStandardMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeeStandardServiceImpl implements FeeStandardService {

    private final FeeStandardMapper standardMapper;

    public FeeStandardServiceImpl(FeeStandardMapper standardMapper) {
        this.standardMapper = standardMapper;
    }

    @Override
    public List<FeeStandard> list() {
        return standardMapper.selectList(Wrappers.<FeeStandard>lambdaQuery()
                .orderByAsc(FeeStandard::getRoomType));
    }

    @Override
    public FeeStandard getById(Long id) {
        FeeStandard s = standardMapper.selectById(id);
        if (s == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "收费标准不存在");
        return s;
    }

    @Override
    public FeeStandard findByRoomType(Integer roomType) {
        return standardMapper.selectOne(Wrappers.<FeeStandard>lambdaQuery()
                .eq(FeeStandard::getRoomType, roomType).last("limit 1"));
    }

    @Override
    public Long create(FeeStandardDTO dto) {
        if (findByRoomType(dto.getRoomType()) != null)
            throw new BizException("该房型收费标准已存在");
        FeeStandard s = new FeeStandard();
        BeanUtils.copyProperties(dto, s);
        standardMapper.insert(s);
        return s.getId();
    }

    @Override
    public void update(Long id, FeeStandardDTO dto) {
        FeeStandard s = getById(id);
        FeeStandard other = findByRoomType(dto.getRoomType());
        if (other != null && !other.getId().equals(id))
            throw new BizException("该房型收费标准已存在");
        s.setRoomType(dto.getRoomType());
        s.setMonthlyPrice(dto.getMonthlyPrice());
        s.setRemark(dto.getRemark());
        standardMapper.updateById(s);
    }

    @Override
    public void delete(Long id) {
        getById(id); // 不存在则抛
        standardMapper.deleteById(id);
    }
}
