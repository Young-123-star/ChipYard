<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '72px' : '204px'" class="aside" :class="{ collapsed }">
      <router-link class="logo" to="/dashboard" aria-label="&#23487;&#33293;&#31649;&#29702;&#31995;&#32479;">
        <span class="logo-mark">CM</span><span v-if="!collapsed" class="logo-word">ChipMore <em>Dorm</em></span>
      </router-link>
      <el-menu :default-active="route.path" :collapse="collapsed" :collapse-transition="false" router class="menu">
        <el-menu-item-group title="&#36816;&#33829;&#24635;&#35272;">
          <el-menu-item index="/dashboard"><el-icon><DataBoard /></el-icon><template #title>&#24037;&#20316;&#21488;</template></el-menu-item>
          <el-menu-item index="/board"><el-icon><Grid /></el-icon><template #title>&#25151;&#24577;&#30475;&#26495;</template></el-menu-item>
          <el-menu-item index="/report"><el-icon><TrendCharts /></el-icon><template #title>&#32479;&#35745;&#25253;&#34920;</template></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="&#25151;&#28304;&#31649;&#29702;">
          <el-menu-item index="/buildings"><el-icon><OfficeBuilding /></el-icon><template #title>&#27004;&#26635;&#31649;&#29702;</template></el-menu-item>
          <el-menu-item index="/floors"><el-icon><Files /></el-icon><template #title>&#27004;&#23618;&#31649;&#29702;</template></el-menu-item>
          <el-menu-item index="/rooms"><el-icon><House /></el-icon><template #title>&#25151;&#38388;&#31649;&#29702;</template></el-menu-item>
          <el-menu-item index="/beds"><el-icon><Bell /></el-icon><template #title>&#24202;&#20301;&#31649;&#29702;</template></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="&#20837;&#36864;&#20303;">
          <el-menu-item index="/residents"><el-icon><User /></el-icon><template #title>&#23621;&#20303;&#20154;&#31649;&#29702;</template></el-menu-item>
          <el-menu-item index="/intakes"><el-icon><DocumentAdd /></el-icon><template #title>&#20837;&#20303;&#24847;&#21521;&#21333;</template></el-menu-item>
          <el-menu-item index="/records"><el-icon><Tickets /></el-icon><template #title>&#20837;&#20303;&#26723;&#26696;</template></el-menu-item>
          <el-menu-item index="/checkout-orders"><el-icon><SwitchButton /></el-icon><template #title>&#36864;&#23487;&#21333;</template></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="&#36153;&#29992;&#31649;&#29702;">
          <el-menu-item index="/fee-standards"><el-icon><PriceTag /></el-icon><template #title>&#25910;&#36153;&#26631;&#20934;</template></el-menu-item>
          <el-menu-item index="/fee-bills"><el-icon><Money /></el-icon><template #title>&#20303;&#23487;&#36153;&#36134;&#21333;</template></el-menu-item>
          <el-menu-item index="/fee-meter"><el-icon><Odometer /></el-icon><template #title>&#25220;&#34920;/&#27700;&#30005;</template></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="&#26381;&#21153;&#36816;&#33829;">
          <el-menu-item index="/repair-orders"><el-icon><Tools /></el-icon><template #title>&#32500;&#20462;&#24037;&#21333;</template></el-menu-item>
          <el-menu-item index="/inspections"><el-icon><CircleCheck /></el-icon><template #title>&#24033;&#26816;&#31649;&#29702;</template></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="&#31995;&#32479;&#31649;&#29702;">
          <el-menu-item index="/dicts"><el-icon><Setting /></el-icon><template #title>&#23383;&#20856;&#31649;&#29702;</template></el-menu-item>
          <el-menu-item index="/data-import"><el-icon><UploadFilled /></el-icon><template #title>&#25968;&#25454;&#21021;&#22987;&#21270;</template></el-menu-item>
        </el-menu-item-group>
      </el-menu>
    </el-aside>
    <el-container class="content-shell">
      <el-header class="header">
        <div class="header-context">
          <el-button text circle :aria-label="collapsed ? '&#23637;&#24320;&#33756;&#21333;' : '&#25910;&#36215;&#33756;&#21333;'" @click="collapsed = !collapsed">
            <el-icon size="18"><Expand v-if="collapsed" /><Fold v-else /></el-icon>
          </el-button>
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <el-dropdown @command="onCommand">
          <button class="user" type="button"><span class="avatar">{{ userInitial }}</span>{{ userName }}<el-icon><ArrowDown /></el-icon></button>
          <template #dropdown><el-dropdown-menu><el-dropdown-item command="logout">&#36864;&#20986;&#30331;&#24405;</el-dropdown-item></el-dropdown-menu></template>
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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Bell, CircleCheck, DataBoard, DocumentAdd, Expand, Files, Fold, Grid, House, Money, Odometer, OfficeBuilding, PriceTag, Setting, SwitchButton, Tickets, Tools, TrendCharts, UploadFilled, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import AppPage from '@/components/layout/AppPage.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(window.innerWidth < 1200)
const userName = computed(() => userStore.userInfo?.realName || 'admin')
const userInitial = computed(() => userName.value.slice(0, 1).toUpperCase())
// 顶栏显示当前页面名，与路由 meta.title 呼应
const pageTitle = computed(() => String(route.meta.title || '宿舍运营控制台'))
function syncSidebar() { if (window.innerWidth < 1200) collapsed.value = true }
function onCommand(cmd: string) { if (cmd === 'logout') { userStore.logout(); router.push('/login') } }
onMounted(() => window.addEventListener('resize', syncSidebar))
onBeforeUnmount(() => window.removeEventListener('resize', syncSidebar))
</script>

<style scoped>
.layout { height: 100vh; overflow: hidden; }.aside { overflow: hidden; padding: 0 10px; border-right: 0; background: linear-gradient(180deg,var(--dms-nav-top),var(--dms-nav-bottom)); box-shadow: 1px 0 0 rgba(9,30,66,.16); transition: width .2s ease; }.aside.collapsed { padding: 0 8px; }.logo { display: flex; align-items: center; gap: 10px; height: 52px; padding: 0 4px; border-bottom: 1px solid var(--dms-nav-divider); color: white; text-decoration: none; }.logo-mark { display: grid; place-items: center; flex: 0 0 28px; width: 28px; height: 28px; border-radius: 8px; color: #fff; font-size: 10px; font-weight: 800; letter-spacing: -.04em; background: linear-gradient(135deg,var(--dms-accent),var(--dms-accent-strong)); box-shadow: 0 4px 12px rgba(43,92,230,.4); }.logo-word { white-space: nowrap; font-size: 15px; font-weight: 700; letter-spacing: -.02em; }.logo-word em { color: #9db9f5; font-style: normal; }.menu { height: calc(100vh - 63px); overflow-x: hidden; overflow-y: auto; border: 0; background: transparent; --el-menu-item-height: 34px; --el-menu-text-color: var(--dms-nav-text); --el-menu-hover-bg-color: var(--dms-nav-hover); --el-menu-hover-text-color: #fff; }.menu::-webkit-scrollbar { width: 0; }.menu :deep(.el-menu-item-group__title) { padding: 9px 10px 3px; color: rgba(255,255,255,.42); font-size: 10px; font-weight: 700; letter-spacing: .1em; }.collapsed .menu :deep(.el-menu-item-group__title) { height: 10px; overflow: hidden; padding: 8px 0 2px; font-size: 0; }.menu .el-menu-item { position: relative; margin-bottom: 2px; border-radius: 7px; color: var(--dms-nav-text); font-size: 12.5px; }.menu .el-menu-item:hover { color: white; }.menu .el-menu-item.is-active { color: white; font-weight: 600; background: var(--dms-nav-active); }
.menu .el-menu-item.is-active::before { content: ""; position: absolute; left: 0; top: 7px; bottom: 7px; width: 3px; border-radius: 0 3px 3px 0; background: linear-gradient(180deg,#6f97f5,#2b5ce6); box-shadow: 0 0 10px rgba(43,92,230,.8); }.menu.el-menu--collapse { width: 56px; }.content-shell { min-width: 0; }.header { display: flex; align-items: center; justify-content: space-between; height: 50px; padding: 0 16px; border-bottom: 1px solid var(--dms-hairline); background: rgba(255,255,255,.92); backdrop-filter: blur(12px); box-shadow: 0 1px 3px rgba(16,32,70,.04); }.header-context { display: flex; align-items: center; gap: 10px; color: var(--dms-ink-2); font-size: 13px; }
.header-context .page-title { color: var(--dms-ink); font-size: 14px; font-weight: 600; }.user { display: flex; align-items: center; gap: 8px; padding: 4px 10px 4px 4px; border: 0; border-radius: 99px; color: var(--dms-ink); background: transparent; cursor: pointer; transition: background .15s; }
.user:hover { background: #f0f2f7; }.avatar { display: grid; place-items: center; width: 26px; height: 26px; border-radius: 50%; color: white; font-size: 12px; font-weight: 700; background: linear-gradient(135deg,var(--dms-accent),var(--dms-accent-strong)); }.el-main { min-width: 0; padding: 16px; }.el-main > :deep(*) { max-width: 1600px; margin-right: auto; margin-left: auto; }
@media (max-width: 1199px) { .el-main { padding: 16px; } .header { padding: 0 16px; } }
@media (max-width: 767px) { .header-context span { display: none; } .user { font-size: 0; } }
</style>
