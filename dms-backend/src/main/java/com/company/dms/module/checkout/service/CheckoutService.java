package com.company.dms.module.checkout.service;

import com.company.dms.common.result.PageResult;
import com.company.dms.module.checkout.dto.CheckoutCreateDTO;
import com.company.dms.module.checkout.dto.CheckoutQuery;
import com.company.dms.module.checkout.dto.CreateCheckoutCommand;
import com.company.dms.module.checkout.entity.CheckoutOrder;
import com.company.dms.module.checkout.vo.CheckoutOrderVO;

public interface CheckoutService {
    PageResult<CheckoutOrderVO> pageOrders(CheckoutQuery query);
    CheckoutOrder getOrder(Long id);
    /** 手工新建：解析居住人在住档案，无在住则拒绝。 */
    Long createManual(CheckoutCreateDTO dto);
    /** 供适配器调用；按 bizNo 幂等。调用方已解析 residentId/checkinRecordId。 */
    Long createOrderFromCommand(CreateCheckoutCommand cmd);
    /** 办理退宿：释放床位 + 刷新房间统计 + 档案置已退宿 + 退宿单置已退宿（事务）。 */
    void confirm(Long orderId, java.time.LocalDate checkoutDate);
    void cancel(Long orderId);
}
