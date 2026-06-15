<template>
  <div class="login-page">
    <!-- 左：深蓝品牌栏 -->
    <div class="brand-panel">
      <div class="brand-top">
        <span class="brand-mark">C</span>
        <span class="brand-word">ChipMore <span class="accent">Dorm</span></span>
      </div>
      <div class="brand-mid">
        <h1>宿舍资源，<br />一目了然</h1>
        <p class="tagline">楼栋 · 楼层 · 房间 · 床位 · 状态看板</p>
        <ul class="feat">
          <li>实时入住率与床位占用看板</li>
          <li>楼栋 / 楼层 / 房间 / 床位 全链路管理</li>
          <li>多条件筛选，宿管一眼掌握全局</li>
        </ul>
      </div>
      <p class="brand-foot">© ChipMore Dorm · 宿舍管理系统</p>
    </div>

    <!-- 右：登录表单 -->
    <div class="form-panel">
      <div class="login-card">
        <h2 class="title">欢迎登录</h2>
        <p class="subtitle">使用管理员账号进入系统</p>
        <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="onSubmit">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password size="large" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="onSubmit">登 录</el-button>
        </el-form>
        <p class="hint">演示账号：admin / admin123</p>
      </div>
    </div>
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
.login-page {
  height: 100vh;
  display: flex;
}

/* ===== 左：深蓝品牌栏 ===== */
.brand-panel {
  flex: 0 0 46%;
  position: relative;
  overflow: hidden;
  background: linear-gradient(155deg, var(--dms-nav-top) 0%, var(--dms-nav-bottom) 70%, #103f80 100%);
  color: #fff;
  padding: 48px 56px;
  display: flex;
  flex-direction: column;
}
/* 柔光装饰 */
.brand-panel::before,
.brand-panel::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  filter: blur(8px);
  pointer-events: none;
}
.brand-panel::before {
  width: 460px; height: 460px;
  top: -160px; right: -120px;
  background: radial-gradient(circle, rgba(122, 173, 255, 0.45), transparent 62%);
}
.brand-panel::after {
  width: 360px; height: 360px;
  bottom: -120px; left: -100px;
  background: radial-gradient(circle, rgba(71, 145, 248, 0.35), transparent 60%);
}
.brand-top {
  position: relative; z-index: 1;
  display: flex; align-items: center; gap: 11px;
  font-size: 21px; font-weight: 700; letter-spacing: -0.02em;
}
.brand-mark {
  width: 34px; height: 34px; border-radius: 9px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 800;
}
.brand-word .accent { color: #9fc4ff; }
.brand-mid {
  position: relative; z-index: 1;
  margin-top: auto; margin-bottom: auto;
}
.brand-mid h1 {
  font-size: clamp(34px, 3.6vw, 50px);
  font-weight: 700; line-height: 1.12; letter-spacing: -0.03em;
  margin: 0 0 18px;
}
.tagline {
  font-size: 16px; color: rgba(255, 255, 255, 0.78);
  letter-spacing: 0.01em; margin: 0 0 30px;
}
.feat { list-style: none; padding: 0; margin: 0; }
.feat li {
  position: relative;
  padding-left: 26px; margin-bottom: 14px;
  font-size: 14.5px; color: rgba(255, 255, 255, 0.9);
}
.feat li::before {
  content: ''; position: absolute; left: 0; top: 6px;
  width: 14px; height: 8px;
  border-left: 2px solid #9fc4ff; border-bottom: 2px solid #9fc4ff;
  transform: rotate(-45deg);
}
.brand-foot {
  position: relative; z-index: 1;
  font-size: 12.5px; color: rgba(255, 255, 255, 0.55); margin: 0;
}

/* ===== 右：登录表单 ===== */
.form-panel {
  flex: 1;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(180deg, var(--dms-bg-top), var(--dms-bg-bottom));
  padding: 24px;
}
.login-card {
  width: 380px;
  background: #fff;
  border: 1px solid var(--dms-hairline);
  border-radius: 18px;
  box-shadow: var(--dms-shadow-card);
  padding: 38px 36px 30px;
}
.title {
  margin: 0 0 6px;
  font-size: 26px; font-weight: 700; letter-spacing: -0.02em;
  color: var(--dms-ink);
}
.subtitle {
  margin: 0 0 26px;
  font-size: 13.5px; color: var(--dms-ink-2);
}
.hint {
  text-align: center; color: var(--dms-ink-2);
  font-size: 12px; margin: 16px 0 0;
}

/* 窄屏：隐藏品牌栏，仅留表单 */
@media (max-width: 820px) {
  .brand-panel { display: none; }
  .login-card { width: min(380px, 92vw); }
}
</style>
