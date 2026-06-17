<template>
  <el-container class="layout">
    <el-aside width="232px" class="aside">
      <div class="logo"><span class="logo-mark">C</span>ChipMore <span class="logo-accent">Dorm</span></div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item-group title="资源管理">
          <el-menu-item index="/buildings"><el-icon><OfficeBuilding /></el-icon><span>楼栋管理</span></el-menu-item>
          <el-menu-item index="/floors"><el-icon><Files /></el-icon><span>楼层管理</span></el-menu-item>
          <el-menu-item index="/rooms"><el-icon><House /></el-icon><span>房间管理</span></el-menu-item>
          <el-menu-item index="/beds"><el-icon><Bell /></el-icon><span>床位管理</span></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="视图">
          <el-menu-item index="/board"><el-icon><Grid /></el-icon><span>房间状态看板</span></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="入住管理">
          <el-menu-item index="/residents"><el-icon><User /></el-icon><span>居住人管理</span></el-menu-item>
          <el-menu-item index="/intakes"><el-icon><DocumentAdd /></el-icon><span>入住意向单</span></el-menu-item>
          <el-menu-item index="/records"><el-icon><Tickets /></el-icon><span>入住档案</span></el-menu-item>
          <el-menu-item index="/checkout-orders"><el-icon><SwitchButton /></el-icon><span>退宿单</span></el-menu-item>
        </el-menu-item-group>
        <el-menu-item-group title="费用管理">
          <el-menu-item index="/fee-standards"><el-icon><PriceTag /></el-icon><span>收费标准</span></el-menu-item>
          <el-menu-item index="/fee-bills"><el-icon><Money /></el-icon><span>住宿费账单</span></el-menu-item>
          <el-menu-item index="/fee-meter"><el-icon><Odometer /></el-icon><span>抄表/水电</span></el-menu-item>
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
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
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
import { OfficeBuilding, Files, House, Bell, Grid, ArrowDown, User, DocumentAdd, Tickets, SwitchButton, PriceTag, Money, Odometer } from '@element-plus/icons-vue'
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
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: var(--dms-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 800;
  color: #fff;
  box-shadow: 0 2px 8px rgba(31, 111, 235, 0.45);
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
