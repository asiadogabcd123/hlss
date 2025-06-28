<template>
  <div class="dashboard-container">
    <div class="header">
      <h1><i class="fas fa-tachometer-alt"></i> 行李管理控制台</h1>
      <div class="stats">
        <stat-card 
          icon="suitcase" 
          title="當前寄存" 
          :value="stats.current" 
          color="#3498db" />
        <stat-card 
          icon="sign-in-alt" 
          title="今日寄存" 
          :value="stats.todayIn" 
          color="#2ecc71" />
        <stat-card 
          icon="sign-out-alt" 
          title="今日取件" 
          :value="stats.todayOut" 
          color="#e74c3c" />
      </div>
    </div>

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

    <div class="recent-bags">
      <h2><i class="fas fa-clock"></i> 最近操作</h2>
      <bag-list :bags="recentBags" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import StatCard from '@/components/StatCard.vue'
import BagList from '@/components/BagList.vue'

const stats = ref({
  current: 0,
  todayIn: 0,
  todayOut: 0
})

const recentBags = ref([
  {
    id: 'BAG-1001',
    guestName: '王小明',
    time: new Date(),
    status: '已寄存',
    area: 'A區'
  },
  {
    id: 'BAG-1002',
    guestName: '陳大文',
    time: new Date(Date.now() - 3600000),
    status: '已取件',
    area: 'B區'
  }
])

onMounted(() => {
  // 模擬載入數據
  setTimeout(() => {
    stats.value = {
      current: 24,
      todayIn: 12,
      todayOut: 8
    }
  }, 500)
})
</script>

<style scoped>
/* 基础布局样式 */
.dashboard-container {
  padding: 20px;
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
}

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

/* 统计卡片容器 */
.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 25px;
}

/* 快速操作按钮 */
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
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  color: #2c3e50;
  border: none;
  cursor: pointer;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.15);
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

/* 最近行李记录 */
.recent-bags {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
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

/* 二维码生成模态框 */
.qr-generator-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(3px);
}

.modal-content {
  background: white;
  border-radius: 15px;
  padding: 30px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
  animation: modalFadeIn 0.3s ease;
}

@keyframes modalFadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.modal-content h2 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

/* 二维码类型选择 */
.qr-type-selector {
  display: flex;
  gap: 10px;
  margin: 25px 0;
}

.qr-type-selector button {
  flex: 1;
  padding: 15px;
  border: 2px solid #ddd;
  border-radius: 8px;
  background: none;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.qr-type-selector button:hover {
  border-color: #3498db;
}

.qr-type-selector button.active {
  border-color: #3498db;
  background: #e3f2fd;
  font-weight: 600;
}

.qr-type-selector button i {
  font-size: 1.2rem;
}

/* 二维码预览区域 */
.qr-preview-container {
  text-align: center;
  margin-top: 20px;
}

.qr-display {
  display: inline-block;
  background: white;
  padding: 25px;
  border-radius: 10px;
  border: 1px solid #eee;
  margin-bottom: 25px;
  box-shadow: 0 3px 10px rgba(0,0,0,0.05);
}

.qr-meta {
  text-align: left;
  margin-top: 20px;
  font-size: 0.95rem;
  color: #555;
  background: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
}

.qr-meta p {
  margin: 8px 0;
  display: flex;
  align-items: center;
}

.qr-meta i {
  margin-right: 10px;
  color: #3498db;
  width: 20px;
  text-align: center;
}

/* 操作按钮 */
.qr-actions {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 25px;
}

.btn {
  padding: 12px 20px;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 1rem;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
  transform: translateY(-2px);
}

.btn-secondary {
  background: #2ecc71;
  color: white;
}

.btn-secondary:hover {
  background: #27ae60;
  transform: translateY(-2px);
}

.btn-warning {
  background: #e74c3c;
  color: white;
}

.btn-warning:hover {
  background: #c0392b;
  transform: translateY(-2px);
}

.btn-generate {
  background: #9b59b6;
  color: white;
  padding: 15px 30px;
  font-size: 1.1rem;
  margin: 20px auto;
  display: block;
}

.btn-generate:hover {
  background: #8e44ad;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(155, 89, 182, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats {
    grid-template-columns: 1fr;
  }
  
  .actions {
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  }
  
  .modal-content {
    padding: 20px;
  }
  
  .qr-type-selector {
    flex-direction: column;
  }
  
  .qr-actions {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
    justify-content: center;
  }
}

@media print {
  body * {
    visibility: hidden;
  }
  .qr-display, .qr-display * {
    visibility: visible;
  }
  .qr-display {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: white;
    border: none;
    box-shadow: none;
  }
}
</style>