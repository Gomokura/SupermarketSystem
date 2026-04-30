import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const userInfo = ref({})
  const currentRole = ref('')
  const adminToken = ref('')
  const courierToken = ref('')

  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) localStorage.setItem('token', newToken)
    else localStorage.removeItem('token')
  }

  const setUserInfo = (info) => {
    userInfo.value = info
  }

  const setCurrentRole = (role) => {
    currentRole.value = role
  }

  const setAdminToken = (t) => {
    adminToken.value = t
    if (t) localStorage.setItem('adminToken', t)
    else localStorage.removeItem('adminToken')
  }

  const setCourierToken = (t) => {
    courierToken.value = t
    if (t) localStorage.setItem('courierToken', t)
    else localStorage.removeItem('courierToken')
  }

  const login = (role, user) => {
    currentRole.value = role
    userInfo.value = user
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    currentRole.value = ''
    adminToken.value = ''
    courierToken.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('adminToken')
    localStorage.removeItem('courierToken')
    localStorage.removeItem('supermarket-user')
  }

  return {
    token,
    userInfo,
    currentRole,
    adminToken,
    courierToken,
    setToken,
    setUserInfo,
    setCurrentRole,
    setAdminToken,
    setCourierToken,
    login,
    logout
  }
}, {
  persist: {
    storage: localStorage,
    key: 'supermarket-user',
    paths: ['token', 'userInfo', 'currentRole', 'adminToken', 'courierToken']
  }
})
