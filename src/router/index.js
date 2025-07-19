import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/guest/access'
  },
  // 工作人員路由
  {
    path: '/staff/login',
    component: () => import('@/views/staff/Login.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/staff/dashboard',
    name: 'StaffDashboard',
    component: () => import('@/views/staff/Dashboard.vue'),
    meta: { requiresAuth: true, allowedRoles: ['STAFF', 'MANAGER','ADMIN'] }
  },
  {
    path: '/staff/scan',
    component: () => import('@/views/staff/Scanner.vue'),
    meta: { requiresStaff: true }
  },
  
  // 客人路由
  {
    path: '/guest/access',
    component: () => import('@/views/guest/Access.vue')
  },
  {
    path: '/guest/qr/:id',
    component: () => import('@/views/guest/QRDisplay.vue'),
    props: true
  },

]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 1. 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!authStore.staffToken) {
      return redirectToLogin()
    }
    
    // 2. 检查角色权限
    if (to.meta.allowedRoles) {
      const userRole = authStore.userInfo?.role // 从Pinia获取用户角色
      if (!userRole || !to.meta.allowedRoles.includes(userRole)) {
        return redirectToUnauthorized() // 或跳转到默认页
      }
    }
    
    // 3. 检查token有效性（如果有过期时间）
    const isTokenValid = authStore.checkAuth() // 调用您现有的方法
    if (!isTokenValid) {
      return redirectToLogin()
    }
  }

  // 已登录用户禁止访问guestOnly页面
  if (to.meta.guestOnly && authStore.staffToken) {
    return next('/staff/dashboard')
  }

  // 默认放行
  next()

  function redirectToLogin() {
    next({
      path: '/staff/login',
      query: { redirect: to.fullPath }
    })
  }
  
  function redirectToUnauthorized() {
    next('/staff/dashboard') // 或自定义无权限页面
  }
})

export default router