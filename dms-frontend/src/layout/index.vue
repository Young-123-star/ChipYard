<template>
  <el-container class="layout">
    <el-aside width="210px" class="aside">
      <div class="logo">宿舍管理系统</div>
      <el-menu :default-active="route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
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
.aside { background: #304156; }
.logo { color: #fff; height: 56px; line-height: 56px; text-align: center; font-weight: bold; font-size: 16px; }
.header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #eee; background: #fff; }
.page-title { font-size: 16px; font-weight: 500; }
.user { cursor: pointer; display: flex; align-items: center; gap: 4px; }
</style>
