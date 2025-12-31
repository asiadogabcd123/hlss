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
          <!-- 當前寄存卡片 -->
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
          <!-- 今日寄存卡片 -->
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
          <!-- 今日取件卡片 -->
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
          <!-- 歷史記錄卡片 -->
          <div class="stat-card-wrapper" @click.stop="openDetailDialog('history')">
            <div class="card-overlay"></div>
            <StatCard 
              icon="history" 
              title="歷史記錄" 
              color="#9b59b6" 
              :loading="loading"
              :style="{ height: '86px' }"  
            />
            <div class="view-detail-tip">
              <i class="fas fa-angle-right"></i> 查詢歷史
            </div>
          </div>
        </div>
      </div>

      <!-- 行李管理模塊 -->
      <div class="embedded-section">
        <LuggageManagement />
      </div>
    </div>

    <!-- 統計詳情彈窗 -->
    <ElDialog
      v-model="detailDialogVisible"
      :title="getDialogTitle()"
      :width="'90%'"
      :before-close="handleDialogClose"
      destroy-on-close
      draggable
      custom-class="enhanced-dialog"
    >
      <!-- 彈窗頂部：搜尋、日期篩選、狀態篩選與Excel匯出 -->
      <div class="detail-header">
        <div class="detail-filter-group">
          <!-- 關鍵字搜尋 -->
          <ElInput
            v-model="searchKeyword"
            placeholder="輸入行李編號/使用者名稱/存放位置搜尋..."
            :prefix-icon="Search"
            :style="{ width: '300px', marginRight: '16px' }"
            class="enhanced-input"
          />
          
          <!-- 日期範圍選擇器（僅歷史記錄顯示） -->
          <div v-if="currentDetailType === 'history'" class="date-filter-container">
            <ElDatePicker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="開始日期"
              end-placeholder="結束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              :shortcuts="dateShortcuts"
              :style="{ width: '320px' }"
              @change="handleFilterChange"
              class="enhanced-datepicker"
              popper-class="enhanced-datepicker-popper"
              :clearable="false"
            />
            
            <!-- 快捷日期按鈕組 -->
            <div class="date-shortcut-buttons">
              <ElButton 
                size="mini" 
                @click="setDateRange('today')"
                :class="{ 'active': currentShortcut === 'today' }"
              >
                今天
              </ElButton>
              <ElButton 
                size="mini" 
                @click="setDateRange('yesterday')"
                :class="{ 'active': currentShortcut === 'yesterday' }"
              >
                昨天
              </ElButton>
              <ElButton 
                size="mini" 
                @click="setDateRange('week')"
                :class="{ 'active': currentShortcut === 'week' }"
              >
                本週
              </ElButton>
              <ElButton 
                size="mini" 
                @click="setDateRange('month')"
                :class="{ 'active': currentShortcut === 'month' }"
              >
                本月
              </ElButton>
              <ElButton 
                size="mini" 
                @click="setDateRange('year')"
                :class="{ 'active': currentShortcut === 'year' }"
              >
                本年
              </ElButton>
            </div>
          </div>
          
          <!-- 行李狀態篩選（多選） -->
          <ElSelect
            v-if="currentDetailType === 'history'"
            v-model="selectedStatus"
            placeholder="選擇行李狀態"
            :style="{ width: '250px', marginRight: '16px' }"
            @change="handleFilterChange"
            class="enhanced-select"
            multiple
            collapse-tags
            collapse-tags-tooltip
            clearable
          >
            <ElOption label="已寄存" value="stored" />
            <ElOption label="已取件" value="retrieved" />
            <ElOption label="已過期" value="expired" />
          </ElSelect>
          
          <!-- Excel匯出按鈕 -->
          <ElButton
            type="success"
            :icon="Download"
            @click="exportToExcel"
            :loading="exportLoading"
            class="export-btn"
          >
            匯出Excel
          </ElButton>
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

        <!-- 存放位置 -->
        <ElTableColumn
          prop="storageLocation"
          label="存放位置"
          align="center"
          min-width="160"
        >
          <template #default="scope">
            <div class="storage-location">
              <i class="fas fa-map-marker-alt location-icon"></i>
              <span>{{ scope.row.storageLocation || '無' }}</span>
            </div>
          </template>
        </ElTableColumn>

        <!-- 電話 -->
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

        <!-- 房間號 -->
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

        <!-- 行李數量 -->
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
        
        <!-- 寄存時間 -->
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
        
        <!-- 取件時間（取件/歷史記錄顯示） -->
        <ElTableColumn
          v-if="currentDetailType === 'todayOut' || currentDetailType === 'history'"
          prop="checkoutTime"
          label="取件時間"
          align="center"
          min-width="180"
        >
          <template #default="scope">
            <div class="time-info">
              <i class="fas fa-clock time-icon"></i>
              <span>{{ scope.row.checkoutTime || '未取件' }}</span>
            </div>
          </template>
        </ElTableColumn>
        
        <!-- 應收費用 -->
        <ElTableColumn
          label="應收費用" 
          align="center"
          min-width="120"
        >
          <template #default="scope">
            <div :class="{ 'overdue-fee': calculateOverdueFee(scope.row) > 0 }">
              {{ calculateOverdueFee(scope.row) > 0 ? 'MOP ' + calculateOverdueFee(scope.row).toFixed(0) : '0' }}
            </div>
          </template>
        </ElTableColumn>
        
        <!-- 身份證號碼（取件/歷史記錄顯示） -->
        <ElTableColumn
          v-if="currentDetailType === 'todayOut' || currentDetailType === 'history'"
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

      <!-- 所有費用總計 -->
      <div v-if="filteredDetailList.length > 0" class="total-fee-section">
        <div class="total-fee-label">所有費用總計：</div>
        <div class="total-fee-value">MOP {{ calculateTotalFee() }}</div>
      </div>

      <!-- 空數據提示 -->
      <div v-if="!detailLoading && filteredDetailList.length === 0" class="empty-detail-tip enhanced-empty-state">
        <div class="empty-icon-container">
          <i class="fas fa-box-open"></i>
        </div>
        <h3 class="empty-title">{{ getEmptyTipText() }}</h3>
        <p class="empty-subtitle" v-if="currentDetailType === 'history'">可調整日期範圍、狀態或搜尋條件查詢更多記錄</p>
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
import * as XLSX from 'xlsx'
import { ElDialog, ElTable, ElTableColumn, ElInput, ElTag, ElButton, ElMessageBox, ElDatePicker, ElSelect, ElOption } from 'element-plus'
import dayjs from 'dayjs'


import StatCard from '@/components/StatCard.vue'
import LuggageManagement from '@/views/staff/LuggageManagement.vue' 
import luggageApi from '@/api/luggage' 

// 路由與狀態管理
const router = useRouter()
const authStore = useAuthStore()

// 日期格式化工具
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 獲取指定日期範圍
const getDateRange = (type) => {
  const today = new Date()
  const start = new Date()
  const end = new Date()
  
  switch (type) {
    case 'today':{
      // 今天
      start.setHours(0, 0, 0, 0)
      end.setHours(23, 59, 59, 999)
      break
    }
    case 'yesterday':{
      // 昨天
      start.setDate(today.getDate() - 1)
      start.setHours(0, 0, 0, 0)
      end.setDate(today.getDate() - 1)
      end.setHours(23, 59, 59, 999)
      break
    }
    case 'week':{
      // 本週 (禮拜一至今天)
      const day = today.getDay() || 7 
      start.setDate(today.getDate() - (day - 1))
      start.setHours(0, 0, 0, 0)
      end.setHours(23, 59, 59, 999)
      break
    }
    case 'month':{
      // 本月
      start.setDate(1)
      start.setHours(0, 0, 0, 0)
      end.setMonth(today.getMonth() + 1, 0)
      end.setHours(23, 59, 59, 999)
      break
    }
    case 'year':{
      // 本年
      start.setMonth(0, 1)
      start.setHours(0, 0, 0, 0)
      end.setMonth(11, 31)
      end.setHours(23, 59, 59, 999)
      break
    }
    default:
      return [formatDate(today), formatDate(today)]
  }
  
  return [formatDate(start), formatDate(end)]
}

// 基礎狀態變量
const loading = ref(true)
const stats = ref({
  current: 0,
  todayIn: 0,
  todayOut: 0,
  historyCount: 0
})
const dateRange = ref(getDateRange('today')) // 默認為今天
const exportLoading = ref(false)
const currentShortcut = ref('today') // 追蹤當前選擇的快捷日期

// 所有行李數據，用於計算總應收費用
const allLuggageData = ref([])

// 日期選擇器快捷選項
const dateShortcuts = [
  { text: '今天', value: () => getDateRange('today') },
  { text: '昨天', value: () => getDateRange('yesterday') },
  { text: '本週', value: () => getDateRange('week') },
  { text: '本月', value: () => getDateRange('month') },
  { text: '本季度', value: () => getDateRange('quarter') },
  { text: '本年', value: () => getDateRange('year') }
]

// 狀態篩選變量（多選）
const selectedStatus = ref([])

// 詳情彈窗相關
const detailDialogVisible = ref(false)
const currentDetailType = ref('')
const detailList = ref([])
const detailLoading = ref(false)
const searchKeyword = ref('')
const deleteLoading = reactive({})

// 反饋提示相關
const feedbackMessage = ref('')
const feedbackType = ref('info')

// 計算屬性：反饋提示圖標
const feedbackIcon = computed(() => {
  if (feedbackType.value === 'success') return 'fas fa-check-circle'
  if (feedbackType.value === 'error') return 'fas fa-exclamation-circle'
  return 'fas fa-info-circle'
})

// 計算屬性：過濾後的詳情列表
const filteredDetailList = computed(() => {
  let result = [...detailList.value]
  
  // 1. 關鍵字搜尋
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    result = result.filter(item => {
      return (
        (item.orderNo && item.orderNo.toLowerCase().includes(keyword)) ||
        (item.guestName && item.guestName.toLowerCase().includes(keyword)) ||
        (item.phone && item.phone.toLowerCase().includes(keyword)) ||
        (item.roomNumber && item.roomNumber.toLowerCase().includes(keyword)) ||
        (item.storageLocation && item.storageLocation.toLowerCase().includes(keyword)) ||
        (item.idNumber && item.idNumber.toLowerCase().includes(keyword))
      )
    })
  }
  
  // 2. 歷史記錄-狀態篩選
  if (currentDetailType.value === 'history' && selectedStatus.value.length > 0) {
    result = result.filter(item => {
      return selectedStatus.value.includes(item.status.toLowerCase())
    })
  }
  
  return result
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

// 工具方法：顯示操作反饋
const showFeedback = (message, type = 'info') => {
  feedbackMessage.value = message
  feedbackType.value = type
  setTimeout(() => {
    feedbackMessage.value = ''
  }, 3000)
}

// 工具方法：獲取彈窗標題
const getDialogTitle = () => {
  const labelMap = {
    current: '當前寄存行李詳情',
    todayIn: '今日寄存行李詳情',
    todayOut: '今日取件行李詳情',
    history: `行李歷史記錄查詢（${dateRange.value?.[0] || ''} 至 ${dateRange.value?.[1] || ''}）`
  }
  return labelMap[currentDetailType.value] || '行李詳情'
}

// 工具方法：獲取空數據提示文本
const getEmptyTipText = () => {
  const tipMap = {
    current: '當前暫無寄存行李',
    todayIn: '今日暫無寄存行李記錄',
    todayOut: '今日暫無取件行李記錄',
    history: `指定日期範圍內（${dateRange.value[0]} 至 ${dateRange.value[1]}）無相關記錄`
  }
  return tipMap[currentDetailType.value] || '暫無數據'
}

// 工具方法：獲取統計類型中文標籤
const getTypeLabel = (type) => {
  const labelMap = {
    current: '當前寄存',
    todayIn: '今日寄存',
    todayOut: '今日取件',
    history: '歷史記錄'
  }
  return labelMap[type] || type
}

// 判斷是否為過期狀態
const isExpired = (item) => {
  return item.status.toLowerCase() === 'expired'
}

// 計算過期費用（每件每天20元，免費3天）
const calculateOverdueFee = (item) => {
  // 檢查必要的寄存時間（使用原始ISO日期）
  if (!item.originalCheckinTime) {
    console.log(`行李ID: ${item.id} - 缺少寄存時間，費用為0`);
    return 0;
  }
  
  // 使用原始ISO日期解析
  const checkinTime = dayjs(item.originalCheckinTime);
  if (!checkinTime.isValid()) {
    console.log(`行李ID: ${item.id} - 無效的寄存時間: ${item.originalCheckinTime}`)
    return 0;
  }
  
  // 確定取件時間（使用原始ISO日期）
  const checkoutTime = item.originalCheckoutTime 
    ? dayjs(item.originalCheckoutTime) 
    : dayjs(); // 未取件則使用當前時間
    
  if (!checkoutTime.isValid()) {
    console.log(`行李ID: ${item.id} - 無效的取件時間: ${item.originalCheckoutTime}`)
    return 0;
  }
  
  // 計算天數差（使用毫秒差計算更精確）
  const checkinTimestamp = checkinTime.valueOf();
  const checkoutTimestamp = checkoutTime.valueOf();
  
  // 確保取件時間不早於寄存時間
  const effectiveCheckoutTimestamp = Math.max(checkoutTimestamp, checkinTimestamp);
  
  // 計算天數差（向上取整，不足一天按一天計算）
  const diffMs = effectiveCheckoutTimestamp - checkinTimestamp;
  const diffDays = diffMs / (1000 * 60 * 60 * 24);
  const storageDays = Math.ceil(diffDays);
  
  // 確保行李數量是有效數字且至少為1
  const luggageCount = Math.max(1, Number(item.luggageCount) || 1);
  const feePerDayPerLuggage = 20; // 每件每天20元
  
  // 免費3天，超過的天數才計算費用
  const freeDays = 3;
  const overdueDays = Math.max(0, storageDays - freeDays);
  
  const totalFee = overdueDays * feePerDayPerLuggage * luggageCount;
  
  // 調試日誌
  console.log(`
    行李ID: ${item.id}
    原始寄存時間(ISO): ${item.originalCheckinTime}
    解析後時間: ${checkinTime.format('YYYY-MM-DD HH:mm')}
    取件時間: ${checkoutTime.format('YYYY-MM-DD HH:mm')}
    天數差: ${diffDays.toFixed(0)}天
    逾期天數: ${overdueDays}天
    費用: ${totalFee}元
  `);
  
  return isNaN(totalFee) ? 0 : totalFee;
};

// 計算所有行李的費用總計
const calculateTotalFee = () => {
  const total = filteredDetailList.value.reduce((sum, item) => {
    const fee = calculateOverdueFee(item);
    return sum + (isNaN(fee) ? 0 : fee);
  }, 0);
  
  // 確保總費用是數字且保留兩位小數
  const result = Number(total.toFixed(0));
  console.log(`總費用計算結果: ${result}元`);
  return result;
};

// 獲取行李狀態中文標籤
const getStatusLabel = (item) => {
  const { status, idNumber } = item;
  const normalizedStatus = status.toLowerCase();
  
  if (['todayOut', 'history'].includes(currentDetailType.value) && 
      normalizedStatus === 'retrieved' && idNumber) {
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
  
  if (['todayOut', 'history'].includes(currentDetailType.value) && 
      normalizedStatus === 'retrieved' && idNumber) {
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
  
  if (['todayOut', 'history'].includes(currentDetailType.value) && 
      normalizedStatus === 'retrieved' && idNumber) {
    return 'warning';
  }
  
  const typeMap = {
    'stored': 'success',
    'retrieved': 'info',
    'expired': 'danger',
  }
  return typeMap[normalizedStatus] || 'default';
}

// 獲取狀態標籤顏色
const getStatusColor = (item) => {
  const { status, idNumber } = item;
  const normalizedStatus = status.toLowerCase();
  
  if (['todayOut', 'history'].includes(currentDetailType.value) && 
      normalizedStatus === 'retrieved' && idNumber) {
    return '#f97316';
  }
  
  const colorMap = {
    'stored': '#16a34a',
    'retrieved': '#2563eb',
    'expired': '#dc2626',
  }
  return colorMap[normalizedStatus] || '#6b7280';
}

// 業務方法：登出處理
const handleLogout = async () => {
  try {
    await authStore.logout()
    router.push('/')
  } catch (error) {
    console.error('登出失敗:', error)
    showFeedback('登出失敗，請重試', 'error')
  }
}

// 業務方法：關閉彈窗處理
const handleDialogClose = () => {
  detailDialogVisible.value = false
}

// 業務方法：加載儀表板統計數據
const loadDashboardData = async () => {
  try {
    loading.value = true
    const { data } = await luggageApi.getStats()
    stats.value = {
      current: data.currentStorage || 0,
      todayIn: data.todayCheckin || 0,
      todayOut: data.todayCheckout || 0,
      historyCount: data.totalHistory || 0
    }
    
    // 加載所有行李數據用於計算總應收費用
    const allLuggageResponse = await luggageApi.getAllCurrentLuggage()
    allLuggageData.value = allLuggageResponse.data.map(formatLuggageItem)
  } catch (error) {
    console.error('獲取儀表板統計數據失敗:', error)
    showFeedback('統計數據加載失敗', 'error')
  } finally {
    loading.value = false
  }
}

// 業務方法：加載詳情列表數據（區分今日和歷史）
const loadDetailData = async (type) => {
  try {
    detailLoading.value = true
    let response, data;
    const [startDate, endDate] = dateRange.value
    
    switch (type) {
      case 'current': {
        // 當前寄存（未取件）
        response = await luggageApi.getAllCurrentLuggage()
        data = response.data
        break
      }
      case 'todayIn': {
        // 今日寄存（只查當天）
        response = await luggageApi.getTodayStoredLuggage()
        data = response.data
        break
      }
      case 'todayOut': {
        // 今日取件（只查當天）
        response = await luggageApi.getTodayRetrievedLuggage()
        data = response.data
        break
      }
      case 'history': {
        // 歷史記錄（合併寄存和取件記錄）
        const storedResponse = await luggageApi.getHistoricalStored(startDate, endDate)
        const retrievedResponse = await luggageApi.getHistoricalRetrieved(startDate, endDate)
        // 合併兩類記錄，並按時間排序
        data = [...storedResponse.data, ...retrievedResponse.data]
          .sort((a, b) => new Date(b.checkinTime) - new Date(a.checkinTime))
        break
      }
      default:
        data = []
        return
    }
    
    // 驗證數據是否為數組
    if (!Array.isArray(data)) {
      console.error(`獲取${getTypeLabel(type)}數據格式錯誤:`, data)
      showFeedback(`${getTypeLabel(type)}數據格式錯誤`, 'error')
      detailList.value = []
      return
    }
    
    detailList.value = data.map(formatLuggageItem)
  } catch (error) {
    console.error(`獲取${getTypeLabel(type)}詳情失敗:`, error)
    const errorMsg = error.response?.data?.message || error.message || '未知錯誤'
    showFeedback(`${getTypeLabel(type)}詳情加載失敗: ${errorMsg}`, 'error')
    detailList.value = []
  } finally {
    detailLoading.value = false
  }
}

// 工具方法：格式化行李數據
const formatLuggageItem = (item) => {
  if (!item) return {};
  
  // 保留原始ISO日期用於計算
  const originalCheckinTime = item.checkinTime;
  const originalCheckoutTime = item.checkoutTime;
  
  // 格式化展示用的日期（轉換為本地時間顯示）
  const displayCheckinTime = originalCheckinTime 
    ? new Date(originalCheckinTime).toLocaleString() 
    : '無';
    
  const displayCheckoutTime = originalCheckoutTime 
    ? new Date(originalCheckoutTime).toLocaleString() 
    : '未取件';
  
  return {
    ...item,
    originalCheckinTime,
    originalCheckoutTime,
    checkinTime: displayCheckinTime,
    checkoutTime: displayCheckoutTime,
    storageLocation: item.storageLocation || '無',
    phone: item.phone || '無',
    roomNumber: item.roomNumber || '無',
    luggageCount: item.luggageCount || 1,
    idNumber: item.idNumber || '',
    orderNo: item.id ? `LUG-${item.id}` : '無'
  }
};

// 業務方法：打開詳情彈窗
const openDetailDialog = async (type) => {
  try {
    currentDetailType.value = type;
    detailDialogVisible.value = true;
    detailList.value = [];
    detailLoading.value = true;
    
    // 如果打開歷史記錄，使用當前日期範圍
    if (type === 'history') {
      await loadDetailData(type);
    } else {
      await loadDetailData(type);
    }
  } catch (error) {
    console.error('打開詳情彈窗失敗：', error);
    showFeedback('打開詳情失敗，請重試', 'error');
    detailDialogVisible.value = false;
  }
}

// 設置日期範圍
const setDateRange = (type) => {
  currentShortcut.value = type
  dateRange.value = getDateRange(type)
  handleFilterChange()
}

// 業務方法：篩選條件變更處理
const handleFilterChange = () => {
  if (detailDialogVisible.value && currentDetailType.value === 'history') {
    loadDetailData(currentDetailType.value)
  }
}

// 業務方法：Excel匯出功能
const exportToExcel = async () => {
  if (filteredDetailList.value.length === 0) {
    showFeedback('沒有可匯出的數據', 'info')
    return
  }
  
  try {
    exportLoading.value = true
    
    // 格式化匯出數據，包含應收費用
    const exportData = filteredDetailList.value.map(item => ({
      '行李編號': item.orderNo,
      '客人名稱': item.guestName,
      '存放位置': item.storageLocation,
      '電話': item.phone,
      '房間號': item.roomNumber,
      '行李數量': item.luggageCount,
      '寄存時間': item.checkinTime,
      '取件時間': item.checkoutTime || '未取件',
      '應收費用': calculateOverdueFee(item) > 0 ? `MOP ${calculateOverdueFee(item).toFixed(0)}` : '0',
      '身份證號碼': item.idNumber || '無',
      '狀態': getStatusLabel(item)
    }))
    
    // 添加總計行
    exportData.push({
      '行李編號': '',
      '客人名稱': '',
      '存放位置': '',
      '電話': '',
      '房間號': '',
      '行李數量': '',
      '寄存時間': '',
      '取件時間': '',
      '應收費用': `所有費用總計: MOP ${calculateTotalFee()}`,
      '身份證號碼': '',
      '狀態': ''
    })
    
    // 創建工作簿和工作表
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, getTypeLabel(currentDetailType.value))
    
    // 生成文件名
    const fileName = `${getTypeLabel(currentDetailType.value)}_${dateRange.value[0]}_至_${dateRange.value[1]}.xlsx`
    
    // 匯出文件
    XLSX.writeFile(workbook, fileName)
    showFeedback(`成功匯出 ${exportData.length - 1} 條記錄至Excel`, 'success')
  } catch (error) {
    console.error('Excel匯出失敗:', error)
    showFeedback('Excel匯出失敗，請重試', 'error')
  } finally {
    exportLoading.value = false
  }
}

// 業務方法：處理過期行李刪除
const handleDelete = async (item) => {
  // 顯示包含應收費用的確認信息
  try {
    const overdueFee = calculateOverdueFee(item)
    let confirmMessage = `確定要刪除這條過期行李記錄嗎？\n\n` +
                         `行李編號: ${item.orderNo}\n` +
                         `客人姓名: ${item.guestName}\n` +
                         `存放位置: ${item.storageLocation}\n` +
                         `寄存時間: ${item.checkinTime}\n`
    
    // 如果有應收費用，顯示費用信息
    if (overdueFee > 0) {
      confirmMessage += `應收費用: MOP ${overdueFee.toFixed(0)}\n\n`
    }
    
    confirmMessage += `此操作代表員工已清理該行李，刪除後將無法恢復，請謹慎操作！`
    
    const confirmResult = await ElMessageBox.confirm(
      confirmMessage,
      '確認刪除過期行李',
      {
        confirmButtonText: '確認刪除',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }
    )

    if (confirmResult === 'confirm') {
      deleteLoading[item.id] = true
      await luggageApi.markAsDeleted(item.id)
      detailList.value = detailList.value.filter(luggage => luggage.id !== item.id)
      allLuggageData.value = allLuggageData.value.filter(luggage => luggage.id !== item.id)
      showFeedback(`已成功刪除過期行李記錄：${item.orderNo}`, 'success')
      loadDashboardData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('刪除過期行李記錄失敗:', error)
      showFeedback('刪除失敗，請重試', 'error')
    }
  } finally {
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
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-filter-group {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

/* 日期過濾容器樣式 */
.date-filter-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-right: 16px;
}

/* 快捷日期按鈕組樣式 */
.date-shortcut-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.date-shortcut-buttons .el-button--mini {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.date-shortcut-buttons .el-button--mini.active {
  background-color: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.date-shortcut-buttons .el-button--mini:not(.active):hover {
  background-color: #eff6ff;
  color: #3b82f6;
  border-color: #dbeafe;
}

/* 增強日期選擇器樣式 */
:deep(.enhanced-datepicker-popper .el-picker-panel__content) {
  width: auto !important;
  min-width: 550px;
}

:deep(.enhanced-datepicker-popper .el-date-range-picker__content) {
  width: 270px !important;
}

.storage-location {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #1e40af;
}

.location-icon {
  color: #3b82f6;
  font-size: 14px;
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

.enhanced-input, .enhanced-datepicker, .enhanced-select {
  border-radius: 8px !important;
  transition: all 0.2s ease;
}

.enhanced-input:focus-within, :deep(.enhanced-datepicker:focus-within), :deep(.enhanced-select:focus-within) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2) !important;
}

/* 優化多選標籤顯示 */
:deep(.enhanced-select .el-select__tags) {
  flex-wrap: nowrap;
  max-width: 100%;
  overflow: hidden;
}

:deep(.enhanced-select .el-tag) {
  margin: 2px 4px 2px 0;
}

.export-btn {
  border-radius: 8px !important;
  transition: all 0.2s ease;
}

.export-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.3);
}

.enhanced-table {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e0e7ff;
}

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

.status-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.status-icon {
  font-size: 14px;
}

.stored-icon {
  color: #16a34a;
}

.picked-icon {
  color: #2563eb;
}

.special-picked-icon {
  color: #f97316;
  animation: pulse 2s infinite;
}

.overdue-icon {
  color: #dc2626;
  animation: pulse 2s infinite;
}

.status-tag {
  border-radius: 12px;
  padding: 3px 10px;
  font-weight: 500;
  font-size: 0.85rem;
}

.delete-btn {
  margin-left: 8px;
  padding: 2px 8px;
  transition: all 0.2s ease;
}

.delete-btn:hover {
  transform: scale(1.05);
}

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

.empty-subtitle {
  color: #64748b;
  margin: 0;
  font-size: 0.9rem;
}

/* 應收費用樣式 */
.overdue-fee {
  color: #dc2626;
  font-weight: 600;
}

/* 總費用樣式 */
.total-fee-section {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 2px solid #3b82f6;
  font-size: 1.2rem;
}

.total-fee-label {
  font-weight: 600;
  color: #334155;
  margin-right: 12px;
}

.total-fee-value {
  font-weight: 700;
  color: #dc2626;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.6; }
  100% { opacity: 1; }
}

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
  
  :deep(.el-input), :deep(.el-date-picker), :deep(.el-select) {
    width: 100% !important;
  }
  
  :deep(.enhanced-datepicker-popper .el-picker-panel__content) {
    min-width: 90vw !important;
  }
  
  :deep(.enhanced-datepicker-popper .el-date-range-picker__content) {
    width: 45vw !important;
  }
  
  .total-fee-section {
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
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
  
  :deep(.el-table .el-table__cell) {
    padding: 8px 4px !important;
  }
  
  .status-badge {
    flex-wrap: wrap;
  }
  
  .delete-btn {
    margin-top: 4px;
    margin-left: 0;
  }
}
</style>
