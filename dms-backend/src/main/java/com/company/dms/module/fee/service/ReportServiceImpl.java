package com.company.dms.module.fee.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.mapper.FeeBillMapper;
import com.company.dms.module.fee.vo.PeriodSummaryVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ReportServiceImpl implements ReportService {

    private final FeeBillMapper billMapper;

    public ReportServiceImpl(FeeBillMapper billMapper) {
        this.billMapper = billMapper;
    }

    /** 收缴率 = paid×100/total，2 位 HALF_UP；total 为 0 时返回 0。 */
    private static BigDecimal collectRate(BigDecimal paid, BigDecimal total) {
        if (total.signum() == 0) return BigDecimal.ZERO;
        return paid.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<PeriodSummaryVO> getPeriodSummary() {
        List<FeeBill> bills = billMapper.selectList(
                Wrappers.<FeeBill>lambdaQuery().ne(FeeBill::getStatus, 3)); // 排除作废
        Map<String, PeriodSummaryVO> map = new TreeMap<>();                 // period 升序
        for (FeeBill b : bills) {
            PeriodSummaryVO vo = map.computeIfAbsent(b.getPeriod(), p -> {
                PeriodSummaryVO v = new PeriodSummaryVO();
                v.setPeriod(p);
                v.setRentTotal(BigDecimal.ZERO);
                v.setElecTotal(BigDecimal.ZERO);
                v.setWaterTotal(BigDecimal.ZERO);
                v.setPaid(BigDecimal.ZERO);
                v.setUnpaid(BigDecimal.ZERO);
                return v;
            });
            BigDecimal amt = b.getAmount();
            Integer t = b.getBillType();
            if (t != null && t == 2) vo.setElecTotal(vo.getElecTotal().add(amt));
            else if (t != null && t == 3) vo.setWaterTotal(vo.getWaterTotal().add(amt));
            else vo.setRentTotal(vo.getRentTotal().add(amt));
            if (b.getStatus() == 2) vo.setPaid(vo.getPaid().add(amt));
            else vo.setUnpaid(vo.getUnpaid().add(amt));                      // status 1 或 4（3 已排除）
        }
        List<PeriodSummaryVO> result = new ArrayList<>(map.values());
        for (PeriodSummaryVO vo : result) {
            vo.setTotal(vo.getRentTotal().add(vo.getElecTotal()).add(vo.getWaterTotal()));
            vo.setCollectRate(collectRate(vo.getPaid(), vo.getTotal()));
        }
        return result;
    }
}
