<template>
  <div class="dashboard-container">
    <!-- 標題區 -->
    <div class="header">
      <h1><i class="fas fa-tachometer-alt"></i> 行李管理控制台</h1>
      <StaffQrGenerator />

      <!-- 統計卡片區 -->
      <div class="stats">
        <StatCard 
          icon="suitcase" 
          title="當前寄存" 
          :value="stats.current" 
          color="#3498db" 
          :loading="loading"
        />
        <StatCard 
          icon="sign-in-alt" 
          title="今日寄存" 
          :value="stats.todayIn" 
          color="#2ecc71" 
          :loading="loading"
        />
        <StatCard 
          icon="sign-out-alt" 
          title="今日取件" 
          :value="stats.todayOut" 
          color="#e74c3c" 
          :loading="loading"
        />
      </div>
    </div>

    <!-- 快速操作區 -->
    <div class="actions">
      <router-link to="/staff/scan" class="action-card">
        <i class="fas fa-qrcode"></i>
        <span>掃描取件</span>
      </router-link>
      <router-link to="/staff/register" class="action-card">
        <i class="fas fa-luggage-cart"></i>
        <span>行李登記</span>
      </router-link>
    </div>

    <!-- 最近操作記錄 -->
    <div class="recent-bags">
      <h2><i class="fas fa-clock"></i> 最近操作</h2>
      <BagList :bags="recentBags" :loading="loading" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchDashboardData } from '@/api/luggage' // 假設的API請求方法
import StatCard from '@/components/StatCard.vue'
import BagList from '@/components/BagList.vue'
import StaffQrGenerator from '@/components/QrGenerator.vue'

const loading = ref(true)
const stats = ref({
  current: 0,
  todayIn: 0,
  todayOut: 0
})

const recentBags = ref([])

// 從API獲取數據
const loadDashboardData = async () => {
  try {
    loading.value = true
    const response = await fetchDashboardData()
    
    // 更新統計數據
    stats.value = {
      current: response.data.currentStorage,
      todayIn: response.data.todayCheckin,
      todayOut: response.data.todayCheckout
    }
    
    // 更新最近行李記錄
    recentBags.value = response.data.recentBags.map(bag => ({
      id: `BAG-${bag.id}`,
      guestName: bag.guest_name,
      time: new Date(bag.checkin_time),
      status: bag.status === 'STORED' ? '已寄存' : '已取件',
      area: `區${bag.location ? bag.location.charAt(0) : 'A'}` // 假設位置第一個字符是區域
    }))
    
  } catch (error) {
    console.error('獲取儀表板數據失敗:', error)
    // 可以在此處添加錯誤處理，如顯示通知
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboardData()
  
  // 可選：設置定時刷新（每5分鐘）
  const refreshInterval = setInterval(loadDashboardData, 5 * 60 * 1000)
  
  // 組件卸載時清除定時器
  return () => clearInterval(refreshInterval)
})
</script>


<style scoped>
/* 基礎佈局 */
.dashboard-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 標題區樣式 */
.header {
  margin-bottom: 30px;
}

.header h1 {
  color: #2c3e50;
  font-size: 1.8rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 統計卡片區 */
.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 25px;
}

/* 快速操作按鈕 */
.actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
  margin-bottom: 30px;
}

.action-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  color: #2c3e50;
  text-decoration: none;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.action-card i {
  font-size: 2.2rem;
  margin-bottom: 12px;
  display: block;
  color: #3498db;
}

.action-card span {
  font-weight: 600;
  font-size: 1.1rem;
}

/* 最近行李記錄區 */
.recent-bags {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-top: 30px;
}

.recent-bags h2 {
  color: #2c3e50;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.5rem;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .stats {
    grid-template-columns: 1fr;
  }
  
  .actions {
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  }
}
</style>