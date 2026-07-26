package com.company.dms.module.fee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.vo.PeriodSummaryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FeeBillMapper extends BaseMapper<FeeBill> {

    /** 按账期聚合账单金额（排除逻辑删除与作废账单），total/收缴率由 service 计算。 */
    @Select("SELECT period, "
            + "SUM(CASE WHEN bill_type = 2 THEN amount ELSE 0 END) AS elec_total, "
            + "SUM(CASE WHEN bill_type = 3 THEN amount ELSE 0 END) AS water_total, "
            + "SUM(CASE WHEN bill_type = 2 OR bill_type = 3 THEN 0 ELSE amount END) AS rent_total, "
            + "SUM(CASE WHEN status = 2 THEN amount ELSE 0 END) AS paid, "
            + "SUM(CASE WHEN status = 2 THEN 0 ELSE amount END) AS unpaid "
            + "FROM dms_fee_bill WHERE deleted_at IS NULL AND status <> 3 "
            + "GROUP BY period ORDER BY period")
    List<PeriodSummaryVO> selectPeriodSummary();
}
