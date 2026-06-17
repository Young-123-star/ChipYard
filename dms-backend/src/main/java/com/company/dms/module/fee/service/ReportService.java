package com.company.dms.module.fee.service;

import com.company.dms.module.fee.vo.BuildingSummaryVO;
import com.company.dms.module.fee.vo.PeriodSummaryVO;
import java.util.List;

public interface ReportService {
    /** 按账期汇总（period 升序）。 */
    List<PeriodSummaryVO> getPeriodSummary();

    /** 按楼栋汇总（buildingId 升序）。 */
    List<BuildingSummaryVO> getBuildingSummary();
}
