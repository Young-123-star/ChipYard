package com.company.dms.module.checkout;

import com.company.dms.module.checkout.service.CheckoutService;
import com.company.dms.module.fee.service.FeeBillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CheckoutArrearsTest {

    @Autowired CheckoutService checkoutService;
    @Autowired FeeBillService billService;

    @Test
    void confirm_settles_arrears_and_records_amount() {
        checkoutService.confirm(1L, LocalDate.of(2026, 7, 1));
        assertEquals(0, new BigDecimal("800.00").compareTo(checkoutService.getOrder(1L).getArrearsAmount()));
        assertEquals(4, billService.getBill(1L).getStatus());
        assertEquals(2, checkoutService.getOrder(1L).getStatus());
    }
}
