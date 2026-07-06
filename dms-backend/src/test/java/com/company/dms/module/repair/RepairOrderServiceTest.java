package com.company.dms.module.repair;

import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.PageResult;
import com.company.dms.module.repair.dto.RepairAcceptDTO;
import com.company.dms.module.repair.dto.RepairCompleteDTO;
import com.company.dms.module.repair.dto.RepairCreateDTO;
import com.company.dms.module.repair.dto.RepairQuery;
import com.company.dms.module.repair.entity.RepairOrder;
import com.company.dms.module.repair.service.RepairService;
import com.company.dms.module.repair.vo.RepairOrderVO;
import com.company.dms.module.resource.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RepairOrderServiceTest {

    @Autowired RepairService repairService;
    @Autowired RoomService roomService;

    @Test
    void create_generates_pending_order() {
        RepairCreateDTO dto = new RepairCreateDTO();
        dto.setRoomId(4L);
        dto.setResidentId(1L);
        dto.setTitle("空调漏水");
        dto.setPriority(2);

        Long id = repairService.create(dto);

        RepairOrder order = repairService.getOrder(id);
        assertTrue(order.getOrderNo().startsWith("RO-"));
        assertEquals(1, order.getStatus());
        assertEquals(4L, order.getRoomId());
        assertEquals(1L, order.getResidentId());
        assertEquals(3, roomService.getById(4L).getStatus());
    }

    @Test
    void create_rejects_missing_room() {
        RepairCreateDTO dto = new RepairCreateDTO();
        dto.setRoomId(999L);
        dto.setTitle("门锁故障");

        assertThrows(BizException.class, () -> repairService.create(dto));
    }

    @Test
    void accept_complete_and_cancel_have_state_guards() {
        Long id = createPending();

        RepairAcceptDTO accept = new RepairAcceptDTO();
        accept.setHandler("维修班张工");
        repairService.accept(id, accept);
        RepairOrder accepted = repairService.getOrder(id);
        assertEquals(2, accepted.getStatus());
        assertEquals("维修班张工", accepted.getHandler());
        assertNotNull(accepted.getAcceptedAt());
        assertThrows(BizException.class, () -> repairService.accept(id, accept));

        RepairCompleteDTO complete = new RepairCompleteDTO();
        complete.setResult("已更换门锁");
        repairService.complete(id, complete);
        RepairOrder completed = repairService.getOrder(id);
        assertEquals(3, completed.getStatus());
        assertEquals("已更换门锁", completed.getResult());
        assertNotNull(completed.getCompletedAt());
        assertThrows(BizException.class, () -> repairService.cancel(id));
    }

    @Test
    void cancel_pending_or_processing_only() {
        Long pending = createPending();
        repairService.cancel(pending);
        assertEquals(4, repairService.getOrder(pending).getStatus());
        assertEquals(1, roomService.getById(4L).getStatus());

        Long processing = createPending();
        RepairAcceptDTO accept = new RepairAcceptDTO();
        accept.setHandler("维修班李工");
        repairService.accept(processing, accept);
        repairService.cancel(processing);
        assertEquals(4, repairService.getOrder(processing).getStatus());
    }

    @Test
    void cancel_pending_keeps_room_repairing_until_last_open_order_is_closed() {
        Long first = createPending();
        Long second = createPending();

        repairService.cancel(first);
        assertEquals(3, roomService.getById(4L).getStatus());

        repairService.cancel(second);
        assertEquals(1, roomService.getById(4L).getStatus());
    }

    @Test
    void page_filters_and_returns_display_names() {
        Long id = createPending(2L);
        RepairQuery query = new RepairQuery();
        query.setStatus(1);
        query.setPriority(1);
        query.setRoomId(2L);

        PageResult<RepairOrderVO> page = repairService.pageOrders(query);

        assertTrue(page.getRecords().stream().anyMatch(o -> o.getId().equals(id)));
        RepairOrderVO vo = page.getRecords().stream().filter(o -> o.getId().equals(id)).findFirst().orElseThrow();
        assertEquals("A102", vo.getRoomNumber());
        assertEquals("A栋员工宿舍", vo.getBuildingName());
        assertEquals("张三", vo.getResidentName());
    }

    @Test
    void accept_marks_room_repairing_and_complete_restores_room_status() {
        Long id = createPending();

        RepairAcceptDTO accept = new RepairAcceptDTO();
        accept.setHandler("worker a");
        repairService.accept(id, accept);

        assertEquals(3, roomService.getById(4L).getStatus());

        RepairCompleteDTO complete = new RepairCompleteDTO();
        complete.setResult("fixed");
        repairService.complete(id, complete);

        assertEquals(1, roomService.getById(4L).getStatus());
    }

    @Test
    void cancel_processing_restores_room_status_after_last_processing_order() {
        Long first = createPending();
        Long second = createPending();
        RepairAcceptDTO accept = new RepairAcceptDTO();
        accept.setHandler("worker a");
        repairService.accept(first, accept);
        repairService.accept(second, accept);

        repairService.cancel(first);
        assertEquals(3, roomService.getById(4L).getStatus());

        repairService.cancel(second);
        assertEquals(1, roomService.getById(4L).getStatus());
    }

    private Long createPending() {
        return createPending(4L);
    }

    private Long createPending(Long roomId) {
        RepairCreateDTO dto = new RepairCreateDTO();
        dto.setRoomId(roomId);
        dto.setResidentId(1L);
        dto.setTitle("门锁故障");
        dto.setPriority(1);
        return repairService.create(dto);
    }
}
