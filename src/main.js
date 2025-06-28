import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/styles.css'

// 引入字體庫
import '@fortawesome/fontawesome-free/css/all.css'

// 引入QR碼庫
import QrcodeVue from 'qrcode.vue'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// 全局組件
app.component('QrcodeVue', QrcodeVue)

// 全局配置
app.config.globalProperties.$filters = {
  formatDate(value) {
    return new Date(value).toLocaleString('zh-TW')
  }
}

app.mount('#app')

console.log('應用程式已啟動 - 酒店行李管理系統 v1.0')