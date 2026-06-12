import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('dms_token') || '')
  const userInfo = ref<CurrentUser | null>(null)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('dms_token', t)
  }

  function setUserInfo(u: CurrentUser) {
    userInfo.value = u
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('dms_token')
  }

  return { token, userInfo, setToken, setUserInfo, logout }
})
