<template>
  <div class="modern-scanner">
    <!-- 頂部操作欄 -->
    <div class="scanner-header">
      <div class="header-content">

      </div>
      
      <!-- 添加身份證取件切換按鈕 -->
      <div class="mode-toggle">
        <button 
          class="toggle-btn" 
          :class="{ active: !useIdCardMode }"
          @click="switchToQrMode"
        >
          <i class="fas fa-qrcode"></i> 二維碼取件
        </button>
        <button 
          class="toggle-btn" 
          :class="{ active: useIdCardMode }"
          @click="switchToIdCardMode"
        >
          <i class="fas fa-id-card"></i> 身份證取件
        </button>
      </div>
    </div>

    <!-- 主要內容區 -->
    <div class="scanner-body">
      <!-- 左側掃描區 -->
      <div class="scan-section">
        <!-- 二維碼模式 -->
        <div class="scan-area" @click="focusScanner" v-if="!useIdCardMode">
          <div class="scanner-visual">
            <div class="scanner-frame">
              <div class="laser" :class="{ active: isScanning }"></div>
              <i class="scan-icon" :class="scanIconClass"></i>
            </div>
          </div>
          <div class="scan-instruction">
            <p>{{ scanGuideText }}</p>
            <small>點擊掃描區域激活掃描器</small>
          </div>
          <input
            type="text"
            class="scanner-input"
            v-model="scanInput"
            @keyup.enter="handleScan"
            @input="handleInput"
            ref="scannerInput"
            autocomplete="off"
            inputmode="text"
            spellcheck="false"
          >
        </div>
        
        <!-- 身份證模式 -->
        <div class="id-card-section" v-else>
          <div class="id-card-input">
            <h4>身份證取件</h4>
            <p>請輸入客人的證件號碼</p>
            
            <div class="input-group">
              <input
                type="text"
                v-model="idCardNumber"
                placeholder="請輸入證件號碼"
                :disabled="hasScannedLuggageTag"
                @keyup.enter="focusScanner"
                ref="idCardInput"
              >
            </div>
          </div>
          
          <!-- 行李掃描區域（身份證模式） -->
          <div class="scan-area" @click="focusScanner">
            <div class="scanner-visual">
              <div class="scanner-frame">
                <div class="laser" :class="{ active: isScanning }"></div>
                <i class="scan-icon" :class="scanIconClass"></i>
              </div>
            </div>
            <div class="scan-instruction">
              <p>{{ idCardScanGuideText }}</p>
              <small>點擊掃描區域激活掃描器</small>
            </div>
            <input
              type="text"
              class="scanner-input"
              v-model="scanInput"
              @keyup.enter="handleIdCardScan"
              @input="handleInput"
              ref="scannerInputIdCard"
              autocomplete="off"
              inputmode="text"
              spellcheck="false"
            >
          </div>
        </div>

        <!-- 掃描記錄 -->
        <div class="scan-history" v-if="hasScannedCustomerTicket || hasScannedLuggageTag || idCardNumber">
          <h4>掃描記錄</h4>
          <div class="history-items">
            <div class="history-item" v-if="hasScannedCustomerTicket">
              <div class="item-type customer">
                <i class="fas fa-ticket-alt"></i>
                <span>客人取件憑證</span>
              </div>
              <div class="item-detail">行李ID: {{ customerTicket?.luggageId }}</div>
            </div>
            <div class="history-item" v-if="hasScannedLuggageTag">
              <div class="item-type luggage">
                <i class="fas fa-tag"></i>
                <span>行李標籤</span>
              </div>
              <div class="item-detail">行李ID: {{ luggageTag?.luggageId }}</div>
            </div>
            <div class="history-item" v-if="idCardNumber">
              <div class="item-type id-card">
                <i class="fas fa-id-card"></i>
                <span>身份證號碼</span>
              </div>
              <div class="item-detail">{{ idCardNumber }}</div>
            </div>
          </div>

          <!-- 匹配狀態 -->
          <div class="match-result" v-if="hasScannedBoth">
            <div class="result-indicator" :class="matchSuccess ? 'success' : 'error'">
              <i class="fas" :class="matchSuccess ? 'fa-check-circle' : 'fa-times-circle'"></i>
              <span>{{ matchResultText }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右側信息區 -->
      <div class="info-section">
        <!-- 訂單信息 -->
        <div class="info-card">
          <div class="card-header">
            <i class="fas fa-info-circle"></i>
            <h3>訂單資訊</h3>
          </div>
          
          <div class="card-body">
            <div class="empty-state" v-if="!hasScannedCustomerTicket && !hasScannedLuggageTag && !idCardNumber">
              <i class="fas fa-id-card-alt"></i>
              <h4>請先掃描客人取件憑證或輸入身份證</h4>
              <p>掃描流程：1. 掃客人二維碼/輸入身份證 → 2. 掃行李二維碼 → 3. 工作人員確認取件</p>
            </div>

            <div class="order-details" v-if="luggageOrder">
              <div class="detail-grid">
                <div class="detail-item">
                  <label>行李ID</label>
                  <div class="value">{{ luggageOrder.id }}</div>
                </div>
                <div class="detail-item">
                  <label>客人姓名</label>
                  <div class="value">{{ luggageOrder.guestName }}</div>
                </div>
                <div class="detail-item">
                  <label>行李數量</label>
                  <div class="value">{{ luggageOrder.luggageCount }} 件</div>
                </div>
                <div class="detail-item" v-if="luggageOrder.phoneLast4">
                  <label>手機尾號</label>
                  <div class="value">****{{ luggageOrder.phoneLast4 }}</div>
                </div>
                <div class="detail-item">
                  <label>寄存時間</label>
                  <div class="value">{{ formatDateTime(luggageOrder.checkinTime) }}</div>
                </div>
                <div class="detail-item">
                  <label>當前狀態</label>
                  <div class="value">
                    <span class="status-badge" :class="luggageOrder.status.toLowerCase()">
                      {{ statusMap[luggageOrder.status] }}
                    </span>
                  </div>
                </div>
                <div class="detail-item" v-if="idCardNumber">
                  <label>身份證號</label>
                  <div class="value">{{ idCardNumber }}</div>
                </div>
              </div>

              <!-- 超期費用提醒 -->
              <div class="overdue-alert" v-if="luggageOrder.status === 'EXPIRED'">
                <div class="alert-icon">
                  <i class="fas fa-exclamation-triangle"></i>
                </div>
                <div class="alert-content">
                  <h5>超期寄存提醒</h5>
                  <p>此行李已寄存 {{ storageDays }} 天，超過免費寄存期限(3天)</p>
                  <p class="fee">需收取超期費用: <span>MOP {{ overdueFee.toFixed(2) }}</span></p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作區 -->
        <div class="action-card">
          <div class="card-header">
            <i class="fas fa-check-square"></i>
            <h3>取件操作</h3>
          </div>
          
          <div class="card-body">
            <div class="verification-status" v-if="verificationStatus">
              <div class="status-message" :class="verificationStatus">
                <i class="fas" :class="verificationIcon"></i>
                <span>{{ verificationMessage }}</span>
              </div>
            </div>
            
            <div class="action-buttons">
              <button 
                class="btn-primary" 
                :disabled="!canRetrieve || isProcessing"
                @click="retrieveLuggage"
              >
                <i class="fas fa-check-circle"></i> 
                {{ luggageOrder?.status === 'RETRIEVED' ? '已完成取件' : '確認取件' }}
              </button>
              <button class="btn-secondary" @click="resetScanner" :disabled="isProcessing">
                <i class="fas fa-redo"></i> 重新掃描
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 通知彈窗 -->
    <transition name="fade">
      <div class="notification-toast" :class="notificationType" v-if="showNotification">
        <div class="toast-content">
          <i class="toast-icon" :class="notificationIcon"></i>
          <div class="toast-message">
            <div class="toast-title">{{ notificationTitle }}</div>
            <div class="toast-text">{{ notificationMessage }}</div>
          </div>
        </div>
        <button class="toast-close" @click="showNotification = false">
          <i class="fas fa-times"></i>
        </button>
      </div>
    </transition>
  </div>
</template>

<script>
// 引入後端API
import luggageApi from '@/api/luggage'

export default {
  name: 'ModernScanner',
  data() {
    return {
      // 掃描模式
      useIdCardMode: false,
      
      // 身份證相關數據
      idCardNumber: '',
      
      // 掃描狀態標記
      hasScannedCustomerTicket: false,
      hasScannedLuggageTag: false,
      
      // 二維碼數據
      customerTicket: null,
      luggageTag: null,
      
      // 訂單資訊（來自後端）
      luggageOrder: null,
      
      // 掃描控制
      scanInput: '',
      isScanning: false,
      scanSuccess: false,
      focusTimer: null,
      isProcessing: false, // 防止重複處理的鎖
      
      // 驗證與提示
      verificationStatus: '',
      verificationMessage: '',
      showNotification: false,
      notificationType: 'success',
      notificationTitle: '',
      notificationMessage: '',
      
      // 狀態映射（與後端 LuggageStatus 對齊）
      statusMap: {
        'STORED': '寄存中',
        'EXPIRED': '已過期',
        'RETRIEVED': '已取件'
      }
    }
  },
  computed: {
    // 是否已掃描兩個二維碼或身份證+行李標籤
    hasScannedBoth() {
      return (this.hasScannedCustomerTicket || this.idCardNumber) && this.hasScannedLuggageTag;
    },
    
    // 行李ID是否匹配
    matchSuccess() {
      if (!this.luggageTag) return false;
      
      // 如果是身份證模式，只要行李標籤掃描成功就視為匹配
      if (this.useIdCardMode && this.idCardNumber) {
        return true;
      }
      
      // 如果是二維碼模式，需要比對兩個二維碼的行李ID
      if (this.customerTicket && this.luggageTag) {
        return String(this.customerTicket.luggageId) === String(this.luggageTag.luggageId);
      }
      
      return false;
    },
    
    // 掃描區引導文字
    scanGuideText() {
      if (this.luggageOrder?.status === 'RETRIEVED') {
        return '此訂單已完成取件，可點擊「重新掃描」開始新流程';
      } else if (!this.hasScannedCustomerTicket) {
        return '請掃描客人的取件憑證二維碼';
      } else if (!this.hasScannedLuggageTag) {
        return `請掃描對應的行李標籤（行李ID: ${this.customerTicket?.luggageId}）`;
      } else {
        return this.matchSuccess 
          ? '行李ID匹配成功，請確認取件' 
          : '行李ID不匹配，請重新掃描';
      }
    },
    
    // 身份證模式掃描引導文字
    idCardScanGuideText() {
      if (this.luggageOrder?.status === 'RETRIEVED') {
        return '此訂單已完成取件';
      } else if (!this.idCardNumber) {
        return '請先輸入身份證號碼';
      } else if (!this.hasScannedLuggageTag) {
        return '請掃描行李標籤';
      } else {
        return '行李標籤已掃描，請確認取件';
      }
    },
    
    // 匹配結果文字
    matchResultText() {
      if (this.useIdCardMode) {
        return '身份證與行李標籤已準備就緒';
      }
      return this.matchSuccess ? '行李ID配對成功' : '行李ID不匹配';
    },
    

    
    // 掃描狀態類別
    scanStatus() {
      if (this.luggageOrder?.status === 'RETRIEVED') return 'completed';
      if (this.hasScannedBoth) return this.matchSuccess ? 'success' : 'error';
      if (this.hasScannedCustomerTicket || this.idCardNumber) return 'scanning';
      return 'idle';
    },
    
    // 掃描圖標類別
    scanIconClass() {
      if (this.luggageOrder?.status === 'RETRIEVED') return 'fas fa-check-circle completed';
      if (this.hasScannedBoth) return this.matchSuccess ? 'fas fa-check-circle success' : 'fas fa-times-circle error';
      if (this.hasScannedCustomerTicket || this.idCardNumber) return 'fas fa-qrcode scanning';
      return 'fas fa-camera idle';
    },
    
    // 是否可完成取件（身份驗證+行李標籤+訂單存在+未取件）
    canRetrieve() {
      return (this.hasScannedCustomerTicket || this.idCardNumber) && 
             this.hasScannedLuggageTag && 
             this.luggageOrder && 
             this.luggageOrder.status !== 'RETRIEVED';
    },
    
    // 通知圖標
    notificationIcon() {
      return {
        'success': 'fas fa-check-circle',
        'error': 'fas fa-exclamation-circle',
        'info': 'fas fa-info-circle'
      }[this.notificationType];
    },
    
    // 驗證狀態圖標
    verificationIcon() {
      switch(this.verificationStatus) {
        case 'success': return 'fa-check-circle';
        case 'error': return 'fa-exclamation-circle';
        default: return 'fa-info-circle';
      }
    },
    
    // 超期天數（基於後端 checkinTime 計算）
    storageDays() {
      if (!this.luggageOrder || !this.luggageOrder.checkinTime) return 0;
      const checkinTime = new Date(this.luggageOrder.checkinTime);
      const now = new Date();
      const diffTime = Math.abs(now - checkinTime);
      return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    },
    
    // 超期費用（基於行李數量 + 超期天數，與後端邏輯統一）
    overdueFee() {
      if (this.luggageOrder?.status !== 'EXPIRED' || !this.luggageOrder.luggageCount) return 0;
      const overdueDays = Math.max(0, this.storageDays - 3);
      const feePerDayPerLuggage = 20;
      return overdueDays * feePerDayPerLuggage * this.luggageOrder.luggageCount;
    }
  },
  methods: {
    // 切換到身份證模式
    switchToIdCardMode() {
      this.useIdCardMode = true;
      this.resetScanner();
      this.$nextTick(() => {
        if (this.$refs.idCardInput) {
          this.$refs.idCardInput.focus();
        }
      });
    },
    
    // 切換到二維碼模式
    switchToQrMode() {
      this.useIdCardMode = false;
      this.resetScanner();
    },
    
    // 處理身份證模式的掃描
    async handleIdCardScan() {
      if (!this.scanInput || this.luggageOrder?.status === 'RETRIEVED' || this.isProcessing) {
        return;
      }
      
      if (!this.idCardNumber) {
        this.showNotificationMessage('error', '操作失敗', '請先輸入身份證號碼');
        return;
      }
      
      this.isProcessing = true;
      
      try {
        const cleanedInput = this.scanInput.trim();
        const jsonString = cleanedInput.replace(/[\r\n\s]+/g, '');
        let scannedData = JSON.parse(jsonString);
        
        // 字段名兼容處理
        scannedData = this.normalizeCamelCase(scannedData);
        
        // 驗證二維碼必要欄位
        if (!scannedData.type || !scannedData.luggageId) {
          throw new Error('二維碼格式無效，缺少「type」或「luggageId」');
        }
        
        // 在身份證模式下，只能掃描行李標籤
        if (scannedData.type !== 'luggage_tag') {
          throw new Error('請掃描「行李標籤」（二維碼類型需為 luggage_tag）');
        }
        
        await this.processLuggageTag(scannedData);
        
      } catch (error) {
        console.error('掃描錯誤:', error);
        this.showNotificationMessage('error', '識別失敗', 
          `${error.message}，請檢查二維碼或切換輸入法為英文`);
        this.scanSuccess = false;
      } finally {
        setTimeout(() => {
          this.scanInput = '';
          this.isProcessing = false;
        }, 3000);
      }
    },
    
    // 格式化日期時間（繁體中文格式）
    formatDateTime(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('zh-TW', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    
    // 顯示通知彈窗（3.5秒後自動關閉）
    showNotificationMessage(type, title, message) {
      this.notificationType = type;
      this.notificationTitle = title;
      this.notificationMessage = message;
      this.showNotification = true;
      
      setTimeout(() => {
        this.showNotification = false;
      }, 3500);
    },
    
    // 手動聚焦掃描輸入框
    focusScanner() {
      let inputRef = this.useIdCardMode ? this.$refs.scannerInputIdCard : this.$refs.scannerInput;
      
      if (inputRef && this.luggageOrder?.status !== 'RETRIEVED') {
        this.isScanning = true;
        
        if (document.activeElement === inputRef) {
          inputRef.blur();
          setTimeout(() => {
            inputRef.focus({ preventScroll: true });
          }, 50);
        } else {
          inputRef.focus({ preventScroll: true });
        }
        
        setTimeout(() => {
          this.isScanning = false;
        }, 2000);
      }
    },
    
    // 即時處理輸入（過濾中文字符）
    handleInput() {
      if (this.luggageOrder?.status === 'RETRIEVED' || this.isProcessing) return;
      
      const input = this.scanInput;
      const filtered = input.replace(/[^\x20-\x7E]/g, '');
      if (filtered !== input) {
        this.scanInput = filtered;
      }
      
      // 僅當輸入包含完整JSON且未處理時，觸發掃描
      if (filtered.includes('{') && filtered.includes('}') && !this.isProcessing) {
        setTimeout(() => {
          if (this.useIdCardMode) {
            this.handleIdCardScan();
          } else {
            this.handleScan();
          }
        }, 50);
      }
    },
    
    // 處理掃描邏輯（二維碼模式）
    async handleScan() {
      if (!this.scanInput || this.luggageOrder?.status === 'RETRIEVED' || this.isProcessing) {
        return;
      }
      this.isProcessing = true;
      
      try {
        const cleanedInput = this.scanInput.trim();
        const jsonString = cleanedInput.replace(/[\r\n\s]+/g, '');
        let scannedData = JSON.parse(jsonString);
        
        // 字段名兼容處理
        scannedData = this.normalizeCamelCase(scannedData);
        
        // 驗證二維碼必要欄位
        if (!scannedData.type || !scannedData.luggageId) {
          throw new Error('二維碼格式無效，缺少「type」或「luggageId」');
        }
        
        // 流程控制：先客人憑證，再行李標籤
        if (!this.hasScannedCustomerTicket) {
          if (scannedData.type !== 'customer_ticket') {
            throw new Error('請先掃描「客人取件憑證」');
          }
          await this.processCustomerTicket(scannedData);
        } else if (!this.hasScannedLuggageTag) {
          if (scannedData.type !== 'luggage_tag') {
            throw new Error('請接著掃描「行李標籤」');
          }
          await this.processLuggageTag(scannedData);
        } else {
          this.showNotificationMessage(
            'info', 
            '操作提示', 
            '已完成所有掃描，請工作人員確認取件或點擊重新處理'
          );
        }
        
      } catch (error) {
        console.error('掃描錯誤:', error);
        this.showNotificationMessage('error', '識別失敗', 
          `${error.message}，請檢查二維碼或切換輸入法為英文`);
        this.scanSuccess = false;
      } finally {
        setTimeout(() => {
          this.scanInput = '';
          this.isProcessing = false;
        }, 3000);
      }
    },
    
    // 字段名規範化方法
    normalizeCamelCase(data) {
      if (typeof data !== 'object' || data === null) {
        return data;
      }
      const normalized = {};
      Object.keys(data).forEach(key => {
        const camelKey = key.replace(/_([a-z])/g, (match, letter) => letter.toUpperCase())
                            .replace(/^([a-z])/, (match, letter) => letter.toLowerCase())
                            .replace(/([a-z])([A-Z])/g, (match, lower, upper) => lower + upper);
        const fixedKey = camelKey === 'luggageid' ? 'luggageId' 
                       : camelKey === 'guestname' ? 'guestName' 
                       : camelKey === 'checkintime' ? 'checkinTime' 
                       : camelKey;
        normalized[fixedKey] = data[key];
      });
      if (normalized.checkinTime && typeof normalized.checkinTime === 'string') {
        normalized.checkinTime = normalized.checkinTime.replace('t', 'T');
      }
      return normalized;
    },
    
    // 處理客人取件憑證
    async processCustomerTicket(ticketData) {
      try {
        this.isProcessing = true;
        
        const luggageId = Number(ticketData.luggageId);
        if (isNaN(luggageId)) {
          throw new Error('二維碼中的 luggageId 不是有效數字');
        }
        
        const { data: backendOrder } = await luggageApi.getLuggageById(luggageId);
        
        this.luggageOrder = backendOrder;
        this.customerTicket = {
          ...ticketData,
          luggageId: luggageId,
          checkinTime: ticketData.checkinTime.replace('t', 'T')
        };
        this.hasScannedCustomerTicket = true;
        this.scanSuccess = true;
        
        this.verificationStatus = 'info';
        this.verificationMessage = `已讀取客人憑證（行李ID: ${luggageId}）`;
        this.showNotificationMessage('info', '請繼續操作', '請掃描對應的行李標籤完成匹配');
        
      } catch (error) {
        this.verificationStatus = 'error';
        const errorMsg = error.response?.data?.message || '訂單查詢失敗，請聯繫管理員';
        this.verificationMessage = `客人憑證處理失敗：${errorMsg}`;
        this.showNotificationMessage('error', '操作失敗', errorMsg);
        throw new Error(`訂單查詢錯誤：${errorMsg}`);
      } finally {
        this.isProcessing = false;
      }
    },
    
    // 處理行李標籤掃描
    async processLuggageTag(tagData) {
      try {
        this.isProcessing = true;
        
        const luggageId = Number(tagData.luggageId);
        if (isNaN(luggageId)) {
          throw new Error('行李標籤中的 luggageId 不是有效數字');
        }
        
        // 如果是身份證模式，需要先獲取訂單信息
        if (this.useIdCardMode && !this.luggageOrder) {
          const { data: backendOrder } = await luggageApi.getLuggageById(luggageId);
          this.luggageOrder = backendOrder;
        }
        
        this.luggageTag = {
          ...tagData,
          luggageId: luggageId
        };
        this.hasScannedLuggageTag = true;
        this.scanSuccess = true;
        
        if (this.matchSuccess) {
          this.verificationStatus = 'success';
          this.verificationMessage = '行李標籤掃描成功，請確認取件';
          if( this.luggageOrder?.status === 'RETRIEVED'){
            this.verificationMessage = '行李標籤掃描成功，但該訂單已完成取件';
          }
          this.showNotificationMessage('success', '掃描成功', '行李標籤已掃描，等待確認取件');
        } else {
          this.verificationStatus = 'error';
          this.verificationMessage = '行李ID不匹配，請重新掃描正確的行李標籤';
          this.showNotificationMessage('error', '配對失敗', 
            `當前掃描ID: ${luggageId}`);
        }
        
      } catch (error) {
        this.showNotificationMessage('error', '行李標籤處理失敗', error.message);
      } finally {
        this.isProcessing = false;
      }
    },
    
    // 完成取件
    async retrieveLuggage() {
      if (!this.canRetrieve || this.isProcessing) return;
      this.isProcessing = true;
      
      try {
        const payload = {
          scannedLuggage: [this.luggageTag?.luggageId],
          idCardNumber: this.useIdCardMode ? this.idCardNumber : null
        };
        
        const { data } = await luggageApi.completeRetrieval(this.luggageOrder.id, payload);
        
        if (data.success) {
          this.luggageOrder.status = 'RETRIEVED';
          this.verificationStatus = 'success';
          this.verificationMessage = `訂單已標記為「已取件」}`;
          this.showNotificationMessage(
            'success', 
            '取件完成', 
            `行李ID ${this.luggageOrder.id} 已成功交還，費用已記錄`
          );
        } else {
          throw new Error('後端處理取件失敗，請重試');
        }
        
      } catch (error) {
        this.verificationStatus = 'error';
        const errorMsg = error.response?.data?.message || '取件操作失敗，請聯繫管理員';
        this.verificationMessage = `取件失敗：${errorMsg}`;
        this.showNotificationMessage('error', '操作失敗', errorMsg);
      } finally {
        this.isProcessing = false;
      }
    },
    
    // 重新處理：重置所有狀態
    resetScanner() {
      if (this.focusTimer) {
        clearInterval(this.focusTimer);
      }
      
      this.hasScannedCustomerTicket = false;
      this.hasScannedLuggageTag = false;
      this.customerTicket = null;
      this.luggageTag = null;
      this.luggageOrder = null;
      this.scanInput = '';
      if (!this.useIdCardMode) {
        this.idCardNumber = '';
      }
      this.scanSuccess = false;
      this.verificationStatus = '';
      this.verificationMessage = '';
      this.isProcessing = false;
    }
  },
  mounted() {
    // 組件掛載時的邏輯
  },
  beforeUnmount() {
    if (this.focusTimer) {
      clearInterval(this.focusTimer);
    }
  }
}
</script>

<style scoped>
.modern-scanner {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.scanner-header {
  background: #2c3e50;
  color: white;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-content h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
}

.header-content i {
  font-size: 1.8rem;
}

.status-indicator {
  margin-left: 16px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.status-indicator.idle {
  background: rgba(255, 255, 255, 0.2);
}

.status-indicator.scanning {
  background: #3498db;
}

.status-indicator.success {
  background: #2ecc71;
}

.status-indicator.error {
  background: #e74c3c;
}

.status-indicator.completed {
  background: #9b59b6;
}

.mode-toggle {
  display: flex;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  overflow: hidden;
}

.toggle-btn {
  padding: 8px 16px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
}

.toggle-btn.active {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.toggle-btn:hover:not(.active) {
  background: rgba(255, 255, 255, 0.1);
}

.scanner-body {
  flex: 1;
  display: flex;
  padding: 24px;
  gap: 24px;
  overflow: auto;
}

.scan-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.scan-area {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
}

.scan-area:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.scanner-visual {
  position: relative;
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
}

.scanner-frame {
  width: 150px;
  height: 212px;
  border: 3px solid #3498db;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.laser {
  position: absolute;
  height: 2px;
  width: 100%;
  background: #e74c3c;
  top: 50%;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.laser.active {
  opacity: 1;
  animation: laserScan 2s infinite;
}

@keyframes laserScan {
  0% { transform: translateY(-75px); }
  50% { transform: translateY(75px); }
  100% { transform: translateY(-75px); }
}

.scan-icon {
  font-size: 3rem;
  transition: all 0.3s ease;
}

.scan-icon.idle {
  color: #95a5a6;
}

.scan-icon.scanning {
  color: #3498db;
}

.scan-icon.success {
  color: #2ecc71;
}

.scan-icon.error {
  color: #e74c3c;
}

.scan-icon.completed {
  color: #9b59b6;
}

.scan-instruction {
  text-align: center;
  margin-bottom: 15px;
}

.scan-instruction p {
  font-size: 1rem;
  font-weight: 500;
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.scan-instruction small {
  color: #7f8c8d;
  font-size: 0.85rem;
}

.scanner-input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.id-card-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.id-card-input h4 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.id-card-input p {
  margin: 0 0 15px 0;
  color: #7f8c8d;
}

.input-group {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.input-group input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
}

.input-group input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.id-instruction {
  margin-top: 15px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
  text-align: center;
}

.id-instruction p {
  margin: 0;
  color: #7f8c8d;
  font-size: 0.9rem;
}

.scan-history {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.scan-history h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 1.1rem;
  font-weight: 600;
}

.history-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.item-type {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.item-type.customer {
  color: #3498db;
}

.item-type.luggage {
  color: #2ecc71;
}

.item-type.id-card {
  color: #9b59b6;
}

.item-detail {
  color: #7f8c8d;
  font-size: 0.95rem;
}

.match-result {
  padding-top: 16px;
  border-top: 1px solid #ecf0f1;
}

.result-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border-radius: 8px;
  font-weight: 500;
}

.result-indicator.success {
  background: #e8f6ef;
  color: #27ae60;
}

.result-indicator.error {
  background: #fdedeb;
  color: #e74c3c;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card, .action-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  background: #f8f9fa;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #ecf0f1;
}

.card-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #2c3e50;
}

.card-header i {
  color: #7f8c8d;
  font-size: 1.2rem;
}

.card-body {
  padding: 24px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #95a5a6;
}

.empty-state i {
  font-size: 3rem;
  margin-bottom: 16px;
}

.empty-state h4 {
  margin: 0 0 12px 0;
  font-size: 1.2rem;
}

.empty-state p {
  margin: 0;
  font-size: 0.95rem;
  line-height: 1.5;
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-size: 0.85rem;
  color: #7f8c8d;
  font-weight: 500;
}

.detail-item .value {
  font-size: 1rem;
  color: #2c3e50;
  font-weight: 500;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.status-badge.stored {
  background: #e1f0ff;
  color: #3498db;
}

.status-badge.expired {
  background: #fdeaea;
  color: #e74c3c;
}

.status-badge.retrieved {
  background: #e7f6ef;
  color: #27ae60;
}

.overdue-alert {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #fff4e6;
  border-radius: 8px;
  border-left: 4px solid #f39c12;
}

.alert-icon {
  font-size: 1.5rem;
  color: #f39c12;
}

.alert-content h5 {
  margin: 0 0 8px 0;
  color: #d35400;
  font-size: 1rem;
}

.alert-content p {
  margin: 0 0 4px 0;
  color: #e67e22;
  font-size: 0.9rem;
}

.alert-content .fee {
  font-weight: 500;
}

.alert-content .fee span {
  font-weight: 700;
  color: #c0392b;
}

.verification-status {
  margin-bottom: 20px;
}

.status-message {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 8px;
  font-weight: 500;
}

.status-message.success {
  background: #e8f6ef;
  color: #27ae60;
}

.status-message.error {
  background: #fdedeb;
  color: #e74c3c;
}

.status-message.info {
  background: #e1f0ff;
  color: #3498db;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.btn-primary, .btn-secondary {
  flex: 1;
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.btn-primary:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  opacity: 0.7;
}

.btn-secondary {
  background: #ecf0f1;
  color: #7f8c8d;
}

.btn-secondary:hover:not(:disabled) {
  background: #dde4e6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(189, 195, 199, 0.3);
}

.notification-toast {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 320px;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: flex-start;
  z-index: 1000;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from { transform: translateX(100%); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}

.notification-toast.success {
  background: white;
  border-left: 4px solid #2ecc71;
}

.notification-toast.error {
  background: white;
  border-left: 4px solid #e74c3c;
}

.notification-toast.info {
  background: white;
  border-left: 4px solid #3498db;
}

.toast-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
}

.toast-icon {
  font-size: 1.5rem;
  margin-top: 2px;
}

.notification-toast.success .toast-icon {
  color: #2ecc71;
}

.notification-toast.error .toast-icon {
  color: #e74c3c;
}

.notification-toast.info .toast-icon {
  color: #3498db;
}

.toast-message {
  flex: 1;
}

.toast-title {
  font-weight: 600;
  margin-bottom: 4px;
  color: #2c3e50;
}

.toast-text {
  font-size: 0.9rem;
  color: #7f8c8d;
}

.toast-close {
  background: none;
  border: none;
  color: #bdc3c7;
  cursor: pointer;
  font-size: 1rem;
  margin-left: 12px;
}

.toast-close:hover {
  color: #7f8c8d;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

@media (max-width: 968px) {
  .scanner-body {
    flex-direction: column;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .scanner-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .mode-toggle {
    width: 100%;
  }
  
  .toggle-btn {
    flex: 1;
    justify-content: center;
  }
}

@media (max-width: 576px) {
  .scanner-header {
    padding: 12px 16px;
  }
  
  .header-content h2 {
    font-size: 1.2rem;
  }
  
  .scanner-body {
    padding: 16px;
  }
  
  .scan-area, .id-card-section {
    padding: 15px;
  }
  
  .scanner-frame {
    width: 120px;
    height: 120px;
  }
  
  .scan-icon {
    font-size: 2.5rem;
  }
  
  .input-group {
    flex-direction: column;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .notification-toast {
    width: calc(100% - 48px);
    right: 16px;
    bottom: 16px;
  }
}
</style>