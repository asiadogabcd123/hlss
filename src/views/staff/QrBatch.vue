<template>
  <div class="qr-batch-container">
    <!-- 主內容區 -->
    <div class="main-content">
      <!-- 左側行李列表 -->
      <div class="list-container">
        <!-- 日期選擇器 -->
        <div class="date-selector">
          <div class="date-filter">
            <label for="date-filter">篩選日期：</label>
            <input 
              type="date" 
              id="date-filter" 
              v-model="selectedDate"
              @change="handleDateChange"
              :max="today" 
            >
            <div class="quick-dates">
              <div 
                v-for="date in quickDates" 
                :key="date.value"
                class="quick-date"
                :class="{ 'active': date.value === selectedDate }"
                @click="selectQuickDate(date.value)"
              >
                {{ date.label }}
              </div>
            </div>
          </div>
          <div class="date-actions">
            <button @click="resetDateFilter" class="action-btn reset-btn">
              <i class="fas fa-times"></i> 清除篩選
            </button>
          </div>
          <div class="date-display">
            <span v-if="selectedQuickDate?.type === 'range'">
              顯示範圍：{{ formatDate(selectedDate) }} 至 {{ formatDate(today) }}
            </span>
            <span v-else>
              顯示日期：{{ formattedSelectedDate }}
            </span>
          </div>
        </div>

        <!-- 待打印行李列表 -->
        <div class="list-section">
          <h2><i class="fas fa-print"></i> 待列印行李標籤</h2>
          <div v-if="pendingLuggage.length === 0 && !loading" class="no-data">
            <i class="fas fa-calendar-times"></i> 
            {{ selectedDate ? '該日期無待列印行李' : '暫無待列印行李' }}
          </div>
          <div v-else>
            <div v-for="dateGroup in groupedPendingLuggage" :key="dateGroup.date">
              <h3 class="date-header">{{ formatGroupDate(dateGroup.date) }}</h3>
              <div class="luggage-grid">
                <div 
                  v-for="luggage in dateGroup.items" 
                  :key="luggage.id" 
                  class="luggage-card" 
                  @click="loadLuggage(luggage.id)"
                  :class="{ 'active': luggageInfo?.id === luggage.id }"
                >
                  <div class="luggage-header">
                    <span class="badge new">新</span>
                    <strong>{{ luggage.id }}</strong>
                  </div>
                  <div class="luggage-body">
                    <p><i class="fas fa-user"></i> {{ luggage.guestName }}</p>
                    <p><i class="fas fa-phone"></i> ***-****-{{ getPhoneLast4(luggage.phone) }}</p>
                    <p><i class="fas fa-building"></i> 房間號：{{ luggage.roomNumber || '未填寫' }}</p>
                    <p><i class="fas fa-warehouse"></i> 存儲區域：{{ luggage.storageLocation || '未指定' }}</p>
                    <p><i class="fas fa-suitcase"></i> {{ luggage.luggageCount }} 件行李</p>
                    <p><i class="fas fa-sticky-note"></i> 備註：{{ luggage.remark || '無' }}</p>
                    <p><i class="fas fa-clock"></i> {{ formatTime(luggage.checkinTime) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 可重新打印行李列表 -->
        <div class="list-section">
          <h2><i class="fas fa-redo"></i> 可重新列印行李標籤</h2>
          <div v-if="printedLuggage.length === 0 && !loading" class="no-data">
            <i class="fas fa-calendar-times"></i> 
            {{ selectedDate ? '該日期無可重新列印行李' : '暫無可重新列印行李' }}
          </div>
          <div v-else>
            <div v-for="dateGroup in groupedPrintedLuggage" :key="dateGroup.date">
              <h3 class="date-header">{{ formatGroupDate(dateGroup.date) }}</h3>
              <div class="luggage-grid">
                <div 
                  v-for="luggage in dateGroup.items" 
                  :key="luggage.id" 
                  class="luggage-card" 
                  @click="loadLuggage(luggage.id)"
                  :class="{ 'active': luggageInfo?.id === luggage.id }"
                >
                  <div class="luggage-header">
                    <span class="badge reprinted">重印</span>
                    <strong>{{ luggage.id }}</strong>
                  </div>
                  <div class="luggage-body">
                    <p><i class="fas fa-user"></i> {{ luggage.guestName }}</p>
                    <p><i class="fas fa-phone"></i> ***-****-{{ getPhoneLast4(luggage.phone) }}</p>
                    <p><i class="fas fa-building"></i> 房間號：{{ luggage.roomNumber || '未填寫' }}</p>
                    <p><i class="fas fa-warehouse"></i> 存儲區域：{{ luggage.storageLocation || '未指定' }}</p>
                    <p><i class="fas fa-suitcase"></i> {{ luggage.luggageCount }} 件行李</p>
                    <p><i class="fas fa-sticky-note"></i> 備註：{{ luggage.remark || '無' }}</p>
                    <p><i class="fas fa-clock"></i> {{ formatTime(luggage.checkinTime) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右側固定預覽區 -->
      <div class="preview-sidebar" :class="{ 'has-content': luggageInfo }">
        <div v-if="luggageInfo" class="preview-content">
          <div class="preview-header">
            <h2><i class="fas fa-tags"></i> 行李標籤預覽 - {{ luggageInfo.id }}</h2>
            <div class="guest-info">
              <p><i class="fas fa-user"></i> {{ luggageInfo.guestName }}</p>
              <p><i class="fas fa-phone"></i> ***-****-{{ luggageInfo.phoneLast4 }}</p>
              <p><i class="fas fa-building"></i> 房間號：{{ luggageInfo.roomNumber || '未填寫' }}</p>
              <p><i class="fas fa-warehouse"></i> 存儲區域：{{ luggageInfo.storageLocation || '未指定' }}</p>
              <p><i class="fas fa-suitcase"></i> {{ luggageInfo.luggageCount }} 件行李</p>
              <p><i class="fas fa-sticky-note"></i> 備註：{{ luggageInfo.remark || '無' }}</p>
            </div>
          </div>
          
          <div class="qr-grid">
            <button @click="printTickets" class="print-btn">
              <i class="fas fa-print"></i> 列印兩種標籤
            </button>

            <div class="qr-card">
              <img :src="customerQRCode" alt="客人取件憑證二維碼">
              <div class="qr-meta">
                <p>客人取件憑證</p>
              </div>
            </div>
            
            <div class="qr-card">
              <img :src="luggageQRCode" alt="行李標籤二維碼">
              <div class="qr-meta">
                <p>行李存放標籤</p>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else class="empty-preview">
          <i class="fas fa-info-circle"></i>
          <p>請從左側選擇行李以預覽標籤</p>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-tw'
import luggageApi from '@/api/luggage'

// 設置dayjs本地化
dayjs.locale('zh-tw')

export default {
  name: 'QrBatch',
  setup() {
    // 響應式數據
    const customerQRCode = ref('')
    const luggageQRCode = ref('')
    const luggageInfo = ref(null)
    const pendingLuggage = ref([])
    const printedLuggage = ref([])
    const selectedDate = ref('')
    const loading = ref(false)
    const today = ref(dayjs().format('YYYY-MM-DD'))
    const refreshTimer = ref(null) // 定時器實例
    const isRequesting = ref(false) // 防止重複請求標記

    const quickDates = ref([
      { label: '今天', value: today.value, type: 'day' },
      { label: '昨天', value: dayjs().subtract(1, 'day').format('YYYY-MM-DD'), type: 'day' },
      { label: '本週', value: dayjs().startOf('week').format('YYYY-MM-DD'), type: 'range' },
      { label: '本月', value: dayjs().startOf('month').format('YYYY-MM-DD'), type: 'range' }
    ])

    // 當前選擇的快捷日期
    const selectedQuickDate = computed(() => {
      return quickDates.value.find(date => date.value === selectedDate.value)
    })

    // 格式化日期顯示
    const formattedSelectedDate = computed(() => {
      if (!selectedDate.value) return '所有日期'
      
      const date = dayjs(selectedDate.value)
      const today = dayjs()
      
      // 根據日期類型返回不同格式
      if (date.isSame(today, 'day')) {
        return '今天 (' + date.format('MM/DD') + ')'
      } 
      else if (date.isSame(today.subtract(1, 'day'), 'day')) {
        return '昨天 (' + date.format('MM/DD') + ')'
      }
      else if (date.isSame(today, 'week')) {
        return '本週 (' + date.format('MM/DD') + ' ~ ' + today.format('MM/DD') + ')'
      }
      else if (date.isSame(today, 'month')) {
        return '本月 (' + date.format('MM/01') + ' ~ ' + today.format('MM/DD') + ')'
      }
      else {
        return date.format('YYYY年MM月DD日')
      }
    })

    // 按日期分組行李
    const groupByDate = (luggageList) => {
      const groups = {}
      
      luggageList.forEach(item => {
        const date = dayjs(item.checkinTime).format('YYYY-MM-DD')
        
        if (!groups[date]) {
          groups[date] = []
        }
        
        groups[date].push(item)
      })
      
      // 轉換為數組並按日期排序（最近日期在前）
      return Object.keys(groups)
        .map(date => ({
          date,
          items: groups[date].sort((a, b) => 
            new Date(b.checkinTime) - new Date(a.checkinTime)
        )}))
        .sort((a, b) => new Date(b.date) - new Date(a.date))
    }

    // 計算分組後的行李
    const groupedPendingLuggage = computed(() => {
      return groupByDate(pendingLuggage.value)
    })

    const groupedPrintedLuggage = computed(() => {
      return groupByDate(printedLuggage.value)
    })

    // 格式化日期標題
    const formatGroupDate = (dateString) => {
      const today = dayjs().format('YYYY-MM-DD')
      const yesterday = dayjs().subtract(1, 'day').format('YYYY-MM-DD')
      
      if (dateString === today) {
        return '今天 (' + dayjs(dateString).format('MM/DD') + ')'
      } else if (dateString === yesterday) {
        return '昨天 (' + dayjs(dateString).format('MM/DD') + ')'
      } else {
        return dayjs(dateString).format('YYYY年MM月DD日')
      }
    }

    // 格式化時間
    const formatTime = (dateTime) => {
      return dayjs(dateTime).format('HH:mm')
    }

    // 格式化日期
    const formatDate = (dateString) => {
      return dayjs(dateString).format('YYYY年MM月DD日')
    }

    // 提取電話號碼後四位
    const getPhoneLast4 = (phone) => {
      // 處理空值或非字符串的情況
      if (!phone || typeof phone !== 'string') {
        return '0000'
      }
      
      // 移除所有非數字字符
      const cleaned = phone.replace(/\D/g, '')
      
      // 如果長度不足4位，返回全部
      if (cleaned.length < 4) {
        return cleaned || '0000'
      }
      
      // 返回最後四位
      return cleaned.slice(-4)
    }

    // 重置日期篩選
    const resetDateFilter = () => {
      selectedDate.value = ''
      handleDateChange()
    }

    // 選擇快捷日期
    const selectQuickDate = (date) => {
      selectedDate.value = date
      handleDateChange()
    }

    // 日期變更時處理
    const handleDateChange = () => {
      // 立即請求新數據
      fetchLuggageData()
      // 重置定時器，確保按新條件刷新
      restartRefreshTimer()
    }

    // 核心方法：請求待列印和可重列印數據（考慮當前篩選條件）
    const fetchLuggageData = async () => {
      // 防止重複請求
      if (isRequesting.value) {
        console.log('已有請求在處理中，跳過本次定時請求')
        return
      }
      
      try {
        isRequesting.value = true
        loading.value = true
        
        // 構建符合當前篩選條件的請求參數
        const params = {}
        
        if (selectedDate.value) {
          if (selectedQuickDate.value?.type === 'range') {
            // 範圍查詢 (從選擇日期到今天)
            params.startDate = selectedDate.value
            params.endDate = today.value
          } else {
            // 單日查詢
            params.date = selectedDate.value
          }
        }
        
        console.log('發送數據請求，參數:', params)
        
        // 併發請求待列印和可重列印數據
        const [pendingRes, printedRes] = await Promise.all([
          luggageApi.getPendingForQR(params),
          luggageApi.getPrintedLuggage(params)
        ])
        
        // 檢查返回數據是否為數組
        if (!Array.isArray(pendingRes.data)) {
          console.error('待列印數據格式錯誤，期望數組:', pendingRes.data)
          return
        }
        
        if (!Array.isArray(printedRes.data)) {
          console.error('可重列印數據格式錯誤，期望數組:', printedRes.data)
          return
        }
        
        // 強制更新數組以觸發Vue重新渲染
        pendingLuggage.value = [...pendingRes.data]
        printedLuggage.value = [...printedRes.data]
        
        console.log('數據刷新完成 - 待列印:', pendingLuggage.value.length, '可重列印:', printedLuggage.value.length)
        
      } catch (error) {
        console.error('數據請求失敗:', error)
      } finally {
        loading.value = false
        isRequesting.value = false
      }
    }

    // 重置並啟動定時器
    const restartRefreshTimer = () => {
      // 清除現有定時器
      if (refreshTimer.value) {
        clearInterval(refreshTimer.value)
        console.log('已清除舊定時器')
      }
      
      // 設置新的3秒定時器
      refreshTimer.value = setInterval(() => {
        console.log('定時器觸發，刷新數據')
        fetchLuggageData()
      }, 3000)
      
      console.log('已啟動新定時器，每3秒刷新一次')
    }

    // 加載指定行李（預覽）- 新增备注字段
    const loadLuggage = async (luggageId) => {
      try {
        loading.value = true
        
        // 只獲取行李詳情
        const detailRes = await luggageApi.getLuggageById(luggageId)
        const luggageDetail = detailRes.data
        
        // 更新UI數據：新增 remark（备注）字段
        luggageInfo.value = {
          id: luggageDetail.id,
          guestName: luggageDetail.guestName,
          phoneLast4: getPhoneLast4(luggageDetail.phone),
          luggageCount: luggageDetail.luggageCount,
          checkinTime: luggageDetail.checkinTime,
          roomNumber: luggageDetail.roomNumber || '', // 房間號（可能為空）
          storageLocation: luggageDetail.storageLocation || '', // 存儲區域（可能為空）
          remark: luggageDetail.remark || '' // 新增：备注（可能為空）
        };
        
        // 生成兩種二維碼（包含新字段）
        await generateQRCodes(luggageDetail)
        
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
        
        alert(`${errorMsg}，請稍後重試`)
      } finally {
        loading.value = false
      }
    }

    // 生成二維碼（包含存儲區域與房間號）
    const generateQRCodes = (luggageDetail) => {
      try {
        // 直接使用前端生成二維碼
        generateFrontendQRCodes(luggageDetail);
      } catch (error) {
        console.error('前端生成二維碼失敗:', error)
        generateExternalServiceQRCodes(luggageDetail);
      }
    }

    const generateFrontendQRCodes = (luggageDetail) => {
      // 二維碼內容可選：是否包含备注（这里暂不包含，如需包含可添加 remark 字段）
      const customerContent = JSON.stringify({
        type: "customer_ticket",
        luggageId: luggageDetail.id,
      });
      
      const luggageContent = JSON.stringify({
        type: "luggage_tag",
        luggageId: luggageDetail.id,
      });
      
      const qrcode = require('qrcode-generator');
      
      const customerQr = qrcode(0, 'M');
      customerQr.addData(customerContent);
      customerQr.make();
      customerQRCode.value = customerQr.createDataURL(6);
      
      // 生成行李二維碼
      const luggageQr = qrcode(0, 'M');
      luggageQr.addData(luggageContent);
      luggageQr.make();
      luggageQRCode.value = luggageQr.createDataURL(6);
    }

    // 外部服務生成二維碼（備用方案，包含新字段）
    const generateExternalServiceQRCodes = (luggageDetail) => {
      // 客人取件憑證二維碼內容（可選添加 remark）
      const customerContent = JSON.stringify({
        type: "customer_ticket",
        luggageId: luggageDetail.id,
        guestName: luggageDetail.guestName,
        remark: luggageDetail.remark || '無' // 可選：加入备注
      });
      
      // 行李標籤二維碼內容（可選添加 remark）
      const luggageContent = JSON.stringify({
        type: "luggage_tag",
        luggageId: luggageDetail.id,
        guestName: luggageDetail.guestName,
        remark: luggageDetail.remark || '無' // 可選：加入备注
      });
      
      // 使用外部服務生成二維碼圖片
      customerQRCode.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(customerContent)}`;
      luggageQRCode.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(luggageContent)}`;
    }

    // 打印兩種小票（新增备注显示）
    const printTickets = async () => {
      try {
        const luggageId = luggageInfo.value.id;
        
        // 檢查是否已經打印過
        const alreadyPrinted = printedLuggage.value.some(item => item.id === luggageId);
        
        // 如果尚未打印，則標記為已打印
        if (!alreadyPrinted) {
          try {
            // 調用標記為已打印API
            await luggageApi.markAsQrGenerated(luggageId);
          } catch (apiError) {
            console.error('標記打印狀態失敗:', apiError);
          }
          
          // 更新本地列表狀態
          const pendingIndex = pendingLuggage.value.findIndex(item => item.id === luggageId);
          if (pendingIndex !== -1) {
            const [movedItem] = pendingLuggage.value.splice(pendingIndex, 1);
            printedLuggage.value.push(movedItem);
          }
        }
        
        // 生成兩種小票的HTML內容（新增备注）
        const customerTicket = generateCustomerTicket();
        const luggageTag = generateLuggageTag();
        
        // 合併兩種小票
        const printContent = customerTicket + luggageTag;
        
        // 使用瀏覽器打印功能
        const printWindow = window.open('', '_blank');
        printWindow.document.write(`
          <!DOCTYPE html>
          <html>
          <head>
            <title>列印行李標籤</title>
            <style>
              @page { size: auto; margin: 5mm; }
              body { font-family: Arial, sans-serif; }
              .ticket { 
                page-break-after: always; 
                padding: 10px; 
                border: 1px dashed #ccc; 
                margin-bottom: 10px;
                text-align: center;
                width: 80mm;
              }
              .ticket:last-child { 
                page-break-after: auto; 
              }
              .hotel-logo {
                max-width: 60px;
                margin-bottom: 5px;
              }
              .ticket-header {
                font-weight: bold;
                font-size: 16px;
                margin-bottom: 8px;
              }
              .ticket-info {
                font-size: 20px;
                margin: 4px 0;
                text-align: left;
              }
              .ticket-qr {
                width: 180px;
                height: 180px;
              }
              .terms {
                font-size: 13px;
                text-align: left;
                margin-top: 10px;
              }
              .ticket-info.large-print {
                font-size: 30pt; 
                font-weight: bold;
                margin: 10px 0; 
                word-break: break-all; 
              }
              .field-label {
                color: #222;
                min-width: 90px;
                display: inline-block;
              }
            </style>
          </head>
          <body>
            ${printContent}
          </body>
          </html>
        `);
        printWindow.document.close();
        
        // 等待內容加載完成後打印
        printWindow.onload = function() {
          printWindow.print();
          // 打印後關閉窗口
          setTimeout(() => {
            printWindow.close();
          }, 100);
        };
        
      } catch (error) {
        console.error('列印失敗:', error)
        alert('列印失敗，請稍後重試')
      }
    }

    // 生成客人小票HTML（新增备注）
    const generateCustomerTicket = () => {
      // 處理空值顯示
      const roomNumber = luggageInfo.value.roomNumber || '未填寫';
      const storageLocation = luggageInfo.value.storageLocation || '未指定';
      const remark = luggageInfo.value.remark || '無'; // 处理备注空值
      
      return `
        <div class="ticket">
          <div class="ticket-header">
            <img src="/images/new_orient_group.jpg" class="hotel-logo"> 
            <div>行李寄存憑證</div>
          </div>
          <div class="divider"></div>
          <div class="ticket-info large-print">
            <span class="field-label">行李ID:</span> 
            <span>${luggageInfo.value.id}</span>
          </div>
          <div class="ticket-info">
            <span class="field-label">客人姓名:</span> 
            <span>${luggageInfo.value.guestName}</span>
          </div>
          <div class="ticket-info">
            <span class="field-label">手機尾號:</span> 
            <span>${luggageInfo.value.phoneLast4}</span>
          </div>
          <div class="ticket-info">
            <span class="field-label">房間號:</span> 
            <span>${roomNumber}</span>
          </div>
          <div class="ticket-info">
            <span class="field-label">存儲區域:</span> 
            <span>${storageLocation}</span>
          </div>
          <div class="ticket-info">
            <span class="field-label">行李件數:</span>   
            <span>${luggageInfo.value.luggageCount} </span>
          </div>
          <div class="ticket-info">
            <span class="field-label">行李備註:</span> 
            <span>${remark}</span>
          </div>
          <div class="ticket-info">
            <span class="field-label">寄存時間:</span> 
            <span>${dayjs(luggageInfo.value.checkinTime).format('YYYY-MM-DD HH:mm:ss')}</span>
          </div>
          <div class="ticket-info">
            <span class="field-label">客戶簽名:</span>
            <span style="border-bottom: 1px solid #333; width: 180px; display: inline-block; margin-left: 10px;"></span>
          </div>
          <img src="${customerQRCode.value}" alt="取件二維碼" class="ticket-qr">
          <div class="terms">
            <div><strong>服務條款:</strong></div>
            <div>1.This is our policy to keep Hotel guest's luggage in our storeroom free of charge for no more than three days. We will levy a storage charge of MOP20.00 per day on each item thereafter until they are being claimed.</div>
            <div>2.The Hotel will not be held responsible for any loss or damage of the luggage deposited or articles contained in them.</div>
            <div>3.The Hotel reserves the right to dispose any unclaimed luggage held in our storage for more than FIFTEEN DAYS without any liability.</div>
            <div>4.By using this service, guests acknowledge and agree to these terms.</div>
            <div>1.本酒店可代客人免費存放行李在貯物室，為期不超過三天，如超過限期，本酒店將按日徵收每件澳門幣貳拾圓正作為貯存費，直至領回為止。</div>
            <div>2.本酒店在任何情況下不負責行李之損壞或遺失。</div>
            <div>3.所有寄存物件，如在十五天內無人認領，本酒店有權處理該物品且無須承擔任何賠償責任。</div>
            <div>4.使用此服務即表示已閱讀並同意上述條款。</div>
          </div>
        </div>
      `;
    }

    // 生成行李標籤HTML（新增备注）
    const generateLuggageTag = () => {
      // 處理空值顯示
      const roomNumber = luggageInfo.value.roomNumber || '未填寫';
      const storageLocation = luggageInfo.value.storageLocation || '未指定';
      const remark = luggageInfo.value.remark || '無'; // 处理备注空值
      
      return `
      <div class="ticket">
        <div class="ticket-header">
          <img src="/images/new_orient_group.jpg" class="hotel-logo"> 
          <div>行李存放標籤</div>
        </div>
        <div class="divider"></div>
        <div class="ticket-info large-print">
          <span class="field-label">行李ID:</span> 
          <span>${luggageInfo.value.id}</span>
        </div>
        <div class="ticket-info">
          <span class="field-label">客人姓名:</span> 
          <span>${luggageInfo.value.guestName}</span>
        </div>
        <div class="ticket-info">
          <span class="field-label">房間號:</span> 
          <span>${roomNumber}</span>
        </div>
        <div class="ticket-info">
          <span class="field-label">存儲區域:</span> 
          <span>${storageLocation}</span>
        </div>
        <div class="ticket-info">
          <span class="field-label">行李件數:</span> 
          <span>${luggageInfo.value.luggageCount} &nbsp&nbsp;</span>
        </div>
        <div class="ticket-info">
          <span class="field-label">行李備註:</span> 
          <span>${remark}</span>
        </div>
        <div class="ticket-info">
          <span class="field-label">寄存時間:</span> 
          <span>${dayjs(luggageInfo.value.checkinTime).format('YYYY-MM-DD HH:mm:ss')}</span>
        </div>
        <div class="ticket-info">
          <span class="field-label">客戶簽名:</span>
          <span style="border-bottom: 1px solid #333; width: 180px; display: inline-block; margin-left: 10px;"></span>
        </div>
        <img src="${luggageQRCode.value}" alt="行李二維碼" class="ticket-qr">
      </div>
      `;
    };

    // 監聽日期變化
    watch(selectedDate, (newDate) => {
      if (newDate && newDate > today.value) {
        selectedDate.value = today.value
        alert('無法選擇未來日期，已自動切換為今天')
      }
    })

    // 組件初始化
    onMounted(() => {
      // 預設顯示今天的行李
      selectedDate.value = today.value
      // 初始加載數據
      fetchLuggageData()
      // 啟動3秒定時刷新
      restartRefreshTimer()
    })

    // 組件銷毀時清除定時器，防止內存洩漏
    onUnmounted(() => {
      if (refreshTimer.value) {
        clearInterval(refreshTimer.value)
        console.log('組件銷毀，已清除定時器')
      }
    })
    
    return {
      customerQRCode,
      luggageQRCode,
      luggageInfo,
      pendingLuggage,
      printedLuggage,
      selectedDate,
      loading,
      today,
      quickDates,
      selectedQuickDate,
      formattedSelectedDate,
      groupedPendingLuggage,
      groupedPrintedLuggage,
      loadLuggage,
      printTickets,
      resetDateFilter,
      selectQuickDate,
      handleDateChange,
      formatGroupDate,
      formatTime,
      formatDate,
      getPhoneLast4
    }
  }
}
</script>

<style scoped>
/* 原有樣式不變，新增备注图标样式（可选，确保图标显示正常） */
.luggage-body i.fa-sticky-note {
  color: #f59e0b; /* 用黄色图标区分备注，与重印标签颜色一致 */
}

.guest-info i.fa-sticky-note {
  color: #f59e0b;
}

/* 原有樣式保持不變 */
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

.qr-batch-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  padding: 20px;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
  gap: 20px;
  max-height: calc(100vh - 120px);
}

.list-container {
  flex: 1;
  overflow-y: auto;
  padding-right: 10px;
  display: flex;
  flex-direction: column;
  border-radius: var(--border-radius);
  padding: 20px;
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(99, 102, 241, 0.2);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);
}

.preview-sidebar {
  width: 380px;
  border-radius: var(--border-radius);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  position: relative;
  transition: var(--transition);
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(99, 102, 241, 0.2);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);
}

.preview-sidebar.has-content {
  border-left: 4px solid #6366f1;
}

.empty-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--gray);
  text-align: center;
  padding: 40px;
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
}

.no-data i {
  margin-right: 10px;
  font-size: 1.8rem;
  color: #475569;
}

.luggage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
  margin-top: 18px;
  flex: 1;
  overflow-y: auto;
  padding-bottom: 10px;
}

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
  flex-direction: column;
  gap: 8px;
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
  font-size: 1.rem;
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

@media (max-width: 1200px) {
  .qr-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }
}

@media (max-width: 992px) {
  .main-content {
    flex-direction: column;
    padding: 0;
  }
  
  .preview-sidebar {
    width: 100%;
    max-height: 50vh;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 100;
    box-shadow: 0 -5px 15px rgba(0, 0, 0, 0.3);
    border-radius: 16px 16px 0 0;
  }
  
  .list-container {
    padding-right: 0;
    padding-bottom: 15px;
  }
  
  .luggage-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}

@media (max-width: 768px) {
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
}

@media (max-width: 480px) {
  .header {
    padding: 15px;
  }
  
  .header h1 {
    font-size: 1.3rem;
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
}
</style>