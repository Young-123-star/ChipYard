import request from '@/utils/request'
import type { Building, PageResult } from './types'

export interface BuildingQuery {
  buildingName?: string
  status?: number
  page?: number
  size?: number
}

export function pageBuildings(params: BuildingQuery): Promise<PageResult<Building>> {
  return request.get('/buildings', { params })
}

export function getBuilding(id: number): Promise<Building> {
  return request.get(`/buildings/${id}`)
}

export function createBuilding(data: Partial<Building>): Promise<number> {
  return request.post('/buildings', data)
}

export function updateBuilding(id: number, data: Partial<Building>): Promise<void> {
  return request.put(`/buildings/${id}`, data)
}

export function deleteBuilding(id: number): Promise<void> {
  return request.delete(`/buildings/${id}`)
}
