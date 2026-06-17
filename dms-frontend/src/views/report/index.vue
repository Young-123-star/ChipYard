<template>
  <div class="report-page">
    <el-card class="block" shadow="never">
      <template #header><span class="block-title">账期汇总</span></template>
      <v-chart class="chart" :option="periodOption" autoresize />
      <el-table :data="periodList" size="small" class="detail-table">
        <el-table-column prop="period" label="账期" min-width="100" />
        <el-table-column label="应收" min-width="110">
          <template #default="{ row }">{{ money(row.total) }}</template>
        </el-table-column>
        <el-table-column label="已缴" min-width="110">
          <template #default="{ row }">{{ money(row.paid) }}</template>
        </el-table-column>
        <el-table-column label="未缴" min-width="110">
          <template #default="{ row }">{{ money(row.unpaid) }}</template>
        </el-table-column>
        <el-table-column label="收缴率" min-width="90">
          <template #default="{ row }">{{ row.collectRate }}%</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="block" shadow="never">
      <template #header><span class="block-title">水电用量趋势</span></template>
      <v-chart class="chart" :option="usageOption" autoresize />
    </el-card>

    <el-card class="block" shadow="never">
      <template #header><span class="block-title">按楼栋汇总</span></template>
      <v-chart class="chart" :option="buildingOption" autoresize />
    </el-card>

    <el-card class="block" shadow="never">
      <template #header><span class="block-title">欠费排行（Top 10）</span></template>
      <el-table :data="arrearsList" size="small">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="residentName" label="居住人" min-width="120" />
        <el-table-column prop="employeeNo" label="工号" min-width="120" />
        <el-table-column label="欠费额" min-width="120">
          <template #default="{ row }">{{ money(row.unpaidAmount) }}</template>
        </el-table-column>
        <el-table-column prop="unpaidCount" label="欠费张数" min-width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPeriodSummary, getBuildingSummary, getArrearsRanking, getUsageTrend } from '@/api/report'
import type { PeriodSummary, BuildingSummary, ArrearsRank, UsageTrend } from '@/api/types'

const periodList = ref<PeriodSummary[]>([])
const buildingList = ref<BuildingSummary[]>([])
const arrearsList = ref<ArrearsRank[]>([])
const usageList = ref<UsageTrend[]>([])

function money(v: number | undefined): string {
  return '¥' + Number(v ?? 0).toFixed(2)
}

const periodOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['住宿费', '电费', '水费', '收缴率'] },
  grid: { left: 56, right: 56, top: 40, bottom: 30 },
  xAxis: { type: 'category', data: periodList.value.map(p => p.period) },
  yAxis: [
    { type: 'value', name: '金额', axisLabel: { formatter: '{value}' } },
    { type: 'value', name: '收缴率', min: 0, max: 100, axisLabel: { formatter: '{value}%' } }
  ],
  series: [
    { name: '住宿费', type: 'bar', stack: 'amt', data: periodList.value.map(p => Number(p.rentTotal)) },
    { name: '电费', type: 'bar', stack: 'amt', data: periodList.value.map(p => Number(p.elecTotal)) },
    { name: '水费', type: 'bar', stack: 'amt', data: periodList.value.map(p => Number(p.waterTotal)) },
    { name: '收缴率', type: 'line', yAxisIndex: 1, data: periodList.value.map(p => Number(p.collectRate)) }
  ]
}))

const usageOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['电(度)', '水(吨)'] },
  grid: { left: 56, right: 30, top: 40, bottom: 30 },
  xAxis: { type: 'category', data: usageList.value.map(u => u.period) },
  yAxis: { type: 'value' },
  series: [
    { name: '电(度)', type: 'line', smooth: true, data: usageList.value.map(u => Number(u.electricity)) },
    { name: '水(吨)', type: 'line', smooth: true, data: usageList.value.map(u => Number(u.water)) }
  ]
}))

const buildingOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['已缴', '未缴'] },
  grid: { left: 100, right: 30, top: 40, bottom: 30 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: buildingList.value.map(b => b.buildingName) },
  series: [
    { name: '已缴', type: 'bar', stack: 'amt', data: buildingList.value.map(b => Number(b.paid)) },
    { name: '未缴', type: 'bar', stack: 'amt', data: buildingList.value.map(b => Number(b.unpaid)) }
  ]
}))

async function load() {
  try {
    periodList.value = await getPeriodSummary()
    buildingList.value = await getBuildingSummary()
    arrearsList.value = await getArrearsRanking(10)
    usageList.value = await getUsageTrend()
  } catch (e) {
    ElMessage.error('报表加载失败')
  }
}

onMounted(load)
</script>

<style scoped>
.report-page { display: flex; flex-direction: column; gap: 18px; }
.block-title { font-weight: 600; }
.chart { height: 320px; width: 100%; }
.detail-table { margin-top: 12px; }
</style>
