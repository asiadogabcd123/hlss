import { defineStore } from 'pinia'
import luggageApi from '@/api'
import { useSocketStore } from './socket'

export const useLuggageStore = defineStore('luggage', {
  state: () => ({
    items: [],
    currentItem: null,
    stats: {
      total: 0,
      checkedIn: 0,
      pickedUp: 0
    },
    realtimeUpdates: []
  }),
  
  actions: {
    // CRUD操作
    async createLuggage(payload) {
      const res = await luggageApi.create(payload)
      this.items.unshift(res.data)
      return res
    },

    async fetchLuggage(id) {
      const res = await luggageApi.get(id)
      this.currentItem = res.data
      return res
    },

    // 业务逻辑
    async processCheckin(data) {
      const res = await luggageApi.checkin(data)
      this.updateStats()
      useSocketStore().sendNotification('checkin', res.data)
      return res
    },

    // 实时状态
    initRealtimeUpdates() {
      const socket = useSocketStore().socket
      socket.on('luggage-update', (data) => {
        this.realtimeUpdates.push(data)
        this.updateStats()
      })
    },

    // 统计更新
    async updateStats() {
      const res = await luggageApi.getStats()
      this.stats = res.data
    }
  },

  getters: {
    overdueItems: (state) => state.items.filter(
      item => item.status === 'STORED' && new Date(item.dueTime) < new Date()
    )
  }
})