import request from '@/utils/request'
import type { Bed } from './types'

export function listBeds(roomId: number): Promise<Bed[]> {
  return request.get('/beds', { params: { roomId } })
}

export function createBed(data: Partial<Bed>): Promise<number> {
  return request.post('/beds', data)
}

export function updateBed(id: number, data: Partial<Bed>): Promise<void> {
  return request.put(`/beds/${id}`, data)
}

export function deleteBed(id: number): Promise<void> {
  return request.delete(`/beds/${id}`)
}
