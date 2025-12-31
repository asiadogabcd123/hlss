<template>
  <div class="access-container">
    <div class="access-card">
      <div class="logo">
        <i class="fas fa-suitcase"></i>
        <h1>行李寄存服務 | Luggage寄存服務</h1>
      </div>

      <!-- 提交中/打印中loading畫面 -->
      <div v-if="isSubmitting || isPrinting" class="loading-section">
        <div class="spinner"></div>
        <p>{{ isSubmitting ? '正在提交資料...' : '後端正在打印憑證...' }}</p>
        <p>{{ isSubmitting ? 'Submitting data...' : 'Backend is printing voucher...' }}</p>
      </div>

      <!-- 表單畫面 -->
      <form 
        @submit.prevent="submitForm"
        class="access-form" 
        v-if="!submissionSuccess && !isSubmitting && !isPrinting"
      >
        <!-- 姓名 -->
        <div class="form-group">
          <label for="name" class="required-field">姓名 | Name</label>
          <input
            id="name"
            v-model="form.name"
            type="text"
            placeholder="請輸入客人姓名 | Please enter your name"
            required
            :disabled="isSubmitting || isPrinting"
          >
        </div>

        <!-- 手機號碼 -->
        <div class="form-group">
          <label for="phone">手機號碼 | Phone Number 
            <span class="optional">(選填 | Optional)</span>
          </label>
          <input
            id="phone"
            v-model="form.phone"
            type="tel"
            placeholder="請輸入手機號碼 | Please enter your phone number"
            :disabled="isSubmitting || isPrinting"
          >
        </div>

        <!-- 房間號碼 -->
        <div class="form-group">
          <label for="roomNumber">房間號碼 | Room Number 
            <span class="optional">(選填 | Optional)</span>
          </label>
          <input
            id="roomNumber"
            v-model="form.roomNumber"
            type="text"
            placeholder="請輸入房間號碼 | Please enter your room number"
            :disabled="isSubmitting || isPrinting"
          >
        </div>

        <!-- 存放位置 -->
        <div class="form-group">
          <label for="storageLocation" class="required-field">存放位置 | Storage Location</label>
          <input
            id="storageLocation"
            v-model="form.storageLocation"
            type="text"
            placeholder="請輸入存放位置"
            required
            :disabled="isSubmitting || isPrinting"
          >
        </div>

        <!-- 行李件數 -->
        <div class="form-group">
          <label for="luggageCount" class="required-field">行李件數 | Number of Luggage</label>
          <input
            id="luggageCount"
            v-model="form.luggageCount"
            type="number"
            min="1"
            max="40"
            required
            :disabled="isSubmitting || isPrinting"
          >
        </div>

        <div class="form-group">
          <label for="remark">備註 | Remark 
            <span class="optional">(選填 | Optional)</span>
          </label>
          <textarea
            id="remark"
            v-model="form.remark"
            rows="1"
            :disabled="isSubmitting || isPrinting"
          ></textarea>
        </div>

        <button type="submit" class="btn-submit" :disabled="isSubmitting || isPrinting">
          <i class="fas fa-check-circle"></i> 提交寄存申請 | Submit Request
        </button>
      </form>

      <!-- 提交成功畫面 -->
      <div v-if="submissionSuccess && !isSubmitting && !isPrinting" class="success-section">
        <div class="success-icon">
          <i class="fas fa-check-circle"></i>
        </div>
        <h2>寄存申請成功！</h2>
        <h2>Request Submitted Successfully!</h2>
        
        <p>已成功儲存資料，後端正在打印憑證</p>
        <p>Data saved successfully, backend is printing voucher</p>
        
        <p v-if="printStatus === 'success'" class="print-success">
          <i class="fas fa-print"></i> 憑證打印成功
        </p>
        <p v-if="printStatus === 'failed'" class="print-failed">
          <i class="fas fa-exclamation-triangle"></i> 打印失敗，請聯繫工作人員
        </p>
        
        <div class="countdown">
          頁面將在 {{ countdown }} 秒後重新整理...
          <br>
          Page will refresh in {{ countdown }} seconds...
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-tw' 
import luggageApi from '@/api/luggage'  // 引入行李相關API
import { useAuthStore } from '@/stores/auth' 

dayjs.locale('zh-tw')

export default {
  name: 'GuestAccess',
  data() {
    return {
      // 表單資料：新增 remark 字段（預設空字符串）
      form: {
        name: '',
        phone: '',
        roomNumber: '',
        storageLocation: '',
        luggageCount: 1, // 預設1件行李
        remark: '' // 新增：備註（選填）
      },
      // 狀態管理（不變）
      isSubmitting: false, 
      isPrinting: false, 
      submissionSuccess: false, 
      printStatus: '', 
      countdown: 2, 
      countdownInterval: null,
      savedLuggageRecord: null
    }
  },
  methods: {
    /**
     * 核心流程：提交表單 → 儲存資料庫 → 請求後端打印
     */
    async submitForm() {
      try {
        this.isSubmitting = true
        const authStore = useAuthStore()
        const authToken = authStore.token || localStorage.getItem('token')

        if (authToken) {
          luggageApi.setAuthToken(authToken)
          console.log('已攜帶權限Token')
        } else {
          console.warn('未取得權限Token，可能導致403錯誤')
        }

        // 2. 準備提交資料：新增 remark 字段到 payload 中
        const payload = {
          guestName: this.form.name.trim(),
          phone: this.form.phone.trim() || null,
          roomNumber: this.form.roomNumber.trim() || null,
          storageLocation: this.form.storageLocation.trim(),
          luggageCount: this.form.luggageCount,
          remark: this.form.remark.trim() || null, // 新增：提交備註（空則傳null）
          checkinTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
        }
        console.log('提交行李寄存資料（含備註）:', payload)

        // 3. 呼叫後端API：建立寄存記錄（後端需對應接收remark字段）
        const response = await luggageApi.create(payload)
        
        if (!response || !response.data) {
          throw new Error('後端未返回有效資料，請檢查API連線')
        }

        const luggageId = response.data.id 
        if (!luggageId) {
          throw new Error(`未獲取到有效的行李編號`)
        }

        // 儲存記錄：新增 remark 字段
        this.savedLuggageRecord = {
          ...response.data,
          luggageId: luggageId,
          guestName: this.form.name,
          phone: this.form.phone,
          roomNumber: this.form.roomNumber ,
          storageLocation: this.form.storageLocation,
          luggageCount: this.form.luggageCount,
          remark: this.form.remark, // 新增：保存備註到本地記錄
          checkinTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
        }
        console.log('成功獲取行李記錄（含備註）:', this.savedLuggageRecord)

        // 後續流程（標記QR、列印）不變
        console.log(`調用markAsPrinted接口，ID: ${this.savedLuggageRecord.luggageId}`)
        await luggageApi.markAsQrGenerated(this.savedLuggageRecord.luggageId)
        console.log('QR標籤已成功標記為已列印狀態')

        this.isPrinting = true
        this.submissionSuccess = true
        console.log(`請求後端打印，行李ID: ${this.savedLuggageRecord.luggageId}`)
        
        const printResponse = await luggageApi.printVoucher(this.savedLuggageRecord.luggageId)
        
        if (printResponse.data.success) {
          this.printStatus = 'success'
          ElMessage.success('後端打印成功')
        } else {
          this.printStatus = 'failed'
          ElMessage.warning('後端打印失敗，請聯繫工作人員')
        }

        this.isPrinting = false
        this.startCountdown()

      } catch (error) {
        // 錯誤處理（不變）
        console.error('提交或打印失敗詳情:', error)
        let errorMsg = '操作失敗，請稍後再試'
        
        if (error.response) {
          const status = error.response.status
          const errorData = error.response.data

          if (status === 403) {
            errorMsg = '權限不足（403）：請確認您有提交行李及打印的權限，或重新登入'
            ElMessageBox.confirm(
              '權限已過期或不足，是否前往登入頁面？',
              '權限錯誤',
              {
                confirmButtonText: '前往登入',
                cancelButtonText: '稍後再試',
                type: 'warning'
              }
            ).then(() => {
              this.$router.push('/login')
            }).catch(() => {
              // 使用者取消
            })
          } else if (status === 404) {
            errorMsg = `接口不存在（404）：請確認接口路徑是否正確`
          } else if (status === 400) {
            errorMsg = `參數錯誤（400）：${errorData.message || '請檢查表單填寫格式'}`
          } else if (status === 500) {
            errorMsg = '伺服器錯誤（500）：請聯繫管理員處理'
          }
        }

        ElMessage.error({
          message: errorMsg,
          duration: 6000
        })

        this.isSubmitting = false
        this.isPrinting = false
      }
    },

    // 倒計時方法（不變）
    startCountdown() {
      if (this.countdownInterval) {
        clearInterval(this.countdownInterval);
      }
      
      this.countdownInterval = setInterval(() => {
        this.countdown--;
        
        if (this.countdown <= 0) {
          clearInterval(this.countdownInterval);
          window.location.reload();
        }
      }, 1000);
    }
  },
  beforeUnmount() {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }
  }
}
</script>

<style scoped>
/* 原有樣式不變，新增備註文本框樣式 */
.form-group textarea {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  transition: border 0.3s;
  resize: none; /* 禁止拉伸文本框 */
  font-family: 'Microsoft JhengHei', 'Noto Sans TC', sans-serif; /* 統一字体 */
}

.form-group textarea:disabled {
  background-color: #f8f9fa;
  color: #6c757d;
  cursor: not-allowed;
  border-color: #dee2e6;
}

.form-group textarea:focus:not(:disabled) {
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2);
  outline: none;
}

/* 原有樣式保持不變 */
.access-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #1a2a6c, #b21f1f);
  font-family: 'Microsoft JhengHei', 'Noto Sans TC', sans-serif;
}

.access-card {
  background: white;
  border-radius: 15px;
  padding: 40px;
  width: 100%;
  max-width: 600px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.logo {
  text-align: center;
  margin-bottom: 30px;
  color: #2c3e50;
}

.logo i {
  font-size: 3.5rem;
  margin-bottom: 15px;
  color: #3498db;
}

.logo h1 {
  font-size: 1.8rem;
  margin: 0;
}

.loading-section {
  text-align: center;
  padding: 40px 20px;
}

.spinner {
  width: 50px;
  height: 50px;
  margin: 0 auto 20px;
  border: 5px solid #f3f3f3;
  border-top: 5px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.access-form {
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #3498db;
}

.form-group input[type="text"],
.form-group input[type="tel"],
.form-group input[type="number"] {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  transition: border 0.3s;
}

.form-group input:disabled {
  background-color: #f8f9fa;
  color: #6c757d;
  cursor: not-allowed;
  border-color: #dee2e6;
}

.form-group input:focus:not(:disabled) {
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2);
  outline: none;
}

.required-field::after {
  content: " *";
  color: #e74c3c;
}

.optional {
  color: #7f8c8d;
  font-weight: normal;
  font-size: 0.9em;
}

.btn-submit {
  width: 100%;
  padding: 14px;
  background: linear-gradient(145deg, #2ecc71, #27ae60);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 10px rgba(46, 204, 113, 0.3);
}

.btn-submit:disabled {
  background: linear-gradient(145deg, #95d5b2, #74c69d);
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.btn-submit:hover:not(:disabled) {
  background: linear-gradient(145deg, #27ae60, #219653);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(46, 204, 113, 0.4);
}

.success-section {
  text-align: center;
  padding: 30px 20px;
  margin-bottom: 30px;
  background: #f8f9fa;
  border-radius: 10px;
  border: 1px solid #e9ecef;
}

.success-icon {
  font-size: 4rem;
  color: #2ecc71;
  margin-bottom: 20px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

.success-section h2 {
  color: #2c3e50;
  margin-bottom: 15px;
  font-size: 1.5rem;
}

.print-success {
  color: #27ae60;
  font-weight: bold;
  margin: 15px 0;
  font-size: 1.1rem;
}

.print-failed {
  color: #e74c3c;
  font-weight: bold;
  margin: 15px 0;
  font-size: 1.1rem;
}

.countdown {
  font-size: 1.1rem;
  color: #e74c3c;
  margin-top: 20px;
  font-weight: bold;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .access-card {
    padding: 25px;
  }
  
  .logo i {
    font-size: 3rem;
  }
  
  .logo h1 {
    font-size: 1.5rem;
  }
  
  .countdown {
    font-size: 1rem;
  }
  
  .form-group {
    margin-bottom: 1.2rem;
  }
}

@media (max-width: 480px) {
  .access-container {
    padding: 15px;
  }
  
  .access-card {
    padding: 15px;
  }
  
  .logo h1 {
    font-size: 1.3rem;
  }
  
  .btn-submit {
    padding: 12px;
    font-size: 1rem;
  }
}
</style>