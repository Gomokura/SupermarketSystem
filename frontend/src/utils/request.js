import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

const clearAuthStorage = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('adminToken')
  localStorage.removeItem('courierToken')
  localStorage.removeItem('user')
  localStorage.removeItem('cart')
  localStorage.removeItem('searchHistory')
}

request.interceptors.request.use(
  config => {
    const path = config.url || ''
    let token = null
    
    if (path.startsWith('/admin') || path.startsWith('/cashier')) {
      token = localStorage.getItem('adminToken')
    } else if (path.startsWith('/courier')) {
      token = localStorage.getItem('courierToken')
    } else {
      token = localStorage.getItem('token')
    }
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(res)
    }
  },
  error => {
    if (error.response?.status === 401) {
      clearAuthStorage()
      const msg = error.response?.data?.message || '登录已过期，请重新登录'
      ElMessage.error(msg)
      const path = error.config?.url || ''
      if (path.startsWith('/courier')) {
        router.push('/login?role=courier')
      } else if (path.startsWith('/admin') || path.startsWith('/cashier')) {
        router.push('/login?role=admin')
      } else {
        router.push('/login?role=customer')
      }
    } else {
      const msg = error.response?.data?.message
      const noResponse = !error.response
      ElMessage.error(
        msg ||
          (noResponse
            ? '无法连接后端：请先启动 Spring Boot（默认 http://localhost:8080），并保证数据库可用'
            : '网络错误')
      )
    }
    return Promise.reject(error)
  }
)

export default request
