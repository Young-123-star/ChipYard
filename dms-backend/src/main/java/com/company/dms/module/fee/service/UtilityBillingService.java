package com.company.dms.module.fee.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.common.exception.BizException;
import com.company.dms.module.checkin.entity.CheckinRecord;
import com.company.dms.module.checkin.service.CheckinService;
import com.company.dms.module.fee.dto.MeterReadingDTO;
import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.entity.MeterReading;
import com.company.dms.module.fee.entity.UtilityRate;
import com.company.dms.module.fee.entity.UtilityRoomResult;
import com.company.dms.module.fee.entity.UtilitySettlement;
import com.company.dms.module.fee.mapper.FeeBillMapper;
import com.company.dms.module.fee.mapper.MeterReadingMapper;
import com.company.dms.module.fee.mapper.UtilityRoomResultMapper;
import com.company.dms.module.fee.mapper.UtilitySettlementMapper;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.mapper.RoomMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UtilityBillingService {
    private static final BigDecimal ELECTRIC_ALLOWANCE = new BigDecimal("250");
    private static final BigDecimal HOUSEHOLD_WATER_ALLOWANCE = new BigDecimal("50");
    private static final BigDecimal ROOM_WATER_ALLOWANCE = new BigDecimal("17");

    private final RoomMapper roomMapper;
    private final MeterReadingMapper readingMapper;
    private final UtilitySettlementMapper settlementMapper;
    private final UtilityRoomResultMapper resultMapper;
    private final FeeBillMapper billMapper;
    private final CheckinService checkinService;
    private final MeterService meterService;

    public UtilityBillingService(RoomMapper roomMapper, MeterReadingMapper readingMapper,
                                 UtilitySettlementMapper settlementMapper, UtilityRoomResultMapper resultMapper,
                                 FeeBillMapper billMapper, CheckinService checkinService, MeterService meterService) {
        this.roomMapper = roomMapper;
        this.readingMapper = readingMapper;
        this.settlementMapper = settlementMapper;
        this.resultMapper = resultMapper;
        this.billMapper = billMapper;
        this.checkinService = checkinService;
        this.meterService = meterService;
    }

    public List<Map<String, Object>> listAccounts(Long buildingId) {
        return groups(buildingId).stream().map(group -> {
            Map<String, Object> item = new LinkedHashMap<>();
            Room first = group.rooms().get(0);
            item.put("buildingId", group.buildingId());
            item.put("accountCode", group.accountCode());
            item.put("settlementMode", first.getSettlementMode());
            item.put("electricityRule", first.getElectricityRule());
            item.put("waterRule", first.getWaterRule());
            item.put("roomIds", group.rooms().stream().map(Room::getId).toList());
            item.put("roomNumbers", group.rooms().stream().map(Room::getRoomNumber).toList());
            item.put("configured", accountErrors(group).isEmpty());
            item.put("errors", accountErrors(group));
            return item;
        }).toList();
    }

    public Long saveReading(MeterReadingDTO dto) {
        Group group = findGroup(dto.getBuildingId(), dto.getAccountCode());
        if (!accountErrors(group).isEmpty()) throw new BizException(String.join("; ", accountErrors(group)));
        if (dto.getTargetType() == null || (dto.getTargetType() != 1 && dto.getTargetType() != 2)) {
            throw new BizException("invalid meter target");
        }
        if (dto.getMeterType() == null || dto.getMeterType() < 1 || dto.getMeterType() > 3) {
            throw new BizException("invalid meter type");
        }
        Long roomId = canonicalRoomId(group, dto.getTargetType(), dto.getRoomId());
        ensureUnlocked(group, dto.getPeriod());

        MeterReading previous = readingMapper.selectOne(Wrappers.<MeterReading>lambdaQuery()
                .eq(MeterReading::getBuildingId, group.buildingId())
                .eq(MeterReading::getAccountCode, group.accountCode())
                .eq(MeterReading::getTargetType, dto.getTargetType())
                .eq(MeterReading::getRoomId, roomId)
                .eq(MeterReading::getMeterType, dto.getMeterType())
                .lt(MeterReading::getPeriod, dto.getPeriod())
                .orderByDesc(MeterReading::getPeriod).last("limit 1"));
        BigDecimal previousValue = previous == null ? dto.getPrevReading() : previous.getCurrentReading();
        if (previousValue == null) throw new BizException("first reading requires previous reading");
        BigDecimal consumption = dto.getCurrentReading().subtract(previousValue);
        if (consumption.signum() < 0) throw new BizException("current reading cannot be less than previous reading");

        MeterReading reading = readingMapper.selectOne(Wrappers.<MeterReading>lambdaQuery()
                .eq(MeterReading::getBuildingId, group.buildingId())
                .eq(MeterReading::getAccountCode, group.accountCode())
                .eq(MeterReading::getTargetType, dto.getTargetType())
                .eq(MeterReading::getRoomId, roomId)
                .eq(MeterReading::getPeriod, dto.getPeriod())
                .eq(MeterReading::getMeterType, dto.getMeterType()).last("limit 1"));
        boolean update = reading != null;
        if (!update) reading = new MeterReading();
        UtilityRate rate = meterService.getRate();
        BigDecimal price = dto.getMeterType() == 1 ? rate.getElectricityPrice() : rate.getWaterPrice();
        reading.setBuildingId(group.buildingId());
        reading.setAccountCode(group.accountCode());
        reading.setTargetType(dto.getTargetType());
        reading.setRoomId(roomId);
        reading.setPeriod(dto.getPeriod());
        reading.setMeterType(dto.getMeterType());
        reading.setPrevReading(previousValue);
        reading.setCurrentReading(dto.getCurrentReading());
        reading.setConsumption(consumption);
        reading.setUnitPrice(price);
        reading.setAmount(money(consumption.multiply(price)));
        if (update) readingMapper.updateById(reading); else readingMapper.insert(reading);
        return reading.getId();
    }

    public List<MeterReading> listReadings(String period, Long buildingId, String accountCode) {
        return readingMapper.selectList(Wrappers.<MeterReading>lambdaQuery()
                .eq(period != null && !period.isBlank(), MeterReading::getPeriod, period)
                .eq(buildingId != null, MeterReading::getBuildingId, buildingId)
                .eq(accountCode != null && !accountCode.isBlank(), MeterReading::getAccountCode, accountCode)
                .isNotNull(MeterReading::getBuildingId)
                .orderByDesc(MeterReading::getId));
    }

    public Map<String, Object> preview(String period) {
        List<String> errors = new ArrayList<>();
        errors.addAll(unconfiguredErrors());
        List<Map<String, Object>> accounts = new ArrayList<>();
        for (Group group : groups(null)) {
            try {
                AccountCalculation calc = calculate(group, period);
                accounts.add(summary(calc));
            } catch (BizException e) {
                errors.add(group.accountCode() + ": " + e.getMessage());
            }
        }
        Map<String, Object> preview = new LinkedHashMap<>();
        preview.put("valid", errors.isEmpty());
        preview.put("errors", errors);
        preview.put("accounts", accounts);
        preview.put("cycleStart", cycleEnd(period).minusMonths(1).withDayOfMonth(25));
        preview.put("cycleEnd", cycleEnd(period));
        return preview;
    }

    @Transactional
    public Map<String, Object> generate(String period) {
        List<AccountCalculation> calculations = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        errors.addAll(unconfiguredErrors());
        for (Group group : groups(null)) {
            try {
                calculations.add(calculate(group, period));
            } catch (BizException e) {
                errors.add(group.accountCode() + ": " + e.getMessage());
            }
        }
        if (!errors.isEmpty()) throw new BizException(String.join("; ", errors));

        int bills = 0;
        int settlements = 0;
        for (AccountCalculation calc : calculations) {
            if (activeSettlement(calc.group(), period) != null) continue;
            UtilitySettlement settlement = new UtilitySettlement();
            settlement.setBuildingId(calc.group().buildingId());
            settlement.setAccountCode(calc.group().accountCode());
            settlement.setPeriod(period);
            settlement.setCycleEnd(cycleEnd(period));
            settlement.setCycleStart(cycleEnd(period).minusMonths(1).withDayOfMonth(25));
            settlement.setElectricityPrice(calc.rate().getElectricityPrice());
            settlement.setWaterPrice(calc.rate().getWaterPrice());
            settlement.setElectricityUsage(calc.electricityUsage());
            settlement.setWaterUsage(calc.waterUsage());
            settlement.setTotalCost(sum(calc.results(), RoomCalculation::totalCost));
            settlement.setEmployeeAmount(sum(calc.results(), RoomCalculation::employeeAmount));
            settlement.setCompanyAmount(settlement.getTotalCost().subtract(settlement.getEmployeeAmount()));
            settlement.setStatus(1);
            settlementMapper.insert(settlement);
            settlements++;

            for (RoomCalculation roomCalc : calc.results()) {
                UtilityRoomResult result = new UtilityRoomResult();
                result.setSettlementId(settlement.getId());
                result.setRoomId(roomCalc.room().getId());
                result.setUtilityType(roomCalc.utilityType());
                result.setActualUsage(roomCalc.actualUsage());
                result.setAllowanceUsage(roomCalc.allowance());
                result.setExcessUsage(roomCalc.excessUsage());
                result.setTotalCost(roomCalc.totalCost());
                result.setEmployeeAmount(roomCalc.employeeAmount());
                result.setCompanyAmount(roomCalc.totalCost().subtract(roomCalc.employeeAmount()));
                result.setOccupantCount(roomCalc.occupants().size());
                result.setCalculationNote(roomCalc.note());
                resultMapper.insert(result);
                bills += createBills(result, roomCalc, period);
            }
        }
        Map<String, Object> generated = new LinkedHashMap<>();
        generated.put("settlements", settlements);
        generated.put("bills", bills);
        return generated;
    }

    public List<UtilitySettlement> listSettlements(String period) {
        return settlementMapper.selectList(Wrappers.<UtilitySettlement>lambdaQuery()
                .eq(period != null && !period.isBlank(), UtilitySettlement::getPeriod, period)
                .orderByDesc(UtilitySettlement::getId));
    }

    @Transactional
    public void voidSettlement(Long id) {
        UtilitySettlement settlement = settlementMapper.selectById(id);
        if (settlement == null) throw new BizException("settlement not found");
        if (settlement.getStatus() != 1) throw new BizException("only active settlement can be voided");
        List<Long> resultIds = resultMapper.selectList(Wrappers.<UtilityRoomResult>lambdaQuery()
                .eq(UtilityRoomResult::getSettlementId, id)).stream().map(UtilityRoomResult::getId).toList();
        if (!resultIds.isEmpty()) {
            List<FeeBill> bills = billMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                    .in(FeeBill::getUtilityResultId, resultIds));
            if (bills.stream().anyMatch(b -> b.getStatus() == 2)) throw new BizException("paid bills block void");
            for (FeeBill bill : bills) {
                bill.setStatus(3);
                billMapper.updateById(bill);
            }
        }
        settlement.setStatus(2);
        settlementMapper.updateById(settlement);
    }

    private AccountCalculation calculate(Group group, String period) {
        List<String> configErrors = accountErrors(group);
        if (!configErrors.isEmpty()) throw new BizException(String.join(", ", configErrors));
        if (activeSettlement(group, period) != null) throw new BizException("settlement already generated");
        UtilityRate rate = meterService.getRate();
        LocalDate cutoff = cycleEnd(period);
        List<RoomCalculation> results = new ArrayList<>();
        BigDecimal electricityUsage = BigDecimal.ZERO;
        BigDecimal waterUsage = BigDecimal.ZERO;
        int electricRule = value(group.rooms().get(0).getElectricityRule());
        int waterRule = value(group.rooms().get(0).getWaterRule());

        if (electricRule != 0) {
            if (electricRule == 1) {
                BigDecimal master = reading(group, period, 1, firstRoom(group).getId(), 1);
                BigDecimal subTotal = BigDecimal.ZERO;
                List<BigDecimal> roomReadings = new ArrayList<>();
                for (Room room : group.rooms()) {
                    BigDecimal use = reading(group, period, 2, room.getId(), 1);
                    roomReadings.add(use);
                    subTotal = subTotal.add(use);
                }
                BigDecimal common = master.subtract(subTotal);
                if (common.signum() < 0) throw new BizException("household electricity common usage is negative");
                BigDecimal commonShare = common.divide(BigDecimal.valueOf(group.rooms().size()), 8, RoundingMode.HALF_UP);
                List<BigDecimal> actuals = roomReadings.stream().map(v -> v.add(commonShare)).toList();
                List<BigDecimal> costs = allocate(master.multiply(rate.getElectricityPrice()), actuals);
                for (int i = 0; i < group.rooms().size(); i++) {
                    results.add(roomCalculation(group.rooms().get(i), 1, actuals.get(i), ELECTRIC_ALLOWANCE,
                            costs.get(i), electricRule, rate.getElectricityPrice(), cutoff));
                }
                electricityUsage = master;
            } else {
                Room room = onlyRoom(group);
                BigDecimal use = reading(group, period, 2, room.getId(), 1);
                results.add(roomCalculation(room, 1, use, ELECTRIC_ALLOWANCE,
                        money(use.multiply(rate.getElectricityPrice())), electricRule, rate.getElectricityPrice(), cutoff));
                electricityUsage = use;
            }
        }

        if (waterRule != 0) {
            if (waterRule == 1) {
                BigDecimal use = reading(group, period, 1, firstRoom(group).getId(), 2)
                        .add(reading(group, period, 1, firstRoom(group).getId(), 3));
                BigDecimal roomUse = use.divide(BigDecimal.valueOf(group.rooms().size()), 8, RoundingMode.HALF_UP);
                List<BigDecimal> actuals = group.rooms().stream().map(r -> roomUse).toList();
                List<BigDecimal> costs = allocate(use.multiply(rate.getWaterPrice()), actuals);
                BigDecimal excessShare = positive(use.subtract(HOUSEHOLD_WATER_ALLOWANCE))
                        .divide(BigDecimal.valueOf(group.rooms().size()), 8, RoundingMode.HALF_UP);
                for (int i = 0; i < group.rooms().size(); i++) {
                    Room room = group.rooms().get(i);
                    List<CheckinRecord> occupants = checkinService.listRecordsByRoomAt(room.getId(), cutoff);
                    BigDecimal employee = occupants.isEmpty() ? BigDecimal.ZERO : money(excessShare.multiply(rate.getWaterPrice()));
                    results.add(new RoomCalculation(room, 2, roomUse,
                            HOUSEHOLD_WATER_ALLOWANCE.divide(BigDecimal.valueOf(group.rooms().size()), 8, RoundingMode.HALF_UP),
                            excessShare, costs.get(i), employee, occupants, "household water allowance 50"));
                }
                waterUsage = use;
            } else {
                Room room = onlyRoom(group);
                BigDecimal use = reading(group, period, 2, room.getId(), 2)
                        .add(reading(group, period, 2, room.getId(), 3));
                results.add(roomCalculation(room, 2, use, ROOM_WATER_ALLOWANCE,
                        money(use.multiply(rate.getWaterPrice())), waterRule, rate.getWaterPrice(), cutoff));
                waterUsage = use;
            }
        }
        return new AccountCalculation(group, rate, electricityUsage, waterUsage, results);
    }

    private RoomCalculation roomCalculation(Room room, int type, BigDecimal usage, BigDecimal allowance,
                                            BigDecimal totalCost, int rule, BigDecimal price, LocalDate cutoff) {
        List<CheckinRecord> occupants = checkinService.listRecordsByRoomAt(room.getId(), cutoff);
        BigDecimal excess = positive(usage.subtract(allowance));
        BigDecimal employee;
        String note;
        if (rule == 3) {
            if (occupants.size() != 2) throw new BizException("couple room requires exactly 2 occupants at cutoff");
            employee = totalCost;
            allowance = BigDecimal.ZERO;
            excess = usage;
            note = "couple room actual cost split equally";
        } else if (rule == 4 || occupants.isEmpty()) {
            employee = BigDecimal.ZERO;
            note = occupants.isEmpty() ? "empty room paid by company" : "company paid";
        } else {
            employee = money(excess.multiply(price));
            note = type == 1 ? "room allowance 250" : "room allowance 17";
        }
        return new RoomCalculation(room, type, usage, allowance, excess, totalCost, employee, occupants, note);
    }

    private int createBills(UtilityRoomResult result, RoomCalculation calc, String period) {
        if (calc.employeeAmount().signum() == 0 || calc.occupants().isEmpty()) return 0;
        List<CheckinRecord> occupants = calc.occupants().stream().sorted(Comparator.comparing(CheckinRecord::getId)).toList();
        List<BigDecimal> shares = equalMoney(calc.employeeAmount(), occupants.size());
        for (int i = 0; i < occupants.size(); i++) {
            CheckinRecord occupant = occupants.get(i);
            FeeBill bill = new FeeBill();
            String type = calc.utilityType() == 1 ? "E" : "W";
            bill.setBillNo("UBILL-" + type + "-" + occupant.getId() + "-" + period.replace("-", ""));
            bill.setCheckinRecordId(occupant.getId());
            bill.setResidentId(occupant.getResidentId());
            bill.setRoomId(calc.room().getId());
            bill.setPeriod(period);
            bill.setAmount(shares.get(i));
            bill.setStatus(1);
            bill.setBillType(calc.utilityType() == 1 ? 2 : 3);
            bill.setRemark(calc.note());
            bill.setUtilityResultId(result.getId());
            billMapper.insert(bill);
        }
        return occupants.size();
    }

    private List<Group> groups(Long buildingId) {
        List<Room> rooms = roomMapper.selectList(Wrappers.<Room>lambdaQuery()
                .eq(buildingId != null, Room::getBuildingId, buildingId)
                .isNotNull(Room::getSettlementMode)
                .isNotNull(Room::getUtilityAccountCode)
                .orderByAsc(Room::getBuildingId).orderByAsc(Room::getRoomNumber));
        return rooms.stream().collect(Collectors.groupingBy(
                        r -> r.getBuildingId() + "|" + r.getUtilityAccountCode(),
                        LinkedHashMap::new, Collectors.toList()))
                .values().stream().map(list -> new Group(list.get(0).getBuildingId(),
                        list.get(0).getUtilityAccountCode(), list)).toList();
    }

    private Group findGroup(Long buildingId, String accountCode) {
        return groups(buildingId).stream().filter(g -> Objects.equals(g.accountCode(), accountCode))
                .findFirst().orElseThrow(() -> new BizException("utility account not found"));
    }

    private List<String> unconfiguredErrors() {
        return roomMapper.selectList(Wrappers.<Room>lambdaQuery()).stream()
                .filter(r -> r.getSettlementMode() == null || r.getUtilityAccountCode() == null || r.getUtilityAccountCode().isBlank())
                .map(r -> "room " + r.getRoomNumber() + " utility account is not configured")
                .toList();
    }

    private List<String> accountErrors(Group group) {
        List<String> errors = new ArrayList<>();
        Room first = firstRoom(group);
        if (group.accountCode().isBlank()) errors.add("utility account code required");
        if (value(first.getElectricityRule()) < 0 || value(first.getElectricityRule()) > 4
                || value(first.getWaterRule()) < 0 || value(first.getWaterRule()) > 4) errors.add("invalid utility rule");
        if (first.getSettlementMode() == 2 && group.rooms().size() != 1) errors.add("room account must contain one room");
        if (value(first.getElectricityRule()) == 0 && value(first.getWaterRule()) == 0)
            errors.add("at least one utility rule is required");
        if (group.rooms().stream().anyMatch(r -> !Objects.equals(r.getSettlementMode(), first.getSettlementMode())
                || !Objects.equals(value(r.getElectricityRule()), value(first.getElectricityRule()))
                || !Objects.equals(value(r.getWaterRule()), value(first.getWaterRule())))) {
            errors.add("rooms in one account must use the same mode and rules");
        }
        if ((value(first.getElectricityRule()) == 1 || value(first.getWaterRule()) == 1)
                && first.getSettlementMode() != 1) errors.add("household rule requires household mode");
        if ((value(first.getElectricityRule()) == 3 || value(first.getWaterRule()) == 3)
                && (first.getSettlementMode() != 2 || group.rooms().size() != 1)) errors.add("couple rule requires one room account");
        return errors;
    }

    private Long canonicalRoomId(Group group, int targetType, Long roomId) {
        if (targetType == 1) {
            if (firstRoom(group).getSettlementMode() != 1) throw new BizException("master meter requires household account");
            return firstRoom(group).getId();
        }
        if (group.rooms().stream().noneMatch(r -> Objects.equals(r.getId(), roomId))) {
            throw new BizException("room does not belong to utility account");
        }
        return roomId;
    }

    private BigDecimal reading(Group group, String period, int targetType, Long roomId, int meterType) {
        MeterReading reading = readingMapper.selectOne(Wrappers.<MeterReading>lambdaQuery()
                .eq(MeterReading::getBuildingId, group.buildingId())
                .eq(MeterReading::getAccountCode, group.accountCode())
                .eq(MeterReading::getTargetType, targetType)
                .eq(MeterReading::getRoomId, roomId)
                .eq(MeterReading::getPeriod, period)
                .eq(MeterReading::getMeterType, meterType).last("limit 1"));
        if (reading == null) throw new BizException("missing meter reading");
        if (reading.getConsumption().signum() < 0) throw new BizException("negative meter usage");
        return reading.getConsumption();
    }

    private void ensureUnlocked(Group group, String period) {
        if (activeSettlement(group, period) != null) throw new BizException("settled readings are locked");
    }

    private UtilitySettlement activeSettlement(Group group, String period) {
        return settlementMapper.selectOne(Wrappers.<UtilitySettlement>lambdaQuery()
                .eq(UtilitySettlement::getBuildingId, group.buildingId())
                .eq(UtilitySettlement::getAccountCode, group.accountCode())
                .eq(UtilitySettlement::getPeriod, period)
                .eq(UtilitySettlement::getStatus, 1).last("limit 1"));
    }

    private Map<String, Object> summary(AccountCalculation calc) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("buildingId", calc.group().buildingId());
        item.put("accountCode", calc.group().accountCode());
        item.put("electricityUsage", calc.electricityUsage());
        item.put("waterUsage", calc.waterUsage());
        item.put("totalCost", sum(calc.results(), RoomCalculation::totalCost));
        item.put("employeeAmount", sum(calc.results(), RoomCalculation::employeeAmount));
        item.put("companyAmount", sum(calc.results(), RoomCalculation::totalCost)
                .subtract(sum(calc.results(), RoomCalculation::employeeAmount)));
        return item;
    }

    private List<BigDecimal> allocate(BigDecimal rawTotal, List<BigDecimal> weights) {
        BigDecimal total = money(rawTotal);
        BigDecimal weightTotal = weights.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (weightTotal.signum() == 0) return equalMoney(total, weights.size());
        List<BigDecimal> amounts = new ArrayList<>();
        BigDecimal assigned = BigDecimal.ZERO;
        for (int i = 0; i < weights.size(); i++) {
            BigDecimal amount = i == 0 ? BigDecimal.ZERO
                    : money(total.multiply(weights.get(i)).divide(weightTotal, 8, RoundingMode.HALF_UP));
            amounts.add(amount);
            assigned = assigned.add(amount);
        }
        amounts.set(0, total.subtract(assigned));
        return amounts;
    }

    private List<BigDecimal> equalMoney(BigDecimal total, int count) {
        BigDecimal each = total.divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
        List<BigDecimal> shares = new ArrayList<>();
        for (int i = 0; i < count; i++) shares.add(each);
        shares.set(0, total.subtract(each.multiply(BigDecimal.valueOf(count - 1))));
        return shares;
    }

    private BigDecimal sum(List<RoomCalculation> values,
                           java.util.function.Function<RoomCalculation, BigDecimal> field) {
        return values.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Room firstRoom(Group group) {
        return group.rooms().get(0);
    }

    private Room onlyRoom(Group group) {
        if (group.rooms().size() != 1) throw new BizException("rule requires one room account");
        return firstRoom(group);
    }

    private LocalDate cycleEnd(String period) {
        try {
            return YearMonth.parse(period).atDay(24);
        } catch (Exception e) {
            throw new BizException("period must be YYYY-MM");
        }
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal positive(BigDecimal value) {
        return value.max(BigDecimal.ZERO);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private record Group(Long buildingId, String accountCode, List<Room> rooms) {}
    private record AccountCalculation(Group group, UtilityRate rate, BigDecimal electricityUsage,
                                      BigDecimal waterUsage, List<RoomCalculation> results) {}
    private record RoomCalculation(Room room, int utilityType, BigDecimal actualUsage, BigDecimal allowance,
                                   BigDecimal excessUsage, BigDecimal totalCost, BigDecimal employeeAmount,
                                   List<CheckinRecord> occupants, String note) {}
}
