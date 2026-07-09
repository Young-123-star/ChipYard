package com.company.dms.module.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.dms.module.dict.service.DictService;
import com.company.dms.module.resource.dto.RoomSaveDTO;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RoomFacilitiesServiceTest {

    @Autowired RoomService roomService;
    @Autowired DictService dictService;
    @Autowired ObjectMapper objectMapper;

    @Test
    void save_room_normalizes_legacy_facilities_and_upserts_custom_names() throws Exception {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setBuildingId(1L);
        dto.setFloorId(1L);
        dto.setRoomNumber("T-FAC-901");
        dto.setRoomType(1);
        dto.setArea(new BigDecimal("20.5"));
        dto.setOrientation("南");
        dto.setBedCount(2);
        dto.setGenderLimit(0);
        dto.setStatus(1);
        dto.setFacilities("{\"air_conditioner\":1,\"冰箱\":2,\"desk\":0,\"\":3}");

        Long id = roomService.create(dto);
        Room room = roomService.getById(id);
        Map<String, Integer> facilities = objectMapper.readValue(room.getFacilities(), new TypeReference<>() {});

        assertEquals(1, facilities.get("空调"));
        assertEquals(2, facilities.get("冰箱"));
        assertFalse(facilities.containsKey("air_conditioner"));
        assertFalse(facilities.containsKey("书桌"));
        assertTrue(dictService.listItems("ROOM_FACILITY", true).stream()
                .anyMatch(i -> "冰箱".equals(i.getDictLabel())));
    }

    @Test
    void update_room_stores_new_facility_format() throws Exception {
        RoomSaveDTO dto = new RoomSaveDTO();
        dto.setBuildingId(1L);
        dto.setFloorId(1L);
        dto.setRoomNumber("T-FAC-902");
        dto.setRoomType(1);
        dto.setArea(new BigDecimal("18.0"));
        dto.setOrientation("北");
        dto.setBedCount(2);
        dto.setGenderLimit(0);
        dto.setStatus(1);
        dto.setFacilities("{\"空调\":1}");

        Long id = roomService.create(dto);
        dto.setFacilities("{\"洗衣机\":1,\"热水器\":1}");
        roomService.update(id, dto);

        Room room = roomService.getById(id);
        Map<String, Integer> facilities = objectMapper.readValue(room.getFacilities(), new TypeReference<>() {});
        assertEquals(1, facilities.get("洗衣机"));
        assertEquals(1, facilities.get("热水器"));
        assertTrue(dictService.listItems("ROOM_FACILITY", true).stream()
                .anyMatch(i -> "洗衣机".equals(i.getDictLabel())));
    }
}
