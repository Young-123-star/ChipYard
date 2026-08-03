import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import {
  Bell, CircleCheck, DataBoard, DocumentAdd, Files, Grid, House, Money,
  Odometer, OfficeBuilding, Operation, PriceTag, Setting, SwitchButton, Tickets, Tools,
  TrendCharts, UploadFilled, User
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

// meta.group 驱动侧栏菜单分组，meta.icon 为菜单图标，meta.menuTitle 为菜单显示名（缺省用 title）
const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('@/views/login/index.vue') },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'dashboard', meta: { title: '运营总览', menuTitle: '工作台', group: '运营总览', icon: DataBoard }, component: () => import('@/views/dashboard/index.vue') },
      { path: 'board', name: 'board', meta: { title: '房间状态看板', menuTitle: '房态看板', group: '运营总览', icon: Grid }, component: () => import('@/views/resource/board.vue') },
      { path: 'report', name: 'report', meta: { title: '统计报表', group: '运营总览', icon: TrendCharts }, component: () => import('@/views/report/index.vue') },
      { path: 'buildings', name: 'buildings', meta: { title: '楼栋管理', group: '房源管理', icon: OfficeBuilding }, component: () => import('@/views/resource/building.vue') },
      { path: 'floors', name: 'floors', meta: { title: '楼层管理', group: '房源管理', icon: Files }, component: () => import('@/views/resource/floor.vue') },
      { path: 'rooms', name: 'rooms', meta: { title: '房间管理', group: '房源管理', icon: House }, component: () => import('@/views/resource/room.vue') },
      { path: 'beds', name: 'beds', meta: { title: '床位管理', group: '房源管理', icon: Bell }, component: () => import('@/views/resource/bed.vue') },
      { path: 'residents', name: 'residents', meta: { title: '居住人管理', group: '入退住', icon: User }, component: () => import('@/views/resident/index.vue') },
      { path: 'intakes', name: 'intakes', meta: { title: '入住意向单', group: '入退住', icon: DocumentAdd }, component: () => import('@/views/checkin/intake.vue') },
      { path: 'records', name: 'records', meta: { title: '入住档案', group: '入退住', icon: Tickets }, component: () => import('@/views/checkin/record.vue') },
      { path: 'checkout-orders', name: 'checkout-orders', meta: { title: '退宿单', group: '入退住', icon: SwitchButton }, component: () => import('@/views/checkout/order.vue') },
      { path: 'fee-standards', name: 'fee-standards', meta: { title: '收费标准', group: '费用管理', icon: PriceTag }, component: () => import('@/views/fee/standard.vue') },
      { path: 'fee-bills', name: 'fee-bills', meta: { title: '住宿费账单', group: '费用管理', icon: Money }, component: () => import('@/views/fee/bill.vue') },
      { path: 'fee-meter', name: 'fee-meter', meta: { title: '抄表/水电', group: '费用管理', icon: Odometer }, component: () => import('@/views/fee/meter.vue') },
      { path: 'fee-rules', name: 'fee-rules', meta: { title: '水电规则', group: '费用管理', icon: Operation }, component: () => import('@/views/fee/rules.vue') },
      { path: 'repair-orders', name: 'repair-orders', meta: { title: '维修工单', group: '服务运营', icon: Tools }, component: () => import('@/views/repair/index.vue') },
      { path: 'inspections', name: 'inspections', meta: { title: '巡检管理', group: '服务运营', icon: CircleCheck }, component: () => import('@/views/inspection/index.vue') },
      { path: 'dicts', name: 'dicts', meta: { title: '字典管理', group: '系统管理', icon: Setting }, component: () => import('@/views/system/dict.vue') },
      { path: 'data-import', name: 'data-import', meta: { title: '数据初始化', group: '系统管理', icon: UploadFilled }, component: () => import('@/views/import/index.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} · 宿舍管理` : '宿舍管理'
  const userStore = useUserStore()
  if (to.path === '/login') {
    if (userStore.token) {
      next('/')
    } else {
      next()
    }
    return
  }
  if (!userStore.token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  // 刷新页面后内存中的用户信息丢失，这里按 token 拉取一次
  if (!userStore.userInfo) {
    try {
      await userStore.fetchCurrentUser()
    } catch {
      userStore.logout()
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }
  next()
})

export default router
