import request from '@/utils/request'
import type { FeeStandard, FeeBill, PageResult } from './types'

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
