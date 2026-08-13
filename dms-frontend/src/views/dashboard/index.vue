<template>
  <AppPage title="运营总览" description="集中查看房态、费用与服务待办，优先处理今天需要推进的事项。">
    <template #actions>
      <el-button @click="load">刷新数据</el-button>
      <el-button type="primary" @click="router.push('/board')">查看房态</el-button>
    </template>

    <el-alert v-if="error" type="error" :closable="false" show-icon>
      <template #title>数据加载失败，请检查服务后重试</template>
      <el-button link type="primary" @click="load">重新加载</el-button>
    </el-alert>

    <section class="metrics" aria-label="关键运营指标">
      <button v-for="item in metrics" :key="item.label" type="button" class="metric" @click="router.push(item.to)">
        <span class="k"><i class="dot" :class="`tone-${item.tone}`"></i>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.note }}</small>
      </button>
    </section>

    <section v-loading="loading" class="hero">
      <div class="hero__copy">
        <span>今日运营</span>
        <strong>{{ occupancyRate }}<small>%</small></strong>
        <p>当前入住率 · 尚有 <b>{{ freeBeds }}</b> 个空闲床位</p>
      </div>
      <button class="room-grid" type="button" aria-label="打开房态看板" @click="router.push('/board')">
        <span v-for="room in previewRooms" :key="room.id" :class="roomClass(room)" :title="`${room.roomNumber}：空闲 ${room.bedCount - room.occupiedBeds} 床`">
          {{ room.roomNumber }}
        </span>
        <em v-if="!previewRooms.length">暂无房态数据</em>
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
        <div class="quick-list">
          <button v-for="link in quickLinks" :key="link.label" type="button" @click="router.push(link.to)">
            <span><b>{{ link.label }}</b><small>{{ link.note }}</small></span>
            <el-icon class="go"><ArrowRight /></el-icon>
          </button>
        </div>
      </el-card>
    </section>
  </AppPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter, type RouteLocationRaw } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import AppPage from '@/components/layout/AppPage.vue'
import { getRoomBoard } from '@/api/room'
import { pageIntakes } from '@/api/checkin'
import { pageCheckoutOrders } from '@/api/checkout'
import { getPeriodSummary } from '@/api/report'
import { pageRepairOrders } from '@/api/repair'
import { pageInspectionTasks } from '@/api/inspection'
import type { PeriodSummary, RoomBoard } from '@/api/types'

const router = useRouter()
const PREVIEW_ROOM_LIMIT = 30
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
const previewRooms = computed(() => rooms.value.slice(0, PREVIEW_ROOM_LIMIT))
const servicePending = computed(() => pendingRepairs.value + pendingInspections.value + rectifyingInspections.value)
const currency = (value = 0) => `¥${Number(value).toLocaleString('zh-CN', { maximumFractionDigits: 0 })}`

const metrics = computed<{ label: string; value: string | number; note: string; tone: string; to: RouteLocationRaw }[]>(() => [
  { label: '入住率', value: `${occupancyRate.value}%`, note: `${occupiedBeds.value}/${totalBeds.value || 0} 床`, tone: 'accent', to: '/board' },
  { label: '空闲床位', value: freeBeds.value, note: '可供分配', tone: 'ok', to: '/board' },
  { label: '待入住', value: pendingIntakes.value, note: '等待分配床位', tone: 'hold', to: { path: '/intakes', query: { status: 1 } } },
  { label: '待退宿', value: pendingCheckouts.value, note: '等待确认', tone: 'warn', to: { path: '/checkout-orders', query: { status: 1 } } },
  { label: '账期欠费', value: currency(latestPeriod.value?.unpaid), note: latestPeriod.value?.period || '暂无账期', tone: 'bad', to: { path: '/fee-bills', query: { status: 1 } } },
  { label: '服务待办', value: servicePending.value, note: '维修与巡检', tone: 'accent', to: { path: '/repair-orders', query: { status: 1 } } }
])

const tasks = computed(() => [
  { label: '入住分配', description: '确认人员与床位', value: pendingIntakes.value, to: '/intakes', tone: 'blue' },
  { label: '退宿确认', description: '释放床位并关闭档案', value: pendingCheckouts.value, to: '/checkout-orders', tone: 'orange' },
  { label: '维修受理', description: '处理待受理工单', value: pendingRepairs.value, to: '/repair-orders', tone: 'red' },
  { label: '巡检执行', description: '待执行及待整改任务', value: pendingInspections.value + rectifyingInspections.value, to: '/inspections', tone: 'green' }
].filter(item => item.value > 0))
const taskTotal = computed(() => tasks.value.reduce((sum, item) => sum + item.value, 0))
const quickLinks = [
  { label: '发起入住', note: '创建入住意向', to: '/intakes' },
  { label: '房间管理', note: '维护房间床位', to: '/rooms' },
  { label: '账单管理', note: '生成与收款', to: '/fee-bills' },
  { label: '维修工单', note: '登记服务需求', to: '/repair-orders' }
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
    const [roomData, intakes, checkouts, periods, repairs, inspections, rectifications] = await Promise.allSettled([
      getRoomBoard({}), pageIntakes({ status: 1, page: 1, size: 1 }), pageCheckoutOrders({ status: 1, page: 1, size: 1 }),
      getPeriodSummary(), pageRepairOrders({ status: 1, page: 1, size: 1 }),
      pageInspectionTasks({ status: 1, page: 1, size: 1 }), pageInspectionTasks({ status: 3, page: 1, size: 1 })
    ])
    // 房态为核心数据，失败时整体提示；其余模块独立降级
    if (roomData.status === 'rejected') {
      error.value = true
      return
    }
    rooms.value = roomData.value
    if (intakes.status === 'fulfilled') pendingIntakes.value = intakes.value.total
    if (checkouts.status === 'fulfilled') pendingCheckouts.value = checkouts.value.total
    if (periods.status === 'fulfilled' && periods.value.length) {
      latestPeriod.value = periods.value.reduce((latest, item) => (item.period > latest.period ? item : latest))
    }
    if (repairs.status === 'fulfilled') pendingRepairs.value = repairs.value.total
    if (inspections.status === 'fulfilled') pendingInspections.value = inspections.value.total
    if (rectifications.status === 'fulfilled') rectifyingInspections.value = rectifications.value.total
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.metrics { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; }
.metric { min-width: 0; padding: 14px 16px; border: 1px solid var(--dms-hairline); border-radius: var(--dms-radius-card); text-align: left; color: inherit; background: var(--dms-surface); box-shadow: none; cursor: pointer; transition: border-color var(--dms-motion-fast), background var(--dms-motion-fast); }
.metric:hover { border-color: var(--el-color-primary-light-7); background: var(--dms-hover); }
.metric .k { display: flex; align-items: center; gap: 6px; color: var(--dms-ink-2); font-size: 12px; }
.metric .dot { width: 7px; height: 7px; border-radius: 50%; flex: 0 0 7px; }
.tone-accent { background: var(--dms-accent); }
.tone-ok { background: var(--dms-ok); }
.tone-warn { background: var(--dms-warn); }
.tone-bad { background: var(--dms-bad); }
.tone-hold { background: var(--dms-hold); }
.metric strong { display: block; overflow: hidden; margin: 6px 0 3px; color: var(--dms-ink); font-size: 22px; font-variant-numeric: tabular-nums; text-overflow: ellipsis; }
.metric small { display: block; color: var(--dms-ink-2); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.hero { display: grid; grid-template-columns: minmax(220px, .6fr) minmax(420px, 1.4fr); align-items: center; gap: 20px; padding: 20px 24px; border: 1px solid var(--dms-hairline); border-radius: var(--dms-radius-card); color: var(--dms-ink); background: linear-gradient(120deg, var(--dms-accent-soft) 0%, var(--dms-surface) 55%); }
.hero__copy > span { color: var(--dms-ink-3); font-size: 12px; font-weight: 700; letter-spacing: .1em; }
.hero__copy > strong { display: block; margin-top: 10px; color: var(--dms-ink); font-size: 44px; line-height: 1; font-variant-numeric: tabular-nums; letter-spacing: -.04em; }
.hero__copy > strong small { margin-left: 3px; color: var(--dms-ink-3); font-size: 18px; }
.hero__copy p { margin: 10px 0 0; color: var(--dms-ink-2); font-size: 13px; }
.hero__copy p b { color: var(--dms-accent); font-weight: 600; font-variant-numeric: tabular-nums; }
.room-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(76px, 1fr)); gap: 6px; margin: 0; padding: 0; border: 0; color: var(--dms-ink-2); background: none; cursor: pointer; }
.room-grid span { display: block; min-height: 26px; line-height: 26px; padding: 0 6px; border: 1px solid var(--dms-hairline); border-left: 3px solid var(--dms-hairline); border-radius: 6px; font-size: 10.5px; text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-variant-numeric: tabular-nums; background: var(--dms-surface); transition: background var(--dms-motion-fast); }
.room-grid span:hover { background: var(--dms-hover); }
.room-grid__free { border-left-color: var(--dms-ok); }
.room-grid__partial { border-left-color: var(--dms-warn); }
.room-grid__full { border-left-color: var(--dms-accent); }
.room-grid__off { border-left-color: var(--dms-ink-3); opacity: .45; }
.room-grid em { grid-column: 1 / -1; color: var(--dms-ink-3); font-style: normal; }
.dashboard-grid { display: grid; grid-template-columns: 1.15fr .85fr; gap: 16px; }
.panel :deep(.el-card__header) { padding: 18px 20px; }
.panel :deep(.el-card__body) { padding: 8px 20px 20px; }
.panel__head { display: flex; align-items: center; justify-content: space-between; }
.panel__head div > * { display: block; }
.panel__head b { font-size: 16px; }
.panel__head span { margin-top: 4px; color: var(--dms-ink-2); font-size: 12px; }
.task { display: grid; grid-template-columns: 8px 1fr auto; align-items: center; gap: 14px; width: 100%; padding: 13px 4px; border: 0; border-bottom: 1px solid var(--dms-hairline); color: inherit; text-align: left; background: none; cursor: pointer; }
.task:last-child { border-bottom: 0; }
.task i { width: 8px; height: 8px; border-radius: 50%; }
.task i.blue { background: var(--dms-accent); }
.task i.orange { background: var(--dms-warn); }
.task i.red { background: var(--dms-bad); }
.task i.green { background: var(--dms-ok); }
.task span > * { display: block; }
.task span small { margin-top: 3px; color: var(--dms-ink-2); }
.task > strong { font-size: 20px; font-variant-numeric: tabular-nums; }
.quick-list { display: flex; flex-direction: column; }
.quick-list button { display: flex; align-items: center; justify-content: space-between; gap: 10px; width: 100%; padding: 13px 4px; border: 0; border-bottom: 1px solid var(--dms-hairline); color: inherit; text-align: left; background: none; cursor: pointer; }
.quick-list button:last-child { border-bottom: 0; }
.quick-list button span > * { display: block; }
.quick-list button small { margin-top: 3px; color: var(--dms-ink-2); }
.quick-list .go { color: var(--dms-ink-3); transition: color var(--dms-motion-fast), transform var(--dms-motion-fast); }
.quick-list button:hover b { color: var(--dms-accent-ink); }
.quick-list button:hover .go { color: var(--dms-accent); transform: translateX(2px); }
@media (max-width: 1199px) { .metrics { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) { .hero { grid-template-columns: 1fr; } .dashboard-grid { grid-template-columns: 1fr; } }
@media (max-width: 767px) { .metrics { grid-template-columns: repeat(2, 1fr); } .hero { padding: 18px; } .hero__copy > strong { font-size: 36px; } .quick-grid { grid-template-columns: 1fr; } }
</style>
