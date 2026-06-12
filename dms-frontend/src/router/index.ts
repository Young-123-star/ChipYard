import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('@/views/login/index.vue') },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/buildings',
    children: [
      { path: 'buildings', name: 'buildings', meta: { title: '楼栋管理' }, component: () => import('@/views/resource/building.vue') },
      { path: 'floors', name: 'floors', meta: { title: '楼层管理' }, component: () => import('@/views/resource/floor.vue') },
      { path: 'rooms', name: 'rooms', meta: { title: '房间管理' }, component: () => import('@/views/resource/room.vue') },
      { path: 'beds', name: 'beds', meta: { title: '床位管理' }, component: () => import('@/views/resource/bed.vue') },
      { path: 'board', name: 'board', meta: { title: '房间状态看板' }, component: () => import('@/views/resource/board.vue') }
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
