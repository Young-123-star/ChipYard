package com.company.dms.module.exporter;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.module.checkin.dto.IntakeQuery;
import com.company.dms.module.checkin.dto.RecordQuery;
import com.company.dms.module.checkin.service.CheckinService;
import com.company.dms.module.checkin.vo.IntakeVO;
import com.company.dms.module.checkin.vo.RecordVO;
import com.company.dms.module.checkout.dto.CheckoutQuery;
import com.company.dms.module.checkout.service.CheckoutService;
import com.company.dms.module.checkout.vo.CheckoutOrderVO;
import com.company.dms.module.fee.dto.BillQuery;
import com.company.dms.module.fee.dto.MeterQuery;
import com.company.dms.module.fee.entity.FeeStandard;
import com.company.dms.module.fee.service.FeeBillService;
import com.company.dms.module.fee.service.FeeStandardService;
import com.company.dms.module.fee.service.MeterService;
import com.company.dms.module.fee.vo.FeeBillVO;
import com.company.dms.module.fee.vo.MeterReadingVO;
import com.company.dms.module.repair.dto.RepairQuery;
import com.company.dms.module.repair.service.RepairService;
import com.company.dms.module.repair.vo.RepairOrderVO;
import com.company.dms.module.resident.dto.ResidentQuery;
import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.service.ResidentService;
import com.company.dms.module.resource.dto.BuildingQuery;
import com.company.dms.module.resource.dto.RoomQuery;
import com.company.dms.module.resource.entity.Bed;
import com.company.dms.module.resource.entity.Floor;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.mapper.BedMapper;
import com.company.dms.module.resource.mapper.FloorMapper;
import com.company.dms.module.resource.service.BuildingService;
import com.company.dms.module.resource.service.RoomService;
import com.company.dms.module.resource.vo.BuildingVO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExportService {
    // ponytail: synchronous ledger export is capped; switch to paged async export if real data exceeds this.
    private static final long EXPORT_SIZE = 100_000L;
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BuildingService buildingService;
    private final FloorMapper floorMapper;
    private final RoomService roomService;
    private final BedMapper bedMapper;
    private final ResidentService residentService;
    private final CheckinService checkinService;
    private final CheckoutService checkoutService;
    private final FeeStandardService standardService;
    private final FeeBillService billService;
    private final MeterService meterService;
    private final RepairService repairService;

    public ExportService(BuildingService buildingService, FloorMapper floorMapper, RoomService roomService,
                         BedMapper bedMapper, ResidentService residentService, CheckinService checkinService,
                         CheckoutService checkoutService, FeeStandardService standardService,
                         FeeBillService billService, MeterService meterService, RepairService repairService) {
        this.buildingService = buildingService;
        this.floorMapper = floorMapper;
        this.roomService = roomService;
        this.bedMapper = bedMapper;
        this.residentService = residentService;
        this.checkinService = checkinService;
        this.checkoutService = checkoutService;
        this.standardService = standardService;
        this.billService = billService;
        this.meterService = meterService;
        this.repairService = repairService;
    }

    public ExportFile export(String type, Map<String, String> params) {
        byte[] body = switch (type) {
            case "buildings" -> buildings(params);
            case "floors" -> floors(params);
            case "rooms" -> rooms(params);
            case "beds" -> beds(params);
            case "residents" -> residents(params);
            case "checkin-intakes" -> checkinIntakes(params);
            case "checkin-records" -> checkinRecords(params);
            case "checkout-orders" -> checkoutOrders(params);
            case "fee-standards" -> feeStandards();
            case "fee-bills" -> feeBills(params);
            case "meter-readings" -> meterReadings(params);
            case "repair-orders" -> repairOrders(params);
            default -> throw new IllegalArgumentException("unsupported export type: " + type);
        };
        return new ExportFile(type + "-" + LocalDateTime.now().format(FILE_TS) + ".xlsx", body);
    }

    private byte[] buildings(Map<String, String> p) {
        BuildingQuery q = new BuildingQuery();
        q.setBuildingName(str(p, "buildingName"));
        q.setStatus(intVal(p, "status"));
        all(q::setPage, q::setSize);
        List<BuildingVO> rows = buildingService.page(q).getRecords();
        return workbook("buildings",
                new String[]{"ID", "楼栋编码", "楼栋名称", "地址", "楼层数", "房间数", "床位数", "已入住", "状态", "备注"},
                rows.stream().map(b -> List.of(b.getId(), b.getBuildingCode(), b.getBuildingName(), b.getAddress(),
                        b.getFloorCount(), b.getRealRoomCount(), b.getRealBedCount(), b.getOccupiedBeds(),
                        buildingStatus(b.getStatus()), val(b.getRemark()))).collect(Collectors.toList()));
    }

    private byte[] floors(Map<String, String> p) {
        List<Floor> rows = floorMapper.selectList(Wrappers.<Floor>lambdaQuery()
                .eq(longVal(p, "buildingId") != null, Floor::getBuildingId, longVal(p, "buildingId"))
                .orderByAsc(Floor::getBuildingId).orderByAsc(Floor::getFloorNumber));
        return workbook("floors",
                new String[]{"ID", "楼栋ID", "楼层号", "楼层名称", "房间数", "床位数", "状态"},
                rows.stream().map(f -> List.of(f.getId(), f.getBuildingId(), f.getFloorNumber(), val(f.getFloorName()),
                        val(f.getRoomCount()), val(f.getBedCount()), enabledStatus(f.getStatus()))).collect(Collectors.toList()));
    }

    private byte[] rooms(Map<String, String> p) {
        RoomQuery q = new RoomQuery();
        q.setBuildingId(longVal(p, "buildingId"));
        q.setFloorId(longVal(p, "floorId"));
        q.setRoomType(intVal(p, "roomType"));
        q.setStatus(intVal(p, "status"));
        all(q::setPage, q::setSize);
        List<Room> rows = roomService.page(q).getRecords();
        return workbook("rooms",
                new String[]{"ID", "楼栋ID", "楼层ID", "房间号", "房型", "床位数", "已入住", "状态", "性别限制", "面积", "朝向", "设施"},
                rows.stream().map(r -> List.of(r.getId(), r.getBuildingId(), r.getFloorId(), r.getRoomNumber(),
                        roomType(r.getRoomType()), val(r.getBedCount()), val(r.getOccupiedBeds()), roomStatus(r.getStatus()),
                        gender(r.getGenderLimit()), val(r.getArea()), val(r.getOrientation()), val(r.getFacilities()))).collect(Collectors.toList()));
    }

    private byte[] beds(Map<String, String> p) {
        List<Bed> rows = bedMapper.selectList(Wrappers.<Bed>lambdaQuery()
                .eq(longVal(p, "roomId") != null, Bed::getRoomId, longVal(p, "roomId"))
                .orderByAsc(Bed::getRoomId).orderByAsc(Bed::getBedNumber));
        return workbook("beds",
                new String[]{"ID", "房间ID", "床位号", "床位类型", "当前居住人ID", "状态"},
                rows.stream().map(b -> List.of(b.getId(), b.getRoomId(), b.getBedNumber(), bedType(b.getBedType()),
                        val(b.getCurrentUserId()), bedStatus(b.getStatus()))).collect(Collectors.toList()));
    }

    private byte[] residents(Map<String, String> p) {
        ResidentQuery q = new ResidentQuery();
        q.setRealName(str(p, "realName"));
        q.setEmployeeNo(str(p, "employeeNo"));
        q.setResidentType(intVal(p, "residentType"));
        q.setStatus(intVal(p, "status"));
        all(q::setPage, q::setSize);
        List<Resident> rows = residentService.page(q).getRecords();
        return workbook("residents",
                new String[]{"ID", "工号", "姓名", "性别", "人员类型", "部门", "电话", "证件号", "状态"},
                rows.stream().map(r -> List.of(r.getId(), r.getEmployeeNo(), r.getRealName(), gender(r.getGender()),
                        residentType(r.getResidentType()), val(r.getDeptName()), val(r.getPhone()), val(r.getIdCard()),
                        residentStatus(r.getStatus()))).collect(Collectors.toList()));
    }

    private byte[] checkinIntakes(Map<String, String> p) {
        IntakeQuery q = new IntakeQuery();
        q.setStatus(intVal(p, "status"));
        q.setSource(intVal(p, "source"));
        all(q::setPage, q::setSize);
        List<IntakeVO> rows = checkinService.pageIntakes(q).getRecords();
        return workbook("checkin-intakes",
                new String[]{"ID", "业务号", "工号", "姓名", "来源", "期望入住日", "房型要求", "性别要求", "状态", "备注"},
                rows.stream().map(i -> List.of(i.getId(), i.getBizNo(), val(i.getEmployeeNo()), val(i.getResidentName()),
                        intakeSource(i.getSource()), val(i.getExpectCheckinDate()), roomType(i.getRoomTypeReq()),
                        gender(i.getGenderLimitReq()), intakeStatus(i.getStatus()), val(i.getRemark()))).collect(Collectors.toList()));
    }

    private byte[] checkinRecords(Map<String, String> p) {
        RecordQuery q = new RecordQuery();
        q.setBuildingId(longVal(p, "buildingId"));
        q.setStatus(intVal(p, "status"));
        all(q::setPage, q::setSize);
        List<RecordVO> rows = checkinService.pageRecords(q).getRecords();
        return workbook("checkin-records",
                new String[]{"ID", "工号", "姓名", "楼栋ID", "楼层ID", "房间ID", "床位ID", "入住日", "状态"},
                rows.stream().map(r -> List.<Object>of(r.getId(), val(r.getEmployeeNo()), val(r.getResidentName()), r.getBuildingId(),
                        r.getFloorId(), r.getRoomId(), r.getBedId(), val(r.getCheckinDate()),
                        recordStatus(r.getStatus()))).collect(Collectors.toList()));
    }

    private byte[] checkoutOrders(Map<String, String> p) {
        CheckoutQuery q = new CheckoutQuery();
        q.setStatus(intVal(p, "status"));
        q.setSource(intVal(p, "source"));
        all(q::setPage, q::setSize);
        List<CheckoutOrderVO> rows = checkoutService.pageOrders(q).getRecords();
        return workbook("checkout-orders",
                new String[]{"ID", "业务号", "工号", "姓名", "入住记录ID", "房间ID", "床位ID", "来源", "预计退宿日", "欠费金额", "状态", "原因"},
                rows.stream().map(o -> List.of(o.getId(), o.getBizNo(), val(o.getEmployeeNo()), val(o.getResidentName()),
                        o.getCheckinRecordId(), val(o.getRoomId()), val(o.getBedId()), checkoutSource(o.getSource()),
                        val(o.getExpectCheckoutDate()), val(o.getArrearsAmount()), checkoutStatus(o.getStatus()),
                        val(o.getReason()))).collect(Collectors.toList()));
    }

    private byte[] feeStandards() {
        List<FeeStandard> rows = standardService.list();
        return workbook("fee-standards",
                new String[]{"ID", "房型", "月单价", "备注"},
                rows.stream().map(s -> List.of(s.getId(), roomType(s.getRoomType()), s.getMonthlyPrice(), val(s.getRemark()))).collect(Collectors.toList()));
    }

    private byte[] feeBills(Map<String, String> p) {
        BillQuery q = new BillQuery();
        q.setPeriod(str(p, "period"));
        q.setStatus(intVal(p, "status"));
        q.setBillType(intVal(p, "billType"));
        q.setResidentId(longVal(p, "residentId"));
        all(q::setPage, q::setSize);
        List<FeeBillVO> rows = billService.pageBills(q).getRecords();
        return workbook("fee-bills",
                new String[]{"ID", "账单号", "账期", "账单类型", "工号", "姓名", "房间号", "房型", "金额", "状态", "缴费方式", "缴费时间", "备注"},
                rows.stream().map(b -> List.of(b.getId(), b.getBillNo(), b.getPeriod(), billType(b.getBillType()),
                        val(b.getEmployeeNo()), val(b.getResidentName()), val(b.getRoomNumber()), roomType(b.getRoomType()),
                        b.getAmount(), billStatus(b.getStatus()), payMethod(b.getPayMethod()), val(b.getPaidAt()),
                        val(b.getRemark()))).collect(Collectors.toList()));
    }

    private byte[] meterReadings(Map<String, String> p) {
        MeterQuery q = new MeterQuery();
        q.setPeriod(str(p, "period"));
        q.setRoomId(longVal(p, "roomId"));
        q.setMeterType(intVal(p, "meterType"));
        all(q::setPage, q::setSize);
        List<MeterReadingVO> rows = meterService.pageReadings(q).getRecords();
        return workbook("meter-readings",
                new String[]{"ID", "房间号", "账期", "表类型", "上期读数", "本期读数", "用量", "单价", "金额"},
                rows.stream().map(m -> List.of(m.getId(), val(m.getRoomNumber()), m.getPeriod(), meterType(m.getMeterType()),
                        m.getPrevReading(), m.getCurrentReading(), m.getConsumption(), m.getUnitPrice(), m.getAmount())).collect(Collectors.toList()));
    }

    private byte[] repairOrders(Map<String, String> p) {
        RepairQuery q = new RepairQuery();
        q.setStatus(intVal(p, "status"));
        q.setPriority(intVal(p, "priority"));
        q.setRoomId(longVal(p, "roomId"));
        all(q::setPage, q::setSize);
        List<RepairOrderVO> rows = repairService.pageOrders(q).getRecords();
        return workbook("repair-orders",
                new String[]{"ID", "工单号", "楼栋", "房间号", "报修人", "标题", "优先级", "状态", "处理人", "受理时间", "完成时间", "结果", "备注"},
                rows.stream().map(r -> List.of(r.getId(), r.getOrderNo(), val(r.getBuildingName()), val(r.getRoomNumber()),
                        val(r.getResidentName()), val(r.getTitle()), repairPriority(r.getPriority()), repairStatus(r.getStatus()),
                        val(r.getHandler()), val(r.getAcceptedAt()), val(r.getCompletedAt()), val(r.getResult()),
                        val(r.getRemark()))).collect(Collectors.toList()));
    }

    private byte[] workbook(String sheetName, String[] headers, List<List<Object>> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet(sheetName);
            var head = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) head.createCell(i).setCellValue(headers[i]);
            for (int r = 0; r < rows.size(); r++) {
                var row = sheet.createRow(r + 1);
                List<Object> values = rows.get(r);
                for (int c = 0; c < values.size(); c++) row.createCell(c).setCellValue(String.valueOf(val(values.get(c))));
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void all(java.util.function.LongConsumer setPage, java.util.function.LongConsumer setSize) {
        setPage.accept(1L);
        setSize.accept(EXPORT_SIZE);
    }

    private String str(Map<String, String> p, String key) {
        String value = p.get(key);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Long longVal(Map<String, String> p, String key) {
        String value = str(p, key);
        return value == null ? null : Long.valueOf(value);
    }

    private Integer intVal(Map<String, String> p, String key) {
        String value = str(p, key);
        return value == null ? null : Integer.valueOf(value);
    }

    private Object val(Object value) {
        return value == null ? "" : value;
    }

    private String label(Map<Integer, String> dict, Integer value) {
        return value == null ? "" : dict.getOrDefault(value, String.valueOf(value));
    }

    private String roomType(Integer v) { return label(Map.of(1, "单人间", 2, "双人间", 3, "四人间", 4, "六人间", 5, "八人间", 6, "其他"), v); }
    private String roomStatus(Integer v) { return label(Map.of(0, "停用", 1, "空闲", 2, "已满", 3, "维修中", 4, "预留"), v); }
    private String bedStatus(Integer v) { return label(Map.of(0, "停用", 1, "空闲", 2, "已入住", 3, "维修中", 4, "预留"), v); }
    private String buildingStatus(Integer v) { return label(Map.of(0, "停用", 1, "启用", 2, "维修中"), v); }
    private String enabledStatus(Integer v) { return label(Map.of(0, "停用", 1, "启用"), v); }
    private String bedType(Integer v) { return label(Map.of(1, "上床", 2, "下床", 3, "单床"), v); }
    private String gender(Integer v) { return label(Map.of(0, "不限", 1, "男", 2, "女"), v); }
    private String residentType(Integer v) { return label(Map.of(1, "正式", 2, "外包", 3, "其他"), v); }
    private String residentStatus(Integer v) { return label(Map.of(0, "离职", 1, "在职"), v); }
    private String intakeStatus(Integer v) { return label(Map.of(1, "待分配", 2, "已入住", 3, "已取消"), v); }
    private String intakeSource(Integer v) { return label(Map.of(1, "OA", 2, "HCP", 3, "手工"), v); }
    private String recordStatus(Integer v) { return label(Map.of(1, "在住", 2, "已退宿"), v); }
    private String checkoutStatus(Integer v) { return label(Map.of(1, "待退宿", 2, "已退宿", 3, "已取消"), v); }
    private String checkoutSource(Integer v) { return label(Map.of(1, "退宿申请", 2, "离职", 3, "手工"), v); }
    private String billStatus(Integer v) { return label(Map.of(1, "未缴", 2, "已缴", 3, "已作废", 4, "挂账"), v); }
    private String payMethod(Integer v) { return label(Map.of(1, "现金", 2, "转账"), v); }
    private String billType(Integer v) { return label(Map.of(1, "住宿费", 2, "电费", 3, "水费"), v); }
    private String meterType(Integer v) { return label(Map.of(1, "电表", 2, "水表"), v); }
    private String repairStatus(Integer v) { return label(Map.of(1, "待受理", 2, "处理中", 3, "已完成", 4, "已取消"), v); }
    private String repairPriority(Integer v) { return label(Map.of(1, "普通", 2, "紧急"), v); }
}

