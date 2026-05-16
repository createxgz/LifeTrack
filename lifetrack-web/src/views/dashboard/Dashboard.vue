<template>
  <div class="dashboard-layout">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="brand-icon-box">
          <el-icon :size="18" color="#FFF7ED"><List /></el-icon>
        </div>
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
        <button class="logout-btn" @click="handleLogout">退出</button>
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
  background: #FAFAF8;
  font-family: 'PingFang SC', 'Helvetica Neue', sans-serif;
}

/* ── Sidebar ── */
.sidebar {
  width: 220px;
  background: #FDF6EE;
  border-right: 1px solid rgba(194, 65, 12, 0.08);
  display: flex;
  flex-direction: column;
  padding: 0;
  flex-shrink: 0;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 18px;
  border-bottom: 1px solid rgba(194, 65, 12, 0.08);
}

.brand-icon-box {
  width: 32px;
  height: 32px;
  background: #C2410C;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.brand-name {
  font-size: 17px;
  font-weight: 600;
  color: #1C1917;
  letter-spacing: -0.02em;
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
  border-radius: 9px;
  color: #78716C;
  text-decoration: none;
  font-size: 14px;
  font-weight: 450;
  transition: all 0.18s;
}

.nav-item:hover {
  background: rgba(194, 65, 12, 0.06);
  color: #1C1917;
}

.nav-item.active {
  background: rgba(194, 65, 12, 0.1);
  color: #C2410C;
  font-weight: 600;
}

.sidebar-footer {
  padding: 16px 18px;
  border-top: 1px solid rgba(194, 65, 12, 0.08);
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
  background: #C2410C;
  color: #FFF7ED;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-name {
  font-size: 13px;
  color: #44403C;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout-btn {
  border: none;
  background: none;
  color: #A8A29E;
  font-size: 12px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  font-family: inherit;
  transition: all 0.15s;
}
.logout-btn:hover {
  color: #DC2626;
  background: rgba(220, 38, 38, 0.06);
}

/* ── Main Content ── */
.main-content {
  flex: 1;
  overflow-y: auto;
  background: #FAFAF8;
}
</style>
