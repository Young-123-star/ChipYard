<template>
  <div class="login-container">
    <div class="brand">
      <div class="wordmark">ChipYard <span>Dram</span></div>
      <p class="tagline">宿舍资源，一目了然</p>
      <p class="desc">楼栋 · 楼层 · 房间 · 床位 · 状态看板</p>
    </div>

    <el-card class="login-card">
      <h2 class="title">登录</h2>
      <p class="subtitle">使用管理员账号进入系统</p>
      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="onSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password size="large" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="onSubmit">登录</el-button>
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
  gap: clamp(48px, 9vw, 160px);
  padding: 24px;
  background-image: url('@/assets/login-bg.svg');
  background-size: cover;
  background-position: center;
}

/* 品牌区 */
.brand { max-width: 520px; }
.wordmark {
  font-size: clamp(44px, 5.2vw, 64px);
  font-weight: 700;
  letter-spacing: -0.035em;
  color: var(--dms-ink);
  line-height: 1.05;
}
.wordmark span {
  background: linear-gradient(92deg, #0071e3 10%, #6e8bff 55%, #38b6d8 95%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.tagline {
  margin: 18px 0 6px;
  font-size: 20px;
  font-weight: 500;
  color: var(--dms-ink);
  letter-spacing: -0.01em;
}
.desc {
  margin: 0;
  font-size: 14px;
  color: var(--dms-ink-2);
  letter-spacing: 0.02em;
}

/* 登录卡 */
.login-card {
  width: 400px;
  flex-shrink: 0;
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  background: rgba(255, 255, 255, 0.62);
  backdrop-filter: blur(36px) saturate(1.7);
  box-shadow: 0 24px 70px rgba(29, 79, 138, 0.16);
  padding: 16px 10px;
}
.title {
  margin: 4px 0 4px;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--dms-ink);
}
.subtitle {
  margin: 0 0 22px;
  font-size: 13.5px;
  color: var(--dms-ink-2);
}
.login-card :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8);
}
.hint {
  text-align: center;
  color: var(--dms-ink-2);
  font-size: 12px;
  margin: 14px 0 0;
}

/* 窄屏：竖排，隐藏副描述 */
@media (max-width: 860px) {
  .login-container { flex-direction: column; gap: 28px; }
  .brand { text-align: center; }
  .desc { display: none; }
  .login-card { width: min(400px, 92vw); }
}
</style>
