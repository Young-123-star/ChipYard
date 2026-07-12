<template>
  <AppPage title="运营总览" description="集中查看房态、费用与服务待办，优先处理今天需要推进的事项。" eyebrow="Operations cockpit">
    <template #actions>
      <el-button @click="load">刷新数据</el-button>
      <el-button type="primary" @click="router.push('/board')">查看房态</el-button>
    </template>

    <el-alert v-if="error" type="error" :closable="false" show-icon>
      <template #title>数据加载失败，请检查服务后重试</template>
      <el-button link type="primary" @click="load">重新加载</el-button>
    </el-alert>

    <section v-loading="loading" class="hero">
      <div class="hero__copy">
        <span>今日运营</span>
        <strong>{{ occupancyRate }}<small>%</small></strong>
        <p>当前入住率 · 尚有 {{ freeBeds }} 个空闲床位</p>
        <div class="hero__paths" aria-hidden="true"><i /><i /><i /></div>
      </div>
      <button class="room-grid" type="button" aria-label="打开房态看板" @click="router.push('/board')">
        <span v-for="room in previewRooms" :key="room.id" :class="roomClass(room)" :title="`${room.roomNumber}：空闲 ${room.bedCount - room.occupiedBeds} 床`">
          {{ room.roomNumber }}
        </span>
        <em v-if="!previewRooms.length">暂无房态数据</em>
      </button>
    </section>

    <section class="metrics" aria-label="关键运营指标">
      <button v-for="item in metrics" :key="item.label" type="button" class="metric" @click="router.push(item.to)">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.note }}</small>
      </button>
    </section>

    <section class="dashboard-grid">
      <el-card shadow="never" class="panel">
        <template #header><div class="panel__head"><div><b>待办事项</b><span>按业务流程快速进入处理</span></div><el-tag effect="light">{{ taskTotal }} 项</el-tag></div></template>
        <el-empty v-if="!taskTotal" description="当前没有待处理事项" :image-size="72" />
        <button v-for="task in tasks" v-else :key="task.label" type="button" class="task" @click="router.push(task.to)">
          <i :class="task.tone" />
          <span><b>{{ task.label }}</b><small>{{ task.description }}</small></span>
          <strong>{{ task.value }}</strong>
        </button>
      </el-card>

      <el-card shadow="never" class="panel">
        <template #header><div class="panel__head"><div><b>快捷入口</b><span>常用运营动作</span></div></div></template>
        <div class="quick-grid">
          <button v-for="link in quickLinks" :key="link.label" type="button" @click="router.push(link.to)">
            <span>{{ link.code }}</span><b>{{ link.label }}</b><small>{{ link.note }}</small>
          </button>
        </div>
      </el-card>
    </section>
  </AppPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppPage from '@/components/layout/AppPage.vue'
import { getRoomBoard } from '@/api/room'
import { pageIntakes } from '@/api/checkin'
import { pageCheckoutOrders } from '@/api/checkout'
import { getPeriodSummary } from '@/api/report'
import { pageRepairOrders } from '@/api/repair'
import { pageInspectionTasks } from '@/api/inspection'
import type { PeriodSummary, RoomBoard } from '@/api/types'

const router = useRouter()
const loading = ref(false)
const error = ref(false)
const rooms = ref<RoomBoard[]>([])
const latestPeriod = ref<PeriodSummary>()
const pendingIntakes = ref(0)
const pendingCheckouts = ref(0)
const pendingRepairs = ref(0)
const pendingInspections = ref(0)
const rectifyingInspections = ref(0)

const totalBeds = computed(() => rooms.value.reduce((sum, room) => sum + room.bedCount, 0))
const occupiedBeds = computed(() => rooms.value.reduce((sum, room) => sum + room.occupiedBeds, 0))
const freeBeds = computed(() => Math.max(0, totalBeds.value - occupiedBeds.value))
const occupancyRate = computed(() => totalBeds.value ? Math.round(occupiedBeds.value / totalBeds.value * 100) : 0)
const previewRooms = computed(() => rooms.value.slice(0, 30))
const servicePending = computed(() => pendingRepairs.value + pendingInspections.value + rectifyingInspections.value)
const currency = (value = 0) => `¥${Number(value).toLocaleString('zh-CN', { maximumFractionDigits: 0 })}`

const metrics = computed(() => [
  { label: '入住率', value: `${occupancyRate.value}%`, note: `${occupiedBeds.value}/${totalBeds.value || 0} 床`, to: '/board' },
  { label: '空闲床位', value: freeBeds.value, note: '可供分配', to: '/beds' },
  { label: '待入住', value: pendingIntakes.value, note: '等待分配床位', to: '/intakes' },
  { label: '待退宿', value: pendingCheckouts.value, note: '等待确认', to: '/checkout-orders' },
  { label: '账期欠费', value: currency(latestPeriod.value?.unpaid), note: latestPeriod.value?.period || '暂无账期', to: '/report' },
  { label: '服务待办', value: servicePending.value, note: '维修与巡检', to: '/repair-orders' }
])

const tasks = computed(() => [
  { label: '入住分配', description: '确认人员与床位', value: pendingIntakes.value, to: '/intakes', tone: 'blue' },
  { label: '退宿确认', description: '释放床位并关闭档案', value: pendingCheckouts.value, to: '/checkout-orders', tone: 'orange' },
  { label: '维修受理', description: '处理待受理工单', value: pendingRepairs.value, to: '/repair-orders', tone: 'red' },
  { label: '巡检执行', description: '待执行及待整改任务', value: pendingInspections.value + rectifyingInspections.value, to: '/inspections', tone: 'green' }
].filter(item => item.value > 0))
const taskTotal = computed(() => tasks.value.reduce((sum, item) => sum + item.value, 0))
const quickLinks = [
  { code: 'IN', label: '发起入住', note: '创建入住意向', to: '/intakes' },
  { code: 'RM', label: '房间管理', note: '维护房间床位', to: '/rooms' },
  { code: 'FI', label: '账单管理', note: '生成与收款', to: '/fee-bills' },
  { code: 'SV', label: '维修工单', note: '登记服务需求', to: '/repair-orders' }
]

function roomClass(room: RoomBoard) {
  if (room.status !== 1) return 'room-grid__off'
  if (!room.occupiedBeds) return 'room-grid__free'
  return room.occupiedBeds >= room.bedCount ? 'room-grid__full' : 'room-grid__partial'
}

async function load() {
  loading.value = true
  error.value = false
  try {
    const [roomData, intakes, checkouts, periods, repairs, inspections, rectifications] = await Promise.all([
      getRoomBoard({}), pageIntakes({ status: 1, page: 1, size: 1 }), pageCheckoutOrders({ status: 1, page: 1, size: 1 }),
      getPeriodSummary(), pageRepairOrders({ status: 1, page: 1, size: 1 }),
      pageInspectionTasks({ status: 1, page: 1, size: 1 }), pageInspectionTasks({ status: 3, page: 1, size: 1 })
    ])
    rooms.value = roomData
    pendingIntakes.value = intakes.total
    pendingCheckouts.value = checkouts.total
    latestPeriod.value = periods.at(-1)
    pendingRepairs.value = repairs.total
    pendingInspections.value = inspections.total
    rectifyingInspections.value = rectifications.total
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.hero { position: relative; display: grid; grid-template-columns: minmax(280px, .8fr) minmax(420px, 1.2fr); min-height: 242px; overflow: hidden; border-radius: 20px; color: white; background: linear-gradient(125deg, #092853, #0c3b78 68%, #10549c); box-shadow: 0 18px 45px rgba(8, 42, 88, .18); }
.hero__copy { position: relative; z-index: 1; padding: 32px; }
.hero__copy > span { color: #9fc5ff; font-size: 12px; font-weight: 700; letter-spacing: .1em; }
.hero__copy > strong { display: block; margin-top: 14px; font-size: 56px; line-height: 1; font-variant-numeric: tabular-nums; letter-spacing: -.06em; }
.hero__copy > strong small { margin-left: 3px; font-size: 22px; }
.hero__copy p { margin: 13px 0 0; color: rgba(255,255,255,.7); font-size: 14px; }
.hero__paths i { position: absolute; display: block; width: 150px; height: 42px; border-top: 1px solid rgba(117,177,255,.28); border-right: 1px solid rgba(117,177,255,.28); border-radius: 0 14px 0 0; }
.hero__paths i:nth-child(1) { left: -18px; bottom: 42px; }.hero__paths i:nth-child(2) { left: 100px; bottom: 14px; width: 220px; }.hero__paths i:nth-child(3) { left: 250px; top: 30px; width: 90px; transform: rotate(90deg); }
.room-grid { align-self: center; display: grid; grid-template-columns: repeat(6, minmax(48px, 1fr)); gap: 8px; margin: 26px 30px 26px 0; padding: 0; border: 0; color: white; background: none; cursor: pointer; }
.room-grid span { display: grid; place-items: center; min-height: 31px; border: 1px solid rgba(255,255,255,.18); border-radius: 6px; font-size: 10px; font-variant-numeric: tabular-nums; }
.room-grid__free { background: rgba(54, 202, 126, .32); }.room-grid__partial { background: rgba(255, 187, 64, .38); }.room-grid__full { background: rgba(255,255,255,.14); }.room-grid__off { opacity: .42; background: rgba(255,255,255,.05); }
.room-grid em { grid-column: 1 / -1; color: rgba(255,255,255,.65); font-style: normal; }
.metrics { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; }
.metric { min-width: 0; padding: 18px; border: 1px solid var(--dms-hairline); border-radius: 14px; text-align: left; color: inherit; background: var(--dms-surface); box-shadow: var(--dms-shadow-card); cursor: pointer; transition: transform .15s, border-color .15s; }
.metric:hover { transform: translateY(-2px); border-color: #b7cdf1; }.metric span,.metric small { display: block; color: var(--dms-ink-2); }.metric span { font-size: 12px; }.metric strong { display: block; overflow: hidden; margin: 10px 0 6px; font-size: 24px; font-variant-numeric: tabular-nums; text-overflow: ellipsis; }.metric small { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.dashboard-grid { display: grid; grid-template-columns: 1.15fr .85fr; gap: 16px; }.panel :deep(.el-card__header) { padding: 18px 20px; }.panel :deep(.el-card__body) { padding: 8px 20px 20px; }.panel__head { display: flex; align-items: center; justify-content: space-between; }.panel__head div > * { display: block; }.panel__head b { font-size: 16px; }.panel__head span { margin-top: 4px; color: var(--dms-ink-2); font-size: 12px; }
.task { display: grid; grid-template-columns: 8px 1fr auto; align-items: center; gap: 14px; width: 100%; padding: 13px 4px; border: 0; border-bottom: 1px solid var(--dms-hairline); color: inherit; text-align: left; background: none; cursor: pointer; }.task:last-child { border-bottom: 0; }.task i { width: 8px; height: 8px; border-radius: 50%; }.task i.blue { background: var(--dms-accent); }.task i.orange { background: var(--dms-warn); }.task i.red { background: var(--dms-bad); }.task i.green { background: var(--dms-ok); }.task span > * { display: block; }.task span small { margin-top: 3px; color: var(--dms-ink-2); }.task > strong { font-size: 20px; font-variant-numeric: tabular-nums; }
.quick-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }.quick-grid button { display: grid; grid-template-columns: 36px 1fr; grid-template-rows: auto auto; column-gap: 10px; padding: 15px; border: 1px solid var(--dms-hairline); border-radius: 12px; color: inherit; text-align: left; background: #fbfcfe; cursor: pointer; }.quick-grid button:hover { border-color: #b7cdf1; background: var(--dms-accent-soft); }.quick-grid span { grid-row: 1 / 3; display: grid; place-items: center; width: 36px; height: 36px; border-radius: 9px; color: var(--dms-accent); font-size: 11px; font-weight: 800; background: var(--dms-accent-soft); }.quick-grid small { margin-top: 3px; color: var(--dms-ink-2); }
@media (max-width: 1199px) { .metrics { grid-template-columns: repeat(3, 1fr); }.hero { grid-template-columns: .75fr 1.25fr; }.room-grid { grid-template-columns: repeat(5, minmax(42px,1fr)); } }
@media (max-width: 900px) { .hero { grid-template-columns: 1fr; }.room-grid { margin: 0 24px 24px; }.dashboard-grid { grid-template-columns: 1fr; } }
@media (max-width: 767px) { .metrics { grid-template-columns: repeat(2, 1fr); }.room-grid { grid-template-columns: repeat(4, 1fr); }.hero__copy { padding: 24px; }.hero__copy > strong { font-size: 46px; }.quick-grid { grid-template-columns: 1fr; } }
</style>
