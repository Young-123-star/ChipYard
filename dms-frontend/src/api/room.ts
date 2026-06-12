import request from '@/utils/request'
import type { Room, RoomBoard, PageResult } from './types'

export interface RoomQuery {
  buildingId?: number
  floorId?: number
  roomType?: number
  status?: number
  page?: number
  size?: number
}

export function pageRooms(params: RoomQuery): Promise<PageResult<Room>> {
  return request.get('/rooms', { params })
}

export function getRoom(id: number): Promise<Room> {
  return request.get(`/rooms/${id}`)
}

export function createRoom(data: Partial<Room>): Promise<number> {
  return request.post('/rooms', data)
}

export function updateRoom(id: number, data: Partial<Room>): Promise<void> {
  return request.put(`/rooms/${id}`, data)
}

export function deleteRoom(id: number): Promise<void> {
  return request.delete(`/rooms/${id}`)
}

export function getRoomBoard(params: { buildingId?: number; floorId?: number }): Promise<RoomBoard[]> {
  return request.get('/rooms/board', { params })
}
