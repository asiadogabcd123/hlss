import { createRouter, createWebHistory } from 'vue-router'

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
    component: () => import('@/views/staff/Dashboard.vue'),
    meta: { requiresStaff: true }
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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router