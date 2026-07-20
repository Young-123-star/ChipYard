import { listDictItems } from '@/api/dict'
import type { DictItem } from '@/api/dict'

export interface DictOption {
  value: number | string
  label: string
  type?: string
  raw?: DictItem
}

export const ROOM_TYPE = [
  { value: 1, label: '单人间', type: 'success' },
  { value: 2, label: '双人间', type: 'primary' },
  { value: 3, label: '四人间', type: 'warning' },
  { value: 4, label: '六人间', type: 'info' },
  { value: 5, label: '八人间', type: 'info' },
  { value: 6, label: '其他', type: 'info' }
]

export const ROOM_STATUS = [
  { value: 0, label: '停用', type: 'info' },
  { value: 1, label: '空闲', type: 'success' },
  { value: 2, label: '已满', type: 'danger' },
  { value: 3, label: '维修中', type: 'warning' },
  { value: 4, label: '预留', type: 'primary' }
]

export const BED_STATUS = [
  { value: 0, label: '停用', type: 'info' },
  { value: 1, label: '空闲', type: 'success' },
  { value: 2, label: '已入住', type: 'primary' },
  { value: 3, label: '维修中', type: 'warning' },
  { value: 4, label: '预留', type: 'primary' }
]

export const BED_TYPE = [
  { value: 1, label: '上铺', type: 'info' },
  { value: 2, label: '下铺', type: 'info' },
  { value: 3, label: '独立床', type: 'success' }
]

export const GENDER_LIMIT = [
  { value: 0, label: '不限', type: 'info' },
  { value: 1, label: '男', type: 'primary' },
  { value: 2, label: '女', type: 'danger' }
]

export const BUILDING_STATUS = [
  { value: 0, label: '停用', type: 'info' },
  { value: 1, label: '启用', type: 'success' },
  { value: 2, label: '维修中', type: 'warning' }
]

export const RESIDENT_TYPE = [
  { value: 1, label: '员工', type: 'primary' },
  { value: 2, label: '外包', type: 'warning' },
  { value: 3, label: '其他', type: 'info' }
]

export const RESIDENT_STATUS = [
  { value: 0, label: '离职', type: 'info' },
  { value: 1, label: '在职', type: 'success' },
  { value: 2, label: '退宿', type: 'info' }
]

export const INTAKE_STATUS = [
  { value: 1, label: '待分配', type: 'warning' },
  { value: 2, label: '已入住', type: 'success' },
  { value: 3, label: '已取消', type: 'info' }
]

export const INTAKE_SOURCE = [
  { value: 1, label: '手工录入', type: 'primary' },
  { value: 2, label: 'Excel导入', type: 'success' },
  { value: 3, label: 'OA同步', type: 'warning' }
]

export const CHECKOUT_STATUS = [
  { value: 1, label: '待确认', type: 'warning' },
  { value: 2, label: '已退宿', type: 'success' },
  { value: 3, label: '已取消', type: 'info' }
]

export const CHECKOUT_SOURCE = [
  { value: 1, label: '手工录入', type: 'primary' },
  { value: 2, label: 'Excel导入', type: 'success' },
  { value: 3, label: 'OA同步', type: 'warning' }
]

export const BILL_STATUS = [
  { value: 1, label: '未支付', type: 'warning' },
  { value: 2, label: '已支付', type: 'success' },
  { value: 3, label: '已作废', type: 'info' },
  { value: 4, label: '挂账', type: 'danger' }
]

export const PAY_METHOD = [
  { value: 1, label: '现金', type: 'info' },
  { value: 2, label: '转账', type: 'primary' },
  { value: 3, label: '代扣', type: 'success' }
]

export const BILL_TYPE = [
  { value: 1, label: '住宿费', type: 'primary' },
  { value: 2, label: '电费', type: 'warning' },
  { value: 3, label: '水费', type: 'primary' }
]

export const METER_TYPE = [
  { value: 1, label: '电表', type: 'warning' },
  { value: 2, label: '水表', type: 'primary' }
]

export const REPAIR_STATUS = [
  { value: 1, label: '待受理', type: 'warning' },
  { value: 2, label: '处理中', type: 'primary' },
  { value: 3, label: '已完成', type: 'success' },
  { value: 4, label: '已取消', type: 'info' }
]

export const REPAIR_PRIORITY = [
  { value: 1, label: '普通', type: 'info' },
  { value: 2, label: '紧急', type: 'danger' }
]

export const ROOM_FACILITY = [
  { value: '空调', label: '空调', type: 'primary' },
  { value: '热水器', label: '热水器', type: 'success' },
  { value: '衣柜', label: '衣柜', type: 'warning' },
  { value: '书桌', label: '书桌', type: 'info' }
]

const FALLBACKS: Record<string, DictOption[]> = {
  ROOM_TYPE,
  ROOM_STATUS,
  BED_STATUS,
  BED_TYPE,
  GENDER_LIMIT,
  BUILDING_STATUS,
  RESIDENT_TYPE,
  RESIDENT_STATUS,
  INTAKE_STATUS,
  INTAKE_SOURCE,
  CHECKOUT_STATUS,
  CHECKOUT_SOURCE,
  BILL_STATUS,
  BILL_TYPE,
  PAY_METHOD,
  METER_TYPE,
  REPAIR_STATUS,
  REPAIR_PRIORITY,
  ROOM_FACILITY
}

const cache = new Map<string, DictOption[]>()

function toValue(value: string): number | string {
  if (/^-?\d+$/.test(value)) return Number(value)
  return value
}

export async function loadDictOptions(dictType: string, fallback: DictOption[] = FALLBACKS[dictType] || []): Promise<DictOption[]> {
  if (cache.has(dictType)) return cache.get(dictType)!
  try {
    const items = await listDictItems(dictType, true)
    const options = items.map((item) => ({ value: toValue(item.dictValue), label: item.dictLabel, type: item.tagType, raw: item }))
    cache.set(dictType, options)
    return options
  } catch {
    return fallback
  }
}

export function clearDictCache(dictType?: string) {
  if (dictType) cache.delete(dictType)
  else cache.clear()
}

export function labelOf(dict: { value: number | string; label: string }[], value?: number | string): string {
  const item = dict.find((d) => d.value === value)
  return item ? item.label : '-'
}

export function tagTypeOf(dict: { value: number | string; label: string; type?: string }[], value?: number | string): string {
  const item = dict.find((d) => d.value === value)
  return item?.type || 'info'
}