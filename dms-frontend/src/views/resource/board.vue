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
          <span v-for="s in ROOM_STATUS" :key="s.value" class="legend">
            <el-tag :type="s.type as any" effect="dark" size="small">{{ s.label }}</el-tag>
          </span>
        </el-form-item>
      </el-form>

      <div v-loading="loading" class="board">
        <div v-for="r in list" :key="r.id" class="cell" :class="'st-' + r.status">
          <div class="no">{{ r.roomNumber }}</div>
          <div class="meta">{{ labelOf(ROOM_TYPE, r.roomType) }}</div>
          <div class="meta">{{ r.occupiedBeds }}/{{ r.bedCount }} 床</div>
          <el-tag :type="tagTypeOf(ROOM_STATUS, r.status) as any" size="small" effect="dark">{{ labelOf(ROOM_STATUS, r.status) }}</el-tag>
        </div>
        <el-empty v-if="!loading && !list.length" description="暂无房间" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { pageBuildings } from '@/api/building'
import { getRoomBoard } from '@/api/room'
import type { Building, RoomBoard } from '@/api/types'
import { ROOM_TYPE, ROOM_STATUS, labelOf, tagTypeOf } from '@/utils/dict'

const buildings = ref<Building[]>([])
const buildingId = ref<number>()
const list = ref<RoomBoard[]>([])
const loading = ref(false)

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
.legend { margin-right: 8px; }
.board { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 8px; }
.cell {
  width: 130px; padding: 12px; border-radius: 6px; color: #fff;
  display: flex; flex-direction: column; gap: 4px; align-items: flex-start;
}
.no { font-size: 16px; font-weight: bold; }
.meta { font-size: 12px; opacity: 0.9; }
.st-0 { background: #909399; }
.st-1 { background: #67c23a; }
.st-2 { background: #e6a23c; }
.st-3 { background: #f56c6c; }
.st-4 { background: #409eff; }
</style>
