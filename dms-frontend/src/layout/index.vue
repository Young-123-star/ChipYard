<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '72px' : '216px'" class="aside" :class="{ collapsed }">
      <router-link class="logo" to="/dashboard" aria-label="宿舍管理系统">
        <span class="logo-mark">宿</span><span v-if="!collapsed" class="logo-word">宿舍管理系统<small>DORM MANAGEMENT</small></span>
      </router-link>
      <el-menu :default-active="route.path" :collapse="collapsed" :collapse-transition="false" router class="menu">
        <el-menu-item-group v-for="g in menuGroups" :key="g.title" :title="g.title">
          <el-menu-item v-for="item in g.items" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </el-menu-item-group>
      </el-menu>
    </el-aside>
    <el-container class="content-shell">
      <el-header class="header">
        <div class="header-context">
          <el-button text circle :aria-label="collapsed ? '展开菜单' : '收起菜单'" @click="collapsed = !collapsed">
            <el-icon size="18"><Expand v-if="collapsed" /><Fold v-else /></el-icon>
          </el-button>
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <el-dropdown @command="onCommand">
          <button class="user" type="button"><span class="avatar">{{ userInitial }}</span>{{ userName }}<el-icon><ArrowDown /></el-icon></button>
          <template #dropdown><el-dropdown-menu><el-dropdown-item command="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
        </el-dropdown>
      </el-header>
      <el-main>
        <router-view v-slot="{ Component }">
          <component :is="Component" v-if="route.name === 'dashboard'" />
          <AppPage v-else :title="String(route.meta.title || '')"><component :is="Component" /></AppPage>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, type Component } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Expand, Fold } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import AppPage from '@/components/layout/AppPage.vue'

interface MenuItem { path: string; title: string; icon: Component | undefined }
interface MenuGroup { title: string; items: MenuItem[] }

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(window.innerWidth < 1200)
const userName = computed(() => userStore.userInfo?.realName || 'admin')
const userInitial = computed(() => userName.value.slice(0, 1).toUpperCase())
// 顶栏显示当前页面名，与路由 meta.title 呼应
const pageTitle = computed(() => String(route.meta.title || '宿舍运营控制台'))

// 菜单由路由 meta（group/menuTitle/icon）驱动，不再单独维护
const menuGroups = computed<MenuGroup[]>(() => {
  const root = router.options.routes.find(r => r.path === '/')
  const groups: MenuGroup[] = []
  for (const child of root?.children ?? []) {
    const group = child.meta?.group as string | undefined
    if (!group) continue
    let g = groups.find(x => x.title === group)
    if (!g) {
      g = { title: group, items: [] }
      groups.push(g)
    }
    g.items.push({
      path: `/${child.path}`,
      title: String(child.meta?.menuTitle || child.meta?.title || ''),
      icon: child.meta?.icon as Component | undefined
    })
  }
  return groups
})

function syncSidebar() { if (window.innerWidth < 1200) collapsed.value = true }
function onCommand(cmd: string) { if (cmd === 'logout') { userStore.logout(); router.push('/login') } }
onMounted(() => window.addEventListener('resize', syncSidebar))
onBeforeUnmount(() => window.removeEventListener('resize', syncSidebar))
</script>

<style scoped>
.layout { height: 100vh; overflow: hidden; }
.aside { overflow: hidden; padding: 0 10px; border-right: 1px solid #0a2750; background: linear-gradient(180deg, var(--dms-nav-top), var(--dms-nav-bottom)); transition: width .2s ease; }
.aside.collapsed { padding: 0 8px; }
.logo { display: flex; align-items: center; gap: 9px; height: 56px; padding: 0 4px; border-bottom: 1px solid var(--dms-nav-divider); color: var(--dms-nav-text-strong); text-decoration: none; }
.logo-mark { display: grid; place-items: center; flex: 0 0 26px; width: 26px; height: 26px; border-radius: 7px; color: #fff; font-size: 12px; font-weight: 800; background: var(--dms-accent); }
.logo-word { white-space: nowrap; font-size: 14px; font-weight: 650; letter-spacing: -.01em; line-height: 1.2; }
.logo-word small { display: block; font-size: 9px; font-weight: 400; letter-spacing: .12em; color: rgba(255,255,255,.38); }
.menu { height: calc(100vh - 67px); overflow-x: hidden; overflow-y: auto; border: 0; background: transparent; --el-menu-item-height: 34px; --el-menu-text-color: var(--dms-nav-text); --el-menu-hover-bg-color: var(--dms-nav-hover); --el-menu-hover-text-color: #fff; }
.menu::-webkit-scrollbar { width: 0; }
.menu :deep(.el-menu-item-group__title) { padding: 12px 10px 4px; color: rgba(255,255,255,.34); font-size: 10px; font-weight: 600; letter-spacing: .08em; }
.collapsed .menu :deep(.el-menu-item-group__title) { height: 10px; overflow: hidden; padding: 8px 0 2px; font-size: 0; }
.menu .el-menu-item { position: relative; margin-bottom: 1px; border-radius: 7px; color: var(--dms-nav-text); font-size: 13px; }
.menu .el-menu-item:hover { color: #fff; }
.menu .el-menu-item.is-active { color: var(--dms-nav-active-text); font-weight: 600; background: var(--dms-nav-active); }
.menu .el-menu-item.is-active::before {
  content: ""; position: absolute; left: -10px; top: 7px; bottom: 7px;
  width: 3px; border-radius: 0 3px 3px 0;
  background: linear-gradient(180deg, #6f97f5, var(--dms-accent));
  box-shadow: 0 0 10px rgba(43, 92, 230, .8);
}
.menu.el-menu--collapse { width: 56px; }
.content-shell { min-width: 0; }
.header { display: flex; align-items: center; justify-content: space-between; height: 52px; padding: 0 20px; border-bottom: 1px solid var(--dms-hairline); background: rgba(255,255,255,.85); backdrop-filter: blur(10px); }
.header-context { display: flex; align-items: center; gap: 10px; color: var(--dms-ink-2); font-size: 13px; }
.header-context .page-title { color: var(--dms-ink); font-size: 14px; font-weight: 600; }
.user { display: flex; align-items: center; gap: 8px; padding: 4px 10px 4px 4px; border: 0; border-radius: 99px; color: var(--dms-ink); background: transparent; cursor: pointer; transition: background var(--dms-motion-fast); }
.user:hover { background: var(--dms-hover); }
.avatar { display: grid; place-items: center; width: 24px; height: 24px; border-radius: 50%; color: var(--dms-accent-ink); font-size: 11px; font-weight: 700; background: var(--dms-accent-soft); }
.el-main { min-width: 0; padding: 20px 24px; }
.el-main > :deep(*) { max-width: 1600px; margin-right: auto; margin-left: auto; }
@media (max-width: 1199px) { .el-main { padding: 16px; } .header { padding: 0 16px; } }
@media (max-width: 767px) { .header-context span { display: none; } .user { font-size: 0; } }
</style>
