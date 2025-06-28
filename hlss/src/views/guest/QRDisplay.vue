<template>
  <div class="qr-display-container">
    <div class="qr-card">
      <div class="qr-header">
        <h2><i class="fas fa-qrcode"></i> 您的取件憑證</h2>
        <p>請向工作人員出示此二維碼</p>
      </div>

      <div class="qr-code-wrapper">
        <qrcode-vue 
          :value="qrData" 
          :size="250" 
          level="H" 
          class="qr-code" />
        <div class="qr-info">
          <p><i class="fas fa-mobile-alt"></i> {{ guestPhone }}</p>
          <p><i class="fas fa-tag"></i> {{ bagId }}</p>
          <p><i class="fas fa-clock"></i> {{ generatedTime }}</p>
        </div>
      </div>

      <div class="qr-actions">
        <button @click="printQR" class="btn btn-primary">
          <i class="fas fa-print"></i> 打印二維碼
        </button>
        <button @click="sendSMS" class="btn btn-secondary">
          <i class="fas fa-sms"></i> 發送至手機
        </button>
      </div>
    </div>

    <div class="qr-notice">
      <h3><i class="fas fa-exclamation-triangle"></i> 注意事項</h3>
      <ul>
        <li>此二維碼是您取件的唯一憑證</li>
        <li>有效期至今日營業時間結束</li>
        <li>遺失需憑身份證明文件領取</li>
      </ul>
    </div>
  </div>
</template>

<script>
import QrcodeVue from 'qrcode.vue'

export default {
  name: 'GuestQRDisplay',
  components: {
    QrcodeVue
  },
  data() {
    return {
      guestPhone: this.$route.query.phone,
      bagId: this.$route.params.id,
      generatedTime: new Date().toLocaleString('zh-TW')
    }
  },
  computed: {
    qrData() {
      return `HLS-GUEST:${this.guestPhone}|${this.bagId}|${this.generatedTime}`
    }
  },
  methods: {
    printQR() {
      window.print()
    },
    sendSMS() {
      alert('模擬簡訊發送: 二維碼已發送至 ' + this.guestPhone)
    }
  },
  mounted() {
    const style = document.createElement('style')
    style.innerHTML = `
      @media print {
        body * {
          visibility: hidden;
        }
        .qr-card, .qr-card * {
          visibility: visible;
        }
        .qr-card {
          position: absolute;
          left: 0;
          top: 0;
          width: 100%;
          box-shadow: none;
        }
        .qr-actions {
          display: none;
        }
      }
    `
    document.head.appendChild(style)
  }
}
</script>

<style scoped>
.qr-display-container {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.qr-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.qr-header {
  text-align: center;
  margin-bottom: 20px;
}

.qr-header h2 {
  color: #2c3e50;
}

.qr-code-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 20px 0;
}

.qr-code {
  border: 1px solid #eee;
  padding: 10px;
  margin-bottom: 15px;
}

.qr-info {
  text-align: center;
  margin-top: 15px;
}

.qr-info p {
  margin: 5px 0;
  color: #7f8c8d;
}

.qr-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.qr-notice {
  background: #fff8e1;
  border-left: 4px solid #ffc107;
  padding: 15px;
  border-radius: 0 5px 5px 0;
}

.qr-notice ul {
  padding-left: 20px;
  margin-top: 10px;
}

.qr-notice li {
  margin-bottom: 8px;
}
</style>