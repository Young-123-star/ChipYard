package com.company.dms.module.resident.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.resident.dto.ResidentQuery;
import com.company.dms.module.resident.dto.ResidentSaveDTO;
import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.mapper.ResidentMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ResidentServiceImpl implements ResidentService {

    private final ResidentMapper residentMapper;

    public ResidentServiceImpl(ResidentMapper residentMapper) {
        this.residentMapper = residentMapper;
    }

    @Override
    public PageResult<Resident> page(ResidentQuery query) {
        Page<Resident> p = residentMapper.selectPage(
                Page.of(query.getPage(), query.getSize()),
                Wrappers.<Resident>lambdaQuery()
                        .like(query.getRealName() != null, Resident::getRealName, query.getRealName())
                        .like(query.getEmployeeNo() != null, Resident::getEmployeeNo, query.getEmployeeNo())
                        .eq(query.getResidentType() != null, Resident::getResidentType, query.getResidentType())
                        .eq(query.getStatus() != null, Resident::getStatus, query.getStatus())
                        .orderByDesc(Resident::getId));
        return PageResult.of(p);
    }

    @Override
    public Resident getById(Long id) {
        Resident r = residentMapper.selectById(id);
        if (r == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "居住人不存在");
        return r;
    }

    @Override
    public Resident getByEmployeeNo(String employeeNo) {
        return residentMapper.selectOne(Wrappers.<Resident>lambdaQuery()
                .eq(Resident::getEmployeeNo, employeeNo).last("limit 1"));
    }

    @Override
    public Long create(ResidentSaveDTO dto) {
        if (getByEmployeeNo(dto.getEmployeeNo()) != null) throw new BizException("工号已存在");
        Resident r = new Resident();
        BeanUtils.copyProperties(dto, r);
        if (r.getSource() == null) r.setSource(2);
        if (r.getStatus() == null) r.setStatus(1);
        residentMapper.insert(r);
        return r.getId();
    }

    @Override
    public void update(Long id, ResidentSaveDTO dto) {
        getById(id);
        Resident r = new Resident();
        BeanUtils.copyProperties(dto, r);
        r.setId(id);
        residentMapper.updateById(r);
    }

    @Override
    public void delete(Long id) {
        getById(id);
        residentMapper.deleteById(id);
    }

    @Override
    public Long upsertByEmployeeNo(ResidentSaveDTO dto, int source) {
        Resident existing = getByEmployeeNo(dto.getEmployeeNo());
        Resident r = new Resident();
        BeanUtils.copyProperties(dto, r);
        r.setSource(source);
        if (r.getStatus() == null) r.setStatus(1);
        if (existing != null) {
            r.setId(existing.getId());
            residentMapper.updateById(r);
            return existing.getId();
        }
        residentMapper.insert(r);
        return r.getId();
    }

    @Override
    public void markResigned(Long id) {
        Resident r = getById(id);
        r.setStatus(0);
        residentMapper.updateById(r);
    }
}
