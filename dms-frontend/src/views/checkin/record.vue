<template>
  <DataView title="入住档案" :total="total">
    <template #filters>
      <div class="filter-item">
        <span class="filter-item__label">楼栋</span>
        <el-select v-model="query.buildingId" placeholder="全部" clearable filterable style="width: 160px" @change="onBuildingChange">
          <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-item__label">楼层</span>
        <el-select v-model="query.floorId" placeholder="全部" clearable filterable :disabled="!query.buildingId" style="width: 120px" @change="onFloorChange">
          <el-option v-for="item in floors" :key="item.id" :label="item.floorName || `${item.floorNumber}层`" :value="item.id" />
        </el-select>
      </div>
      <div class="filter-item">
        <span class="filter-item__label">房间</span>
        <el-select v-model="query.roomId" placeholder="全部" clearable filterable :disabled="!query.floorId" style="width: 130px" @change="search">
          <el-option v-for="item in rooms" :key="item.id" :label="item.roomNumber" :value="item.id" />
        </el-select>
      </div>
    </template>
    <template #filter-actions>
      <el-button @click="search">查询</el-button>
    </template>
    <template #actions>
      <el-button :loading="exporting" @click="onExport">导出</el-button>
    </template>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="居住人" width="180">
        <template #default="{ row }"><div class="cell-main">{{ row.residentName }}</div><div class="cell-sub">{{ row.employeeNo }}</div></template>
      </el-table-column>
      <el-table-column label="楼栋" width="160">
        <template #default="{ row }">{{ buildingName(row.buildingId) }}</template>
      </el-table-column>
      <el-table-column prop="roomId" label="房间ID" width="100" />
      <el-table-column prop="bedId" label="床位ID" width="100" />
      <el-table-column prop="checkinDate" label="入住日期" width="140" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" round>{{ row.status === 1 ? '在住' : '已退宿' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <template #pagination>
      <el-pagination layout="total, prev, pager, next" :total="total" :current-page="query.page" :page-size="query.size"
        @current-change="onPageChange" />
    </template>
  </DataView>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { pageRecords } from '@/api/checkin'
import { useRoomLocationOptions } from '@/composables/useRoomLocationOptions'
import type { CheckinRecord } from '@/api/types'
import { exportLedger } from '@/api/export'
import DataView from '@/components/layout/DataView.vue'

const loading = ref(false)
const exporting = ref(false)
const list = ref<CheckinRecord[]>([])
const total = ref(0)
const { buildings, floors, rooms, loadBuildings, loadFloors, loadRooms } = useRoomLocationOptions()
const query = reactive({ buildingId: undefined as number | undefined, floorId: undefined as number | undefined, roomId: undefined as number | undefined, page: 1, size: 10 })

function buildingName(id?: number) {
  return buildings.value.find((b) => b.id === id)?.buildingName || '-'
}
async function reload() {
  loading.value = true
  try {
    const res = await pageRecords(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}
function onPageChange(p: number) { query.page = p; reload() }
function search() { query.page = 1; reload() }
async function onBuildingChange() {
  query.floorId = undefined; query.roomId = undefined
  await loadFloors(query.buildingId); search()
}
async function onFloorChange() {
  query.roomId = undefined
  await loadRooms(query.buildingId, query.floorId); search()
}
async function onExport() {
  exporting.value = true
  try {
    await exportLedger('checkin-records', { ...query })
  } finally {
    exporting.value = false
  }
}

onMounted(() => { loadBuildings(); reload() })
</script>

<style scoped>
.filter-item { display: flex; align-items: center; gap: 6px; }
.filter-item__label { color: var(--dms-ink-2); font-size: 13px; white-space: nowrap; }
</style>
