<template>
  <el-container class="layout">
    <el-aside width="232px" class="aside">
      <div class="logo"><img class="logo-mark" src="/logo.png" alt="" />ChipMore <span class="logo-accent">Dorm</span></div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item-group title="&#36164;&#28304;&#31649;&#29702;">
          <el-menu-item index="/buildings"><el-icon><OfficeBuilding /></el-icon><span>&#27004;&#26635;&#31649;&#29702;</span></el-menu-item>
          <el-menu-item index="/floors"><el-icon><Files /></el-icon><span>&#27004;&#23618;&#31649;&#29702;</span></el-menu-item>
          <el-menu-item index="/rooms"><el-icon><House /></el-icon><span>&#25151;&#38388;&#31649;&#29702;</span></el-menu-item>
          <el-menu-item index="/beds"><el-icon><Bell /></el-icon><span>&#24202;&#20301;&#31649;&#29702;</span></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="&#35270;&#22270;">
          <el-menu-item index="/board"><el-icon><Grid /></el-icon><span>&#25151;&#38388;&#29366;&#24577;&#30475;&#26495;</span></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="&#20837;&#20303;&#31649;&#29702;">
          <el-menu-item index="/residents"><el-icon><User /></el-icon><span>&#23621;&#20303;&#20154;&#31649;&#29702;</span></el-menu-item>
          <el-menu-item index="/intakes"><el-icon><DocumentAdd /></el-icon><span>&#20837;&#20303;&#24847;&#21521;&#21333;</span></el-menu-item>
          <el-menu-item index="/records"><el-icon><Tickets /></el-icon><span>&#20837;&#20303;&#26723;&#26696;</span></el-menu-item>
          <el-menu-item index="/checkout-orders"><el-icon><SwitchButton /></el-icon><span>&#36864;&#23487;&#21333;</span></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="&#36153;&#29992;&#31649;&#29702;">
          <el-menu-item index="/fee-standards"><el-icon><PriceTag /></el-icon><span>&#25910;&#36153;&#26631;&#20934;</span></el-menu-item>
          <el-menu-item index="/fee-bills"><el-icon><Money /></el-icon><span>&#20303;&#23487;&#36153;&#36134;&#21333;</span></el-menu-item>
          <el-menu-item index="/fee-meter"><el-icon><Odometer /></el-icon><span>&#25220;&#34920;/&#27700;&#30005;</span></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="&#32500;&#20462;&#31649;&#29702;">
          <el-menu-item index="/repair-orders"><el-icon><Tools /></el-icon><span>&#32500;&#20462;&#24037;&#21333;</span></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="&#31995;&#32479;&#24037;&#20855;">
          <el-menu-item index="/data-import"><el-icon><UploadFilled /></el-icon><span>&#25968;&#25454;&#21021;&#22987;&#21270;</span></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="&#32479;&#35745;&#25253;&#34920;">
          <el-menu-item index="/report"><el-icon><TrendCharts /></el-icon><span>&#36134;&#21333;&#25253;&#34920;</span></el-menu-item>
        </el-menu-item-group>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="page-title">{{ route.meta.title || '' }}</span>
        <el-dropdown @command="onCommand">
          <span class="user">{{ userStore.userInfo?.realName || 'admin' }}<el-icon><ArrowDown /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">&#36864;&#20986;&#30331;&#24405;</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { OfficeBuilding, Files, House, Bell, Grid, ArrowDown, User, DocumentAdd, Tickets, SwitchButton, PriceTag, Money, Odometer, TrendCharts, Tools, UploadFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function onCommand(cmd: string) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside {
  background: linear-gradient(180deg, var(--dms-nav-top) 0%, var(--dms-nav-bottom) 100%);
  border-right: none;
  padding: 0 14px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 9px;
  color: var(--dms-nav-text-strong);
  height: 64px;
  padding: 0 6px;
  font-weight: 700;
  font-size: 18px;
  letter-spacing: -0.02em;
  border-bottom: 1px solid var(--dms-nav-divider);
  margin-bottom: 12px;
}
.logo-mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #fff;
  object-fit: contain;
  padding: 2px;
  box-shadow: 0 2px 8px rgba(31, 111, 235, 0.35);
}
.logo-accent { color: #8fbaff; }
.menu {
  border-right: none;
  background: transparent;
  --el-menu-item-height: 42px;
  --el-menu-text-color: var(--dms-nav-text);
  --el-menu-hover-bg-color: var(--dms-nav-hover);
  --el-menu-hover-text-color: #fff;
}
.menu :deep(.el-menu-item-group__title) {
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.45);
  padding: 8px 10px 6px;
}
.menu .el-menu-item {
  border-radius: 10px;
  margin-bottom: 4px;
  font-size: 14px;
  color: var(--dms-nav-text);
}
.menu .el-menu-item:hover { color: #fff; }
.menu .el-menu-item.is-active {
  background: var(--dms-nav-active);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(47, 128, 247, 0.4);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--dms-hairline);
  background: var(--dms-surface);
}
.page-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.02em;
}
.user {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--dms-ink);
  font-size: 14px;
}
.el-main { padding: 28px 32px; }
</style>
