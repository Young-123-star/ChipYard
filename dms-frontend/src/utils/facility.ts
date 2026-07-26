// 房间配套设施的 JSON 解析/序列化工具，统一以全角 × 展示数量

/** 历史英文设施名到中文名的映射 */
export const FACILITY_NAMES: Record<string, string> = {
  air_conditioner: '空调',
  water_heater: '热水器',
  wardrobe: '衣柜',
  desk: '书桌'
}

/** 设施编辑行 */
export interface FacilityRow {
  name: string
  count: number
}

function normalizeFacilityName(name: string): string {
  return FACILITY_NAMES[name] || name
}

/** 解析设施 JSON 为展示文本列表，数量大于 1 时显示“名称×数量” */
export function parseFacilities(json?: string): string[] {
  if (!json) return []
  try {
    const obj = JSON.parse(json) as Record<string, unknown>
    return Object.entries(obj)
      .filter(([, v]) => Number(v) > 0)
      .map(([k, v]) => {
        const name = normalizeFacilityName(k)
        return Number(v) > 1 ? `${name}×${v}` : name
      })
  } catch {
    return []
  }
}

/** 解析设施 JSON 为编辑行列表（非法 JSON 返回空数组） */
export function parseFacilityRows(json?: string): FacilityRow[] {
  if (!json) return []
  try {
    const obj = JSON.parse(json) as Record<string, unknown>
    return Object.entries(obj)
      .map(([key, value]) => ({ name: normalizeFacilityName(key), count: Number(value) }))
      .filter((item) => item.name && Number.isFinite(item.count) && item.count > 0)
  } catch {
    return []
  }
}

/** 将编辑行列表序列化为设施 JSON，同名设施数量合并 */
export function serializeFacilities(rows: FacilityRow[]): string {
  const obj: Record<string, number> = {}
  rows.forEach((row) => {
    const name = row.name.trim()
    const count = Number(row.count)
    if (!name || !Number.isFinite(count) || count <= 0) return
    obj[name] = (obj[name] || 0) + Math.floor(count)
  })
  return Object.keys(obj).length ? JSON.stringify(obj) : ''
}
