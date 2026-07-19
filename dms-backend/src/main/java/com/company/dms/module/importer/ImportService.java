package com.company.dms.module.importer;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.module.checkin.dto.AssignDTO;
import com.company.dms.module.checkin.dto.CreateIntakeCommand;
import com.company.dms.module.checkin.service.CheckinService;
import com.company.dms.module.resident.dto.ResidentSaveDTO;
import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.service.ResidentService;
import com.company.dms.module.resource.dto.BedSaveDTO;
import com.company.dms.module.resource.dto.BuildingSaveDTO;
import com.company.dms.module.resource.dto.FloorSaveDTO;
import com.company.dms.module.resource.dto.RoomSaveDTO;
import com.company.dms.module.resource.entity.Bed;
import com.company.dms.module.resource.entity.Building;
import com.company.dms.module.resource.entity.Floor;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.mapper.BedMapper;
import com.company.dms.module.resource.mapper.BuildingMapper;
import com.company.dms.module.resource.mapper.FloorMapper;
import com.company.dms.module.resource.mapper.RoomMapper;
import com.company.dms.module.resource.service.BedService;
import com.company.dms.module.resource.service.BuildingService;
import com.company.dms.module.resource.service.FloorService;
import com.company.dms.module.resource.service.RoomService;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ImportService {
    private final BuildingService buildingService;
    private final FloorService floorService;
    private final RoomService roomService;
    private final BedService bedService;
    private final ResidentService residentService;
    private final CheckinService checkinService;
    private final BuildingMapper buildingMapper;
    private final FloorMapper floorMapper;
    private final RoomMapper roomMapper;
    private final BedMapper bedMapper;

    public ImportService(BuildingService buildingService, FloorService floorService, RoomService roomService,
                         BedService bedService, ResidentService residentService, CheckinService checkinService,
                         BuildingMapper buildingMapper, FloorMapper floorMapper, RoomMapper roomMapper, BedMapper bedMapper) {
        this.buildingService = buildingService;
        this.floorService = floorService;
        this.roomService = roomService;
        this.bedService = bedService;
        this.residentService = residentService;
        this.checkinService = checkinService;
        this.buildingMapper = buildingMapper;
        this.floorMapper = floorMapper;
        this.roomMapper = roomMapper;
        this.bedMapper = bedMapper;
    }

    public ImportResult validate(String type, byte[] file) {
        List<List<String>> rows = readRows(file);
        ImportResult result = new ImportResult();
        result.setTotalRows(rows.size());
        switch (type) {
            case "resource" -> validateResource(rows, result);
            case "resident" -> validateResidents(rows, result);
            case "checkin-record" -> validateCheckins(rows, result);
            default -> result.addError(0, "type", type, "unsupported import type");
        }
        result.setSuccessRows(result.isSuccess() ? rows.size() : 0);
        return result;
    }

    @Transactional
    public ImportResult execute(String type, byte[] file) {
        ImportResult result = validate(type, file);
        if (!result.isSuccess()) return result;
        List<List<String>> rows = readRows(file);
        switch (type) {
            case "resource" -> executeResource(rows);
            case "resident" -> executeResidents(rows);
            case "checkin-record" -> executeCheckins(rows);
            default -> throw new IllegalArgumentException("unsupported import type: " + type);
        }
        result.setSuccessRows(rows.size());
        return result;
    }

    public byte[] template(String type, boolean sample) {
        return switch (type) {
            case "resource" -> workbook(headersResource(), sample ? sampleResource() : List.of());
            case "resident" -> workbook(headersResident(), sample ? sampleResident() : List.of());
            case "checkin-record" -> workbook(headersCheckin(), sample ? sampleCheckin() : List.of());
            default -> throw new IllegalArgumentException("unsupported import type: " + type);
        };
    }

    private void validateResource(List<List<String>> rows, ImportResult result) {
        Set<String> rooms = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            List<String> r = rows.get(i);
            int rowNo = i + 2;
            required(result, rowNo, "buildingCode", col(r, 0));
            required(result, rowNo, "buildingName", col(r, 1));
            required(result, rowNo, "floorNumber", col(r, 3));
            required(result, rowNo, "roomNumber", col(r, 5));
            required(result, rowNo, "roomType", col(r, 6));
            required(result, rowNo, "bedCount", col(r, 7));
            int bedCount = intValue(col(r, 7), 0);
            if (bedCount <= 0) result.addError(rowNo, "bedCount", col(r, 7), "must be greater than 0");
            String roomKey = col(r, 0) + ":" + col(r, 5);
            if (!rooms.add(roomKey)) result.addError(rowNo, "roomNumber", col(r, 5), "duplicate room in file");
            List<String> beds = bedNumbers(col(r, 8), bedCount);
            if (!blank(col(r, 8)) && beds.size() != bedCount) {
                result.addError(rowNo, "bedNumbers", col(r, 8), "bed number count must equal bedCount");
            }
            if (new HashSet<>(beds).size() != beds.size()) {
                result.addError(rowNo, "bedNumbers", col(r, 8), "duplicate bed number in row");
            }
            if (!blank(col(r, 14))) {
                int mode = intValue(col(r, 14), 0);
                if (mode != 1 && mode != 2) result.addError(rowNo, "settlementMode", col(r, 14), "must be 1 or 2");
                if (mode == 1 && blank(col(r, 15))) result.addError(rowNo, "utilityAccountCode", col(r, 15), "required for household mode");
            }
        }
    }

    private void validateResidents(List<List<String>> rows, ImportResult result) {
        Set<String> employeeNos = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            List<String> r = rows.get(i);
            int rowNo = i + 2;
            required(result, rowNo, "employeeNo", col(r, 0));
            required(result, rowNo, "realName", col(r, 1));
            if (!blank(col(r, 0)) && !employeeNos.add(col(r, 0))) {
                result.addError(rowNo, "employeeNo", col(r, 0), "duplicate employeeNo in file");
            }
        }
    }

    private void validateCheckins(List<List<String>> rows, ImportResult result) {
        Set<String> bedKeys = new HashSet<>();
        Set<String> employeeNos = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            List<String> r = rows.get(i);
            int rowNo = i + 2;
            String employeeNo = col(r, 0), buildingCode = col(r, 1), roomNumber = col(r, 2), bedNumber = col(r, 3);
            required(result, rowNo, "employeeNo", employeeNo);
            required(result, rowNo, "buildingCode", buildingCode);
            required(result, rowNo, "roomNumber", roomNumber);
            required(result, rowNo, "bedNumber", bedNumber);
            if (!bedKeys.add(buildingCode + ":" + roomNumber + ":" + bedNumber)) {
                result.addError(rowNo, "bedNumber", bedNumber, "duplicate bed in file");
            }
            if (!blank(employeeNo) && !employeeNos.add(employeeNo)) {
                result.addError(rowNo, "employeeNo", employeeNo, "duplicate employeeNo in file");
            }
            Resident resident = blank(employeeNo) ? null : residentService.getByEmployeeNo(employeeNo);
            if (resident == null) {
                result.addError(rowNo, "employeeNo", employeeNo, "resident not found");
                continue;
            }
            if (checkinService.findActiveRecordByResident(resident.getId()) != null) {
                result.addError(rowNo, "employeeNo", employeeNo, "resident already checked in");
            }
            Building building = findBuilding(buildingCode);
            Room room = building == null ? null : findRoom(building.getId(), roomNumber);
            Bed bed = room == null ? null : findBed(room.getId(), bedNumber);
            if (building == null) result.addError(rowNo, "buildingCode", buildingCode, "building not found");
            if (room == null) result.addError(rowNo, "roomNumber", roomNumber, "room not found");
            if (bed == null) result.addError(rowNo, "bedNumber", bedNumber, "bed not found");
            if (bed != null && (bed.getStatus() == null || bed.getStatus() != 1)) {
                result.addError(rowNo, "bedNumber", bedNumber, "bed is not free");
            }
            if (room != null && room.getGenderLimit() != null && room.getGenderLimit() != 0
                    && resident.getGender() != null && !resident.getGender().equals(room.getGenderLimit())) {
                result.addError(rowNo, "gender", String.valueOf(resident.getGender()), "resident gender does not match room limit");
            }
        }
    }

    private void executeResource(List<List<String>> rows) {
        Map<String, Integer> maxFloorByBuilding = new HashMap<>();
        for (List<String> r : rows) {
            maxFloorByBuilding.merge(col(r, 0), intValue(col(r, 3), 1), Math::max);
        }
        for (List<String> r : rows) {
            Building building = findBuilding(col(r, 0));
            if (building == null) {
                BuildingSaveDTO dto = new BuildingSaveDTO();
                dto.setBuildingCode(col(r, 0));
                dto.setBuildingName(col(r, 1));
                dto.setAddress(col(r, 2));
                dto.setFloorCount(maxFloorByBuilding.getOrDefault(col(r, 0), intValue(col(r, 3), 1)));
                dto.setHasElevator(0);
                dto.setStatus(intValue(col(r, 13), 1));
                building = buildingService.getById(buildingService.create(dto));
            }
            Floor floor = findFloor(building.getId(), intValue(col(r, 3), 1));
            if (floor == null) {
                FloorSaveDTO dto = new FloorSaveDTO();
                dto.setBuildingId(building.getId());
                dto.setFloorNumber(intValue(col(r, 3), 1));
                dto.setFloorName(col(r, 4));
                dto.setStatus(1);
                floor = floorMapper.selectById(floorService.create(dto));
            }
            Room room = findRoom(building.getId(), col(r, 5));
            if (room == null) {
                RoomSaveDTO dto = new RoomSaveDTO();
                dto.setBuildingId(building.getId());
                dto.setFloorId(floor.getId());
                dto.setRoomNumber(col(r, 5));
                dto.setRoomType(intValue(col(r, 6), 1));
                dto.setBedCount(intValue(col(r, 7), 1));
                dto.setGenderLimit(intValue(col(r, 9), 0));
                dto.setArea(decimalValue(col(r, 10)));
                dto.setOrientation(col(r, 11));
                dto.setFacilities(col(r, 12));
                dto.setStatus(intValue(col(r, 13), 1));
                if (!blank(col(r, 14))) {
                    dto.setSettlementMode(intValue(col(r, 14), 0));
                    dto.setUtilityAccountCode(col(r, 15));
                    dto.setElectricityRule(intValue(col(r, 16), 0));
                    dto.setWaterRule(intValue(col(r, 17), 0));
                }
                room = roomService.getById(roomService.create(dto));
            }
            for (String bedNumber : bedNumbers(col(r, 8), intValue(col(r, 7), 1))) {
                if (findBed(room.getId(), bedNumber) == null) {
                    BedSaveDTO dto = new BedSaveDTO();
                    dto.setRoomId(room.getId());
                    dto.setBedNumber(bedNumber);
                    dto.setBedType(3);
                    dto.setStatus(1);
                    bedService.create(dto);
                }
            }
        }
    }

    private void executeResidents(List<List<String>> rows) {
        for (List<String> r : rows) {
            ResidentSaveDTO dto = new ResidentSaveDTO();
            dto.setEmployeeNo(col(r, 0));
            dto.setRealName(col(r, 1));
            dto.setGender(intValue(col(r, 2), 0));
            dto.setResidentType(intValue(col(r, 3), 1));
            dto.setDeptName(col(r, 4));
            dto.setPhone(col(r, 5));
            dto.setIdCard(col(r, 6));
            dto.setStatus(intValue(col(r, 7), 1));
            residentService.upsertByEmployeeNo(dto, 2);
        }
    }

    private void executeCheckins(List<List<String>> rows) {
        for (List<String> r : rows) {
            Resident resident = residentService.getByEmployeeNo(col(r, 0));
            Building building = findBuilding(col(r, 1));
            Room room = findRoom(building.getId(), col(r, 2));
            Bed bed = findBed(room.getId(), col(r, 3));
            CreateIntakeCommand cmd = new CreateIntakeCommand();
            cmd.setBizNo("IMPORT-" + col(r, 0) + "-" + System.nanoTime());
            cmd.setSource(3);
            cmd.setResidentId(resident.getId());
            cmd.setExpectCheckinDate(dateValue(col(r, 4)));
            cmd.setBuildingIdReq(building.getId());
            cmd.setRemark(col(r, 5));
            Long intakeId = checkinService.createIntakeFromCommand(cmd);
            AssignDTO assign = new AssignDTO();
            assign.setBedId(bed.getId());
            assign.setCheckinDate(dateValue(col(r, 4)));
            checkinService.assign(intakeId, assign);
        }
    }

    private List<List<String>> readRows(byte[] file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file))) {
            var sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<List<String>> rows = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                List<String> values = new ArrayList<>();
                boolean any = false;
                for (int c = 0; c < 20; c++) {
                    String value = formatter.formatCellValue(row.getCell(c)).trim();
                    values.add(value);
                    any = any || !value.isBlank();
                }
                if (any) rows.add(values);
            }
            return rows;
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid xlsx file", e);
        }
    }

    private byte[] workbook(String[] headers, List<String[]> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("data");
            var head = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) head.createCell(i).setCellValue(headers[i]);
            for (int r = 0; r < rows.size(); r++) {
                var row = sheet.createRow(r + 1);
                for (int c = 0; c < rows.get(r).length; c++) row.createCell(c).setCellValue(rows.get(r)[c]);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Building findBuilding(String code) {
        if (blank(code)) return null;
        return buildingMapper.selectOne(Wrappers.<Building>lambdaQuery().eq(Building::getBuildingCode, code).last("limit 1"));
    }

    private Floor findFloor(Long buildingId, Integer floorNumber) {
        return floorMapper.selectOne(Wrappers.<Floor>lambdaQuery()
                .eq(Floor::getBuildingId, buildingId).eq(Floor::getFloorNumber, floorNumber).last("limit 1"));
    }

    private Room findRoom(Long buildingId, String roomNumber) {
        if (blank(roomNumber)) return null;
        return roomMapper.selectOne(Wrappers.<Room>lambdaQuery()
                .eq(Room::getBuildingId, buildingId).eq(Room::getRoomNumber, roomNumber).last("limit 1"));
    }

    private Bed findBed(Long roomId, String bedNumber) {
        if (blank(bedNumber)) return null;
        return bedMapper.selectOne(Wrappers.<Bed>lambdaQuery()
                .eq(Bed::getRoomId, roomId).eq(Bed::getBedNumber, bedNumber).last("limit 1"));
    }

    private void required(ImportResult result, int rowNo, String field, String value) {
        if (blank(value)) result.addError(rowNo, field, value, "required");
    }

    private boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String col(List<String> row, int index) {
        return index < row.size() ? row.get(index).trim() : "";
    }

    private int intValue(String value, int fallback) {
        if (blank(value)) return fallback;
        return new BigDecimal(value.trim()).intValue();
    }

    private BigDecimal decimalValue(String value) {
        return blank(value) ? null : new BigDecimal(value.trim());
    }

    private LocalDate dateValue(String value) {
        return blank(value) ? LocalDate.now() : LocalDate.parse(value.trim());
    }

    private List<String> bedNumbers(String value, int bedCount) {
        if (blank(value)) {
            List<String> generated = new ArrayList<>();
            for (int i = 1; i <= bedCount; i++) generated.add(String.valueOf(i));
            return generated;
        }
        return Arrays.stream(value.split("[,;\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private String[] headersResource() {
        return new String[]{"\u697c\u680b\u7f16\u7801", "\u697c\u680b\u540d\u79f0", "\u5730\u5740", "\u697c\u5c42\u53f7", "\u697c\u5c42\u540d\u79f0", "\u623f\u95f4\u53f7", "\u623f\u578b", "\u5e8a\u4f4d\u6570", "\u5e8a\u4f4d\u7f16\u53f7", "\u6027\u522b\u9650\u5236", "\u9762\u79ef", "\u671d\u5411", "\u8bbe\u65bd", "\u72b6\u6001", "\u7ed3\u7b97\u65b9\u5f0f", "\u6c34\u7535\u8d26\u6237\u7f16\u7801", "\u7528\u7535\u89c4\u5219", "\u7528\u6c34\u89c4\u5219"};
    }

    private String[] headersResident() {
        return new String[]{"\u5de5\u53f7", "\u59d3\u540d", "\u6027\u522b", "\u5c45\u4f4f\u4eba\u7c7b\u578b", "\u90e8\u95e8", "\u624b\u673a", "\u8bc1\u4ef6\u53f7", "\u72b6\u6001"};
    }

    private String[] headersCheckin() {
        return new String[]{"\u5de5\u53f7", "\u697c\u680b\u7f16\u7801", "\u623f\u95f4\u53f7", "\u5e8a\u4f4d\u53f7", "\u5165\u4f4f\u65e5\u671f", "\u5907\u6ce8"};
    }

    private List<String[]> sampleResource() {
        return Collections.singletonList(new String[]{"T", "Test Building", "Park", "1", "F1", "101", "2", "2", "A,B", "0", "20", "S", "", "1", "2", "101", "2", "2"});
    }

    private List<String[]> sampleResident() {
        return Collections.singletonList(new String[]{"IMP001", "Import User", "1", "1", "QA", "13900000001", "ID001", "1"});
    }

    private List<String[]> sampleCheckin() {
        return Collections.singletonList(new String[]{"E2001", "A", "A101", "B", "2026-07-01", "seed import"});
    }
}




