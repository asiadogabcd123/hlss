import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const api = axios.create({
  baseURL: process.env.VUE_APP_API_BASE || 'http://localhost:8001/api',
  timeout: 10000,
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

export const luggageApi = {
  register: (data) => api.post('/luggage', data),
  get: (id) => api.get(`/luggage/${id}`),
  pickup: (id) => api.patch(`/luggage/${id}/pickup`)
}

// 請求攔截器
api.interceptors.request.use(config => {
  const token = localStorage.getItem('staffToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  if (process.env.NODE_ENV === 'development') {
    console.log('[API請求]', {
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