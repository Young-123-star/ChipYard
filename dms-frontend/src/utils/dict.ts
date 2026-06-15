export const ROOM_TYPE = [
  { value: 1, label: '单人间' },
  { value: 2, label: '双人间' },
  { value: 3, label: '四人间' },
  { value: 4, label: '六人间' },
  { value: 5, label: '八人间' },
  { value: 6, label: '其他' }
]

export const ROOM_STATUS = [
  { value: 0, label: '停用', type: 'info' },
  { value: 1, label: '空闲', type: 'success' },
  { value: 2, label: '已满', type: 'warning' },
  { value: 3, label: '维修中', type: 'danger' },
  { value: 4, label: '预留', type: 'primary' }
]

export const BED_STATUS = [
  { value: 0, label: '停用', type: 'info' },
  { value: 1, label: '空闲', type: 'success' },
  { value: 2, label: '已入住', type: 'warning' },
  { value: 3, label: '维修中', type: 'danger' },
  { value: 4, label: '预留', type: 'primary' }
]

export const BED_TYPE = [
  { value: 1, label: '上床' },
  { value: 2, label: '下床' },
  { value: 3, label: '单床' }
]

export const GENDER_LIMIT = [
  { value: 0, label: '不限' },
  { value: 1, label: '男' },
  { value: 2, label: '女' }
]

export const BUILDING_STATUS = [
  { value: 0, label: '停用', type: 'info' },
  { value: 1, label: '启用', type: 'success' },
  { value: 2, label: '维修中', type: 'danger' }
]

export const RESIDENT_TYPE = [
  { value: 1, label: '正式' },
  { value: 2, label: '外包' },
  { value: 3, label: '其他' }
]

export const RESIDENT_STATUS = [
  { value: 0, label: '离职', type: 'info' },
  { value: 1, label: '在职', type: 'success' }
]

export const INTAKE_STATUS = [
  { value: 1, label: '待分配', type: 'warning' },
  { value: 2, label: '已入住', type: 'success' },
  { value: 3, label: '已取消', type: 'info' }
]

export const INTAKE_SOURCE = [
  { value: 1, label: 'OA' },
  { value: 2, label: 'HCP' },
  { value: 3, label: '手工' }
]

export function labelOf(dict: { value: number; label: string }[], value?: number): string {
  const item = dict.find((d) => d.value === value)
  return item ? item.label : '-'
}

export function tagTypeOf(dict: { value: number; label: string; type?: string }[], value?: number): string {
  const item = dict.find((d) => d.value === value)
  return item?.type || 'info'
}
