export interface R<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface Building {
  id: number
  buildingCode: string
  buildingName: string
  address?: string
  floorCount: number
  hasElevator?: number
  totalRooms?: number
  totalBeds?: number
  status: number
  remark?: string
  /** 后端实时统计 */
  realRoomCount?: number
  realBedCount?: number
  occupiedBeds?: number
}

export interface Floor {
  id: number
  buildingId: number
  floorNumber: number
  floorName?: string
  /** 后端实时统计 */
  roomCount?: number
  bedCount?: number
  occupiedBeds?: number
  status: number
}

export interface Room {
  id: number
  buildingId: number
  floorId: number
  roomNumber: string
  roomType: number
  area?: number
  orientation?: string
  bedCount: number
  occupiedBeds?: number
  facilities?: string
  genderLimit?: number
  status: number
  remark?: string
}

export interface Bed {
  id: number
  roomId: number
  bedNumber: string
  bedType?: number
  currentUserId?: number
  status: number
}

export interface RoomBoard {
  id: number
  buildingId: number
  floorId: number
  floorNumber?: number
  roomNumber: string
  roomType: number
  bedCount: number
  occupiedBeds: number
  genderLimit?: number
  status: number
  area?: number
  orientation?: string
  facilities?: string
}

export interface RoomSummary {
  total: number
  totalBeds: number
  occupiedBeds: number
  freeBeds: number
}

export interface Resident {
  id: number
  employeeNo: string
  realName: string
  gender?: number
  residentType?: number
  deptName?: string
  phone?: string
  idCard?: string
  source?: number
  status?: number
}

export interface CheckinIntake {
  id: number
  bizNo: string
  residentId: number
  residentName?: string
  employeeNo?: string
  residentGender?: number
  source: number
  expectCheckinDate?: string
  genderLimitReq?: number
  roomTypeReq?: number
  buildingIdReq?: number
  remark?: string
  status: number
}

export interface CheckinRecord {
  id: number
  residentId: number
  residentName?: string
  employeeNo?: string
  buildingId?: number
  floorId?: number
  roomId?: number
  bedId?: number
  checkinDate?: string
  status: number
}

export interface CheckoutOrder {
  id: number
  bizNo: string
  residentId: number
  residentName?: string
  employeeNo?: string
  checkinRecordId?: number
  roomId?: number
  bedId?: number
  source: number
  reason?: string
  expectCheckoutDate?: string
  status: number
  arrearsAmount?: number
}

export interface FeeStandard {
  id: number
  roomType: number
  monthlyPrice: number
  remark?: string
}

export interface FeeBill {
  id: number
  billNo: string
  checkinRecordId: number
  residentId: number
  residentName?: string
  employeeNo?: string
  roomId?: number
  roomNumber?: string
  roomType?: number
  period: string
  amount: number
  status: number
  paidAt?: string
  payMethod?: number
  billType?: number
  remark?: string
}

export interface UtilityRate {
  id: number
  electricityPrice: number
  waterPrice: number
}

export interface MeterReading {
  id: number
  roomId: number
  roomNumber?: string
  period: string
  meterType: number
  prevReading: number
  currentReading: number
  consumption: number
  unitPrice: number
  amount: number
}

export interface PeriodSummary {
  period: string
  rentTotal: number
  elecTotal: number
  waterTotal: number
  total: number
  paid: number
  unpaid: number
  collectRate: number
}

export interface BuildingSummary {
  buildingId: number
  buildingName: string
  total: number
  paid: number
  unpaid: number
  collectRate: number
}

export interface ArrearsRank {
  residentId: number
  residentName?: string
  employeeNo?: string
  unpaidAmount: number
  unpaidCount: number
}

export interface UsageTrend {
  period: string
  electricity: number
  water: number
}

export interface RepairOrder {
  id: number
  orderNo: string
  roomId: number
  roomNumber?: string
  buildingName?: string
  residentId?: number
  residentName?: string
  title: string
  description?: string
  priority: number
  status: number
  handler?: string
  acceptedAt?: string
  result?: string
  completedAt?: string
  remark?: string
}