<template>
  <el-card shadow="never">
    <!-- 单价配置 -->
    <el-form :inline="true" style="margin-bottom: 8px">
      <el-form-item label="电价(元/度)"><el-input-number v-model="rate.electricityPrice" :min="0" :precision="2" :step="0.1" /></el-form-item>
      <el-form-item label="水价(元/吨)"><el-input-number v-model="rate.waterPrice" :min="0" :precision="2" :step="0.5" /></el-form-item>
      <el-form-item><el-button type="primary" @click="saveRate">保存单价</el-button></el-form-item>
    </el-form>

    <!-- 抄表 + 生成 -->
    <el-form :inline="true" :model="query" @keyup.enter="reload">
      <el-form-item label="账期">
        <el-date-picker v-model="query.period" type="month" value-format="YYYY-MM" placeholder="全部" clearable style="width: 140px" @change="reload" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.meterType" placeholder="全部" clearable style="width: 110px" @change="reload">
          <el-option v-for="t in METER_TYPE" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="reload">查询</el-button>
        <el-button type="primary" @click="openReading">录入抄表</el-button>
        <el-button @click="openGenerate">生成水电账单</el-button>
          <el-button :loading="exporting" @click="onExport">导出</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="房间" width="120">
        <template #default="{ row }">{{ row.roomNumber ?? row.roomId }}</template>
      </el-table-column>
      <el-table-column prop="period" label="账期" width="100" />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">{{ labelOf(METER_TYPE, row.meterType) }}</template>
      </el-table-column>
      <el-table-column prop="prevReading" label="上期" width="100" />
      <el-table-column prop="currentReading" label="本期" width="100" />
      <el-table-column prop="consumption" label="用量" width="100" />
      <el-table-column prop="unitPrice" label="单价" width="90" />
      <el-table-column label="金额" width="110">
        <template #default="{ row }">¥{{ Number(row.amount).toFixed(2) }}</template>
      </el-table-column>
    </el-table>

    <el-pagination v-if="total > query.size" style="margin-top: 12px; justify-content: flex-end"
      layout="total, prev, pager, next" :total="total" :current-page="query.page" :page-size="query.size"
      @current-change="onPageChange" />

    <!-- 录入抄表 -->
    <el-dialog v-model="readingVisible" title="录入抄表" width="460px">
      <el-form ref="readingRef" :model="readingForm" :rules="readingRules" label-width="90px">
        <el-form-item label="楼栋" prop="buildingId">
          <el-select v-model="readingForm.buildingId" placeholder="选择楼栋" style="width: 100%" @change="onBuildingChange">
            <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间" prop="roomId">
          <el-select v-model="readingForm.roomId" placeholder="选择房间" style="width: 100%">
            <el-option v-for="r in rooms" :key="r.id" :label="r.roomNumber" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="账期" prop="period"><el-date-picker v-model="readingForm.period" type="month" value-format="YYYY-MM" style="width: 100%" /></el-form-item>
        <el-form-item label="表类型" prop="meterType">
          <el-select v-model="readingForm.meterType" style="width: 100%">
            <el-option v-for="t in METER_TYPE" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="本期读数" prop="currentReading"><el-input-number v-model="readingForm.currentReading" :min="0" :precision="2" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="readingVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSaveReading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 生成水电账单 -->
    <el-dialog v-model="genVisible" title="生成水电账单" width="420px">
      <el-form label-width="90px">
        <el-form-item label="账期"><el-date-picker v-model="genPeriod" type="month" value-format="YYYY-MM" placeholder="选择账期" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onGenerate">生成</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { getUtilityRate, updateUtilityRate, pageMeterReadings, saveMeterReading, generateUtilityBills } from '@/api/fee'
import { pageBuildings } from '@/api/building'
import { pageRooms } from '@/api/room'
import type { MeterReading, Building, Room } from '@/api/types'
import { METER_TYPE, labelOf } from '@/utils/dict'
import { exportLedger } from '@/api/export'

const loading = ref(false)
const exporting = ref(false)
const saving = ref(false)
const list = ref<MeterReading[]>([])
const total = ref(0)
const query = reactive({ period: undefined as string | undefined, meterType: undefined as number | undefined, page: 1, size: 10 })

const rate = reactive<{ electricityPrice: number; waterPrice: number }>({ electricityPrice: 0, waterPrice: 0 })

const buildings = ref<Building[]>([])
const rooms = ref<Room[]>([])
const readingVisible = ref(false)
const readingRef = ref<FormInstance>()
const readingForm = reactive<{ buildingId?: number; roomId?: number; period?: string; meterType?: number; currentReading?: number }>({})
const readingRules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  period: [{ required: true, message: '请选择账期', trigger: 'change' }],
  meterType: [{ required: true, message: '请选择表类型', trigger: 'change' }],
  currentReading: [{ required: true, message: '请输入本期读数', trigger: 'blur' }]
}

const genVisible = ref(false)
const genPeriod = ref<string>()

async function loadRate() {
  const r = await getUtilityRate()
  rate.electricityPrice = Number(r.electricityPrice)
  rate.waterPrice = Number(r.waterPrice)
}
async function saveRate() {
  await updateUtilityRate({ electricityPrice: rate.electricityPrice, waterPrice: rate.waterPrice })
  ElMessage.success('单价已保存')
}

async function reload() {
  loading.value = true
  try {
    const res = await pageMeterReadings(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}
function onPageChange(p: number) { query.page = p; reload() }

async function openReading() {
  Object.assign(readingForm, { buildingId: undefined, roomId: undefined, period: undefined, meterType: 1, currentReading: undefined })
  rooms.value = []
  if (!buildings.value.length) buildings.value = (await pageBuildings({ page: 1, size: 200 })).records
  readingVisible.value = true
}
async function onBuildingChange() {
  readingForm.roomId = undefined
  rooms.value = readingForm.buildingId ? (await pageRooms({ buildingId: readingForm.buildingId, page: 1, size: 200 })).records : []
}
async function onSaveReading() {
  await readingRef.value?.validate()
  saving.value = true
  try {
    await saveMeterReading(readingForm as { roomId: number; period: string; meterType: number; currentReading: number })
    ElMessage.success('抄表已保存')
    readingVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}

function openGenerate() { genPeriod.value = undefined; genVisible.value = true }
async function onGenerate() {
  if (!genPeriod.value) { ElMessage.warning('请选择账期'); return }
  saving.value = true
  try {
    const r = await generateUtilityBills({ period: genPeriod.value })
    ElMessage.success(`生成 ${r.generated} 张 / 跳过 ${r.skipped} 张`)
    genVisible.value = false
  } finally {
    saving.value = false
  }
}

async function onExport() {
  exporting.value = true
  try {
    await exportLedger('meter-readings', { ...query })
  } finally {
    exporting.value = false
  }
}

onMounted(() => { loadRate(); reload() })
</script>
