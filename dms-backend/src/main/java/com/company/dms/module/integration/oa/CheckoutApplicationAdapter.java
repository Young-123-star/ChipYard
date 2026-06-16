package com.company.dms.module.integration.oa;

import com.company.dms.module.checkin.entity.CheckinRecord;
import com.company.dms.module.checkin.service.CheckinService;
import com.company.dms.module.checkout.dto.CreateCheckoutCommand;
import com.company.dms.module.checkout.service.CheckoutService;
import com.company.dms.module.checkout.vo.CheckoutResultVO;
import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.service.ResidentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CheckoutApplicationAdapter {

    private final ResidentService residentService;
    private final CheckinService checkinService;
    private final CheckoutService checkoutService;
    private final ObjectMapper objectMapper;

    public CheckoutApplicationAdapter(ResidentService residentService, CheckinService checkinService,
                                      CheckoutService checkoutService, ObjectMapper objectMapper) {
        this.residentService = residentService;
        this.checkinService = checkinService;
        this.checkoutService = checkoutService;
        this.objectMapper = objectMapper;
    }

    public CheckoutResultVO handle(OaCheckoutApplicationDTO dto) {
        Resident resident = residentService.getByEmployeeNo(dto.getEmployeeNo());
        if (resident == null) return CheckoutResultVO.of("NO_RESIDENT", null, false, "无居住记录");

        CheckinRecord active = checkinService.findActiveRecordByResident(resident.getId());
        if (active == null) return CheckoutResultVO.of("NO_ACTIVE_CHECKIN", null, false, "无在住记录");

        CreateCheckoutCommand cmd = new CreateCheckoutCommand();
        cmd.setBizNo(dto.getApplicationNo());
        cmd.setSource(1);
        cmd.setResidentId(resident.getId());
        cmd.setCheckinRecordId(active.getId());
        cmd.setReason(dto.getReason());
        cmd.setExpectCheckoutDate(parseDate(dto.getExpectDate()));
        cmd.setRawPayload(toJson(dto));
        Long orderId = checkoutService.createOrderFromCommand(cmd);
        return CheckoutResultVO.of("ORDER_CREATED", orderId, false, "已生成待退宿单");
    }

    private LocalDate parseDate(String s) {
        try { return s == null ? null : LocalDate.parse(s); } catch (Exception e) { return null; }
    }
    private String toJson(Object o) {
        try { return objectMapper.writeValueAsString(o); } catch (Exception e) { return null; }
    }
}
