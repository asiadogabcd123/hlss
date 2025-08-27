<template>
  <div class="access-container">
    <div class="access-card">
      <div class="logo">
        <i class="fas fa-suitcase"></i>
        <h1>行李寄存服務 | Luggage Storage Service</h1>
      </div>

      <div class="terms-section">
        <h3><i class="fas fa-file-contract"></i> 寄存條款 | Terms & Conditions</h3>
        <div class="terms-content">
          <p class="english-terms">
            This is our policy to keep Hotel guest's luggage in our storeroom free of charge for no more than three days. We will levy a storage charge of MOP20.00 per day on each item thereafter until they are being claimed.<br><br>
            The Hotel will not be held responsible for any loss or damage of the luggage deposited or articles contained in them.<br><br>
            The Hotel reserves the right to dispose any unclaimed luggage held in our storage for more than FIFTEEN DAYS without any liability.<br><br>
            By using this service, guests acknowledge and agree to these terms.
          </p>
          <p class="chinese-terms">
            本酒店可代客人免費存放行李在貯物室，為期不超過三天，如超過限期，本酒店將按日徵收每件澳門幣貳拾圓正作為貯存費，直至閣下領回貯物。<br><br>
            本酒店在任何情況下不負責行李之損壞或遺失。<br><br>
            所有寄存物件，如在十五天內無人認領，本酒店有權處理該物品且無須承擔任何賠償責任。<br><br>
            使用此服務即表示閣下已閱讀並同意上述條款。<br><br>
          </p>
        </div>
      </div>

      <form @submit.prevent="submitForm" class="access-form" v-if="!submissionSuccess">
        <!-- 姓名 -->
        <div class="form-group">
          <label for="name" class="required-field">姓名 | Name</label>
          <input
            id="name"
            v-model="form.name"
            type="text"
            placeholder="請輸入您的姓名 | Please enter your name"
            required
          >
        </div>

        <!-- 手機號碼 -->
        <div class="form-group">
          <label for="phone">手機號碼 | Phone Number <span class="optional">(選填 | Optional)</span></label>
          <input
            id="phone"
            v-model="form.phone"
            type="tel"
            placeholder="請輸入手機號碼 | Please enter your phone number"
          >
        </div>

        <!-- 房間號碼 -->
        <div class="form-group">
          <label for="roomNumber">房間號碼 | Room Number <span class="optional">(選填 | Optional)</span></label>
          <input
            id="roomNumber"
            v-model="form.roomNumber"
            type="text"
            placeholder="請輸入房間號碼 | Please enter your room number"
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
            max="20"
            required
          >
        </div>

        <button type="submit" class="btn-submit">
          <i class="fas fa-check-circle"></i> 提交寄存申請 | Submit Request
        </button>
      </form>

      <!-- 成功提示區 -->
      <div v-if="submissionSuccess" class="success-section">
        <div class="success-icon">
          <i class="fas fa-check-circle"></i>
        </div>
        <h2>寄存申請成功！</h2>
        <h2>Request Submitted Successfully!</h2>
        
        <p>已成功預約，等待工作人員確認後給予寄存憑證</p>
        <p>Your request has been submitted. Please wait for staff confirmation to receive your storage voucher.</p>
        
        <div class="countdown">
          頁面將在 {{ countdown }} 秒後刷新...
          <br>
          Page will refresh in {{ countdown }} seconds...
        </div>
      </div>

      <!-- 使用說明 -->
      <div class="instructions">
        <h3><i class="fas fa-info-circle"></i> 使用說明 | Instructions</h3>
        <ol>
          <li>填寫基本資訊並提交 | Fill in basic information and submit</li>
          <li>等待工作人員確認 | Wait for staff confirmation</li>
          <li>獲得寄存憑證 | Receive storage voucher</li>
          <li>取件時向工作人員出示憑證 | Present voucher to staff when retrieving luggage</li>
        </ol>
      </div>
    </div>
  </div>
</template>

<script>
import { useLuggageStore } from '@/stores/luggage'
import { ElMessage } from 'element-plus'

export default {
  name: 'GuestAccess',
  data() {
    return {
      form: {
        name: '',
        phone: '',
        roomNumber: '',
        luggageCount: 1
      },
      submissionSuccess: false,
      countdown: 5, 
      countdownInterval: null
    }
  },
  methods: {
    async submitForm() {
      try {
        const luggageStore = useLuggageStore()
        
        // 准备提交数据
        const formData = new FormData()
        formData.append('guestName', this.form.name)
        formData.append('phone', this.form.phone)
        formData.append('roomNumber', this.form.roomNumber)
        formData.append('luggageCount', this.form.luggageCount.toString())
        
        // 使用 API 提交数据
        await luggageStore.createLuggage(formData)
        
        // 显示成功消息
        this.submissionSuccess = true
        
        // 开始倒计时
        this.countdown = 10
        this.countdownInterval = setInterval(() => {
          this.countdown--
          
          if (this.countdown <= 0) {
            clearInterval(this.countdownInterval)
            // 刷新页面而不是重置表单
            window.location.reload()
          }
        }, 1000)
        
      } catch (error) {
        console.error('提交失敗:', error)
        ElMessage.error(error.response?.data?.message || '提交失敗，請稍後再試')
      }
    }
  },
  beforeUnmount() {
    // 组件销毁前清除计时器
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval)
    }
  }
}
</script>

<style scoped>
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

.terms-section {
  margin-bottom: 30px;
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.terms-section h3 {
  color: #2c3e50;
  margin-top: 0;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.terms-content {
  font-size: 0.85rem;
  line-height: 1.6;
}

.english-terms, .chinese-terms {
  text-align: justify;
  margin: 10px 0;
}

.english-terms {
  font-style: italic;
  color: #555;
}

.chinese-terms {
  color: #333;
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
  color: #34495e;
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

.form-group input:focus {
  border-color: #3498db;
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

.hint {
  display: block;
  margin-top: 5px;
  color: #7f8c8d;
  font-size: 0.85rem;
}

.btn-submit {
  width: 100%;
  padding: 14px;
  background: #2ecc71;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s;
  margin-top: 10px;
}

.btn-submit:hover {
  background: #27ae60;
}

.btn-submit i {
  margin-right: 8px;
}

/* 成功提示區 */
.success-section {
  text-align: center;
  padding: 30px 20px;
  margin-bottom: 30px;
  background: #f8f9fa;
  border-radius: 10px;
}

.success-icon {
  font-size: 4rem;
  color: #2ecc71;
  margin-bottom: 20px;
}

.success-section h2 {
  color: #2c3e50;
  margin-bottom: 15px;
}

.countdown {
  font-size: 1.1rem;
  color: #e74c3c;
  margin-top: 20px;
  font-weight: bold;
  line-height: 1.5;
}

/* 使用說明 */
.instructions {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 20px;
  margin-top: 30px;
}

.instructions h3 {
  margin-top: 0;
  color: #2c3e50;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.instructions ol {
  padding-left: 20px;
}

.instructions li {
  margin-bottom: 10px;
  line-height: 1.6;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .access-card {
    padding: 20px;
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
}
</style>