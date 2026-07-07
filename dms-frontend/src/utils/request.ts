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
  (response) => {
    if (response.config.responseType === 'blob') return response.data
    const res = response.data
    if (res.code === 0) {
      return res.data
    }
    ElMessage.error(res.message || '\u8bf7\u6c42\u5931\u8d25')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error('\u767b\u5f55\u5df2\u8fc7\u671f\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55')
    } else {
      ElMessage.error(error.message || '\u7f51\u7edc\u9519\u8bef')
    }
    return Promise.reject(error)
  }
)

export default service
