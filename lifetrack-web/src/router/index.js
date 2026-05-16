import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../views/dashboard/Dashboard.vue'),
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/DashboardHome.vue')
      },
      {
        path: 'tasks',
        name: 'TaskList',
        component: () => import('../views/tasks/TaskList.vue')
      },
      {
        path: 'tasks/:id',
        name: 'TaskDetail',
        component: () => import('../views/tasks/TaskDetail.vue')
      },
      {
        path: 'health',
        name: 'Health',
        component: () => import('../views/health/HealthPage.vue')
      },
      {
        path: 'ledger',
        name: 'Ledger',
        component: () => import('../views/ledger/LedgerPage.vue')
      }
    ]
  },
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('../views/auth/AuthPage.vue'),
    meta: { guest: true }
  },
  {
    path: '/auth/register',
    name: 'Register',
    component: () => import('../views/auth/AuthPage.vue'),
    meta: { guest: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/auth/login')
  } else if (to.meta.guest && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
