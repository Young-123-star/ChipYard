<template>
  <div>
    <el-card shadow="never">
      <el-form :inline="true">
        <el-form-item label="楼栋">
          <el-select v-model="buildingId" placeholder="全部" clearable style="width: 180px" @change="reload">
            <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <span class="legend">
            <button
              v-for="s in ROOM_STATUS"
              :key="s.value"
              class="legend-item"
              :class="{ active: statusFilter === s.value, dimmed: statusFilter !== null && statusFilter !== s.value }"
              @click.prevent="toggleStatus(s.value)"
            >
              <i class="legend-swatch" :class="'sw-' + s.value"></i>{{ s.label }}
              <em v-if="statusCount(s.value)" class="legend-count">{{ statusCount(s.value) }}</em>
            </button>
          </span>
        </el-form-item>
      </el-form>

      <div class="title-block">
        <div class="tb-cell tb-rate">
          <span>入住率</span>
          <b>{{ occupancyRate }}<small>%</small></b>
        </div>
        <div class="tb-cell"><span>空闲床位</span><b class="c-ok">{{ freeBeds }}</b></div>
        <div class="tb-cell"><span>已满房间</span><b class="c-warn">{{ countByStatus(2) }}</b></div>
        <div class="tb-cell"><span>维修中</span><b class="c-bad">{{ countByStatus(3) }}</b></div>
        <div class="tb-cell"><span>房间</span><b>{{ list.length }}</b></div>
        <div class="tb-cell"><span>床位</span><b>{{ totalBeds }}</b></div>
        <div v-if="currentBuilding" class="tb-cell"><span>地址</span><b>{{ currentBuilding.address || '-' }}</b></div>
      </div>

      <div v-loading="loading">
        <div v-for="g in groupedFloors" :key="g.key" class="floor-group">
          <div class="floor-head">
            <span class="floor-name">{{ g.label }}</span>
            <span class="floor-stat">{{ g.rooms.length }} 间 · 空闲床位 {{ g.freeBeds }}</span>
          </div>
          <div class="plan">
            <el-tooltip v-for="r in g.rooms" :key="r.id" placement="top" :show-after="120">
              <template #content>
                <div class="tip">
                  <div class="tip-title">{{ r.roomNumber }} · {{ labelOf(ROOM_TYPE, r.roomType) }}</div>
                  <div>面积：{{ r.area ?? '-' }} ㎡ ｜ 朝向：{{ r.orientation || '-' }}</div>
                  <div>床位：{{ r.occupiedBeds }}/{{ r.bedCount }} ｜ 限制：{{ labelOf(GENDER_LIMIT, r.genderLimit) }}</div>
                  <div>设施：{{ parseFacilities(r.facilities).join('、') || '无' }}</div>
                </div>
              </template>
              <div class="room" :class="'st-' + r.status">
                <div class="room-head">
                  <span class="no">
                    {{ r.roomNumber }}
                    <i v-if="r.genderLimit === 1" class="gender male">♂</i>
                    <i v-else-if="r.genderLimit === 2" class="gender female">♀</i>
                  </span>
                  <span class="beds">
                    <i v-for="i in r.bedCount" :key="i" class="bed" :class="{ occupied: i <= r.occupiedBeds }"></i>
                  </span>
                </div>
                <div class="meta">{{ labelOf(ROOM_TYPE, r.roomType) }} · {{ r.occupiedBeds }}/{{ r.bedCount }} 床</div>
                <span class="status">{{ labelOf(ROOM_STATUS, r.status) }}</span>
              </div>
            </el-tooltip>
          </div>
        </div>
        <el-empty v-if="!loading && !filteredList.length" description="暂无符合条件的房间" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { pageBuildings } from '@/api/building'
import { getRoomBoard } from '@/api/room'
import type { Building, RoomBoard } from '@/api/types'
import { ROOM_TYPE, ROOM_STATUS, GENDER_LIMIT, labelOf } from '@/utils/dict'

const FACILITY_NAMES: Record<string, string> = {
  air_conditioner: '空调', water_heater: '热水器', wardrobe: '衣柜', desk: '书桌'
}
function parseFacilities(json?: string): string[] {
  if (!json) return []
  try {
    return Object.entries(JSON.parse(json))
      .filter(([, v]) => Number(v) > 0)
      .map(([k, v]) => {
        const name = FACILITY_NAMES[k] || k
        return Number(v) > 1 ? `${name}×${v}` : name
      })
  } catch {
    return []
  }
}

const buildings = ref<Building[]>([])
const buildingId = ref<number>()
const list = ref<RoomBoard[]>([])
const loading = ref(false)
const statusFilter = ref<number | null>(null)

const currentBuilding = computed(() =>
  buildings.value.find((b) => b.id === (buildingId.value ?? list.value[0]?.buildingId))
)
const totalBeds = computed(() => list.value.reduce((s, r) => s + r.bedCount, 0))
const freeBeds = computed(() => list.value.reduce((s, r) => s + (r.bedCount - r.occupiedBeds), 0))
const occupancyRate = computed(() =>
  totalBeds.value ? Math.round(((totalBeds.value - freeBeds.value) / totalBeds.value) * 100) : 0
)

function countByStatus(st: number) {
  return list.value.filter((r) => r.status === st).length
}
const statusCount = countByStatus

const filteredList = computed(() =>
  statusFilter.value === null ? list.value : list.value.filter((r) => r.status === statusFilter.value)
)

interface FloorGroup { key: string; label: string; rooms: RoomBoard[]; freeBeds: number }
const groupedFloors = computed<FloorGroup[]>(() => {
  const map = new Map<string, RoomBoard[]>()
  for (const r of filteredList.value) {
    const key = `${r.buildingId}-${r.floorNumber ?? 0}`
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(r)
  }
  const showBuilding = !buildingId.value && buildings.value.length > 1
  return [...map.entries()]
    .sort((a, b) => a[0].localeCompare(b[0], undefined, { numeric: true }))
    .map(([key, rooms]) => {
      const first = rooms[0]
      const bName = buildings.value.find((b) => b.id === first.buildingId)?.buildingName || ''
      return {
        key,
        label: `${showBuilding ? bName + ' · ' : ''}${first.floorNumber ?? '-'}F`,
        rooms,
        freeBeds: rooms.reduce((s, r) => s + (r.bedCount - r.occupiedBeds), 0)
      }
    })
})

function toggleStatus(st: number) {
  statusFilter.value = statusFilter.value === st ? null : st
}

async function loadBuildings() {
  const res = await pageBuildings({ page: 1, size: 100 })
  buildings.value = res.records
}

async function reload() {
  loading.value = true
  try {
    list.value = await getRoomBoard({ buildingId: buildingId.value })
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadBuildings(); reload() })
</script>

<style scoped>
/* 图例（可点击筛选） */
.legend { display: inline-flex; gap: 8px; align-items: center; }
.legend-item {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 12.5px; color: var(--dms-ink-2);
  padding: 4px 10px; border-radius: 99px;
  border: 1px solid transparent; background: transparent; cursor: pointer;
  font-family: inherit; transition: all 0.15s;
}
.legend-item:hover { background: rgba(0, 0, 0, 0.04); }
.legend-item.active { border-color: var(--dms-accent); color: var(--dms-accent); background: rgba(0, 113, 227, 0.06); }
.legend-item.dimmed { opacity: 0.45; }
.legend-count { font-style: normal; font-weight: 700; font-size: 11.5px; }
.legend-swatch { width: 11px; height: 11px; border-radius: 3px; border: 1.5px solid; display: inline-block; }
.sw-0 { color: #98989d; } .sw-1 { color: var(--dms-ok); } .sw-2 { color: var(--dms-warn); }
.sw-3 { color: var(--dms-bad); } .sw-4 { color: var(--dms-hold); }

/* 图签信息条 */
.title-block {
  display: inline-flex;
  border: 1px solid var(--dms-hairline);
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 18px;
  background: rgba(255, 255, 255, 0.55);
}
.tb-cell { padding: 10px 18px; border-right: 1px solid var(--dms-hairline); }
.tb-cell:last-child { border-right: none; }
.tb-cell span { display: block; font-size: 11.5px; color: var(--dms-ink-2); margin-bottom: 2px; }
.tb-cell b { font-size: 14.5px; font-weight: 600; letter-spacing: -0.01em; }
.tb-rate b { font-size: 22px; font-weight: 700; color: var(--dms-accent); }
.tb-rate small { font-size: 12px; font-weight: 600; margin-left: 1px; }
.c-ok { color: #1d8a3e; } .c-warn { color: #b06b00; } .c-bad { color: #c22a20; }

/* 楼层分组 */
.floor-group { margin-bottom: 18px; }
.floor-head {
  display: flex; align-items: baseline; gap: 12px;
  padding: 0 2px 8px;
}
.floor-name { font-size: 15px; font-weight: 700; letter-spacing: -0.01em; }
.floor-stat { font-size: 12px; color: var(--dms-ink-2); }

/* 平面图：连体房间格 */
.plan {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  border: 1px solid var(--dms-hairline);
  border-radius: 14px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.45);
}
.room {
  position: relative;
  min-height: 124px;
  padding: 16px 18px;
  border-right: 1px solid var(--dms-hairline);
  border-bottom: 1px solid var(--dms-hairline);
  transition: background 0.18s;
}
.room:hover { background: rgba(0, 113, 227, 0.04); }
.room-head { display: flex; justify-content: space-between; align-items: flex-start; }
.no { font-size: 18px; font-weight: 700; letter-spacing: -0.01em; display: inline-flex; align-items: center; gap: 6px; }
.gender {
  font-style: normal; font-size: 12px; font-weight: 700;
  width: 18px; height: 18px; border-radius: 50%;
  display: inline-flex; align-items: center; justify-content: center;
}
.gender.male { background: rgba(0, 113, 227, 0.12); color: #0b6bd0; }
.gender.female { background: rgba(255, 45, 85, 0.12); color: #d6336c; }
.meta { font-size: 12.5px; color: var(--dms-ink-2); margin-top: 4px; }

/* 床位图标：空心=空闲，实心=已入住 */
.beds { display: flex; gap: 4px; }
.bed { width: 9px; height: 15px; border: 1.5px solid #c7c7cc; border-radius: 3px; }
.bed.occupied { background: var(--st-c); border-color: var(--st-c); }

/* 状态角标 */
.status {
  position: absolute;
  left: 18px; bottom: 14px;
  font-size: 11.5px; font-weight: 600;
  padding: 3px 10px;
  border-radius: 99px;
  color: var(--st-c);
  background: color-mix(in srgb, var(--st-c) 12%, transparent);
}
.st-0 { --st-c: #98989d; }
.st-1 { --st-c: #1d8a3e; }
.st-2 { --st-c: #b06b00; }
.st-3 { --st-c: #c22a20; }
.st-4 { --st-c: #2f6fbe; }

/* 悬停提示 */
.tip { font-size: 12.5px; line-height: 1.7; }
.tip-title { font-weight: 700; margin-bottom: 2px; }
@media (max-width: 1199px) { .title-block { display: grid; grid-template-columns: repeat(4, 1fr); width: 100%; } .tb-cell { border-bottom: 1px solid var(--dms-hairline); } .plan { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); } }
@media (max-width: 767px) { .legend { flex-wrap: wrap; } .title-block { grid-template-columns: repeat(2, 1fr); } .tb-cell { padding: 10px 12px; } .plan { grid-template-columns: repeat(2, minmax(132px, 1fr)); overflow-x: auto; } .room { min-height: 112px; } }
</style>
