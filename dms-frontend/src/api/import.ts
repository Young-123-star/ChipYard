import request from '@/utils/request'

export type ImportType = 'resource' | 'resident' | 'checkin-record'

export interface ImportRowError {
  rowNumber: number
  field: string
  value: string
  message: string
}

export interface ImportResult {
  totalRows: number
  successRows: number
  errors: ImportRowError[]
  success: boolean
}

export function validateImport(type: ImportType, file: File): Promise<ImportResult> {
  const data = new FormData()
  data.append('file', file)
  return request.post(`/import/${type}/validate`, data, { timeout: 60000 })
}

export function executeImport(type: ImportType, file: File): Promise<ImportResult> {
  const data = new FormData()
  data.append('file', file)
  return request.post(`/import/${type}/execute`, data, { timeout: 60000 })
}

export async function downloadImportFile(type: ImportType, sample = false): Promise<Blob> {
  // 拦截器对 blob 响应放行完整 response，这里取 data
  const response = (await request.get(`/import/${sample ? 'samples' : 'templates'}/${type}`, { responseType: 'blob' })) as unknown as { data: Blob }
  return response.data
}
