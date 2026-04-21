import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authAPI } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('user') || '{}'))

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('user', JSON.stringify(info))
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('adminToken')
    localStorage.removeItem('courierToken')
    localStorage.removeItem('user')
  }

  const fetchUserInfo = async () => {
    try {
      const res = await authAPI.getUserInfo()
      setUserInfo(res.data)
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    logout,
    fetchUserInfo
  }
})
