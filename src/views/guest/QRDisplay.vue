<template>
  <!-- 用户界面 -->
  <div class="qr-display-container">
    <div class="qr-card">
      <div class="qr-header">
        <h2><i class="fas fa-qrcode"></i> 您的取件憑證</h2>
        <p>請向工作人員出示此二維碼</p>
      </div>

      <div class="qr-code-wrapper">
        <!-- 添加 canvas ref 直接引用 -->
        <qrcode-vue 
          :value="qrData" 
          :size="250" 
          level="H" 
          render-as="canvas"
          class="qr-code"
          @ready="onQRCodeReady"
        />
        <div class="qr-info">
          <p><i class="fas fa-mobile-alt"></i> {{ guestPhone }}</p>
          <p><i class="fas fa-tag"></i> {{ bagId }}</p>
          <p><i class="fas fa-clock"></i> {{ generatedTime }}</p>
        </div>
      </div>

      <button @click="printQR" class="btn btn-primary">
        <i class="fas fa-print"></i> 打印二維碼
      </button>
    </div>

    <div class="qr-notice">
      <h3><i class="fas fa-exclamation-triangle"></i> 注意事項</h3>
      <ul>
        <li>此二維碼是您取件的唯一憑證</li>
        <li>遺失需憑身份證明文件領取</li>
      </ul>
    </div>

    <!-- 打印专用区域 -->
    <div class="print-area" v-show="false" ref="printArea">
      <div class="print-content">
        <img :src="qrImageData" class="printed-qr">
        <div class="printed-info">
          <p>電話: {{ guestPhone }}</p>
          <p>包裹ID: {{ bagId }}</p>
          <p>時間: {{ generatedTime }}</p>
        </div>
      </div>
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
      generatedTime: new Date().toLocaleString('zh-TW'),
      qrImageData: '',
      qrCanvas: null
    }
  },
  computed: {
    qrData() {
      return `HLS-GUEST:${this.guestPhone}|${this.bagId}|${this.generatedTime}`
    }
  },
  methods: {
    onQRCodeReady(canvas) {
      // 直接获取二维码canvas元素
      this.qrCanvas = canvas
      this.qrImageData = canvas.toDataURL('image/png')
    },
    
    printQR() {
      // 如果没有预先生成图片，则立即生成
      if (!this.qrImageData && this.qrCanvas) {
        this.qrImageData = this.qrCanvas.toDataURL('image/png')
      }
      
      // 显示打印区域
      this.$refs.printArea.style.display = 'block'
      
      // 等待DOM更新后打印
      this.$nextTick(() => {
        window.print()
        
        // 打印完成后隐藏
        setTimeout(() => {
          this.$refs.printArea.style.display = 'none'
        }, 500)
      })
    }
  }
}
</script>

<style scoped>
/* 全局基础样式 */
.qr-display-container {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
  font-family: 'Segoe UI', Arial, sans-serif;
}

/* 卡片样式 */
.qr-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

/* 头部样式 */
.qr-header {
  text-align: center;
  margin-bottom: 20px;
}

.qr-header h2 {
  color: #2c3e50;
  font-weight: 600;
}

.qr-header p {
  color: #7f8c8d;
  margin-top: 8px;
}

/* 二维码显示区域 */
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
  background: white;
}

/* 信息文字样式 */
.qr-info {
  text-align: center;
  margin-top: 15px;
}

.qr-info p {
  margin: 8px 0;
  color: #34495e;
  font-size: 16px;
}

.qr-info i {
  margin-right: 8px;
  color: #3498db;
}

/* 操作按钮 */
.btn-primary {
  background: #3498db;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.btn-primary:hover {
  background: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

/* 注意事项样式 */
.qr-notice {
  background: #fff8e1;
  border-left: 4px solid #ffc107;
  padding: 15px;
  border-radius: 0 5px 5px 0;
}

.qr-notice h3 {
  color: #e65100;
  margin-bottom: 10px;
}

.qr-notice ul {
  padding-left: 20px;
  margin-top: 10px;
}

.qr-notice li {
  margin-bottom: 8px;
  color: #5d4037;
}

/* 打印区域样式 */
.print-area {
  display: none;
  position: fixed;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: white;
  z-index: 9999;
  justify-content: center;
  align-items: center;
}

.print-content {
  width: 90mm;
  height: 60mm;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10mm;
  box-sizing: border-box;
}

.printed-qr {
  width: 50mm;
  height: 50mm;
  margin-bottom: 5mm;
}

.printed-info {
  font-size: 14pt;
  line-height: 1.5;
  text-align: center;
}

/* 打印时样式 */
@media print {
  body * {
    visibility: hidden;
  }
  
  .print-area,
  .print-area * {
    visibility: visible;
  }
  
  .print-area {
    position: static;
    display: flex !important;
  }
  
  @page {
    size: auto;
    margin: 0;
  }
}

/* 响应式调整 */
@media (max-width: 480px) {
  .qr-card {
    padding: 15px;
  }
  
  .qr-info p {
    font-size: 14px;
  }
  
  .btn-primary {
    padding: 10px 20px;
  }
}
</style>