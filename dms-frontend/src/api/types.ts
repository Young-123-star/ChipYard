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
