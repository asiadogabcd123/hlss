import { defineStore } from 'pinia'
import luggageApi from '@/api/luggage'

export const useLuggageStore = defineStore('luggage', {
  state: () => ({
    items: [],
    currentItem: null,
    stats: {
      current: 0,    // 當前寄存
      todayIn: 0,    // 今日寄存
      todayOut: 0    // 今日取件
    },
    recentBags: [],  // 最近行李記錄
    realtimeUpdates: []
  }),
  
  actions: {
    // 創建行李記錄 (支持FormData)
    async createLuggage(formData) {
      try {
        // 使用 luggageApi 的 create 方法
        const response = await luggageApi.create(formData)
        this.items.unshift(response.data)
        return response
      } catch (error) {
        console.error('創建行李記錄失敗:', error)
        throw error // 重新拋出錯誤以便組件捕獲
      }
    },

    async updateStats() {
      try {
        const response = await luggageApi.getStats()
        this.stats = {
          current: response.data.currentStorage,
          todayIn: response.data.todayCheckin,
          todayOut: response.data.todayCheckout
        }
        this.recentBags = response.data.recentBags.map(bag => ({
          id: `BAG-${bag.id}`,
          guestName: bag.guestName,
          time: new Date(bag.checkinTime),
          status: bag.status === 'STORED' ? '已寄存' : '已取件',
          area: `區${bag.storageLocation?.charAt(0) || 'A'}`
        }))
      } catch (error) {
        console.error('獲取統計數據失敗:', error)
      }
    },

    // 初始化實時更新
    initRealtimeUpdates() {
      // 保持您原有的實時更新邏輯
    }
  },

  getters: {
    // 可添加其他需要的getters
    overdueItems: (state) => state.items.filter(
      item => item.status === 'STORED' && new Date(item.dueTime) < new Date()
    )
  }
})