<template>
  <div>
    <el-card shadow="never">
      <div class="filters">
        <el-select v-model="buildingId" placeholder="全部楼栋" clearable style="width: 180px" @change="reload">
          <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
        </el-select>
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
      </div>

      <div class="board-head">
        <div class="bh-main">
          <h2>{{ buildingId && currentBuilding ? currentBuilding.buildingName : '全部楼栋' }}</h2>
          <div class="addr">{{ buildingId && currentBuilding ? (currentBuilding.address || '-') : '全局统计' }}</div>
        </div>
        <div class="kv num">
          <div class="item"><div class="n c-accent">{{ occupancyRate }}<small>%</small></div><div class="l">入住率</div></div>
          <div class="item"><div class="n c-ok">{{ freeBeds }}</div><div class="l">空闲床位</div></div>
          <div class="item"><div class="n c-warn">{{ countByStatus(2) }}</div><div class="l">已满</div></div>
          <div class="item"><div class="n c-bad">{{ countByStatus(3) }}</div><div class="l">维修中</div></div>
          <div class="item"><div class="n">{{ list.length }}</div><div class="l">房间数</div></div>
          <div class="item"><div class="n">{{ totalBeds }}</div><div class="l">床位数</div></div>
        </div>
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
                  <span class="status">{{ labelOf(ROOM_STATUS, r.status) }}</span>
                </div>
                <div class="meta">
                  {{ labelOf(ROOM_TYPE, r.roomType) }}<template v-if="r.orientation"> · {{ r.orientation }}</template><template v-if="r.area"> · {{ r.area }}㎡</template>
                </div>
                <div class="bedbar" :title="`已住 ${r.occupiedBeds}/${r.bedCount} 床`">
                  <i v-for="i in r.bedCount" :key="i" :class="{ used: i <= r.occupiedBeds }"></i>
                </div>
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
import { parseFacilities } from '@/utils/facility'

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
  const res = await pageBuildings({ page: 1, size: 1000 })
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
/* 筛选行：楼栋选择 + 可点击图例 */
.filters { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; margin-bottom: 14px; }

/* 图例（可点击筛选） */
.legend { display: inline-flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.legend-item {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 12.5px; color: var(--dms-ink-2);
  padding: 4px 10px; border-radius: 99px;
  border: 1px solid transparent; background: transparent; cursor: pointer;
  font-family: inherit; transition: all 0.15s;
}
.legend-item:hover { background: var(--dms-hover); }
.legend-item.active { border-color: var(--dms-accent); color: var(--dms-accent); background: var(--dms-accent-soft); }
.legend-item.dimmed { opacity: 0.45; }
.legend-count { font-style: normal; font-weight: 700; font-size: 11.5px; font-variant-numeric: tabular-nums; }
.legend-swatch { width: 9px; height: 9px; border-radius: 3px; background: currentColor; display: inline-block; }
.sw-0 { color: var(--dms-ink-3); } .sw-1 { color: var(--dms-ok); } .sw-2 { color: var(--dms-warn); }
.sw-3 { color: var(--dms-bad); } .sw-4 { color: var(--dms-hold); }

/* board-head：左楼栋名 + 地址，右侧大数字统计 */
.board-head {
  display: flex; align-items: center; gap: 16px; flex-wrap: wrap;
  padding: 14px 18px; margin-bottom: 16px;
  border: 1px solid var(--dms-hairline);
  border-radius: var(--dms-radius-card);
  background: var(--dms-surface);
  box-shadow: none;
}
.bh-main { min-width: 200px; }
.bh-main h2 { margin: 0; font-size: 16px; font-weight: 700; letter-spacing: -0.01em; }
.bh-main .addr { font-size: 12px; color: var(--dms-ink-3); margin-top: 2px; }
.kv { display: flex; gap: 24px; margin-left: auto; flex-wrap: wrap; }
.kv .item .n { font-size: 18px; font-weight: 700; font-variant-numeric: tabular-nums; }
.kv .item .n small { font-size: 12px; font-weight: 600; margin-left: 1px; }
.kv .item .l { font-size: 11px; color: var(--dms-ink-3); margin-top: 1px; }
.c-accent { color: var(--dms-accent-ink); }
.c-ok { color: var(--dms-ok); } .c-warn { color: var(--dms-warn); } .c-bad { color: var(--dms-bad); }

/* 楼层分组 */
.floor-group { margin-bottom: 18px; }
.floor-head {
  display: flex; align-items: baseline; gap: 12px;
  padding: 0 2px 8px;
}
.floor-name { font-size: 15px; font-weight: 700; letter-spacing: -0.01em; }
.floor-stat { font-size: 12px; color: var(--dms-ink-2); }

/* 平面图：独立卡片网格 */
.plan {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  gap: 10px;
}
.room {
  position: relative;
  min-height: 108px;
  padding: 13px 14px 12px;
  border: 1px solid var(--dms-hairline);
  border-radius: var(--dms-radius-card);
  background: var(--dms-surface);
  transition: border-color 0.15s;
}
.room:hover { border-color: var(--el-border-color); }
/* 左缘 3px 状态色条 */
.room::before {
  content: ""; position: absolute; left: 0; top: 13px; bottom: 13px;
  width: 3px; border-radius: 0 2px 2px 0;
  background: var(--st-c);
}
.room-head { display: flex; justify-content: space-between; align-items: center; }
.no { font-size: 15px; font-weight: 700; letter-spacing: -0.01em; display: inline-flex; align-items: center; gap: 6px; font-variant-numeric: tabular-nums; }
.gender {
  font-style: normal; font-size: 12px; font-weight: 700;
  width: 18px; height: 18px; border-radius: 50%;
  display: inline-flex; align-items: center; justify-content: center;
}
.gender.male { background: var(--dms-accent-soft); color: var(--dms-accent-ink); }
.gender.female { background: color-mix(in srgb, var(--dms-bad) 12%, transparent); color: var(--dms-bad); }
.meta { font-size: 12px; color: var(--dms-ink-3); margin-top: 4px; }

/* 床位方格：一格=一张床，实心=已入住 */
.bedbar { display: flex; gap: 5px; margin-top: 12px; }
.bedbar i { width: 14px; height: 14px; border-radius: 4px; border: 1.5px solid var(--el-border-color); background: var(--dms-surface); }
.bedbar i.used { background: var(--dms-accent); border-color: var(--dms-accent); }
.room.st-3 .bedbar i.used { background: color-mix(in srgb, var(--dms-bad) 55%, transparent); border-color: transparent; }
.room.st-0 .bedbar i.used { background: var(--dms-ink-3); border-color: transparent; }

/* 状态圆点标签（右上角） */
.status {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 11.5px; font-weight: 500;
  padding: 2px 8px;
  border-radius: 6px;
  color: var(--st-c);
  background: color-mix(in srgb, var(--st-c) 12%, transparent);
}
.status::before { content: ""; width: 5px; height: 5px; border-radius: 50%; background: currentColor; }
.st-0 { --st-c: var(--dms-ink-3); }
.st-1 { --st-c: var(--dms-ok); }
.st-2 { --st-c: var(--dms-warn); }
.st-3 { --st-c: var(--dms-bad); }
.st-4 { --st-c: var(--dms-hold); }

/* 悬停提示 */
.tip { font-size: 12.5px; line-height: 1.7; }
.tip-title { font-weight: 700; margin-bottom: 2px; }
@media (max-width: 1199px) { .kv { gap: 18px; } .plan { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); } }
@media (max-width: 767px) { .board-head { padding: 12px 14px; } .kv { margin-left: 0; gap: 16px; } .plan { grid-template-columns: repeat(2, minmax(132px, 1fr)); overflow-x: auto; } .room { min-height: 112px; } }
</style>
