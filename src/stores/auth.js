import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const role = ref(null)
  const token = ref(null)

  const staffLogin = async (username, ) => {
    // 模拟登录成功
    role.value = 'STAFF'
    token.value = 'mock-token-' + username
    return Promise.resolve()
  }

  const logout = () => {
    role.value = null
    token.value = null
  }

  return { role, token, staffLogin, logout }
})