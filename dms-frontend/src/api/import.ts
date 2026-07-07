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
  return request.post(`/import/${type}/validate`, data)
}

export function executeImport(type: ImportType, file: File): Promise<ImportResult> {
  const data = new FormData()
  data.append('file', file)
  return request.post(`/import/${type}/execute`, data)
}

export function downloadImportFile(type: ImportType, sample = false): Promise<Blob> {
  return request.get(`/import/${sample ? 'samples' : 'templates'}/${type}`, { responseType: 'blob' })
}
