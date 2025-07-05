<template>
  <div class="qr-generator-container">
    <div class="qr-card">
      <h2><i class="fas fa-qrcode"></i> 生成行李寄存二維碼</h2>
      
      <button 
        @click="generateQR" 
        class="generate-btn"
      >
        <i class="fas fa-magic"></i> 立即生成
      </button>

      <div v-if="qrCode" class="qr-result">
        <div class="qr-code-wrapper">
          <qrcode-vue 
            :value="qrCode" 
            :size="250"
            level="H"
          />
        </div>
        <p class="usage-hint">
          ➤ 此二維碼長期有效，請展示給客人掃描<br>
          ➤ 掃描後將進入行李寄存頁面
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import QrcodeVue from 'qrcode.vue';

const qrCode = ref('');

// 自动获取本机IPv4地址（兼容所有操作系统）
const getLocalIP = () => {
  return new Promise((resolve) => {
    console.log(window.location.hostname)
    // 方法1：通过浏览器API获取（最快）
    if (window.location.hostname !== 'localhost') {
      resolve(window.location.hostname);
      
      return;
    }

    // 方法2：通过WebRTC获取（备用方案）
    window.RTCPeerConnection = window.RTCPeerConnection || window.mozRTCPeerConnection || window.webkitRTCPeerConnection;
    const pc = new RTCPeerConnection({ iceServers: [] });
    pc.createDataChannel('');
    pc.createOffer().then(offer => pc.setLocalDescription(offer));
    
    pc.onicecandidate = (ice) => {
      if (!ice.candidate) return;
      const ipRegex = /([0-9]{1,3}(\.[0-9]{1,3}){3})/;
      const ipMatch = ipRegex.exec(ice.candidate.candidate);
      if (ipMatch) resolve(ipMatch[1]);
    };

    // 方法3：超时回退到提示手动输入
    setTimeout(() => {
      const manualIP = prompt('自動取得IP失敗，請手動輸入本機IPv4位址：\n(在cmd輸入ipconfig查看)');
      resolve(manualIP || '192.168.x.x');
    }, 1000);
  });
};

// 生成二维码
const generateQR = async () => {
  try {
    const ip = await getLocalIP();
    const port = window.location.port || '8081';
    qrCode.value = `http://${ip}:${port}/guest/access`;
    
    alert(`请确保手机和电脑连接同一网络\n访问链接：${qrCode.value}`);
  } catch (error) {
    alert(`自动获取IP失败：${error}\n请手动输入IP地址`);
  }
};
</script>

<style scoped>
.qr-generator-container {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.qr-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

h2 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 25px;
  font-size: 1.5rem;
}

.generate-btn {
  background: #4285f4;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  display: block;
  width: 100%;
  transition: all 0.3s;
}

.generate-btn:hover {
  background: #3367d6;
  transform: translateY(-2px);
}

.generate-btn i {
  margin-right: 8px;
}

.qr-result {
  margin-top: 30px;
  text-align: center;
}

.qr-code-wrapper {
  padding: 15px;
  background: white;
  border: 1px solid #eee;
  border-radius: 8px;
  display: inline-block;
}

.usage-hint {
  margin-top: 20px;
  color: #666;
  line-height: 1.6;
  text-align: left;
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
}
</style>