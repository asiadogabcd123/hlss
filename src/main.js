import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'  // 确保路径正确
import './assets/styles.css'

// 引入字体图标库
import '@fortawesome/fontawesome-free/css/all.css'

const app = createApp(App)

// 使用状态管理
app.use(createPinia())

// 使用路由
app.use(router)

app.mount('#app')