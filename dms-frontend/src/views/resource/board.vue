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
            <span v-for="s in ROOM_STATUS" :key="s.value" class="legend-item">
              <i class="legend-swatch" :class="'sw-' + s.value"></i>{{ s.label }}
            </span>
          </span>
        </el-form-item>
      </el-form>

      <div v-if="currentBuilding" class="title-block">
        <div class="tb-cell"><span>楼栋</span><b>{{ currentBuilding.buildingName }}</b></div>
        <div class="tb-cell"><span>地址</span><b>{{ currentBuilding.address || '-' }}</b></div>
        <div class="tb-cell"><span>层数</span><b>{{ currentBuilding.floorCount }}F</b></div>
        <div class="tb-cell"><span>房间</span><b>{{ list.length }}</b></div>
        <div class="tb-cell"><span>床位</span><b>{{ totalBeds }}</b></div>
        <div class="tb-cell"><span>空闲床位</span><b>{{ freeBeds }}</b></div>
      </div>

      <div v-loading="loading" class="plan">
        <div v-for="r in list" :key="r.id" class="room" :class="'st-' + r.status">
          <div class="room-head">
            <span class="no">{{ r.roomNumber }}</span>
            <span class="beds">
              <i v-for="i in r.bedCount" :key="i" class="bed" :class="{ occupied: i <= r.occupiedBeds }"></i>
            </span>
          </div>
          <div class="meta">{{ labelOf(ROOM_TYPE, r.roomType) }} · {{ r.occupiedBeds }}/{{ r.bedCount }} 床</div>
          <span class="status">{{ labelOf(ROOM_STATUS, r.status) }}</span>
        </div>
        <el-empty v-if="!loading && !list.length" description="暂无房间" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { pageBuildings } from '@/api/building'
import { getRoomBoard } from '@/api/room'
import type { Building, RoomBoard } from '@/api/types'
import { ROOM_TYPE, ROOM_STATUS, labelOf } from '@/utils/dict'

const buildings = ref<Building[]>([])
const buildingId = ref<number>()
const list = ref<RoomBoard[]>([])
const loading = ref(false)

const currentBuilding = computed(() =>
  buildings.value.find((b) => b.id === (buildingId.value ?? list.value[0]?.buildingId))
)
const totalBeds = computed(() => list.value.reduce((s, r) => s + r.bedCount, 0))
const freeBeds = computed(() => list.value.reduce((s, r) => s + (r.bedCount - r.occupiedBeds), 0))

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
/* 图例 */
.legend { display: inline-flex; gap: 16px; align-items: center; }
.legend-item { display: inline-flex; align-items: center; gap: 6px; font-size: 12.5px; color: var(--dms-ink-2); }
.legend-swatch { width: 11px; height: 11px; border-radius: 3px; border: 1.5px solid; display: inline-block; }
.sw-0 { color: #98989d; } .sw-1 { color: var(--dms-ok); } .sw-2 { color: var(--dms-warn); }
.sw-3 { color: var(--dms-bad); } .sw-4 { color: var(--dms-hold); }

/* 图签信息条（蓝图 title block 的 Apple 化） */
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
.no { font-size: 18px; font-weight: 700; letter-spacing: -0.01em; }
.meta { font-size: 12.5px; color: var(--dms-ink-2); margin-top: 4px; }

/* 床位图标：空心=空闲，实心=已入住 */
.beds { display: flex; gap: 4px; }
.bed {
  width: 9px; height: 15px;
  border: 1.5px solid #c7c7cc;
  border-radius: 3px;
}
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
</style>
