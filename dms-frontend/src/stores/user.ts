import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCurrentUser, type CurrentUser } from '@/api/auth'
import { clearDictCache } from '@/utils/dict'

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

  async function fetchCurrentUser() {
    userInfo.value = await getCurrentUser()
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('dms_token')
    clearDictCache()
  }

  return { token, userInfo, setToken, setUserInfo, fetchCurrentUser, logout }
})
