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
  // 拦截器对 blob 响应放行完整 response，便于读取 Content-Disposition 文件名
  const response = (await request.get(`/export/${type}`, { params, responseType: 'blob', timeout: 60000 })) as unknown as {
    data: Blob
    headers: Record<string, string>
  }
  const disposition = response.headers['content-disposition'] || ''
  const starMatch = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  const plainMatch = disposition.match(/filename="?([^";]+)"?/i)
  const filename = starMatch
    ? decodeURIComponent(starMatch[1].trim())
    : plainMatch
      ? plainMatch[1].trim()
      : `${type}.xlsx`
  const url = URL.createObjectURL(response.data)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}
