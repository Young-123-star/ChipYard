<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">宿舍管理</div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item index="/buildings"><el-icon><OfficeBuilding /></el-icon><span>楼栋管理</span></el-menu-item>
        <el-menu-item index="/floors"><el-icon><Files /></el-icon><span>楼层管理</span></el-menu-item>
        <el-menu-item index="/rooms"><el-icon><House /></el-icon><span>房间管理</span></el-menu-item>
        <el-menu-item index="/beds"><el-icon><Bell /></el-icon><span>床位管理</span></el-menu-item>
        <el-menu-item index="/board"><el-icon><Grid /></el-icon><span>房间状态看板</span></el-menu-item>
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
import { OfficeBuilding, Files, House, Bell, Grid, ArrowDown } from '@element-plus/icons-vue'
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
  background: var(--dms-surface);
  backdrop-filter: var(--dms-blur);
  border-right: 1px solid var(--dms-hairline);
  padding: 16px 12px 0;
}
.logo {
  color: var(--dms-ink);
  height: 48px;
  line-height: 48px;
  padding-left: 14px;
  font-weight: 600;
  font-size: 17px;
  letter-spacing: -0.02em;
}
.menu {
  border-right: none;
  background: transparent;
  --el-menu-item-height: 42px;
  --el-menu-text-color: var(--dms-ink-2);
  --el-menu-hover-bg-color: rgba(0, 0, 0, 0.04);
}
.menu .el-menu-item {
  border-radius: 10px;
  margin-bottom: 2px;
  font-size: 14px;
}
.menu .el-menu-item.is-active {
  background: rgba(0, 113, 227, 0.1);
  color: var(--dms-accent);
  font-weight: 600;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--dms-hairline);
  background: var(--dms-surface);
  backdrop-filter: var(--dms-blur);
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
