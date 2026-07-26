import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

service.interceptors.response.use(
  async (response) => {
    if (response.config.responseType === 'blob') {
      // 导出失败时后端返回 JSON 错误体而非文件，避免把错误内容下载成假 xlsx
      if (response.data instanceof Blob && response.data.type.includes('application/json')) {
        let message = '导出失败'
        try {
          const res = JSON.parse(await response.data.text())
          message = res.message || message
        } catch {
          // 非标准 JSON 时保留默认提示
        }
        ElMessage.error(message)
        return Promise.reject(new Error(message))
      }
      // 放行完整 response，调用处可读 headers（如 Content-Disposition 文件名）
      return response
    }
    const res = response.data
    if (res.code === 0) {
      return res.data
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      const userStore = useUserStore()
      const authorization = error.config?.headers?.get?.('Authorization')
      if (authorization === `Bearer ${userStore.token}`) {
        userStore.logout()
        router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
        ElMessage.error('登录已过期，请重新登录')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
