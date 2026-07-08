import request from '@/utils/request'

export type ExportType =
  | 'buildings'
  | 'floors'
  | 'rooms'
  | 'beds'
  | 'residents'
  | 'checkin-intakes'
  | 'checkin-records'
  | 'checkout-orders'
  | 'fee-standards'
  | 'fee-bills'
  | 'meter-readings'
  | 'repair-orders'

export async function exportLedger(type: ExportType, params?: Record<string, unknown>) {
  const blob = await request.get(`/export/${type}`, { params, responseType: 'blob' }) as unknown as Blob
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${type}.xlsx`
  a.click()
  URL.revokeObjectURL(url)
}
