package com.company.dms.module.fee.service;

import com.company.dms.module.fee.vo.ArrearsRankVO;
import com.company.dms.module.fee.vo.BuildingSummaryVO;
import com.company.dms.module.fee.vo.PeriodSummaryVO;
import com.company.dms.module.fee.vo.UsageTrendVO;
import java.util.List;

public interface ReportService {
    /** 按账期汇总（period 升序）。 */
    List<PeriodSummaryVO> getPeriodSummary();

    /** 按楼栋汇总（buildingId 升序）。 */
    List<BuildingSummaryVO> getBuildingSummary();

    /** 欠费排行（未缴+挂账，按欠费额降序取前 limit；limit<=0 视为 10）。 */
    List<ArrearsRankVO> getArrearsRanking(int limit);

    /** 水电用量趋势（period 升序，按表型合计 consumption）。 */
    List<UsageTrendVO> getUsageTrend();
}
