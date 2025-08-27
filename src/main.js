import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'  
import './assets/styles.css'
import 'element-plus/dist/index.css'



import '@fortawesome/fontawesome-free/css/all.css'

const app = createApp(App)


app.use(createPinia())
// 抑制 ResizeObserver 警告
if (window.ResizeObserver) {
  const originalResizeObserver = window.ResizeObserver;
  window.ResizeObserver = class ResizeObserver extends originalResizeObserver {
    constructor(callback) {
      super((entries, observer) => {
        // 使用 setTimeout 避免循环阻塞
        setTimeout(() => {
          callback(entries, observer);
        }, 0);
      });
    }
  };
}

app.use(router)

app.mount('#app')