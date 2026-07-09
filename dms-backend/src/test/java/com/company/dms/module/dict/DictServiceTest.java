package com.company.dms.module.dict;

import com.company.dms.common.exception.BizException;
import com.company.dms.module.dict.dto.DictItemSaveDTO;
import com.company.dms.module.dict.entity.DictItem;
import com.company.dms.module.dict.service.DictService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DictServiceTest {

    @Autowired DictService dictService;

    @Test
    void seeded_room_facility_items_are_available() {
        List<DictItem> items = dictService.listItems("ROOM_FACILITY", true);

        assertTrue(items.stream().anyMatch(i -> "空调".equals(i.getDictLabel())));
        assertTrue(items.stream().anyMatch(i -> "热水器".equals(i.getDictLabel())));
        assertTrue(items.stream().anyMatch(i -> "衣柜".equals(i.getDictLabel())));
        assertTrue(items.stream().anyMatch(i -> "书桌".equals(i.getDictLabel())));
    }

    @Test
    void system_item_cannot_change_value_or_be_deleted() {
        DictItem item = dictService.listItems("ROOM_STATUS", false).stream()
                .filter(i -> Integer.valueOf(1).equals(i.getSystemFlag()))
                .findFirst()
                .orElseThrow();

        DictItemSaveDTO update = new DictItemSaveDTO();
        update.setDictType(item.getDictType());
        update.setDictValue(item.getDictValue() + "_changed");
        update.setDictLabel(item.getDictLabel());
        update.setSortOrder(item.getSortOrder());
        update.setTagType(item.getTagType());
        update.setStatus(item.getStatus());
        update.setRemark(item.getRemark());

        assertThrows(BizException.class, () -> dictService.updateItem(item.getId(), update));
        assertThrows(BizException.class, () -> dictService.deleteItem(item.getId()));
    }

    @Test
    void custom_item_can_be_created_updated_disabled_and_deleted() {
        DictItemSaveDTO create = new DictItemSaveDTO();
        create.setDictType("ROOM_FACILITY");
        create.setDictValue("冰箱");
        create.setDictLabel("冰箱");
        create.setSortOrder(99);
        create.setTagType("info");
        create.setStatus(1);
        create.setRemark("test");

        Long id = dictService.createItem(create);
        DictItem created = dictService.getItem(id);
        assertEquals("冰箱", created.getDictLabel());
        assertEquals(0, created.getSystemFlag());

        create.setDictLabel("小冰箱");
        create.setStatus(0);
        dictService.updateItem(id, create);

        DictItem updated = dictService.getItem(id);
        assertEquals("小冰箱", updated.getDictLabel());
        assertEquals(0, updated.getStatus());

        dictService.deleteItem(id);
        assertThrows(BizException.class, () -> dictService.getItem(id));
    }
}
