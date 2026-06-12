<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">宿舍管理系统</h2>
      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="onSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="onSubmit">登录</el-button>
      </el-form>
      <p class="hint">演示账号：admin / admin123</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login, getCurrentUser } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const res = await login(form)
    userStore.setToken(res.token)
    const me = await getCurrentUser()
    userStore.setUserInfo(me)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(1200px 600px at 80% -10%, rgba(0, 113, 227, 0.08), transparent 60%),
    linear-gradient(180deg, #fbfbfd 0%, #eef0f4 100%);
}
.login-card {
  width: 380px;
  border-radius: 24px;
  border: 1px solid var(--dms-hairline);
  background: var(--dms-surface);
  backdrop-filter: var(--dms-blur);
  box-shadow: var(--dms-shadow-float);
  padding: 12px 8px;
}
.title {
  text-align: center;
  margin: 4px 0 26px;
  color: var(--dms-ink);
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.025em;
}
.hint { text-align: center; color: var(--dms-ink-2); font-size: 12px; margin: 14px 0 0; }
</style>
