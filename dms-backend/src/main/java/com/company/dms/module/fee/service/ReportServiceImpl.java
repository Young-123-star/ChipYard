package com.company.dms.module.fee.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.entity.MeterReading;
import com.company.dms.module.fee.entity.UtilitySettlement;
import com.company.dms.module.fee.mapper.FeeBillMapper;
import com.company.dms.module.fee.mapper.MeterReadingMapper;
import com.company.dms.module.fee.mapper.UtilitySettlementMapper;
import com.company.dms.module.fee.vo.ArrearsRankVO;
import com.company.dms.module.fee.vo.BuildingSummaryVO;
import com.company.dms.module.fee.vo.PeriodSummaryVO;
import com.company.dms.module.fee.vo.UsageTrendVO;
import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.service.ResidentService;
import com.company.dms.module.resource.entity.Building;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.mapper.BuildingMapper;
import com.company.dms.module.resource.mapper.RoomMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final FeeBillMapper billMapper;
    private final MeterReadingMapper readingMapper;
    private final UtilitySettlementMapper settlementMapper;
    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;
    private final ResidentService residentService;

    public ReportServiceImpl(FeeBillMapper billMapper,
                             MeterReadingMapper readingMapper,
                             UtilitySettlementMapper settlementMapper,
                             RoomMapper roomMapper,
                             BuildingMapper buildingMapper,
                             ResidentService residentService) {
        this.billMapper = billMapper;
        this.readingMapper = readingMapper;
        this.settlementMapper = settlementMapper;
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
        this.residentService = residentService;
    }

    /** 收缴率 = paid×100/total，2 位 HALF_UP；total 为 0 时返回 0。 */
    private static BigDecimal collectRate(BigDecimal paid, BigDecimal total) {
        if (total.signum() == 0) return BigDecimal.ZERO;
        return paid.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<PeriodSummaryVO> getPeriodSummary() {
        // 数据库按账期聚合，避免全表加载
        List<PeriodSummaryVO> result = billMapper.selectPeriodSummary();
        for (PeriodSummaryVO vo : result) {
            vo.setTotal(vo.getRentTotal().add(vo.getElecTotal()).add(vo.getWaterTotal()));
            vo.setCollectRate(collectRate(vo.getPaid(), vo.getTotal()));
        }
        return result;
    }

    @Override
    public List<BuildingSummaryVO> getBuildingSummary() {
        List<FeeBill> bills = billMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                .ne(FeeBill::getStatus, 3).isNotNull(FeeBill::getRoomId));   // 排除作废 + 必须有房间
        Map<Long, Long> roomToBuilding = roomMapper.selectList(Wrappers.<Room>lambdaQuery()).stream()
                .filter(r -> r.getBuildingId() != null)
                .collect(Collectors.toMap(Room::getId, Room::getBuildingId)); // dms_room 全量一次查
        Map<Long, String> buildingNames = buildingMapper.selectList(Wrappers.<Building>lambdaQuery()).stream()
                .collect(Collectors.toMap(Building::getId, Building::getBuildingName)); // dms_building 全量一次查
        Map<Long, BuildingSummaryVO> map = new TreeMap<>(); // buildingId 升序
        for (FeeBill b : bills) {
            Long buildingId = roomToBuilding.get(b.getRoomId());
            if (buildingId == null) continue;                               // 房间不存在则跳过
            BuildingSummaryVO vo = map.get(buildingId);
            if (vo == null) {
                vo = new BuildingSummaryVO();
                vo.setBuildingId(buildingId);
                vo.setBuildingName(buildingNames.get(buildingId));          // 楼栋不存在则名称留空
                vo.setTotal(BigDecimal.ZERO);
                vo.setPaid(BigDecimal.ZERO);
                vo.setUnpaid(BigDecimal.ZERO);
                map.put(buildingId, vo);
            }
            vo.setTotal(vo.getTotal().add(b.getAmount()));
            if (b.getStatus() == 2) vo.setPaid(vo.getPaid().add(b.getAmount()));
            else vo.setUnpaid(vo.getUnpaid().add(b.getAmount()));
        }
        List<BuildingSummaryVO> result = new ArrayList<>(map.values());
        for (BuildingSummaryVO vo : result) {
            vo.setCollectRate(collectRate(vo.getPaid(), vo.getTotal()));
        }
        return result;
    }

    @Override
    public List<ArrearsRankVO> getArrearsRanking(int limit) {
        int top = limit <= 0 ? 10 : limit;
        List<FeeBill> bills = billMapper.selectList(
                Wrappers.<FeeBill>lambdaQuery().in(FeeBill::getStatus, 1, 4)); // 未缴 + 挂账
        List<Long> residentIds = bills.stream()
                .map(FeeBill::getResidentId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Resident> residentMap = residentIds.isEmpty() ? Map.of()
                : residentService.listByIds(residentIds).stream()
                        .collect(Collectors.toMap(Resident::getId, Function.identity()));
        Map<Long, ArrearsRankVO> map = new HashMap<>();
        for (FeeBill b : bills) {
            ArrearsRankVO vo = map.get(b.getResidentId());
            if (vo == null) {
                vo = new ArrearsRankVO();
                vo.setResidentId(b.getResidentId());
                vo.setUnpaidAmount(BigDecimal.ZERO);
                vo.setUnpaidCount(0);
                Resident r = residentMap.get(b.getResidentId());
                if (r != null) {                                 // 居住人不存在则留空
                    vo.setResidentName(r.getRealName());
                    vo.setEmployeeNo(r.getEmployeeNo());
                }
                map.put(b.getResidentId(), vo);
            }
            vo.setUnpaidAmount(vo.getUnpaidAmount().add(b.getAmount()));
            vo.setUnpaidCount(vo.getUnpaidCount() + 1);
        }
        return map.values().stream()
                .sorted(Comparator.comparing(ArrearsRankVO::getUnpaidAmount).reversed())
                .limit(top)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsageTrendVO> getUsageTrend() {
        List<UtilitySettlement> settlements =
                settlementMapper.selectList(Wrappers.<UtilitySettlement>lambdaQuery()
                        .eq(UtilitySettlement::getStatus, 1));
        if (!settlements.isEmpty()) {
            Map<String, UsageTrendVO> settled = new TreeMap<>();
            for (UtilitySettlement settlement : settlements) {
                UsageTrendVO value = settled.computeIfAbsent(settlement.getPeriod(), p -> {
                    UsageTrendVO trend = new UsageTrendVO();
                    trend.setPeriod(p);
                    trend.setElectricity(BigDecimal.ZERO);
                    trend.setWater(BigDecimal.ZERO);
                    return trend;
                });
                value.setElectricity(value.getElectricity().add(settlement.getElectricityUsage()));
                value.setWater(value.getWater().add(settlement.getWaterUsage()));
            }
            return new ArrayList<>(settled.values());
        }
        List<MeterReading> readings =
                readingMapper.selectList(Wrappers.<MeterReading>lambdaQuery());
        Map<String, UsageTrendVO> map = new TreeMap<>(); // period 升序
        for (MeterReading m : readings) {
            UsageTrendVO vo = map.computeIfAbsent(m.getPeriod(), p -> {
                UsageTrendVO v = new UsageTrendVO();
                v.setPeriod(p);
                v.setElectricity(BigDecimal.ZERO);
                v.setWater(BigDecimal.ZERO);
                return v;
            });
            if (m.getMeterType() != null && m.getMeterType() == 1)
                vo.setElectricity(vo.getElectricity().add(m.getConsumption()));
            else if (m.getMeterType() != null && m.getMeterType() == 2)
                vo.setWater(vo.getWater().add(m.getConsumption()));
        }
        return new ArrayList<>(map.values());
    }
}
