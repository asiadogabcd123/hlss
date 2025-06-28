<template>
  <div id="app">
    <!-- 工作人員界面頂部導航 -->
    <staff-nav v-if="authStore.role === 'STAFF'" />
    
    <!-- 主內容區域 -->
    <main :class="{ 'staff-layout': authStore.role === 'STAFF' }">
      <router-view />
    </main>
    
    <!-- 全局通知 -->
    <notification-toast />
  </div>
</template>

<script setup>
import { watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 監聽身份變化
watch(
  () => authStore.role,
  (newRole) => {
    if (newRole === 'STAFF') {
      router.push('/staff/dashboard')
    } else if (newRole === null) {
      router.push('/staff/login')
    }
  }
)
</script>

<style>
#app {
  font-family: 'Microsoft JhengHei', 'Noto Sans TC', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}

.staff-layout {
  padding-top: 60px; /* 導航欄高度 */
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
}

/* 全域表單樣式 */
.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
</style>