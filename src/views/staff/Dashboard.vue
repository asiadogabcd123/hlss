<template>
  <div class="dashboard-container">
    <!-- 標題區 -->
    <div class="header">
      <div class="header-top">
        <div class="header-title">
          <i class="fas fa-luggage-cart header-icon"></i>
          <h1>行李管理控制台</h1>
        </div>
        <div class="header-actions">
          <button @click="handleLogout" class="logout-btn">
            <i class="fas fa-sign-out-alt"></i> 登出
          </button>
        </div>
      </div>
    </div>

    <!-- 主要內容區 -->
    <div class="main-content">
      <!-- 統計卡片區 -->
      <div class="stats-section">
        <h2 class="section-title"><i class="fas fa-chart-bar"></i> 行李統計概覽</h2>
        <div class="stats-grid">
          <!-- 當前寄存卡片（可點擊查看詳情） -->
          <div class="stat-card-wrapper" @click.stop="openDetailDialog('current')">
            <div class="card-overlay"></div>
            <StatCard 
              icon="suitcase" 
              title="當前寄存" 
              :value="stats.current" 
              color="#3498db" 
              :loading="loading"
            />
            <div class="view-detail-tip">
              <i class="fas fa-angle-right"></i> 查看詳情
            </div>
          </div>
          <!-- 今日寄存卡片（可點擊查看詳情） -->
          <div class="stat-card-wrapper" @click.stop="openDetailDialog('todayIn')">
            <div class="card-overlay"></div>
            <StatCard 
              icon="sign-in-alt" 
              title="今日寄存" 
              :value="stats.todayIn" 
              color="#2ecc71" 
              :loading="loading"
            />
            <div class="view-detail-tip">
              <i class="fas fa-angle-right"></i> 查看詳情
            </div>
          </div>
          <!-- 今日取件卡片（可點擊查看詳情） -->
          <div class="stat-card-wrapper" @click.stop="openDetailDialog('todayOut')">
            <div class="card-overlay"></div>
            <StatCard 
              icon="sign-out-alt" 
              title="今日取件" 
              :value="stats.todayOut" 
              color="#e74c3c" 
              :loading="loading"
            />
            <div class="view-detail-tip">
              <i class="fas fa-angle-right"></i> 查看詳情
            </div>
          </div>
        </div>
      </div>

      <!-- 行李管理模塊（嵌入式） -->
      <div class="embedded-section">
        <LuggageManagement />
      </div>
    </div>

    <!-- 統計詳情彈窗 -->
    <ElDialog
      v-model="detailDialogVisible"
      :title="`${ getTypeLabel(currentDetailType)}行李詳情`"
      :width="'90%'"
      :before-close="handleDialogClose"
      destroy-on-close
      draggable
      custom-class="enhanced-dialog"
    >
      <!-- 彈窗頂部：搜尋與刷新 -->
      <div class="detail-header">
        <div class="detail-filter-group">
          <!-- 關鍵字搜尋 -->
          <ElInput
            v-model="searchKeyword"
            placeholder="輸入行李編號/使用者名稱搜尋..."
            prefix-icon="Search"
            :style="{ width: '300px', marginRight: '16px' }"
            class="enhanced-input"
          />
        </div>
      </div>

      <!-- 詳情列表 -->
      <ElTable
        :data="filteredDetailList"
        :loading="detailLoading"
        border
        stripe
        :header-cell-style="headerCellStyle"
        :row-style="rowStyle"
        :row-hover-style="rowHoverStyle"
        style="width: 100%; margin-top: 16px;"
        class="enhanced-table"
        :cell-style="{ padding: '12px 8px' }"
      >
        <!-- 行李編號 -->
        <ElTableColumn
          prop="orderNo"
          label="行李編號"
          align="center"
          min-width="120"
        >
          <template #default="scope">
            <span class="order-no">{{ scope.row.orderNo }}</span>
          </template>
        </ElTableColumn>
        
        <!-- 使用者名稱 -->
        <ElTableColumn
          prop="guestName"
          label="客人名稱"
          align="center"
          min-width="140"
        >
          <template #default="scope">
            <div class="user-info">
              <i class="fas fa-user-circle user-icon"></i>
              <span>{{ scope.row.guestName }}</span>
            </div>
          </template>
        </ElTableColumn>

        <!-- 電話（所有類型都顯示） -->
        <ElTableColumn
          prop="phone"
          label="電話"
          align="center"
          min-width="140"
        >
          <template #default="scope">
            <div>
              {{ scope.row.phone || '無' }}
            </div>
          </template>
        </ElTableColumn>

        <!-- 房間號（所有類型都顯示） -->
        <ElTableColumn
          prop="roomNumber"
          label="房間號"
          align="center"
          min-width="100"
        >
          <template #default="scope">
            <div>
              {{ scope.row.roomNumber || '無' }}
            </div>
          </template>
        </ElTableColumn>

        <!-- 行李數量（所有類型都顯示） -->
        <ElTableColumn
          prop="luggageCount"
          label="行李數量"
          align="center"
          min-width="100"
        >
          <template #default="scope">
            <div>
              {{ scope.row.luggageCount || 1 }}
            </div>
          </template>
        </ElTableColumn>
        
        <!-- 寄存時間（所有類型都顯示） -->
        <ElTableColumn
          prop="checkinTime"
          label="寄存時間"
          align="center"
          min-width="180"
        >
          <template #default="scope">
            <div class="time-info">
              <i class="fas fa-clock time-icon"></i>
              <span>{{ scope.row.checkinTime }}</span>
            </div>
          </template>
        </ElTableColumn>
        
        <!-- 取件時間（今日取件顯示） -->
        <ElTableColumn
          v-if="currentDetailType === 'todayOut'"
          prop="checkoutTime"
          label="取件時間"
          align="center"
          min-width="180"
        >
          <template #default="scope">
            <div class="time-info">
              <i class="fas fa-clock time-icon"></i>
              <span>{{ scope.row.checkoutTime }}</span>
            </div>
          </template>
        </ElTableColumn>

        <!-- 身份證號碼（僅今日取件顯示） -->
        <ElTableColumn
          v-if="currentDetailType === 'todayOut'"
          prop="idNumber"
          label="身份證號碼"
          align="center"
          min-width="180"
        >
          <template #default="scope">
            <div>
              {{ scope.row.idNumber || '無' }}
            </div>
          </template>
        </ElTableColumn>
        
        <!-- 行李狀態 -->
        <ElTableColumn
          prop="status"
          label="行李狀態"
          align="center"
          min-width="220"
        >
          <template #default="scope">
            <div class="status-badge">
              <i :class="getStatusIcon(scope.row)"></i>
              <ElTag
                :type="getStatusType(scope.row)"
                :color="getStatusColor(scope.row)"
                class="status-tag"
              >
                {{ getStatusLabel(scope.row) }}
              </ElTag>
              
              <ElButton
              v-if="isExpired(scope.row)"
              size="mini"
              type="danger"
              @click.stop="handleDelete(scope.row)"
              class="delete-btn"
              :loading="deleteLoading[scope.row.id]"
            >
              <i class="fas fa-trash-alt"></i>
            </ElButton>

            </div>
          </template>
        </ElTableColumn>
      </ElTable>

      <!-- 空數據提示 -->
      <div v-if="!detailLoading && filteredDetailList.length === 0" class="empty-detail-tip enhanced-empty-state">
        <div class="empty-icon-container">
          <i class="fas fa-box-open"></i>
        </div>
        <h3 class="empty-title">{{ getTypeLabel(currentDetailType) }}暫無數據</h3>
      </div>
    </ElDialog>

    <!-- 操作反饋提示 -->
    <div v-if="feedbackMessage" class="feedback-message" :class="feedbackType">
      <i :class="feedbackIcon"></i> {{ feedbackMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElDialog, ElTable, ElTableColumn, ElInput, ElTag, ElButton, ElMessageBox,   } from 'element-plus'

import StatCard from '@/components/StatCard.vue'
import LuggageManagement from '@/views/staff/LuggageManagement.vue' 
import luggageApi from '@/api/luggage' 

// 路由與狀態管理
const router = useRouter()
const authStore = useAuthStore()

// 日期格式化
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 基礎狀態變量
const loading = ref(true) // 儀表板加載狀態
const stats = ref({       // 統計數據
  current: 0,    // 當前寄存數
  todayIn: 0,    // 今日寄存數
  todayOut: 0    // 今日取件數
})

// 詳情彈窗相關
const detailDialogVisible = ref(false)  // 詳情彈窗顯示狀態
const currentDetailType = ref('')       // 當前查看類型：current/todayIn/todayOut
const detailList = ref([])              // 詳情列表數據
const detailLoading = ref(false)        // 詳情加載狀態
const searchKeyword = ref('')           // 詳情搜尋關鍵字
const detailDate = ref(formatDate(new Date())) // 預設今日
const deleteLoading = reactive({})      // 刪除按鈕加載狀態

// 反饋提示相關
const feedbackMessage = ref('')  // 反饋消息內容
const feedbackType = ref('info') // 反饋類型：info/success/error

// 計算屬性
// 反饋提示圖標
const feedbackIcon = computed(() => {
  if (feedbackType.value === 'success') return 'fas fa-check-circle'
  if (feedbackType.value === 'error') return 'fas fa-exclamation-circle'
  return 'fas fa-info-circle'
})

// 過濾後的詳情列表（搜尋功能）
const filteredDetailList = computed(() => {
  if (!searchKeyword.value.trim()) return detailList.value
  const keyword = searchKeyword.value.trim().toLowerCase()
  return detailList.value.filter(item => {
    return (
      (item.orderNo && item.orderNo.toLowerCase().includes(keyword)) ||
      (item.guestName && item.guestName.toLowerCase().includes(keyword)) ||
      (item.phone && item.phone.toLowerCase().includes(keyword)) ||
      (item.roomNumber && item.roomNumber.toLowerCase().includes(keyword))
    )
  })
})

// 表格樣式計算屬性
const headerCellStyle = computed(() => ({
  background: '#f0f5ff',
  color: '#1e40af',
  fontWeight: '600',
  padding: '12px 8px',
  borderBottom: '2px solid #3b82f6'
}))

const rowStyle = computed(() => ({
  transition: 'all 0.2s ease-in-out'
}))

const rowHoverStyle = computed(() => ({
  background: '#eff6ff',
  transform: 'translateZ(0)',
  boxShadow: '0 2px 10px rgba(59, 130, 246, 0.1)'
}))

// 工具方法
// 顯示操作反饋
const showFeedback = (message, type = 'info') => {
  feedbackMessage.value = message
  feedbackType.value = type
  // 3秒後自動隱藏
  setTimeout(() => {
    feedbackMessage.value = ''
  }, 3000)
}

// 獲取統計類型中文標籤
const getTypeLabel = (type) => {
  const labelMap = {
    current: '當前寄存',
    todayIn: '今日寄存',
    todayOut: '今日取件'
  }
  return labelMap[type] || type
}

// 判斷是否為過期狀態
const isExpired = (item) => {
  return item.status.toLowerCase() === 'expired'
}

// 獲取行李狀態中文標籤
// 新增：在今日取件中，若有身份證號碼則顯示特殊狀態
const getStatusLabel = (item) => {
  const { status, idNumber } = item;
  const normalizedStatus = status.toLowerCase();
  
  // 今日取件且有身份證號碼，顯示特殊狀態
  if (currentDetailType.value === 'todayOut' && normalizedStatus === 'retrieved' && idNumber) {
    return '已取件(遺失憑證取件)';
  }
  
  const statusMap = {
    'stored': '已寄存',
    'retrieved': '已取件',
    'expired': '已過期'
  }
  return statusMap[normalizedStatus] || status;
}

// 獲取狀態對應的圖標
const getStatusIcon = (item) => {
  const { status, idNumber } = item;
  const normalizedStatus = status.toLowerCase();
  
  // 特殊取件狀態使用不同圖標
  if (currentDetailType.value === 'todayOut' && normalizedStatus === 'retrieved' && idNumber) {
    return 'fas fa-id-card-alt status-icon special-picked-icon';
  }
  
  const iconMap = {
    'stored': 'fas fa-check-circle status-icon stored-icon',
    'retrieved': 'fas fa-sign-out-alt status-icon picked-icon',
    'expired': 'fas fa-exclamation-triangle status-icon overdue-icon',
  }
  return iconMap[normalizedStatus] || 'fas fa-question-circle status-icon';
}

// 獲取狀態標籤類型
const getStatusType = (item) => {
  const { status, idNumber } = item;
  const normalizedStatus = status.toLowerCase();
  
  // 特殊取件狀態使用不同類型
  if (currentDetailType.value === 'todayOut' && normalizedStatus === 'retrieved' && idNumber) {
    return 'warning';
  }
  
  const typeMap = {
    'stored': 'success',
    'retrieved': 'info',
    'expired': 'danger',
  }
  return typeMap[normalizedStatus] || 'default';
}

// 獲取狀態標籤顏色（優化後的顏色方案）
const getStatusColor = (item) => {
  const { status, idNumber } = item;
  const normalizedStatus = status.toLowerCase();
  
  // 特殊取件狀態使用不同顏色
  if (currentDetailType.value === 'todayOut' && normalizedStatus === 'retrieved' && idNumber) {
    return '#f97316'; // 橙色 - 用於特殊取件狀態
  }
  
  // 優化後的顏色方案，更加協調美觀
  const colorMap = {
    'stored': '#16a34a',      // 深綠色 - 已寄存
    'retrieved': '#2563eb',    // 深藍色 - 已取件
    'expired': '#dc2626',      // 深紅色 - 逾期未取
  }
  return colorMap[normalizedStatus] || '#6b7280';
}

// 業務方法
// 登出處理
const handleLogout = async () => {
  try {
    await authStore.logout()
    router.push('/')
  } catch (error) {
    console.error('登出失敗:', error)
    showFeedback('登出失敗，請重試', 'error')
  }
}

// 關閉彈窗處理
const handleDialogClose = () => {
  detailDialogVisible.value = false
}

// 加載儀表板統計數據
const loadDashboardData = async () => {
  try {
    loading.value = true
    const { data } = await luggageApi.getStats()
    stats.value = {
      current: data.currentStorage || 0,
      todayIn: data.todayCheckin || 0,
      todayOut: data.todayCheckout || 0
    }
    
    if (data.recentBags && currentDetailType.value === 'current') {
      detailList.value = data.recentBags.map(formatLuggageItem)
    }
  } catch (error) {
    console.error('獲取儀表板統計數據失敗:', error)
    showFeedback('統計數據加載失敗', 'error')
  } finally {
    loading.value = false
  }
}

// 加載詳情列表數據
const loadDetailData = async (type) => {
  try {
    detailLoading.value = true
    let response
    switch (type) {
      case 'current':
        response = await luggageApi.getAllCurrentLuggage()
        detailList.value = response.data.map(formatLuggageItem)
        break
      case 'todayIn':
        response = await luggageApi.getTodayStoredLuggage(detailDate.value.trim());
        detailList.value = response.data.map(formatLuggageItem)
        break
      case 'todayOut':
        response = await luggageApi.getTodayRetrievedLuggage(detailDate.value.trim());
        detailList.value = response.data.map(formatLuggageItem)
        break
      default:
        detailList.value = []
    }
  } catch (error) {
    console.error(`獲取${getTypeLabel(type)}詳情失敗:`, error)
    showFeedback(`${getTypeLabel(type)}詳情加載失敗`, 'error')
    detailList.value = []
  } finally {
    detailLoading.value = false
  }
}

// 格式化行李數據
const formatLuggageItem = (item) => {
  return {
    ...item,
    // 處理新增字段
    phone: item.phone || '無',
    roomNumber: item.roomNumber || '無',
    luggageCount: item.luggageCount || 1,
    idNumber: item.idNumber || '',
    // 調整時間格式
    checkinTime: item.checkinTime ? new Date(item.checkinTime).toLocaleString() : '無',
    checkoutTime: item.checkoutTime ? new Date(item.checkoutTime).toLocaleString() : '未取件',
    // 保持訂單編號格式
    orderNo: item.id ? `LUG-${item.id}` : '無'
  }
}

// 打開詳情彈窗
const openDetailDialog = async (type) => {
  try {
    currentDetailType.value = type;
    detailDialogVisible.value = true;
    await loadDetailData(type);
  } catch (error) {
    console.error('打開詳情彈窗失敗：', error);
    showFeedback('打開詳情失敗，請重試', 'error');
    detailDialogVisible.value = false;
  }
}

// 處理過期行李刪除
const handleDelete = async (item) => {
  try {
    // 顯示確認對話框
    const confirmResult = await ElMessageBox.confirm(
      `確定要刪除這條過期行李記錄嗎？\n\n行李編號: ${item.orderNo}\n客人姓名: ${item.guestName}\n寄存時間: ${item.checkinTime}\n\n此操作代表員工已清理該行李，刪除後將無法恢復，請謹慎操作！`,
      '確認刪除過期行李',
      {
        confirmButtonText: '確認刪除',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }
    )

    if (confirmResult === 'confirm') {
      // 設置刪除按鈕加載狀態
      deleteLoading[item.id] = true
      
      // 調用API刪除過期行李記錄
      await luggageApi.markAsDeleted(item.id)
      
      // 從本地列表中移除該記錄
      detailList.value = detailList.value.filter(luggage => luggage.id !== item.id)
      
      // 顯示成功反饋
      showFeedback(`已成功刪除過期行李記錄：${item.orderNo}`, 'success')
      
      // 重新加載統計數據，保持數據一致性
      loadDashboardData()
    }
  } catch (error) {
    if (error !== 'cancel') { // 排除用戶取消的情況
      console.error('刪除過期行李記錄失敗:', error)
      showFeedback('刪除失敗，請重試', 'error')
    }
  } finally {
    // 重置刪除按鈕加載狀態
    deleteLoading[item.id] = false
  }
}

// 生命週期鉤子
onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 25px;
  max-width: 1600px;
  width: 100%;
  margin: 0 auto;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
}


.header {
  margin-bottom: 30px;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 15px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(52, 152, 219, 0.2);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-title h1 {
  color: #2c3e50;
  font-size: 2rem;
  margin: 0;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.header-icon {
  font-size: 2.2rem;
  color: #3498db;
  background: rgba(52, 152, 219, 0.1);
  padding: 12px;
  border-radius: 12px;
}

.logout-btn {
  background: linear-gradient(145deg, #e74c3c, #c0392b);
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(231, 76, 60, 0.3);
}

.logout-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(231, 76, 60, 0.4);
}

.logout-btn i {
  font-size: 1.2rem;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.section-title {
  color: #2c3e50;
  font-size: 1.5rem;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(52, 152, 219, 0.15);
}

.stats-section {
  background: white;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 25px;
}

.stat-card-wrapper {
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 16px;
  overflow: hidden;
  min-height: 100px;
  display: flex;
  flex-direction: column;
  z-index: 10;
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, rgba(0,0,0,0) 0%, rgba(0,0,0,0.05) 100%);
}

.stat-card-wrapper:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
}

.stat-card-wrapper > * {
  position: relative;
  z-index: 2;
}

.view-detail-tip {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px 0;
  text-align: center;
  font-size: 0.9rem;
  font-weight: 500;
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  z-index: 3;
}

.stat-card-wrapper:hover .view-detail-tip {
  opacity: 1;
}

.embedded-section {
  background: white;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
  margin-top: 30px;
  border: 2px solid #3498db;
  position: relative;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-filter-group {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.feedback-message {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  padding: 16px 40px;
  border-radius: 12px;
  color: white;
  font-weight: 600;
  font-size: 1.1rem;
  z-index: 3000;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  animation: fadeSlideInOut 3s forwards;
  display: flex;
  align-items: center;
  gap: 15px;
  max-width: 90%;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.feedback-message.info {
  background: linear-gradient(90deg, #3498db, #2980b9);
}

.feedback-message.success {
  background: linear-gradient(90deg, #2ecc71, #27ae60);
}

.feedback-message.error {
  background: linear-gradient(90deg, #e74c3c, #c0392b);
}

@keyframes fadeSlideInOut {
  0% {
    opacity: 0;
    bottom: -50px;
    transform: translateX(-50%) scale(0.95);
  }
  15% {
    opacity: 1;
    bottom: 30px;
    transform: translateX(-50%) scale(1);
  }
  85% {
    opacity: 1;
    bottom: 30px;
    transform: translateX(-50%) scale(1);
  }
  100% {
    opacity: 0;
    bottom: -50px;
    transform: translateX(-50%) scale(0.95);
  }
}

.enhanced-dialog {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.enhanced-dialog .el-dialog__header) {
  background: #f0f5ff;
  padding: 16px 24px;
  border-bottom: 1px solid #e0e7ff;
}

:deep(.enhanced-dialog .el-dialog__title) {
  color: #1e40af;
  font-size: 18px;
  font-weight: 600;
}

.enhanced-input {
  border-radius: 8px !important;
  transition: all 0.2s ease;
}

.enhanced-input:focus-within {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2) !important;
}

.enhanced-table {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e0e7ff;
}

/* 表格內容樣式增強 */
.order-no {
  color: #1e40af;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.user-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.user-icon {
  color: #60a5fa;
  font-size: 14px;
}

.time-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #4b5563;
}

.time-icon {
  font-size: 14px;
  color: #94a3b8;
}

/* 狀態樣式增強 */
.status-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.status-icon {
  font-size: 14px;
}

.stored-icon {
  color: #16a34a; /* 深綠色 */
}

.picked-icon {
  color: #2563eb; /* 深藍色 */
}

.special-picked-icon {
  color: #f97316; /* 橙色 - 特殊取件狀態 */
  animation: pulse 2s infinite;
}

.overdue-icon {
  color: #dc2626; /* 深紅色 */
  animation: pulse 2s infinite;
}

.status-tag {
  border-radius: 12px;
  padding: 3px 10px;
  font-weight: 500;
  font-size: 0.85rem;
}

/* 刪除按鈕樣式 */
.delete-btn {
  margin-left: 8px;
  padding: 2px 8px;
  transition: all 0.2s ease;
}

.delete-btn:hover {
  transform: scale(1.05);
}

/* 空狀態樣式增強 */
.enhanced-empty-state {
  padding: 80px 20px;
  text-align: center;
  background-color: #f8fafc;
  border-radius: 8px;
  margin-top: 16px;
}

.empty-icon-container {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  margin-left: auto;
  margin-right: auto;
}

.empty-icon-container i {
  font-size: 40px;
  color: #60a5fa;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #334155;
  margin: 0 0 8px 0;
}

/* 動畫效果 */
@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.6; }
  100% { opacity: 1; }
}

/* 響應式調整 */
@media (max-width: 1600px) {
  .dashboard-container {
    padding: 25px 40px;
  }
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  }
}

@media (max-width: 992px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .header-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }

  :deep(.el-dialog) {
    width: 95% !important;
    margin: 0 auto !important;
  }
  
  .detail-filter-group {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  :deep(.el-input) {
    width: 100% !important;
  }
}

@media (max-width: 480px) {
  .dashboard-container {
    padding: 15px;
  }
  
  .header-title h1 {
    font-size: 1.6rem;
  }
  
  .logout-btn span {
    display: none;
  }
  
  .logout-btn {
    padding: 12px;
    width: 50px;
    height: 50px;
    justify-content: center;
    border-radius: 12px;
  }
  
  .section-title {
    font-size: 1.3rem;
  }
  
  .embedded-section {
    padding: 15px;
  }
  
  /* 詳情表格在小屏幕下的調整 */
  :deep(.el-table .el-table__cell) {
    padding: 8px 4px !important;
  }
  
  /* 狀態列調整 */
  .status-badge {
    flex-wrap: wrap;
  }
  
  .delete-btn {
    margin-top: 4px;
    margin-left: 0;
  }
}
</style>
