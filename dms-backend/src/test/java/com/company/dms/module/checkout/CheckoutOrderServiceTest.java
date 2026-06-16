package com.company.dms.module.checkout;

import com.company.dms.common.exception.BizException;
import com.company.dms.module.checkout.dto.CheckoutCreateDTO;
import com.company.dms.module.checkout.dto.CreateCheckoutCommand;
import com.company.dms.module.checkout.service.CheckoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CheckoutOrderServiceTest {

    @Autowired CheckoutService checkoutService;

    @Test
    void manual_create_for_active_resident() {
        CheckoutCreateDTO dto = new CheckoutCreateDTO();
        dto.setResidentId(1L);
        dto.setReason("手工退宿");
        Long id = checkoutService.createManual(dto);
        assertNotNull(id);
    }

    @Test
    void manual_create_rejected_when_no_active_checkin() {
        CheckoutCreateDTO dto = new CheckoutCreateDTO();
        dto.setResidentId(3L);
        assertThrows(BizException.class, () -> checkoutService.createManual(dto));
    }

    @Test
    void create_from_command_idempotent_by_biz_no() {
        CreateCheckoutCommand cmd = new CreateCheckoutCommand();
        cmd.setBizNo("CO-DUP-1");
        cmd.setSource(1);
        cmd.setResidentId(1L);
        cmd.setCheckinRecordId(1L);
        Long id1 = checkoutService.createOrderFromCommand(cmd);
        Long id2 = checkoutService.createOrderFromCommand(cmd);
        assertEquals(id1, id2);
    }

    @Test
    void cancel_pending_order() {
        checkoutService.cancel(1L);
        assertEquals(3, checkoutService.getOrder(1L).getStatus());
    }
}
