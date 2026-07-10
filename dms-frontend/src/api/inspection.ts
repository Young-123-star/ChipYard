import request from '@/utils/request'
import type { PageResult } from './types'

export interface InspectionPlan {
  id: number
  planName: string
  cycleType: number
  targetType: number
  targetId: number
  targetName: string
  inspector: string
  items: string[]
  status: number
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface InspectionItemResult {
  item: string
  passed: boolean
  note?: string
}

export interface InspectionTask {
  id: number
  taskNo: string
  planId: number
  planName: string
  targetType: number
  targetId: number
  targetName: string
  inspector: string
  plannedDate: string
  items: string[]
  results: InspectionItemResult[]
  status: number
  completedAt?: string
  rectificationNote?: string
  rectifiedAt?: string
}

export interface PlanSave {
  planName: string
  cycleType: number
  targetType: number
  targetId: number
  inspector: string
  items: string[]
  remark?: string
}

export function pageInspectionPlans(params: Record<string, unknown>): Promise<PageResult<InspectionPlan>> {
  return request.get('/inspection/plans', { params })
}

export function createInspectionPlan(data: PlanSave): Promise<number> {
  return request.post('/inspection/plans', data)
}

export function updateInspectionPlan(id: number, data: PlanSave): Promise<void> {
  return request.put(`/inspection/plans/${id}`, data)
}

export function changeInspectionPlanStatus(id: number, status: number): Promise<void> {
  return request.post(`/inspection/plans/${id}/status`, { status })
}

export function generateInspectionTask(id: number, data: { plannedDate: string; inspector?: string }): Promise<number> {
  return request.post(`/inspection/plans/${id}/tasks`, data)
}

export function pageInspectionTasks(params: Record<string, unknown>): Promise<PageResult<InspectionTask>> {
  return request.get('/inspection/tasks', { params })
}

export function getInspectionTask(id: number): Promise<InspectionTask> {
  return request.get(`/inspection/tasks/${id}`)
}

export function executeInspectionTask(id: number, items: InspectionItemResult[]): Promise<void> {
  return request.post(`/inspection/tasks/${id}/execute`, { items })
}

export function rectifyInspectionTask(id: number, note: string): Promise<void> {
  return request.post(`/inspection/tasks/${id}/rectify`, { note })
}

export function cancelInspectionTask(id: number): Promise<void> {
  return request.post(`/inspection/tasks/${id}/cancel`)
}
