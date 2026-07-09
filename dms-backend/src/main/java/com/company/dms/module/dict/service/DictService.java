package com.company.dms.module.dict.service;

import com.company.dms.module.dict.dto.DictItemSaveDTO;
import com.company.dms.module.dict.dto.DictTypeSaveDTO;
import com.company.dms.module.dict.entity.DictItem;
import com.company.dms.module.dict.entity.DictType;

import java.util.Collection;
import java.util.List;

public interface DictService {
    List<DictType> listTypes();

    Long createType(DictTypeSaveDTO dto);

    void updateType(Long id, DictTypeSaveDTO dto);

    void deleteType(Long id);

    List<DictItem> listItems(String dictType, boolean activeOnly);

    DictItem getItem(Long id);

    Long createItem(DictItemSaveDTO dto);

    void updateItem(Long id, DictItemSaveDTO dto);

    void deleteItem(Long id);

    void ensureItems(String dictType, Collection<String> labels);
}
