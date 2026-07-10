import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('@/views/login/index.vue') },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/buildings',
    children: [
      { path: 'buildings', name: 'buildings', meta: { title: '\u697c\u680b\u7ba1\u7406' }, component: () => import('@/views/resource/building.vue') },
      { path: 'floors', name: 'floors', meta: { title: '\u697c\u5c42\u7ba1\u7406' }, component: () => import('@/views/resource/floor.vue') },
      { path: 'rooms', name: 'rooms', meta: { title: '\u623f\u95f4\u7ba1\u7406' }, component: () => import('@/views/resource/room.vue') },
      { path: 'beds', name: 'beds', meta: { title: '\u5e8a\u4f4d\u7ba1\u7406' }, component: () => import('@/views/resource/bed.vue') },
      { path: 'board', name: 'board', meta: { title: '\u623f\u95f4\u72b6\u6001\u770b\u677f' }, component: () => import('@/views/resource/board.vue') },
      { path: 'residents', name: 'residents', meta: { title: '\u5c45\u4f4f\u4eba\u7ba1\u7406' }, component: () => import('@/views/resident/index.vue') },
      { path: 'intakes', name: 'intakes', meta: { title: '\u5165\u4f4f\u610f\u5411\u5355' }, component: () => import('@/views/checkin/intake.vue') },
      { path: 'records', name: 'records', meta: { title: '\u5165\u4f4f\u6863\u6848' }, component: () => import('@/views/checkin/record.vue') },
      { path: 'checkout-orders', name: 'checkout-orders', meta: { title: '\u9000\u5bbf\u5355' }, component: () => import('@/views/checkout/order.vue') },
      { path: 'fee-standards', name: 'fee-standards', meta: { title: '\u6536\u8d39\u6807\u51c6' }, component: () => import('@/views/fee/standard.vue') },
      { path: 'fee-bills', name: 'fee-bills', meta: { title: '\u4f4f\u5bbf\u8d39\u8d26\u5355' }, component: () => import('@/views/fee/bill.vue') },
      { path: 'fee-meter', name: 'fee-meter', meta: { title: '\u6284\u8868/\u6c34\u7535' }, component: () => import('@/views/fee/meter.vue') },
      { path: 'report', name: 'report', meta: { title: '\u7edf\u8ba1\u62a5\u8868' }, component: () => import('@/views/report/index.vue') },
      { path: 'repair-orders', name: 'repair-orders', meta: { title: '\u7ef4\u4fee\u5de5\u5355' }, component: () => import('@/views/repair/index.vue') },
      { path: 'inspections', name: 'inspections', meta: { title: '\u5de1\u68c0\u7ba1\u7406' }, component: () => import('@/views/inspection/index.vue') },
      { path: 'dicts', name: 'dicts', meta: { title: '\u5b57\u5178\u7ba1\u7406' }, component: () => import('@/views/system/dict.vue') },
      { path: 'data-import', name: 'data-import', meta: { title: '\u6570\u636e\u521d\u59cb\u5316' }, component: () => import('@/views/import/index.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.path === '/login') {
    next()
  } else if (!userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
