<template>
  <div id="app">
    <!-- 仅工作人员界面显示顶部导航 -->
    <staff-nav v-if="showStaffNav" />
    
    <!-- 主内容区域 -->
    <main :class="{ 'staff-layout': showStaffNav }">
      <router-view />
    </main>
    
    <!-- 全局通知 -->
    <notification-toast />
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 计算属性：是否显示工作人员导航栏
const showStaffNav = computed(() => {
  return authStore.role === 'STAFF' && !route.meta.hideNav
})

// 监听身份变化
watch(
  () => authStore.role,
  (newRole) => {
    if (newRole === 'STAFF') {
      router.push('/staff/dashboard')
    } else if (newRole === null && route.path.startsWith('/staff')) {
      router.push('/staff/login')
    }
  },
  { immediate: true }
)

// 防止已登录用户访问登录页
watch(
  () => route.path,
  (newPath) => {
    if (authStore.role === 'STAFF' && newPath === '/staff/login') {
      router.push('/staff/dashboard')
    }
  }
)
</script>

<style>
/* 原有样式保持不变 */
#app {
  font-family: 'Microsoft JhengHei', 'Noto Sans TC', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}

.staff-layout {
  padding-top: 60px; /* 导航栏高度 */
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
}

/* 全局表单样式 */
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