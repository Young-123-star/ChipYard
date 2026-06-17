import request from '@/utils/request'
import type { FeeStandard, FeeBill, UtilityRate, MeterReading, PageResult } from './types'

export function listStandards(): Promise<FeeStandard[]> {
  return request.get('/fee/standards')
}

export function createStandard(data: { roomType: number; monthlyPrice: number; remark?: string }): Promise<number> {
  return request.post('/fee/standards', data)
}

export function updateStandard(id: number, data: { roomType: number; monthlyPrice: number; remark?: string }): Promise<void> {
  return request.put(`/fee/standards/${id}`, data)
}

export function deleteStandard(id: number): Promise<void> {
  return request.delete(`/fee/standards/${id}`)
}

export interface BillQuery {
  period?: string
  status?: number
  billType?: number
  residentId?: number
  page?: number
  size?: number
}

export function pageBills(params: BillQuery): Promise<PageResult<FeeBill>> {
  return request.get('/fee/bills', { params })
}

export function generateBills(data: { period: string }): Promise<{ generated: number; skipped: number }> {
  return request.post('/fee/bills/generate', data)
}

export function payBill(id: number, data: { payMethod: number }): Promise<void> {
  return request.post(`/fee/bills/${id}/pay`, data)
}

export function voidBill(id: number): Promise<void> {
  return request.post(`/fee/bills/${id}/void`)
}

export function getArrears(checkinRecordId: number): Promise<{ count: number; totalAmount: number }> {
  return request.get('/fee/arrears', { params: { checkinRecordId } })
}

export function getUtilityRate(): Promise<UtilityRate> {
  return request.get('/fee/utility-rate')
}

export function updateUtilityRate(data: { electricityPrice: number; waterPrice: number }): Promise<void> {
  return request.put('/fee/utility-rate', data)
}

export interface MeterQuery {
  period?: string
  roomId?: number
  meterType?: number
  page?: number
  size?: number
}

export function pageMeterReadings(params: MeterQuery): Promise<PageResult<MeterReading>> {
  return request.get('/fee/meter-readings', { params })
}

export function saveMeterReading(data: { roomId: number; period: string; meterType: number; currentReading: number }): Promise<number> {
  return request.post('/fee/meter-readings', data)
}

export function generateUtilityBills(data: { period: string }): Promise<{ generated: number; skipped: number }> {
  return request.post('/fee/utility-bills/generate', data)
}
