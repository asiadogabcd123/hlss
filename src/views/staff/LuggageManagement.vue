<template>
  <div class="container">
    <!-- 標題區 -->
    <div class="header">
      <h1><i class="fas fa-luggage-cart"></i> 行李標籤管理與掃描系統</h1>
    </div>

    <!-- 主內容區 - 使用選項卡切換功能 -->
    <div class="main-tabs">
      <div class="tabs">
        <button 
          class="tab-btn" 
          :class="{ 'active': activeTab === 'management' }"
          @click="handleTabChange('management')" 
        >
          <i class="fas fa-qrcode"></i> 標籤管理
        </button>
        <button 
          class="tab-btn" 
          :class="{ 'active': activeTab === 'scanner' }"
          @click="handleTabChange('scanner')" 
        >
          <i class="fas fa-camera"></i> 掃描取件
        </button>
      </div>

      <!-- 導入標籤管理組件 (QrBatch.vue) -->
      <QrBatch 
        v-if="activeTab === 'management'" 
        class="tab-content"
        :today="today"
        :quick-dates="quickDates"
        @load-luggage="loadLuggage"
        @print-single="printSingle"
        @load-luggage-data="loadLuggageData"
      />

      <!-- 導入掃描取件組件 (Scanner.vue) -->
      <Scanner 
        v-if="activeTab === 'scanner'" 
        class="tab-content"
        @verify-identity="verifyIdentity"
        @retrieve-luggage="retrieveLuggage"
        @reset-scanner="resetScanner"
        @focus-scanner="focusScanner"
      />
    </div>
    
    <!-- 通知訊息 -->
    <div 
      class="notification" 
      :class="[notificationType, notificationType + '-notification']"
      v-if="showNotification"
    >
      <i class="notification-icon" :class="notificationIcon"></i>
      <div class="notification-content">
        <div class="notification-title">{{ notificationTitle }}</div>
        <div class="notification-message">{{ notificationMessage }}</div>
      </div>
      <button class="notification-close" @click="showNotification = false">
        <i class="fas fa-times"></i>
      </button>
    </div>
    
    <!-- 加載指示器 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner">
        <i class="fas fa-spinner fa-spin"></i>
        <p>加載中，請稍候...</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'  // 移除了未使用的computed和watch
import dayjs from 'dayjs'
import 'dayjs/locale/zh-tw'
import luggageApi from '@/api/luggage'
import printJS from 'print-js'

// 导入两个子组件，使用指定的名称
import QrBatch from './QrBatch.vue'
import Scanner from './Scanner.vue'

dayjs.locale('zh-tw')

export default {
  name: 'LuggageSystem',
  components: {
    QrBatch,  // 注册QrBatch组件
    Scanner   // 注册Scanner组件
  },
  setup() {
    // 共用狀態
    const loading = ref(false)
    const activeTab = ref('management') // 默認為標籤管理
    const today = ref(dayjs().format('YYYY-MM-DD'))
    
    // 標籤管理相關狀態
    const qrCodes = ref([])
    const luggageInfo = ref(null)
    const pendingLuggage = ref([])
    const printedLuggage = ref([])
    const selectedDate = ref('')
    
    const quickDates = ref([
      { label: '今天', value: today.value, type: 'day' },
      { label: '昨天', value: dayjs().subtract(1, 'day').format('YYYY-MM-DD'), type: 'day' },
      { label: '本週', value: dayjs().startOf('week').format('YYYY-MM-DD'), type: 'range' },
      { label: '本月', value: dayjs().startOf('month').format('YYYY-MM-DD'), type: 'range' }
    ])
    
    // 掃描系統相關狀態
    const currentScan = ref(null)
    // 移除了未使用的scanTime
    // 移除了未使用的isScanning
    const scanInput = ref('')
    // 移除了未使用的scanMessage
    
    const luggageOrder = ref(null)
    const scannedLuggage = ref([])
    const scannerInput = ref(null)
    
    const verificationMethod = ref('code')
    const verificationInput = ref('')
    const isVerified = ref(false)
    
    const showNotification = ref(false)
    const notificationType = ref('success')
    const notificationTitle = ref('')
    const notificationMessage = ref('')
    
    // 移除了未使用的statusMap
    
    // 標籤切換
    const handleTabChange = (tabName) => {
      activeTab.value = tabName
      // 切换到扫描描页时自动聚焦输入框
      if (tabName === 'scanner') {
        setTimeout(() => focusScanner(), 300)
      }
    }
    
    // 移除了未使用的goToHome方法
    
    // 移除了未使用的formatDateTime方法
    
    // 移除了未使用的formatDate方法
    
    const showNotificationMessage = (type, title, message) => {
      notificationType.value = type
      notificationTitle.value = title
      notificationMessage.value = message
      showNotification.value = true
      
      setTimeout(() => {
        showNotification.value = false
      }, 3000)
    }
    
    // 標籤管理相關方法
    const loadLuggageData = async () => {
      try {
        loading.value = true
        
        const params = {}
        // 修复了拼写错误：quickDatesDates -> quickDates
        const selectedQuickDate = quickDates.value.find(date => date.value === selectedDate.value)

        if (selectedDate.value) {
          if (selectedQuickDate?.type === 'range') {
            params.startDate = selectedDate.value
            params.endDate = today.value
          } else {
            params.date = selectedDate.value
          }
        }
        
        // 修复了拼写错误：luggageApiApi -> luggageApi
        const [pendingRes, printedRes] = await Promise.all([
          luggageApi.getPendingForQR(params),
          luggageApi.getPrintedLuggage(params)
        ])
        
        pendingLuggage.value = pendingRes.data
        printedLuggage.value = printedRes.data
        
      } catch (error) {
        console.error('加載行李數據失敗:', error)
        showNotificationMessage('error', '錯誤', '獲取行李數據失敗，請檢查網絡連接或稍後重試')
      } finally {
        loading.value = false
      }
    }
    
    const loadLuggage = async (luggageId) => {
      try {
        loading.value = true
        
        const detailRes = await luggageApi.getLuggageById(luggageId)
        const luggageDetail = detailRes.data
        
        luggageInfo.value = {
          id: luggageDetail.id,
          guestName: luggageDetail.guestName,
          phoneLast4: luggageDetail.phone?.slice(-4) || '0000',
          luggageCount: luggageDetail.luggageCount
        }
        
        qrCodes.value = generatePreviewQRCodes(luggageDetail)
        
      } catch (error) {
        console.error('加載行李信息失敗:', error)
        
        let errorMsg = '加載行李信息失敗'
        if (error.response) {
          if (error.response.status === 404) {
            errorMsg = '行李記錄不存在'
          } else if (error.response.data && error.response.data.message) {
            errorMsg = error.response.data.message
          }
        }
        
        showNotificationMessage('error', '錯誤', `${errorMsg}，請稍後重試`)
      } finally {
        loading.value = false
      }
    }
    
    const generatePreviewQRCodes = (luggageDetail) => {
      const labels = []
      for (let i = 1; i <= luggageDetail.luggageCount; i++) {
        const qrContent = `LUG-${luggageDetail.id}-${i}`
        labels.push({
          id: `${luggageDetail.id}-${i}`,
          sequence: i,
          totalCount: luggageDetail.luggageCount,
          qrImage: `https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${encodeURIComponent(qrContent)}`
        })
      }
      return labels
    }
    
    const printSingle = async (qr, index) => {
      try {
        const luggageId = luggageInfo.value.id
        
        const alreadyPrinted = printedLuggage.value.some(item => item.id === luggageId)
        
        if (!alreadyPrinted) {
          const qrRes = await luggageApi.generateLabels(luggageId, luggageInfo.value.luggageCount)
          
          qrCodes.value = qrRes.data
          
          const pendingIndex = pendingLuggage.value.findIndex(item => item.id === luggageId)
          if (pendingIndex !== -1) {
            const [movedItem] = pendingLuggage.value.splice(pendingIndex, 1)
            printedLuggage.value.push(movedItem)
          }
        }
        
        const printContent = `
          <div style="text-align: center; padding: 20px;">
            <img src="${qr.qrImage}" alt="行李標籤" style="max-width: 100%; height: auto;">
            <p>行李 ${index + 1}/${qrCodes.value.length} | LUG-${luggageInfo.value.id}</p>
            <p>打印時間: ${new Date().toLocaleString()}</p>
          </div>
        `
        
        printJS({
          printable: printContent,
          type: 'raw-html',
          style: '@page { size: auto; margin: 5mm; }',
          onPrintDialogClose: () => {
            console.log('打印對話框已關閉')
          }
        })
        
      } catch (error) {
        console.error('打印失敗:', error)
        
        let errorMsg = '打印失敗'
        if (error.response) {
          if (error.response.status === 404) {
            errorMsg = '行李記錄不存在'
          } else if (error.response.data && error.response.data.message) {
            errorMsg = error.response.data.message
          }
        }
        
        showNotificationMessage('error', '錯誤', `${errorMsg}，請稍後重試`)
      }
    }
    
    // 掃描系統相關方法
    const focusScanner = () => {
      if (scannerInput.value) {
        scannerInput.value.focus()
      }
    }
    
    const verifyIdentity = async () => {
      if (!luggageOrder.value) {
        showNotificationMessage('error', '錯誤', '請先掃描行李標籤')
        return
      }
      
      if (!verificationInput.value.trim()) {
        showNotificationMessage('error', '錯誤', '請輸入驗證資訊')
        return
      }
      
      try {
        let isValid = false
        
        if (verificationMethod.value === 'code') {
          isValid = verificationInput.value === luggageOrder.value.verification_code
        } else {
          const response = await luggageApi.verifyIdNumber(
            luggageOrder.value.id,
            verificationInput.value
          )
          isValid = response.data.valid
        }
        
        isVerified.value = isValid
        
        if (isValid) {
          showNotificationMessage('success', '成功', '身份驗證已通過')
        } else {
          showNotificationMessage('error', '錯誤', '驗證資訊不正確，請重新輸入')
          verificationInput.value = ''
        }
      } catch (error) {
        console.error('驗證失敗:', error)
        showNotificationMessage('error', '錯誤', error.response?.data?.message || '驗證過程中發生錯誤')
      }
    }
    
    const retrieveLuggage = async () => {
      // 实现取件逻辑
      if (!luggageOrder.value) return
      
      try {
        loading.value = true
        const response = await luggageApi.retrieveLuggage(luggageOrder.value.id)
        
        if (response.data.success) {
          showNotificationMessage('success', '成功', '行李已成功取出')
          // 重置扫描状态
          resetScanner()
        } else {
          showNotificationMessage('error', '錯誤', '取件失敗，請重試')
        }
      } catch (error) {
        console.error('取件失敗:', error)
        showNotificationMessage('error', '錯誤', '取件過程中發生錯誤')
      } finally {
        loading.value = false
      }
    }
    
    const resetScanner = () => {
      currentScan.value = null
      luggageOrder.value = null
      scannedLuggage.value = []
      isVerified.value = false
      verificationInput.value = ''
      scanInput.value = ''
      // 移除了未使用的scanSuccess
    }
    
    // 初始化
    onMounted(() => {
      selectedDate.value = today.value
      loadLuggageData()
    })
    
    return {
      // 共用
      loading,
      activeTab,
      today,
      quickDates,
      handleTabChange,
      showNotification,
      notificationType,
      notificationTitle,
      notificationMessage,
      
      // 方法
      loadLuggage,
      printSingle,
      verifyIdentity,
      retrieveLuggage,
      resetScanner,
      focusScanner,
      loadLuggageData
    }
  }
}
</script>


<style scoped>
/* 全局變量 */
:root {
  --primary: #2c5282;
  --primary-light: #4299e1;
  --secondary: #38a169;
  --dark: #0f172a;
  --light: #f8fafc;
  --gray: #94a3b8;
  --light-gray: #334155;
  --border-radius: 12px;
  --transition: all 0.3s ease;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Segoe UI', 'Microsoft JhengHei', sans-serif;
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%);
  color: #e2e8f0;
  line-height: 1.6;
  min-height: 100vh;
}

/* 主容器：修复高度计算，确保内容不被截断 */
.container {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 40px); /* 避免溢出屏幕 */
  overflow: hidden;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.3);
  max-width: 1200px;
  margin: 20px auto;
}

/* 標題區 */
.header {
  position: relative;
  text-align: center;
  padding: 18px 20px;
  background: rgba(30, 41, 59, 0.8);
  color: white;
  z-index: 10;
  border-bottom: 1px solid rgba(99, 102, 241, 0.3);
}

.header h1 {
  font-size: 1.8rem;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-weight: 600;
}

/* 返回按鈕 */
.home-btn {
  position: absolute;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(79, 70, 229, 0.7);
  color: white;
  border: 1px solid rgba(99, 102, 241, 0.5);
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: var(--transition);
  font-weight: 500;
}

.home-btn:hover {
  background: rgba(99, 102, 241, 0.9);
  transform: translateY(-50%) translateY(-2px);
  box-shadow: 0 4px 8px rgba(79, 70, 229, 0.4);
}

/* 選項卡：修复高度，确保内容区域有足够空间 */
.main-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: auto; /* 取消固定高度，避免内容被截断 */
}

.tabs {
  display: flex;
  background: rgba(15, 23, 42, 0.7);
  border-bottom: 1px solid rgba(99, 102, 241, 0.3);
}

.tab-btn {
  flex: 1;
  padding: 15px;
  background: transparent;
  border: none;
  color: #cbd5e1;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.tab-btn.active {
  background: rgba(99, 102, 241, 0.2);
  color: #e2e8f0;
  border-bottom: 3px solid #6366f1;
}

.tab-btn:hover {
  background: rgba(99, 102, 241, 0.1);
}

/* 修复tab-content样式：确保默认显示，避免被覆盖 */
.tab-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px; /* 新增内边距，避免内容贴边 */
  background: rgba(15, 23, 42, 0.7); /* 统一背景色，确保内容可见 */
}

/* 標籤管理樣式 */
.qr-batch-container {
  display: flex;
  height: 100%;
  overflow: hidden;
  gap: 20px; /* 新增间距，避免内容重叠 */
}

.list-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: rgba(15, 23, 42, 0.7);
}

.preview-sidebar {
  width: 380px;
  border-left: 1px solid rgba(99, 102, 241, 0.2);
  overflow-y: auto;
  background: rgba(15, 23, 42, 0.7);
  padding: 20px; /* 新增内边距 */
}

/* 日期選擇器 */
.date-selector {
  background: rgba(30, 41, 59, 0.6);
  border-radius: var(--border-radius);
  padding: 18px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  border: 1px solid var(--light-gray);
}

.date-filter {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.date-filter label {
  font-weight: 600;
  color: #e2e8f0;
  font-size: 1.05rem;
  min-width: 100px;
}

.date-filter input[type="date"] {
  padding: 10px 14px;
  border: 1px solid var(--light-gray);
  border-radius: 8px;
  font-size: 1rem;
  min-width: 200px;
  background: rgba(15, 23, 42, 0.8);
  color: #e2e8f0;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.3);
}

.date-filter input[type="date"]:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.date-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 10px 18px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: var(--transition);
  border: none;
  font-weight: 500;
}

.reset-btn {
  background: #64748b;
  color: white;
}

.reset-btn:hover {
  background: #475569;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(100, 116, 139, 0.4);
}

.date-display {
  font-weight: 600;
  color: #cbd5e1;
  font-size: 1.1rem;
  padding: 8px 0;
  border-top: 1px dashed var(--light-gray);
  margin-top: 10px;
}

/* 列表區域 */
.list-section {
  background: rgba(30, 41, 59, 0.6);
  border-radius: var(--border-radius);
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--light-gray);
}

.list-section h2 {
  margin-top: 0;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--light-gray);
  color: #e2e8f0;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.4rem;
}

.list-section h2 i {
  color: #818cf8;
}

/* 無數據提示：确保可见性 */
.no-data {
  text-align: center;
  padding: 40px 20px;
  background: rgba(15, 23, 42, 0.5);
  border-radius: var(--border-radius);
  color: var(--gray);
  font-size: 1.15rem;
  border: 2px dashed var(--light-gray);
  margin: 20px 0;
  font-weight: 500;
  z-index: 1; /* 确保在最上层 */
}

.no-data i {
  margin-right: 10px;
  font-size: 1.8rem;
  color: #475569;
}

/* 行李卡片網格 */
.luggage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
  margin-top: 18px;
  flex: 1;
  overflow-y: auto;
  padding-bottom: 10px;
}

/* 行李卡片 */
.luggage-card {
  border: 1px solid var(--light-gray);
  border-radius: var(--border-radius);
  padding: 18px;
  background: rgba(15, 23, 42, 0.7);
  cursor: pointer;
  transition: var(--transition);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.luggage-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
  border-color: #6366f1;
  background: rgba(30, 41, 59, 0.8);
}

.luggage-card.active {
  border: 2px solid #6366f1;
  background: rgba(30, 41, 59, 0.9);
}

.luggage-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: #6366f1;
  opacity: 0;
  transition: var(--transition);
}

.luggage-card:hover::before {
  opacity: 1;
}

.luggage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--light-gray);
}

.badge {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.badge.new {
  background: #10b981;
  color: white;
}

.badge.reprinted {
  background: #f59e0b;
  color: #1e293b;
}

.luggage-header strong {
  font-size: 1.1rem;
  color: #e2e8f0;
}

.luggage-body p {
  margin: 10px 0;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #cbd5e1;
}

.luggage-body i {
  width: 20px;
  color: #818cf8;
  text-align: center;
}

/* 日期標題樣式 */
.date-header {
  margin: 22px 0 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--light-gray);
  color: #e2e8f0;
  font-size: 1.25rem;
  position: relative;
  padding-left: 18px;
  font-weight: 600;
}

.date-header::before {
  content: "";
  position: absolute;
  left: 0;
  top: 5px;
  bottom: 5px;
  width: 5px;
  background: #6366f1;
  border-radius: 3px;
}

/* 預覽內容 */
.preview-content {
  padding: 22px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.preview-header {
  margin-bottom: 22px;
  padding-bottom: 18px;
  border-bottom: 2px solid var(--light-gray);
}

.preview-header h2 {
  font-size: 1.35rem;
  color: #e2e8f0;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.preview-header h2 i {
  color: #818cf8;
}

.guest-info {
  display: flex;
  gap: 25px;
  font-size: 1.05rem;
  background: rgba(30, 41, 59, 0.6);
  padding: 12px 18px;
  border-radius: 8px;
  margin-top: 10px;
  border: 1px solid var(--light-gray);
}

.guest-info p {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  color: #cbd5e1;
  font-weight: 500;
}

/* QR碼網格 */
.qr-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 18px;
  overflow-y: auto;
  padding-bottom: 20px;
  flex: 1;
}

.qr-card {
  border: 1px solid var(--light-gray);
  padding: 18px;
  text-align: center;
  border-radius: var(--border-radius);
  transition: var(--transition);
  background: rgba(15, 23, 42, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.qr-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
  border-color: #6366f1;
  background: rgba(30, 41, 59, 0.8);
}

.qr-card img {
  width: 140px;
  height: 140px;
  object-fit: contain;
  margin-bottom: 12px;
  border: 1px solid var(--light-gray);
  padding: 5px;
  background: white;
  border-radius: 8px;
}

.qr-meta {
  margin: 12px 0;
  font-size: 1rem;
  font-weight: 500;
  color: #e2e8f0;
}

.print-btn {
  background: #4f46e5;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 8px;
  cursor: pointer;
  margin-top: 12px;
  width: 100%;
  transition: var(--transition);
  font-weight: 500;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.print-btn:hover {
  background: #6366f1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
}

/* 空預覽狀態樣式：确保可见 */
.empty-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--gray);
  text-align: center;
  padding: 40px;
  z-index: 1;
}

.empty-preview i {
  font-size: 4rem;
  margin-bottom: 25px;
  color: #334155;
  opacity: 0.7;
}

.empty-preview p {
  font-size: 1.4rem;
  color: var(--gray);
  font-weight: 500;
  max-width: 300px;
  line-height: 1.8;
}

/* 掃描系統專用樣式 */
.scanner-section {
  padding: 20px;
  background: rgba(15, 23, 42, 0.7);
  border-bottom: 1px solid rgba(99, 102, 241, 0.2);
}

.info-section {
  padding: 20px;
  background: rgba(15, 23, 42, 0.7);
}

.scanner-visible-input {
  width: 80%;
  padding: 10px;
  border: 2px solid #1a2a6c;
  border-radius: 8px;
  font-size: 1rem;
  text-align: center;
  outline: none;
  margin-bottom: 10px;
  background: white; /* 确保输入框可见 */
  color: #333;
}

.scanner-visible-input:focus {
  border-color: #4CAF50;
  box-shadow: 0 0 5px rgba(76, 175, 80, 0.5);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.4rem;
  color: #e2e8f0; /* 修复：原代码是#1a2a6c，与背景色冲突 */
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #1a2a6c;
}

.section-title i {
  font-size: 1.2rem;
  color: #818cf8;
}

.scanner-box {
  background: white;
  border-radius: 15px;
  height: 250px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  border: 3px dashed #1a2a6c;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.scanner-box:hover {
  transform: scale(1.02);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.scanner-placeholder {
  text-align: center;
  color: #666;
  z-index: 2;
}

.scanner-placeholder i {
  font-size: 5rem;
  color: #1a2a6c;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.scanner-placeholder .success-icon {
  color: #4CAF50;
}

.scanner-placeholder p {
  color: #666;
  font-size: 1.1rem;
}

.scanner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 19px,
    rgba(26, 42, 108, 0.1) 20px,
    rgba(26, 42, 108, 0.1) 20px
  );
  z-index: 1;
  pointer-events: none;
}

.scanned-tag {
  background: white;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
  margin-top: 20px;
  color: #333;
}

.tag-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.tag-status {
  color: #4CAF50;
  font-weight: 600;
}

.tag-id {
  font-size: 1.2rem;
  font-weight: bold;
  color: #1a2a6c;
  background: #e6eeff;
  padding: 5px 15px;
  border-radius: 20px;
}

.tag-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.tag-item {
  display: flex;
  flex-direction: column;
}

.tag-label {
  font-size: 0.85rem;
  color: #777;
}

.tag-value {
  font-weight: 500;
  color: #333;
}

.luggage-count {
  display: flex;
  align-items: center;
  gap: 15px;
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
  margin-top: 20px;
  color: #333;
}

.count-circle {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: #1a2a6c;
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.count-circle .number {
  font-size: 1.8rem;
  line-height: 1;
}

.count-circle .label {
  font-size: 0.8rem;
}

.count-text {
  flex: 1;
}

.count-text h3 {
  color: #1a2a6c;
  margin-bottom: 8px;
}

.count-text p {
  color: #666;
  line-height: 1.5;
}

.order-info {
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
  margin-top: 20px;
  color: #333;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-top: 15px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 0.85rem;
  color: #777;
  margin-bottom: 5px;
}

.info-value {
  font-weight: 500;
  color: #333;
  word-break: break-word;
}

.status {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 0.9rem;
}

.status.stored {
  background-color: #e3f2fd;
  color: #0d47a1;
}

.status.retrieved {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.status.expired {
  background-color: #ffebee;
  color: #c62828;
}

.verification {
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
  margin-top: 20px;
  color: #333;
}

.verification p {
  margin-bottom: 15px;
  font-weight: 500;
}

.verification-methods {
  display: flex;
  gap: 15px;
  margin: 15px 0;
}

.method {
  flex: 1;
  border: 2px solid #ddd;
  border-radius: 10px;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.method:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.1);
}

.method.active {
  border-color: #1a2a6c;
  background: #e6eeff;
}

.method i {
  font-size: 2rem;
  color: #1a2a6c;
  margin-bottom: 10px;
}

.method h3 {
  color: #1a2a6c;
  margin-bottom: 5px;
}

.method p {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0;
}

.verification-input {
  margin: 20px 0;
}

.input-group {
  display: flex;
  gap: 10px;
}

.input-group input {
  flex: 1;
  padding: 15px;
  border: 2px solid #ddd;
  border-radius: 10px;
  font-size: 1rem;
  outline: none;
  transition: border 0.3s ease;
}

.input-group input:focus {
  border-color: #1a2a6c;
}

.input-group button {
  background: #1a2a6c;
  color: white;
  border: none;
  border-radius: 10px;
  padding: 0 25px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s ease;
}

.input-group button:hover {
  background: #0d1a52;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 10px;
}

.action-buttons button {
  flex: 1;
  padding: 15px;
  border: none;
  border-radius: 10px;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retrieve-btn {
  background: #1a2a6c;
  color: white;
}

.retrieve-btn:hover:not(:disabled) {
  background: #0d1a52;
  transform: translateY(-2px);
}

.retrieve-btn:disabled {
  background: #9e9e9e;
  cursor: not-allowed;
  opacity: 0.7;
}

.reset-btn {
  background: #e0e0e0;
  color: #333;
}

.reset-btn:hover {
  background: #d0d0d0;
  transform: translateY(-2px);
}

.scan-animation {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: #1a2a6c;
  animation: scan 2s linear infinite;
}

@keyframes scan {
  0% {
    top: 0;
  }
  50% {
    top: calc(100% - 4px);
  }
  100% {
    top: 0;
  }
}

/* 加載指示器 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.loading-spinner {
  text-align: center;
  background: rgba(30, 41, 59, 0.9);
  padding: 50px;
  border-radius: var(--border-radius);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(99, 102, 241, 0.3);
}

.loading-spinner i {
  font-size: 3.5rem;
  color: #818cf8;
  margin-bottom: 25px;
  animation: spin 1.5s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-spinner p {
  font-size: 1.3rem;
  color: #e2e8f0;
  font-weight: 500;
}

/* 通知訊息 */
.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 300px;
  padding: 15px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.2);
  display: flex;
  align-items: flex-start;
  z-index: 1000;
  opacity: 0;
  transform: translateY(-20px);
  transition: all 0.3s ease;
}

.notification.show {
  opacity: 1;
  transform: translateY(0);
}

.notification.success {
  background: #e8f5e9;
  border-left: 4px solid #4CAF50;
}

.notification.error {
  background: #ffebee;
  border-left: 4px solid #f44336;
}

.notification.info {
  background: #e3f2fd;
  border-left: 4px solid #2196F3;
}

.notification-icon {
  font-size: 1.5rem;
  margin-right: 10px;
}

.notification-success .notification-icon {
  color: #4CAF50;
}

.notification-error .notification-icon {
  color: #f44336;
}

.notification-info .notification-icon {
  color: #2196F3;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.notification-close {
  background: none;
  border: none;
  cursor: pointer;
  color: #666;
  font-size: 1.2rem;
}

/* 快捷按鈕組 */
.quick-dates {
  display: flex;
  gap: 10px;
  margin-top: 15px;
  flex-wrap: wrap;
}

.quick-date {
  padding: 8px 16px;
  background: rgba(30, 41, 59, 0.7);
  border: 1px solid var(--light-gray);
  border-radius: 20px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: var(--transition);
  font-weight: 500;
  color: #cbd5e1;
}

.quick-date:hover, .quick-date.active {
  background: #4f46e5;
  color: white;
  border-color: #6366f1;
}

/* 響應式設計 */
@media (max-width: 1200px) {
  .qr-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }
}

@media (max-width: 992px) {
  .qr-batch-container {
    flex-direction: column;
  }
  
  .preview-sidebar {
    width: 100%;
    max-height: 50vh;
  }
  
  .tabs {
    flex-direction: column;
  }
  
  .main-content {
    flex-direction: column;
    padding: 0;
  }
  
  .preview-sidebar {
    width: 100%;
    max-height: 50vh;
    position: relative;
  }
  
  .luggage-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}

@media (max-width: 768px) {
  .header h1 {
    font-size: 1.5rem;
  }
  
  .home-btn {
    padding: 7px 14px;
    font-size: 0.85rem;
  }
  
  .tab-btn {
    font-size: 1rem;
    padding: 12px;
  }
  
  .date-selector {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .date-filter {
    width: 100%;
  }
  
  .date-filter input[type="date"] {
    flex: 1;
    min-width: auto;
  }
  
  .reset-btn {
    width: 100%;
    justify-content: center;
  }
  
  .luggage-grid {
    grid-template-columns: 1fr;
  }
  
  .qr-grid {
    grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  }
  
  .info-grid, .tag-content {
    grid-template-columns: 1fr;
  }
  
  .verification-methods {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 15px;
  }
  
  .header h1 {
    font-size: 1.3rem;
  }
  
  .home-btn {
    position: static;
    transform: none;
    margin-bottom: 10px;
    width: 100%;
    justify-content: center;
  }
  
  .list-section {
    padding: 15px;
  }
  
  .preview-header h2 {
    font-size: 1.15rem;
  }
  
  .guest-info {
    flex-direction: column;
    gap: 8px;
    font-size: 0.95rem;
  }
  
  .qr-card img {
    width: 120px;
    height: 120px;
  }
  
  .notification {
    width: 90%;
    right: 5%;
    left: 5%;
  }
}
</style>