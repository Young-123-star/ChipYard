import request from '@/utils/request'
import type { CheckinIntake, CheckinRecord, PageResult } from './types'

export interface IntakeQuery {
  status?: number
  source?: number
  page?: number
  size?: number
}

export function pageIntakes(params: IntakeQuery): Promise<PageResult<CheckinIntake>> {
  return request.get('/checkin/intakes', { params })
}

export function createIntake(data: { residentId: number; expectCheckinDate?: string; genderLimitReq?: number; roomTypeReq?: number; buildingIdReq?: number; remark?: string }): Promise<number> {
  return request.post('/checkin/intakes', data)
}

export function assignIntake(id: number, data: { bedId: number; checkinDate?: string }): Promise<number> {
  return request.post(`/checkin/intakes/${id}/assign`, data)
}

export function cancelIntake(id: number): Promise<void> {
  return request.post(`/checkin/intakes/${id}/cancel`)
}

export interface RecordQuery {
  buildingId?: number
  roomId?: number
  status?: number
  page?: number
  size?: number
}

export function pageRecords(params: RecordQuery): Promise<PageResult<CheckinRecord>> {
  return request.get('/checkin/records', { params })
}
