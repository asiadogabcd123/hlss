import api from '@/api'

export default {
  // 基础CRUD
  create: (data) => api.post('/luggage', data),
  get: (id) => api.get(`/luggage/${id}`),
  update: (id, data) => api.put(`/luggage/${id}`, data),
  delete: (id) => api.delete(`/luggage/${id}`),

  // 业务逻辑
  checkin: (data) => api.post('/luggage/checkin', data),
  pickup: (id) => api.patch(`/luggage/${id}/pickup`),
  search: (params) => api.get('/luggage/search', { params }),

  // 状态监控
  getRealtimeStatus: () => api.get('/luggage/status-stream'),

  // 报表
  generateReport: (params) => api.get('/luggage/reports', { params })
}