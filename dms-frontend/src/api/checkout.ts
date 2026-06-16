import request from '@/utils/request'
import type { CheckoutOrder, PageResult } from './types'

export interface CheckoutQuery {
  status?: number
  source?: number
  page?: number
  size?: number
}

export function pageCheckoutOrders(params: CheckoutQuery): Promise<PageResult<CheckoutOrder>> {
  return request.get('/checkout/orders', { params })
}

export function createCheckoutOrder(data: { residentId: number; reason?: string; expectCheckoutDate?: string }): Promise<number> {
  return request.post('/checkout/orders', data)
}

export function confirmCheckout(id: number, data: { checkoutDate?: string }): Promise<void> {
  return request.post(`/checkout/orders/${id}/confirm`, data)
}

export function cancelCheckout(id: number): Promise<void> {
  return request.post(`/checkout/orders/${id}/cancel`)
}
