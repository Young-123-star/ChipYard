package com.company.dms.module.fee.service;

import com.company.dms.common.result.PageResult;
import com.company.dms.module.fee.dto.MeterQuery;
import com.company.dms.module.fee.dto.MeterReadingDTO;
import com.company.dms.module.fee.dto.UtilityRateDTO;
import com.company.dms.module.fee.entity.MeterReading;
import com.company.dms.module.fee.entity.UtilityRate;
import com.company.dms.module.fee.vo.GenerateResultVO;
import com.company.dms.module.fee.vo.MeterReadingVO;

public interface MeterService {
    UtilityRate getRate();
    void updateRate(UtilityRateDTO dto);
    PageResult<MeterReadingVO> pageReadings(MeterQuery query);
    MeterReading getReading(Long id);
    /** 录入/更新本期读数（自动取上期、算用量金额；幂等 room+period+type）。 */
    Long saveReading(MeterReadingDTO dto);
    /** 按账期生成均摊水电账单（事务）。 */
    GenerateResultVO generateUtilityBills(String period);
}
