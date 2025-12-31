import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const baseURL = process.env.NODE_ENV === 'development' ? 'http://192.168.28.10:8001/api':'/api';

const api = axios.create({
  baseURL:  baseURL,
  timeout: 10000,
  withCredentials: false,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  transformResponse: [function (data) {
    try {
      return JSON.parse(data)
    } catch (e) {
      return data
    }
  }]
})


// 請求攔截器
api.interceptors.request.use(config => {
  const token = localStorage.getItem('staffToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  
  
  if (process.env.NODE_ENV === 'development') {
    console.log('[API请求]', {
      url: config.url,
      method: config.method,
      params: config.params,
      data: config.data
    })
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 回應攔截器
api.interceptors.response.use(
  response => {
    if (process.env.NODE_ENV === 'development') {
      console.log('[API回應]', {
        url: response.config.url,
        status: response.status,
        data: response.data
      })
    }
    return response
  },
  error => {
    if (error.response?.status === 401) {
      useAuthStore().logout()
      if (!window.location.pathname.includes('/staff/login')) {
        window.location.href = '/staff/login?redirect=' + 
          encodeURIComponent(window.location.pathname)
      }
    }
    return Promise.reject({
      message: error.response?.data?.message || '請求失敗',
      status: error.response?.status,
      data: error.response?.data
    })
  }
)

export default api