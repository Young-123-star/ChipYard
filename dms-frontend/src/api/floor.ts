import request from '@/utils/request'
import type { Floor } from './types'

export function listFloors(buildingId: number): Promise<Floor[]> {
  return request.get('/floors', { params: { buildingId } })
}

export function createFloor(data: Partial<Floor>): Promise<number> {
  return request.post('/floors', data)
}

export function updateFloor(id: number, data: Partial<Floor>): Promise<void> {
  return request.put(`/floors/${id}`, data)
}

export function deleteFloor(id: number): Promise<void> {
  return request.delete(`/floors/${id}`)
}
