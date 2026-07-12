<template>
  <div v-loading="loading" class="report-page">
    <el-alert v-if="error" type="error" :closable="false" show-icon title="报表加载失败">
      <el-button link type="primary" @click="load">重新加载</el-button>
    </el-alert>

    <section class="period-summary">
      <div class="period-summary__title"><span>最新账期</span><strong>{{ latest?.period || '暂无数据' }}</strong></div>
      <div v-for="item in summaryItems" :key="item.label" class="summary-item">
        <span>{{ item.label }}</span><strong :class="item.tone">{{ item.value }}</strong><small>{{ item.note }}</small>
      </div>
    </section>

    <section class="chart-grid">
      <el-card shadow="never">
        <template #header><div class="card-title"><div><b>账期收缴趋势</b><span>应收构成与收缴率</span></div></div></template>
        <v-chart v-if="periodList.length" class="chart" :option="periodOption" autoresize />
        <el-empty v-else description="暂无账期数据" :image-size="72" />
      </el-card>
      <el-card shadow="never">
        <template #header><div class="card-title"><div><b>楼栋收缴对比</b><span>已缴与未缴金额</span></div></div></template>
        <v-chart v-if="buildingList.length" class="chart" :option="buildingOption" autoresize />
        <el-empty v-else description="暂无楼栋数据" :image-size="72" />
      </el-card>
      <el-card shadow="never" class="usage-card">
        <template #header><div class="card-title"><div><b>水电用量趋势</b><span>月度用量变化</span></div></div></template>
        <v-chart v-if="usageList.length" class="chart" :option="usageOption" autoresize />
        <el-empty v-else description="暂无用量数据" :image-size="72" />
      </el-card>
    </section>

    <el-card shadow="never" class="arrears-card">
      <template #header><div class="card-title"><div><b>欠费明细</b><span>欠费金额前 10 名</span></div><el-tag type="danger" effect="light">{{ arrearsList.length }} 人</el-tag></div></template>
      <el-table :data="arrearsList" empty-text="当前没有欠费数据">
        <el-table-column type="index" label="#" width="54" />
        <el-table-column prop="residentName" label="居住人" min-width="120" />
        <el-table-column prop="employeeNo" label="工号" min-width="120" />
        <el-table-column label="欠费金额" min-width="130"><template #default="{ row }"><strong class="danger-text">{{ money(row.unpaidAmount) }}</strong></template></el-table-column>
        <el-table-column prop="unpaidCount" label="欠费账单" min-width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getArrearsRanking, getBuildingSummary, getPeriodSummary, getUsageTrend } from '@/api/report'
import type { ArrearsRank, BuildingSummary, PeriodSummary, UsageTrend } from '@/api/types'

const COLORS = ['#1f6feb', '#53a6e8', '#7cc8b5', '#d23b34']
const axis = { axisLine: { lineStyle: { color: '#dfe4ec' } }, axisLabel: { color: '#6b7485' }, splitLine: { lineStyle: { color: '#eef1f5' } } }
const loading = ref(false)
const error = ref(false)
const periodList = ref<PeriodSummary[]>([])
const buildingList = ref<BuildingSummary[]>([])
const arrearsList = ref<ArrearsRank[]>([])
const usageList = ref<UsageTrend[]>([])
const latest = computed(() => periodList.value.at(-1))
const money = (value?: number) => `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
const summaryItems = computed(() => [
  { label: '本期应收', value: money(latest.value?.total), note: '住宿与水电合计', tone: '' },
  { label: '已收金额', value: money(latest.value?.paid), note: '已完成缴费', tone: 'success-text' },
  { label: '未收金额', value: money(latest.value?.unpaid), note: '需要跟进', tone: 'danger-text' },
  { label: '收缴率', value: `${Number(latest.value?.collectRate || 0).toFixed(1)}%`, note: '当前账期', tone: 'accent-text' }
])
const base = { color: COLORS, tooltip: { trigger: 'axis' }, legend: { top: 0, textStyle: { color: '#5b6577' } }, grid: { left: 58, right: 28, top: 48, bottom: 34 } }
const periodOption = computed(() => ({ ...base, xAxis: { ...axis, type: 'category', data: periodList.value.map(v => v.period) }, yAxis: [{ ...axis, type: 'value' }, { ...axis, type: 'value', min: 0, max: 100, axisLabel: { formatter: '{value}%', color: '#6b7485' } }], series: [
  { name: '住宿费', type: 'bar', stack: 'amount', data: periodList.value.map(v => Number(v.rentTotal)), barMaxWidth: 26 },
  { name: '电费', type: 'bar', stack: 'amount', data: periodList.value.map(v => Number(v.elecTotal)) },
  { name: '水费', type: 'bar', stack: 'amount', data: periodList.value.map(v => Number(v.waterTotal)) },
  { name: '收缴率', type: 'line', yAxisIndex: 1, smooth: true, symbolSize: 7, data: periodList.value.map(v => Number(v.collectRate)) }
]}))
const buildingOption = computed(() => ({ ...base, grid: { left: 88, right: 28, top: 48, bottom: 34 }, xAxis: { ...axis, type: 'value' }, yAxis: { ...axis, type: 'category', data: buildingList.value.map(v => v.buildingName) }, series: [
  { name: '已缴', type: 'bar', stack: 'amount', data: buildingList.value.map(v => Number(v.paid)), barMaxWidth: 24 },
  { name: '未缴', type: 'bar', stack: 'amount', data: buildingList.value.map(v => Number(v.unpaid)) }
]}))
const usageOption = computed(() => ({ ...base, xAxis: { ...axis, type: 'category', data: usageList.value.map(v => v.period) }, yAxis: { ...axis, type: 'value' }, series: [
  { name: '电（度）', type: 'line', smooth: true, symbolSize: 7, areaStyle: { opacity: .08 }, data: usageList.value.map(v => Number(v.electricity)) },
  { name: '水（吨）', type: 'line', smooth: true, symbolSize: 7, data: usageList.value.map(v => Number(v.water)) }
]}))

async function load() {
  loading.value = true; error.value = false
  try { [periodList.value, buildingList.value, arrearsList.value, usageList.value] = await Promise.all([getPeriodSummary(), getBuildingSummary(), getArrearsRanking(10), getUsageTrend()]) }
  catch { error.value = true }
  finally { loading.value = false }
}
onMounted(load)
</script>

<style scoped>
.report-page { display: grid; gap: 16px; }.period-summary { display: grid; grid-template-columns: 1.1fr repeat(4, 1fr); overflow: hidden; border: 1px solid var(--dms-hairline); border-radius: 14px; background: var(--dms-surface); box-shadow: var(--dms-shadow-card); }.period-summary__title,.summary-item { min-width: 0; padding: 20px; border-right: 1px solid var(--dms-hairline); }.summary-item:last-child { border-right: 0; }.period-summary__title { display: flex; flex-direction: column; justify-content: center; color: white; background: linear-gradient(135deg,#0c2f63,#14569b); }.period-summary span,.summary-item small { display: block; font-size: 12px; }.period-summary__title span { color: #a9ceff; }.period-summary__title strong { margin-top: 7px; font-size: 22px; }.summary-item span,.summary-item small { color: var(--dms-ink-2); }.summary-item strong { display: block; overflow: hidden; margin: 8px 0 5px; font-size: 21px; font-variant-numeric: tabular-nums; text-overflow: ellipsis; }.chart-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }.usage-card { grid-column: 1 / -1; }.card-title { display: flex; align-items: center; justify-content: space-between; }.card-title div > * { display: block; }.card-title b { font-size: 16px; }.card-title span { margin-top: 4px; color: var(--dms-ink-2); font-size: 12px; }.chart { width: 100%; height: 310px; }.success-text { color: var(--dms-ok); }.danger-text { color: var(--dms-bad); }.accent-text { color: var(--dms-accent); }
@media (max-width: 1050px) { .period-summary { grid-template-columns: repeat(2,1fr); }.period-summary__title { grid-column: 1 / -1; }.summary-item { border-top: 1px solid var(--dms-hairline); }.chart-grid { grid-template-columns: 1fr; }.usage-card { grid-column: auto; } }
@media (max-width: 767px) { .period-summary { grid-template-columns: 1fr; }.summary-item { border-right: 0; }.chart { min-width: 560px; }.chart-grid :deep(.el-card__body) { overflow-x: auto; } }
</style>
