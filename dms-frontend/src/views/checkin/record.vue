<template>
  <el-card shadow="never">
    <el-form :inline="true" :model="query">
      <el-form-item label="楼栋">
        <el-select v-model="query.buildingId" placeholder="全部" clearable style="width: 180px" @change="reload">
          <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="reload">查询</el-button></el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="居住人" width="180">
        <template #default="{ row }">{{ row.residentName }}（{{ row.employeeNo }}）</template>
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

    <el-pagination v-if="total > query.size" style="margin-top: 12px; justify-content: flex-end"
      layout="total, prev, pager, next" :total="total" :current-page="query.page" :page-size="query.size"
      @current-change="onPageChange" />
  </el-card>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { pageRecords } from '@/api/checkin'
import { pageBuildings } from '@/api/building'
import type { CheckinRecord, Building } from '@/api/types'

const loading = ref(false)
const list = ref<CheckinRecord[]>([])
const total = ref(0)
const buildings = ref<Building[]>([])
const query = reactive({ buildingId: undefined as number | undefined, page: 1, size: 10 })

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
async function loadBuildings() {
  const res = await pageBuildings({ page: 1, size: 100 })
  buildings.value = res.records
}
onMounted(() => { loadBuildings(); reload() })
</script>
