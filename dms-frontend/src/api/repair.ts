import request from '@/utils/request'
import type { PageResult, RepairOrder } from './types'

export interface RepairQuery {
  status?: number
  priority?: number
  roomId?: number
  page?: number
  size?: number
}

export function pageRepairOrders(params: RepairQuery): Promise<PageResult<RepairOrder>> {
  return request.get('/repair/orders', { params })
}

export function createRepairOrder(data: { roomId?: number; residentId?: number; roomCode?: string; residentCode?: string; title: string; description?: string; priority?: number; remark?: string }): Promise<number> {
  return request.post('/repair/orders', data)
}

export function acceptRepairOrder(id: number, data: { handler: string }): Promise<void> {
  return request.post('/repair/orders/' + id + '/accept', data)
}

export function completeRepairOrder(id: number, data: { result: string }): Promise<void> {
  return request.post('/repair/orders/' + id + '/complete', data)
}

export function cancelRepairOrder(id: number): Promise<void> {
  return request.post('/repair/orders/' + id + '/cancel')
}
