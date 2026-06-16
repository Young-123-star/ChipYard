package com.company.dms.module.fee.service;

import com.company.dms.common.result.PageResult;
import com.company.dms.module.fee.dto.BillQuery;
import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.vo.FeeBillVO;
import com.company.dms.module.fee.vo.GenerateResultVO;

public interface FeeBillService {
    PageResult<FeeBillVO> pageBills(BillQuery query);
    FeeBill getBill(Long id);
    /** 测试/内部用：按档案+账期取账单，无则 null。 */
    FeeBill getByRecordAndPeriod(Long checkinRecordId, String period);
    /** 对所有在住档案按账期幂等生成账单。 */
    GenerateResultVO generate(String period);
    /** 缴费：未缴 → 已缴 + 记时间/方式。 */
    void pay(Long id, Integer payMethod);
    /** 作废：未缴 → 作废。 */
    void voidBill(Long id);
    /** 查某在住档案的未缴(1)账单。 */
    java.util.List<FeeBill> listUnpaidByRecord(Long checkinRecordId);
    /** 结算挂账：该档案未缴账单置挂账(4)，返回欠费总额（无则 0）。 */
    java.math.BigDecimal settleArrearsForRecord(Long checkinRecordId);
}
