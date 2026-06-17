package com.company.dms.module.fee.service;

import com.company.dms.module.fee.vo.PeriodSummaryVO;
import java.util.List;

public interface ReportService {
    /** 按账期汇总（period 升序）。 */
    List<PeriodSummaryVO> getPeriodSummary();
}
