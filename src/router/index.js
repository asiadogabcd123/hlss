import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    component: () => import('@/views/IdentitySelection.vue'),
    meta: { hideNav: true }
  },
  // 工作人員路由
  {
    path: '/staff',
    children: [
      {
        path: 'login',
        component: () => import('@/views/staff/Login.vue'),
        meta: { guestOnly: true }
      },
      {
        path: 'dashboard',
        name: 'StaffDashboard',
        component: () => import('@/views/staff/Dashboard.vue'),
        meta: { 
          requiresAuth: true,
          allowedRoles: ['STAFF', 'MANAGER', 'ADMIN']
        }
      },
      {
        path: 'luggagemanagement',
        component: () => import('@/views/staff/LuggageManagement.vue'),
        meta: { 
          requiresAuth: true,
          allowedRoles: ['STAFF', 'MANAGER', 'ADMIN'],
          title: '綜合行李管理'
        }
      },
      {
        path: 'qr-batch',
        component: () => import('@/views/staff/QrBatch.vue'),
        meta: { 
          requiresAuth: true,
          allowedRoles: ['STAFF', 'MANAGER', 'ADMIN'],
          title: 'QR批次管理'
        }
      },
      {
        path: 'scanner',
        component: () => import('@/views/staff/Scanner.vue'),  // 修復組件路徑
        meta: { 
          requiresAuth: true,
          allowedRoles: ['STAFF', 'MANAGER', 'ADMIN'],
          title: '掃描器'  // 修正標題
        }
      }
    ]
  },
  // 客人路由
  {
    path: '/guest/access',
    component: () => import('@/views/guest/Access.vue')
  }

]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守衛
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 檢查是否需要認證
  if (to.meta.requiresAuth) {
    if (!authStore.staffToken) {
      return next({
        path: '/staff/login',
        query: { redirect: to.fullPath }
      })
    }
    
    // 檢查角色權限
    if (to.meta.allowedRoles) {
      const userRole = authStore.userInfo?.role
      if (!userRole || !to.meta.allowedRoles.includes(userRole)) {
        return next('/staff/dashboard') // 或跳轉到無權限頁面
      }
    }
  }

  // 已登入用戶禁止訪問guestOnly頁面
  if (to.meta.guestOnly && authStore.staffToken) {
    return next('/staff/dashboard')
  }

  next()
})

export default router
