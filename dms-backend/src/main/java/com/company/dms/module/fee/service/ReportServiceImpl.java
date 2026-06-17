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
    private final com.company.dms.module.resource.service.RoomService roomService;
    private final com.company.dms.module.resource.service.BuildingService buildingService;
    private final com.company.dms.module.resident.service.ResidentService residentService;

    public ReportServiceImpl(FeeBillMapper billMapper,
                             com.company.dms.module.resource.service.RoomService roomService,
                             com.company.dms.module.resource.service.BuildingService buildingService,
                             com.company.dms.module.resident.service.ResidentService residentService) {
        this.billMapper = billMapper;
        this.roomService = roomService;
        this.buildingService = buildingService;
        this.residentService = residentService;
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

    @Override
    public List<com.company.dms.module.fee.vo.BuildingSummaryVO> getBuildingSummary() {
        List<FeeBill> bills = billMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                .ne(FeeBill::getStatus, 3).isNotNull(FeeBill::getRoomId));   // 排除作废 + 必须有房间
        java.util.Map<Long, Long> roomToBuilding = new java.util.HashMap<>(); // roomId→buildingId 缓存
        Map<Long, com.company.dms.module.fee.vo.BuildingSummaryVO> map = new TreeMap<>(); // buildingId 升序
        for (FeeBill b : bills) {
            Long buildingId = roomToBuilding.computeIfAbsent(b.getRoomId(), rid -> {
                try { return roomService.getById(rid).getBuildingId(); }
                catch (Exception e) { return null; }                         // 房间不存在则跳过
            });
            if (buildingId == null) continue;
            com.company.dms.module.fee.vo.BuildingSummaryVO vo = map.get(buildingId);
            if (vo == null) {
                vo = new com.company.dms.module.fee.vo.BuildingSummaryVO();
                vo.setBuildingId(buildingId);
                try { vo.setBuildingName(buildingService.getById(buildingId).getBuildingName()); }
                catch (Exception ignore) { /* 楼栋不存在则名称留空 */ }
                vo.setTotal(BigDecimal.ZERO);
                vo.setPaid(BigDecimal.ZERO);
                vo.setUnpaid(BigDecimal.ZERO);
                map.put(buildingId, vo);
            }
            vo.setTotal(vo.getTotal().add(b.getAmount()));
            if (b.getStatus() == 2) vo.setPaid(vo.getPaid().add(b.getAmount()));
            else vo.setUnpaid(vo.getUnpaid().add(b.getAmount()));
        }
        List<com.company.dms.module.fee.vo.BuildingSummaryVO> result = new ArrayList<>(map.values());
        for (com.company.dms.module.fee.vo.BuildingSummaryVO vo : result) {
            vo.setCollectRate(collectRate(vo.getPaid(), vo.getTotal()));
        }
        return result;
    }

    @Override
    public List<com.company.dms.module.fee.vo.ArrearsRankVO> getArrearsRanking(int limit) {
        int top = limit <= 0 ? 10 : limit;
        List<FeeBill> bills = billMapper.selectList(
                Wrappers.<FeeBill>lambdaQuery().in(FeeBill::getStatus, 1, 4)); // 未缴 + 挂账
        Map<Long, com.company.dms.module.fee.vo.ArrearsRankVO> map = new java.util.HashMap<>();
        for (FeeBill b : bills) {
            com.company.dms.module.fee.vo.ArrearsRankVO vo = map.get(b.getResidentId());
            if (vo == null) {
                vo = new com.company.dms.module.fee.vo.ArrearsRankVO();
                vo.setResidentId(b.getResidentId());
                vo.setUnpaidAmount(BigDecimal.ZERO);
                vo.setUnpaidCount(0);
                try {
                    com.company.dms.module.resident.entity.Resident r = residentService.getById(b.getResidentId());
                    vo.setResidentName(r.getRealName());
                    vo.setEmployeeNo(r.getEmployeeNo());
                } catch (Exception ignore) { /* 居住人不存在则留空 */ }
                map.put(b.getResidentId(), vo);
            }
            vo.setUnpaidAmount(vo.getUnpaidAmount().add(b.getAmount()));
            vo.setUnpaidCount(vo.getUnpaidCount() + 1);
        }
        return map.values().stream()
                .sorted(java.util.Comparator.comparing(
                        com.company.dms.module.fee.vo.ArrearsRankVO::getUnpaidAmount).reversed())
                .limit(top)
                .collect(java.util.stream.Collectors.toList());
    }
}
