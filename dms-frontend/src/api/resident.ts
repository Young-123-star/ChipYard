import request from '@/utils/request'
import type { Resident, PageResult } from './types'

export interface ResidentQuery {
  realName?: string
  employeeNo?: string
  residentType?: number
  status?: number
  page?: number
  size?: number
}

export function pageResidents(params: ResidentQuery): Promise<PageResult<Resident>> {
  return request.get('/residents', { params })
}

export function createResident(data: Partial<Resident>): Promise<number> {
  return request.post('/residents', data)
}

export function updateResident(id: number, data: Partial<Resident>): Promise<void> {
  return request.put(`/residents/${id}`, data)
}

export function deleteResident(id: number): Promise<void> {
  return request.delete(`/residents/${id}`)
}
