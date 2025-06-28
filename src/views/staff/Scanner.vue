<template>
  <div class="scanner-container">
    <div class="scanner-header">
      <h2><i class="fas fa-camera"></i> 掃描客人二維碼</h2>
      <button @click="toggleCamera" class="btn btn-small">
        {{ isCameraOn ? '關閉相機' : '開啟相機' }}
      </button>
    </div>

    <div class="scanner-wrapper">
      <div v-if="isCameraOn" ref="scannerElement" class="scanner"></div>
      <div v-else class="camera-off">
        <i class="fas fa-camera-slash"></i>
        <p>相機未啟動</p>
      </div>
    </div>

    <div v-if="scanResult" class="scan-result">
      <div class="result-card">
        <h3>掃描成功！</h3>
        <p>手機號碼: {{ scanResult.phone }}</p>
        <p>行李編號: {{ scanResult.bagId }}</p>
        
        <div class="action-buttons">
          <button @click="fetchBagDetails" class="btn btn-primary">
            <i class="fas fa-info-circle"></i> 查看行李詳情
          </button>
          <button @click="resetScanner" class="btn btn-secondary">
            <i class="fas fa-redo"></i> 重新掃描
          </button>
        </div>
      </div>
    </div>

    <div v-else-if="errorMessage" class="error-message">
      <i class="fas fa-exclamation-triangle"></i> {{ errorMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { Html5Qrcode } from 'html5-qrcode'

const scannerElement = ref(null)
const isCameraOn = ref(false)
const scanResult = ref(null)
const errorMessage = ref('')
let html5Qrcode = null

const toggleCamera = async () => {
  if (isCameraOn.value) {
    await stopScanner()
  } else {
    await startScanner()
  }
}

const startScanner = async () => {
  try {
    html5Qrcode = new Html5Qrcode(scannerElement.value)
    await html5Qrcode.start(
      { facingMode: "environment" },
      { fps: 10 },
      onScanSuccess,
      onScanError
    )
    isCameraOn.value = true
    errorMessage.value = ''
  } catch (err) {
    errorMessage.value = '無法啟動相機: ' + err.message
  }
}

const stopScanner = async () => {
  try {
    if (html5Qrcode) {
      await html5Qrcode.stop()
      html5Qrcode = null
    }
    isCameraOn.value = false
  } catch (err) {
    console.error('關閉相機錯誤:', err)
  }
}

const onScanSuccess = (decodedText) => {
  const match = decodedText.match(/^HLS-GUEST:(\d+)\|(.+)$/)
  if (match) {
    scanResult.value = {
      phone: match[1],
      bagId: match[2]
    }
    stopScanner()
  }
}

const onScanError = (error) => {
  console.warn('掃描錯誤:', error)
}

const fetchBagDetails = () => {
  // 跳轉到行李詳情頁
  console.log('獲取行李詳情:', scanResult.value)
}

const resetScanner = () => {
  scanResult.value = null
  startScanner()
}

onUnmounted(() => {
  if (html5Qrcode) {
    stopScanner()
  }
})
</script>

<style scoped>
.scanner-container {
  padding: 20px;
}

.scanner-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.scanner-wrapper {
  width: 100%;
  height: 300px;
  background: #000;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.camera-off {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: white;
}

.camera-off i {
  font-size: 3rem;
  margin-bottom: 10px;
}

.scan-result {
  margin-top: 20px;
}

.result-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.error-message {
  color: #e74c3c;
  text-align: center;
  margin-top: 20px;
  padding: 10px;
  background: #fef2f2;
  border-radius: 5px;
}
</style>