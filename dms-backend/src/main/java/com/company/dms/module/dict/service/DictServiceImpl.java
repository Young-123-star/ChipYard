package com.company.dms.module.dict.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.dict.dto.DictItemSaveDTO;
import com.company.dms.module.dict.dto.DictTypeSaveDTO;
import com.company.dms.module.dict.entity.DictItem;
import com.company.dms.module.dict.entity.DictType;
import com.company.dms.module.dict.mapper.DictItemMapper;
import com.company.dms.module.dict.mapper.DictTypeMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

@Service
public class DictServiceImpl implements DictService {

    private final DictTypeMapper typeMapper;
    private final DictItemMapper itemMapper;

    public DictServiceImpl(DictTypeMapper typeMapper, DictItemMapper itemMapper) {
        this.typeMapper = typeMapper;
        this.itemMapper = itemMapper;
    }

    @Override
    public List<DictType> listTypes() {
        return typeMapper.selectList(Wrappers.<DictType>lambdaQuery()
                .orderByAsc(DictType::getSortOrder)
                .orderByAsc(DictType::getId));
    }

    @Override
    public Long createType(DictTypeSaveDTO dto) {
        DictType type = new DictType();
        BeanUtils.copyProperties(dto, type);
        type.setDictType(dto.getDictType().trim());
        type.setSystemFlag(0);
        if (type.getStatus() == null) type.setStatus(1);
        if (type.getSortOrder() == null) type.setSortOrder(0);
        typeMapper.insert(type);
        return type.getId();
    }

    @Override
    public void updateType(Long id, DictTypeSaveDTO dto) {
        DictType existing = getType(id);
        if (Integer.valueOf(1).equals(existing.getSystemFlag())
                && !existing.getDictType().equals(dto.getDictType())) {
            throw new BizException("系统字典类型编码不允许修改");
        }
        DictType type = new DictType();
        BeanUtils.copyProperties(dto, type);
        type.setId(id);
        type.setSystemFlag(existing.getSystemFlag());
        typeMapper.updateById(type);
    }

    @Override
    public void deleteType(Long id) {
        DictType existing = getType(id);
        if (Integer.valueOf(1).equals(existing.getSystemFlag())) {
            throw new BizException("系统字典类型不允许删除");
        }
        typeMapper.deleteById(id);
    }

    @Override
    public List<DictItem> listItems(String dictType, boolean activeOnly) {
        return itemMapper.selectList(Wrappers.<DictItem>lambdaQuery()
                .eq(DictItem::getDictType, dictType)
                .eq(activeOnly, DictItem::getStatus, 1)
                .orderByAsc(DictItem::getSortOrder)
                .orderByAsc(DictItem::getId));
    }

    @Override
    public DictItem getItem(Long id) {
        DictItem item = itemMapper.selectById(id);
        if (item == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "字典项不存在");
        }
        return item;
    }

    @Override
    public Long createItem(DictItemSaveDTO dto) {
        DictItem item = new DictItem();
        BeanUtils.copyProperties(dto, item);
        item.setDictType(dto.getDictType().trim());
        item.setDictValue(dto.getDictValue().trim());
        item.setDictLabel(dto.getDictLabel().trim());
        item.setSystemFlag(0);
        if (item.getStatus() == null) item.setStatus(1);
        if (item.getSortOrder() == null) item.setSortOrder(0);
        itemMapper.insert(item);
        return item.getId();
    }

    @Override
    public void updateItem(Long id, DictItemSaveDTO dto) {
        DictItem existing = getItem(id);
        if (Integer.valueOf(1).equals(existing.getSystemFlag())) {
            if (!existing.getDictValue().equals(dto.getDictValue())) {
                throw new BizException("系统字典项值不允许修改");
            }
            if (!existing.getDictType().equals(dto.getDictType())) {
                throw new BizException("系统字典项类型不允许修改");
            }
        }
        DictItem item = new DictItem();
        BeanUtils.copyProperties(dto, item);
        item.setId(id);
        item.setSystemFlag(existing.getSystemFlag());
        itemMapper.updateById(item);
    }

    @Override
    public void deleteItem(Long id) {
        DictItem existing = getItem(id);
        if (Integer.valueOf(1).equals(existing.getSystemFlag())) {
            throw new BizException("系统字典项不允许删除");
        }
        itemMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void ensureItems(String dictType, Collection<String> labels) {
        if (labels == null || labels.isEmpty()) return;
        int sort = listItems(dictType, false).size() + 1;
        for (String raw : labels) {
            if (!StringUtils.hasText(raw)) continue;
            String label = raw.trim();
            Long exists = itemMapper.selectCount(Wrappers.<DictItem>lambdaQuery()
                    .eq(DictItem::getDictType, dictType)
                    .and(w -> w.eq(DictItem::getDictValue, label).or().eq(DictItem::getDictLabel, label)));
            if (exists > 0) continue;

            DictItem item = new DictItem();
            item.setDictType(dictType);
            item.setDictValue(label);
            item.setDictLabel(label);
            item.setSortOrder(1000 + sort++);
            item.setTagType("info");
            item.setStatus(1);
            item.setSystemFlag(0);
            itemMapper.insert(item);
        }
    }

    private DictType getType(Long id) {
        DictType type = typeMapper.selectById(id);
        if (type == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "字典类型不存在");
        }
        return type;
    }
}
