import request from '@/utils/request'
import type { PeriodSummary, BuildingSummary, ArrearsRank, UsageTrend } from './types'

export function getPeriodSummary(): Promise<PeriodSummary[]> {
  return request.get('/report/period-summary')
}

export function getBuildingSummary(): Promise<BuildingSummary[]> {
  return request.get('/report/building-summary')
}

export function getArrearsRanking(limit = 10): Promise<ArrearsRank[]> {
  return request.get('/report/arrears-ranking', { params: { limit } })
}

export function getUsageTrend(): Promise<UsageTrend[]> {
  return request.get('/report/usage-trend')
}
