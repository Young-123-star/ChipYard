<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '72px' : '232px'" class="aside" :class="{ collapsed }">
      <router-link class="logo" to="/dashboard" aria-label="??????">
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
          <el-button text circle :aria-label="collapsed ? '????' : '????'" @click="collapsed = !collapsed">
            <el-icon size="18"><Expand v-if="collapsed" /><Fold v-else /></el-icon>
          </el-button>
          <span>&#23487;&#33293;&#36816;&#33829;&#25511;&#21046;&#21488;</span>
        </div>
        <el-dropdown @command="onCommand">
          <button class="user" type="button"><span class="avatar">{{ userInitial }}</span>{{ userName }}<el-icon><ArrowDown /></el-icon></button>
          <template #dropdown><el-dropdown-menu><el-dropdown-item command="logout">&#36864;&#20986;&#30331;&#24405;</el-dropdown-item></el-dropdown-menu></template>
        </el-dropdown>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Bell, CircleCheck, DataBoard, DocumentAdd, Expand, Files, Fold, Grid, House, Money, Odometer, OfficeBuilding, PriceTag, Setting, SwitchButton, Tickets, Tools, TrendCharts, UploadFilled, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(window.innerWidth < 1200)
const userName = computed(() => userStore.userInfo?.realName || 'admin')
const userInitial = computed(() => userName.value.slice(0, 1).toUpperCase())
function syncSidebar() { if (window.innerWidth < 1200) collapsed.value = true }
function onCommand(cmd: string) { if (cmd === 'logout') { userStore.logout(); router.push('/login') } }
onMounted(() => window.addEventListener('resize', syncSidebar))
onBeforeUnmount(() => window.removeEventListener('resize', syncSidebar))
</script>

<style scoped>
.layout { height: 100vh; overflow: hidden; }.aside { overflow: hidden; padding: 0 14px; border-right: 0; background: linear-gradient(180deg,var(--dms-nav-top),var(--dms-nav-bottom)); transition: width .2s ease; }.aside.collapsed { padding: 0 8px; }.logo { display: flex; align-items: center; gap: 10px; height: 64px; padding: 0 4px; border-bottom: 1px solid var(--dms-nav-divider); color: white; text-decoration: none; }.logo-mark { display: grid; place-items: center; flex: 0 0 34px; width: 34px; height: 34px; border: 1px solid rgba(255,255,255,.24); border-radius: 9px; color: #b9d5ff; font-size: 11px; font-weight: 800; letter-spacing: -.04em; background: rgba(255,255,255,.08); }.logo-word { white-space: nowrap; font-size: 17px; font-weight: 700; letter-spacing: -.02em; }.logo-word em { color: #8fbaff; font-style: normal; }.menu { height: calc(100vh - 76px); overflow-x: hidden; overflow-y: auto; border: 0; background: transparent; --el-menu-item-height: 40px; --el-menu-text-color: var(--dms-nav-text); --el-menu-hover-bg-color: var(--dms-nav-hover); --el-menu-hover-text-color: #fff; }.menu::-webkit-scrollbar { width: 0; }.menu :deep(.el-menu-item-group__title) { padding: 15px 10px 5px; color: rgba(255,255,255,.42); font-size: 10px; font-weight: 700; letter-spacing: .1em; }.collapsed .menu :deep(.el-menu-item-group__title) { height: 10px; overflow: hidden; padding: 8px 0 2px; font-size: 0; }.menu .el-menu-item { margin-bottom: 3px; border-radius: 9px; color: var(--dms-nav-text); font-size: 13px; }.menu .el-menu-item:hover { color: white; }.menu .el-menu-item.is-active { color: white; font-weight: 600; background: var(--dms-nav-active); box-shadow: 0 4px 14px rgba(31,111,235,.32); }.menu.el-menu--collapse { width: 56px; }.content-shell { min-width: 0; }.header { display: flex; align-items: center; justify-content: space-between; padding: 0 24px; border-bottom: 1px solid var(--dms-hairline); background: rgba(255,255,255,.92); backdrop-filter: blur(12px); }.header-context { display: flex; align-items: center; gap: 10px; color: var(--dms-ink-2); font-size: 13px; }.user { display: flex; align-items: center; gap: 8px; padding: 5px 0 5px 8px; border: 0; color: var(--dms-ink); background: transparent; cursor: pointer; }.avatar { display: grid; place-items: center; width: 30px; height: 30px; border-radius: 9px; color: white; font-size: 12px; font-weight: 700; background: var(--dms-accent); }.el-main { min-width: 0; padding: 24px; }.el-main > :deep(*) { max-width: 1600px; margin-right: auto; margin-left: auto; }
@media (max-width: 1199px) { .el-main { padding: 16px; } .header { padding: 0 16px; } }
@media (max-width: 767px) { .header-context span { display: none; } .user { font-size: 0; } }
</style>
