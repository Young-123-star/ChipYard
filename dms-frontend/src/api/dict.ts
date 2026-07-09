import request from '@/utils/request'

export interface DictType {
  id: number
  dictType: string
  dictName: string
  sortOrder?: number
  status: number
  systemFlag?: number
  remark?: string
}

export interface DictItem {
  id: number
  dictType: string
  dictValue: string
  dictLabel: string
  sortOrder?: number
  tagType?: string
  status: number
  systemFlag?: number
  remark?: string
}

export type DictTypePayload = Partial<Omit<DictType, 'id' | 'systemFlag'>>
export type DictItemPayload = Partial<Omit<DictItem, 'id' | 'systemFlag'>>

export function listDictTypes(): Promise<DictType[]> {
  return request.get('/dict/types')
}

export function createDictType(data: DictTypePayload): Promise<number> {
  return request.post('/dict/types', data)
}

export function updateDictType(id: number, data: DictTypePayload): Promise<void> {
  return request.put(`/dict/types/${id}`, data)
}

export function deleteDictType(id: number): Promise<void> {
  return request.delete(`/dict/types/${id}`)
}

export function listDictItems(dictType: string, activeOnly = true): Promise<DictItem[]> {
  return request.get('/dict/items', { params: { dictType, activeOnly } })
}

export function createDictItem(data: DictItemPayload): Promise<number> {
  return request.post('/dict/items', data)
}

export function updateDictItem(id: number, data: DictItemPayload): Promise<void> {
  return request.put(`/dict/items/${id}`, data)
}

export function deleteDictItem(id: number): Promise<void> {
  return request.delete(`/dict/items/${id}`)
}