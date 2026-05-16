<template>
  <div class="dashboard-layout">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-brand">
        <span class="brand-icon">📋</span>
        <span class="brand-name">LifeTrack</span>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/dashboard" class="nav-item" active-class="active">
          <el-icon><HomeFilled /></el-icon>
          <span>仪表盘</span>
        </router-link>
        <router-link to="/tasks" class="nav-item" active-class="active">
          <el-icon><List /></el-icon>
          <span>任务打卡</span>
        </router-link>
        <router-link to="/health" class="nav-item" active-class="active">
          <el-icon><Histogram /></el-icon>
          <span>健康记录</span>
        </router-link>
        <router-link to="/ledger" class="nav-item" active-class="active">
          <el-icon><Money /></el-icon>
          <span>账本管理</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <div class="user-info">
          <span class="user-avatar">{{ user?.nickname?.charAt(0) || '?' }}</span>
          <span class="user-name">{{ user?.nickname }}</span>
        </div>
        <el-button text type="danger" size="small" @click="handleLogout">退出</el-button>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { HomeFilled, List, Histogram, Money } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const user = ref(null)

onMounted(async () => {
  try {
    user.value = await authStore.fetchProfile()
  } catch {}
})

function handleLogout() {
  authStore.logout()
  router.push('/auth/login')
}
</script>

<style scoped>
.dashboard-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f6fa;
}

/* Sidebar */
.sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #edf0f5;
  display: flex;
  flex-direction: column;
  padding: 0;
  flex-shrink: 0;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px;
  border-bottom: 1px solid #f0f2f5;
}

.brand-icon {
  font-size: 24px;
}

.brand-name {
  font-size: 17px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.3px;
}

.sidebar-nav {
  flex: 1;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 8px;
  color: #64748b;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.15s;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #334155;
}

.nav-item.active {
  background: #eef2ff;
  color: #6366f1;
  font-weight: 600;
}

.nav-item.disabled {
  cursor: default;
  opacity: 0.5;
}

.nav-item.disabled:hover {
  background: transparent;
  color: #64748b;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #6366f1;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-name {
  font-size: 13px;
  color: #334155;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Main Content */
.main-content {
  flex: 1;
  overflow-y: auto;
}
</style>
