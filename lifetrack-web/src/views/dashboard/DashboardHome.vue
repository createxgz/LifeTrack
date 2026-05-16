<template>
  <div class="dashboard-home">
    <!-- ═══ 顶部问候栏 ═══ -->
    <header class="greeting-bar">
      <div class="greeting-text">
        <h1 class="greeting-title">{{ greeting }}，<strong>{{ user?.nickname || '用户' }}</strong> <span class="greeting-wave">👋</span></h1>
        <p class="greeting-date">{{ dateStr }} · {{ weekDayStr }}</p>
      </div>
      <div class="greeting-avatar">{{ user?.nickname?.charAt(0) || '?' }}</div>
    </header>

    <!-- ═══ 数据概览行 ═══ -->
    <div class="metric-row">
      <!-- 今日任务 -->
      <div class="metric-card task-metric" @click="$router.push('/tasks')">
        <div class="metric-icon-box task-icon-box">
          <el-icon :size="18"><List /></el-icon>
        </div>
        <div class="metric-info">
          <div class="metric-value">{{ overview.todayCheckins }}<span class="metric-unit">/{{ overview.totalTasks }}</span></div>
          <div class="metric-label">今日打卡</div>
        </div>
        <div class="metric-sub" v-if="overview.totalTasks > overview.todayCheckins">
          还剩 {{ overview.totalTasks - overview.todayCheckins }} 个待完成
        </div>
        <div class="metric-sub done-all" v-else-if="overview.totalTasks > 0">
          全部完成 🎉
        </div>
        <div class="metric-sub empty" v-else>暂无任务</div>
      </div>

      <!-- 今日体重 -->
      <div class="metric-card weight-metric" @click="$router.push('/health')">
        <div class="metric-icon-box weight-icon-box">
          <el-icon :size="18"><Histogram /></el-icon>
        </div>
        <div class="metric-info">
          <div class="metric-value">{{ overview.latestWeight || '--' }}<span v-if="overview.latestWeight" class="metric-unit"> kg</span></div>
          <div class="metric-label">最新体重</div>
        </div>
        <div class="metric-sub" v-if="overview.weightDirection === 'down'">
          <span class="dir-down">▼ {{ latestWeightDiff }}</span>
        </div>
        <div class="metric-sub" v-else-if="overview.weightDirection === 'up'">
          <span class="dir-up">▲ {{ latestWeightDiff }}</span>
        </div>
        <div class="metric-sub" v-else-if="overview.weightDirection === 'stable'">
          <span class="dir-stable">→ 持平</span>
        </div>
        <div class="metric-sub empty" v-else>暂无记录</div>
      </div>

      <!-- 热量摄入 -->
      <div class="metric-card diet-metric" @click="$router.push('/health')">
        <div class="metric-icon-box diet-icon-box">
          <el-icon :size="18"><KnifeFork /></el-icon>
        </div>
        <div class="metric-info">
          <div class="metric-value">{{ fmtNum(overview.todayCalories) || 0 }}<span class="metric-unit"> kcal</span></div>
          <div class="metric-label">今日摄入</div>
        </div>
        <div class="metric-sub" v-if="calorieTarget > 0">
          目标 {{ calorieTarget }} · 剩余 {{ calorieTarget - (overview.todayCalories || 0) }}
        </div>
        <div class="metric-sub placeholder" v-else>目标未设置</div>
      </div>

      <!-- 本月结余 -->
      <div class="metric-card ledger-metric" @click="$router.push('/ledger')">
        <div class="metric-icon-box ledger-icon-box">
          <el-icon :size="18"><Money /></el-icon>
        </div>
        <div class="metric-info">
          <div class="metric-value" :class="{ negative: balanceNegative }">{{ balanceStr }}</div>
          <div class="metric-label">本月结余</div>
        </div>
        <div class="metric-sub">
          收 ¥{{ fmtNum(overview.monthlyIncome || 0) }} · 支 ¥{{ fmtNum(overview.monthlyExpense || 0) }}
        </div>
      </div>
    </div>

    <!-- ═══ 主内容区：双列布局 ═══ -->
    <div class="content-grid">
      <!-- 左列：任务打卡列表 + 体重趋势 -->
      <div class="left-col">
        <!-- 今日任务打卡列表 -->
        <div class="card task-list-card">
          <div class="card-header">
            <h3 class="card-title">
              <span class="title-dot task-dot"></span>
              今日任务打卡
            </h3>
            <router-link to="/tasks" class="card-link">全部 →</router-link>
          </div>

          <div class="task-list" v-loading="taskLoading">
            <template v-if="todayTasks.length > 0">
              <div
                v-for="task in todayTasks"
                :key="task.id"
                class="task-row"
                :class="{ checked: task.checkedToday }"
              >
                <span class="task-dot-color" :style="{ background: taskColor(task.id) }"></span>
                <span class="task-name">{{ task.title }}</span>
                <span class="task-streak" v-if="!task.checkedToday && task.streakDays > 0">
                  🔥 连续 {{ task.streakDays }} 天
                </span>
                <span class="task-streak done" v-else-if="task.checkedToday">
                  ✓ 已完成
                </span>
                <button
                  class="checkin-btn"
                  :class="{ done: task.checkedToday, bouncing: task._bouncing }"
                  :disabled="task.checkedToday"
                  @click="handleCheckin(task)"
                >
                  <el-icon v-if="task.checkedToday" :size="14"><Check /></el-icon>
                  <span v-else class="checkin-circle"></span>
                </button>
              </div>
            </template>
            <div v-else class="empty-state">
              <div class="empty-illustration">📋</div>
              <p>今天没有计划的任务</p>
              <router-link to="/tasks" class="empty-link">去添加一个 →</router-link>
            </div>
          </div>
        </div>

        <!-- 近14天体重趋势 -->
        <div class="card weight-trend-card">
          <div class="card-header">
            <h3 class="card-title">
              <span class="title-dot weight-dot"></span>
              近7天体重趋势
            </h3>
            <div class="trend-tabs">
              <span class="trend-tab active">7天</span>
              <span class="trend-tab muted">30天</span>
              <span class="trend-tab muted">90天</span>
            </div>
          </div>
          <div class="chart-box" ref="weightTrendChartRef"></div>
        </div>
      </div>

      <!-- 右列：热量环形图 + 收支小结 + 本周打卡率 -->
      <div class="right-col">
        <!-- 今日热量环形图 -->
        <div class="card calorie-card">
          <div class="card-header">
            <h3 class="card-title">
              <span class="title-dot diet-dot"></span>
              今日热量
            </h3>
          </div>
          <div class="calorie-body">
            <div class="calorie-ring-wrap">
              <svg viewBox="0 0 100 100" class="calorie-svg">
                <circle cx="50" cy="50" r="38" fill="none" stroke="#F3F4F6" stroke-width="7" />
                <circle cx="50" cy="50" r="38" fill="none" stroke="#F97316" stroke-width="7"
                  :stroke-dasharray="calorieDash"
                  stroke-linecap="round"
                  transform="rotate(-90 50 50)" />
              </svg>
              <span class="calorie-pct">{{ caloriePct }}%</span>
            </div>
            <div class="calorie-text">
              <div class="calorie-intake">{{ fmtNum(overview.todayCalories) || 0 }} <span class="calorie-unit">kcal</span></div>
              <div class="calorie-target-text" v-if="calorieTarget > 0">/ {{ calorieTarget }} kcal</div>
              <div class="calorie-target-text placeholder" v-else>目标未设置</div>
              <div class="calorie-remain" v-if="calorieTarget > 0 && calorieRemaining >= 0">
                还剩 {{ calorieRemaining }} kcal
              </div>
              <div class="calorie-remain over" v-else-if="calorieTarget > 0">
                超出 {{ Math.abs(calorieRemaining) }} kcal
              </div>
            </div>
          </div>
          <!-- 餐次标签 -->
          <div class="meal-tags" v-if="mealTags.length > 0">
            <span v-for="m in mealTags" :key="m.label" class="meal-tag" :style="{ background: m.bg, color: m.color }">
              {{ m.label }} {{ m.kcal }}kcal
            </span>
          </div>
          <div class="meal-tags" v-else>
            <span class="meal-tag empty-tag">今天还没有饮食记录</span>
          </div>
        </div>

        <!-- 本月收支小结 -->
        <div class="card finance-card">
          <div class="card-header">
            <h3 class="card-title">
              <span class="title-dot finance-dot"></span>
              本月收支
            </h3>
            <span class="card-month">{{ currentMonth }}月</span>
          </div>
          <div class="finance-rows">
            <div class="finance-row">
              <span class="finance-icon income-icon">↑</span>
              <span class="finance-label">收入</span>
              <span class="finance-val income-val">+¥{{ fmtNum(overview.monthlyIncome || 0) }}</span>
            </div>
            <div class="finance-row">
              <span class="finance-icon expense-icon">↓</span>
              <span class="finance-label">支出</span>
              <span class="finance-val expense-val">-¥{{ fmtNum(overview.monthlyExpense || 0) }}</span>
            </div>
            <div class="finance-divider"></div>
            <div class="finance-row balance-row">
              <span class="finance-icon balance-icon">=</span>
              <span class="finance-label">结余</span>
              <span class="finance-val" :class="{ negative: balanceNegative }">{{ balanceStr }}</span>
            </div>
          </div>
          <!-- 预算占位 -->
          <div class="budget-placeholder">
            <el-icon :size="13"><WarningFilled /></el-icon>
            <span>预算功能开发中</span>
          </div>
        </div>

        <!-- 本周打卡率柱状图 -->
        <div class="card weekly-card">
          <div class="card-header">
            <h3 class="card-title">
              <span class="title-dot habit-dot"></span>
              本周打卡趋势
            </h3>
          </div>
          <div class="weekly-bars" ref="weeklyBarsRef">
            <div
              v-for="(d, idx) in weeklyBarData"
              :key="idx"
              class="weekly-bar-col"
            >
              <div class="bar-label">{{ d.count }}次</div>
              <div class="bar-fill-wrap">
                <div
                  class="bar-fill"
                  :class="{ today: d.isToday, past: d.isPast && !d.isToday, future: !d.isPast }"
                  :style="{ height: d.barHeight + 'px' }"
                ></div>
              </div>
              <div class="bar-day">{{ d.day }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { List, Histogram, KnifeFork, Money, Check, WarningFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { dashboardApi } from '@/api/dashboard'
import { taskApi } from '@/api/tasks'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// ═══════════════ 时间 & 问候 ═══════════════
const todayStr = new Date().toISOString().slice(0, 10)
const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const dateStr = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
})
const weekDayStr = computed(() => '星期' + weekDays[new Date().getDay()])
const currentMonth = computed(() => new Date().getMonth() + 1)

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h >= 6 && h < 11) return '早安'
  if (h >= 11 && h < 18) return '下午好'
  if (h >= 18 && h < 24) return '晚上好'
  return '夜深了，注意休息'
})

// ═══════════════ 用户 ═══════════════
const user = computed(() => authStore.user)

// ═══════════════ 概览数据 ═══════════════
const overview = reactive({
  totalTasks: 0,
  todayCheckins: 0,
  todayRate: 0,
  latestWeight: null,
  weightDirection: null,
  weightMiniTrend: [],
  todayCalories: 0,
  todayProtein: 0,
  monthlyIncome: 0,
  monthlyExpense: 0,
  balance: 0,
  weekCheckins: {}
})

const balanceStr = computed(() => {
  const v = Number(overview.balance || 0)
  return (v >= 0 ? '+' : '') + v.toFixed(0)
})
const balanceNegative = computed(() => Number(overview.balance || 0) < 0)

// 体重差值（最近两天比较）
const latestWeightDiff = computed(() => {
  const trend = overview.weightMiniTrend || []
  if (trend.length < 2) return ''
  const a = trend[trend.length - 2]?.weightKg
  const b = trend[trend.length - 1]?.weightKg
  if (a != null && b != null) {
    const diff = b - a
    return diff !== 0 ? Math.abs(diff).toFixed(1) + ' kg' : ''
  }
  return ''
})

// 热量目标（占位，等待设置功能）
const calorieTarget = ref(0)
const caloriePct = computed(() => {
  if (calorieTarget.value <= 0) return 0
  return Math.min(100, Math.round(((overview.todayCalories || 0) / calorieTarget.value) * 100))
})
const calorieRemaining = computed(() => {
  return calorieTarget.value - (overview.todayCalories || 0)
})
const calorieDash = computed(() => {
  const len = 2 * Math.PI * 38
  return `${len * caloriePct.value / 100} ${len}`
})

// 餐次标签（占位，等后端提供具体餐次数据）
const mealTags = computed(() => {
  // 目前 API 只返回总热量，餐次分布待后端支持
  return []
})

// ═══════════════ 今日任务 ═══════════════
const todayTasks = ref([])
const taskLoading = ref(false)
const taskColors = ['#C2410C', '#0F766E', '#0369A1', '#7C3AED', '#B45309', '#059669', '#4F46E5', '#DB2777']

function taskColor(id) {
  return taskColors[id % taskColors.length]
}

async function fetchTasks() {
  taskLoading.value = true
  try {
    const res = await taskApi.list({ status: 1, size: 50 })
    const records = res.data?.records || []
    // 给每个 task 添加 _bouncing 状态用于动画
    todayTasks.value = records.map(t => ({ ...t, _bouncing: false }))
  } catch {} finally {
    taskLoading.value = false
  }
}

async function handleCheckin(task) {
  if (task.checkedToday) return
  task._bouncing = true
  try {
    await taskApi.checkin(task.id, { date: todayStr })
    task.checkedToday = true
    task.streakDays = (task.streakDays || 0) + 1
    ElMessage.success('打卡成功！')
    await fetchOverview()
  } catch {} finally {
    setTimeout(() => { task._bouncing = false }, 320)
  }
}

// ═══════════════ 本周打卡柱状图数据 ═══════════════
const weeklyBarData = computed(() => {
  const today = new Date()
  const dayOfWeek = today.getDay() // 0=Sun
  const monday = new Date(today)
  monday.setDate(today.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1))

  const maxCount = Math.max(1, ...Object.values(overview.weekCheckins || {}))
  const dayLabels = ['一', '二', '三', '四', '五', '六', '日']

  return dayLabels.map((label, i) => {
    const d = new Date(monday)
    d.setDate(monday.getDate() + i)
    const key = d.toISOString().slice(0, 10)
    const count = overview.weekCheckins?.[key] || 0
    const isToday = key === todayStr
    const isPast = d <= today
    const barHeight = count > 0 ? Math.max(8, (count / maxCount) * 80) : 4

    return { day: label, count, isToday, isPast, barHeight }
  })
})

// ═══════════════ 体重趋势图 ═══════════════
const weightTrendChartRef = ref(null)
let weightTrendChart = null

function renderWeightTrend() {
  if (!weightTrendChartRef.value) return
  if (!weightTrendChart) weightTrendChart = echarts.init(weightTrendChartRef.value)

  const trend = overview.weightMiniTrend || []
  const dates = trend.map(d => {
    const parts = (d.date || '').split('-')
    return parts.length === 3 ? parts[1] + '/' + parts[2] : d.date || ''
  })
  const weights = trend.map(d => d.weightKg)

  weightTrendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#E5E7EB',
      textStyle: { color: '#44403C', fontSize: 12 },
      formatter: (p) => `${p[0].axisValue}<br/>体重: <b>${p[0].value ?? '--'}</b> kg`
    },
    grid: { left: 8, right: 20, top: 12, bottom: 8 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisTick: { show: false },
      axisLabel: { color: '#A8A29E', fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      name: 'kg',
      axisLabel: { color: '#A8A29E', fontSize: 10 },
      splitLine: { lineStyle: { color: '#F5F0EB', type: 'dashed' } },
      nameTextStyle: { color: '#A8A29E', fontSize: 9 }
    },
    series: [{
      type: 'line',
      data: weights,
      smooth: true,
      symbol: 'circle',
      symbolSize: 5,
      lineStyle: { color: '#C2410C', width: 2 },
      itemStyle: { color: '#C2410C' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(253,186,116,0.25)' },
          { offset: 1, color: 'rgba(253,186,116,0.02)' }
        ])
      },
      connectNulls: true
    }]
  }, true)
}

// ═══════════════ 数据获取 ═══════════════
async function fetchOverview() {
  try {
    const res = await dashboardApi.getOverview()
    Object.assign(overview, res.data)
    await nextTick()
    renderWeightTrend()
  } catch {}
}

// ═══════════════ 工具函数 ═══════════════
function fmtNum(v) {
  const n = Number(v || 0)
  return n % 1 === 0 ? n.toLocaleString('zh-CN') : n.toFixed(1)
}

// ═══════════════ 生命周期 ═══════════════
let resizeHandler

onMounted(async () => {
  resizeHandler = () => {
    weightTrendChart?.resize()
  }
  window.addEventListener('resize', resizeHandler)

  await Promise.all([fetchOverview(), fetchTasks()])
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeHandler)
  weightTrendChart?.dispose()
})
</script>

<style scoped>
/* ═══════════════ 根容器 ═══════════════ */
.dashboard-home {
  padding: 28px 32px;
  background: #FAFAF8;
  font-family: 'PingFang SC', 'Helvetica Neue', sans-serif;
  min-height: 100vh;
}

/* ═══════════════ 问候栏 ═══════════════ */
.greeting-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 24px;
  margin-bottom: 24px;
  border-bottom: 0.5px solid #E5E7EB;
}

.greeting-text {
  flex: 1;
}

.greeting-title {
  font-size: 24px;
  font-weight: 450;
  color: #1C1917;
  margin: 0 0 4px;
  letter-spacing: -0.02em;
}
.greeting-title strong {
  font-weight: 650;
  color: #C2410C;
}
.greeting-wave {
  display: inline-block;
  animation: wave 1.5s ease-in-out infinite;
}

@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(20deg); }
  50% { transform: rotate(-10deg); }
  75% { transform: rotate(15deg); }
}

.greeting-date {
  margin: 0;
  font-size: 13px;
  color: #78716C;
  font-weight: 450;
}

.greeting-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #F5E6D8;
  color: #C2410C;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
  margin-left: 16px;
}

/* ═══════════════ 数据概览卡片行 ═══════════════ */
.metric-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 22px;
}

.metric-card {
  background: #F5F0EB;
  border-radius: 12px;
  padding: 18px;
  cursor: pointer;
  transition: all 0.22s;
  border: 0.5px solid transparent;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.metric-card:hover {
  background: #FDF6EE;
  border-color: rgba(194, 65, 12, 0.15);
  transform: translateY(-1px);
}

.metric-icon-box {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2px;
}

.task-icon-box { background: #FEF3C7; color: #C2410C; }
.weight-icon-box { background: #ECFDF5; color: #0F766E; }
.diet-icon-box { background: #FFF7ED; color: #B45309; }
.ledger-icon-box { background: #EFF6FF; color: #0369A1; }

.metric-info {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: #1C1917;
  letter-spacing: -0.03em;
  line-height: 1.2;
}
.metric-value.negative { color: #DC2626; }

.metric-unit {
  font-size: 12px;
  font-weight: 500;
  color: #A8A29E;
  margin-left: 1px;
}

.metric-label {
  font-size: 12px;
  color: #A8A29E;
  font-weight: 500;
  margin-top: 2px;
}

.metric-sub {
  font-size: 11px;
  color: #78716C;
}
.metric-sub.done-all { color: #0F766E; }
.metric-sub.empty { color: #A8A29E; }
.metric-sub.placeholder { color: #D6D3D1; font-style: italic; }

.dir-down { color: #0F766E; font-weight: 600; }
.dir-up { color: #C2410C; font-weight: 600; }
.dir-stable { color: #78716C; }

/* ═══════════════ 主内容区双列 ═══════════════ */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.left-col, .right-col {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* ═══════════════ 通用卡片 ═══════════════ */
.card {
  background: #FFF;
  border-radius: 14px;
  padding: 20px 22px;
  border: 0.5px solid #E5E7EB;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.card-title {
  font-size: 15px;
  font-weight: 650;
  color: #1C1917;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.task-dot { background: #C2410C; }
.weight-dot { background: #0F766E; }
.diet-dot { background: #B45309; }
.finance-dot { background: #0369A1; }
.habit-dot { background: #7C3AED; }

.card-link {
  font-size: 12px;
  color: #A8A29E;
  text-decoration: none;
  transition: color 0.15s;
}
.card-link:hover { color: #C2410C; }

.card-month {
  font-size: 12px;
  color: #A8A29E;
  font-weight: 500;
}

/* ═══════════════ 任务列表 ═══════════════ */
.task-list-card {
  flex: 1;
  min-height: 0;
}

.task-list {
  display: flex;
  flex-direction: column;
}

.task-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 10px;
  border-radius: 9px;
  transition: background 0.15s;
}
.task-row:not(:last-child) {
  border-bottom: 0.5px solid #F5F0EB;
}
.task-row:hover {
  background: #FDF6EE;
}
.task-row.checked {
  opacity: 0.65;
}

.task-dot-color {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.task-name {
  flex: 1;
  font-size: 14px;
  color: #1C1917;
  font-weight: 480;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.task-row.checked .task-name {
  text-decoration: line-through;
  color: #A8A29E;
}

.task-streak {
  font-size: 11px;
  font-weight: 600;
  background: #FEF3C7;
  color: #B45309;
  padding: 3px 8px;
  border-radius: 20px;
  white-space: nowrap;
  flex-shrink: 0;
}
.task-streak.done {
  background: #ECFDF5;
  color: #0F766E;
}

.checkin-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 2px solid #E5E7EB;
  background: #FFF;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}
.checkin-btn:hover:not(:disabled) {
  border-color: #C2410C;
  background: #FFF7ED;
}
.checkin-btn.done {
  background: #0F766E;
  border-color: #0F766E;
  color: #FFF;
  cursor: default;
}
.checkin-btn.bouncing {
  animation: checkinBounce 0.3s ease;
}

@keyframes checkinBounce {
  0% { transform: scale(0.85); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.checkin-circle {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #D6D3D1;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 32px 16px;
}
.empty-illustration {
  font-size: 36px;
  margin-bottom: 10px;
}
.empty-state p {
  font-size: 13px;
  color: #A8A29E;
  margin: 0 0 10px;
}
.empty-link {
  font-size: 13px;
  color: #C2410C;
  text-decoration: none;
  font-weight: 500;
}
.empty-link:hover { text-decoration: underline; }

/* ═══════════════ 体重趋势 ═══════════════ */
.trend-tabs {
  display: flex;
  gap: 2px;
}
.trend-tab {
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 6px;
  cursor: default;
  transition: all 0.15s;
}
.trend-tab.active {
  background: rgba(194, 65, 12, 0.1);
  color: #C2410C;
  font-weight: 600;
}
.trend-tab.muted {
  color: #D6D3D1;
}

.chart-box {
  width: 100%;
  height: 200px;
}

/* ═══════════════ 热量环形图 ═══════════════ */
.calorie-body {
  display: flex;
  align-items: center;
  gap: 18px;
}

.calorie-ring-wrap {
  position: relative;
  width: 80px;
  height: 80px;
  flex-shrink: 0;
}

.calorie-svg {
  width: 100%;
  height: 100%;
}

.calorie-pct {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 17px;
  font-weight: 700;
  color: #1C1917;
}

.calorie-text {
  flex: 1;
  min-width: 0;
}

.calorie-intake {
  font-size: 22px;
  font-weight: 700;
  color: #1C1917;
  line-height: 1.2;
}

.calorie-unit {
  font-size: 12px;
  font-weight: 500;
  color: #A8A29E;
}

.calorie-target-text {
  font-size: 12px;
  color: #78716C;
}
.calorie-target-text.placeholder {
  color: #D6D3D1;
  font-style: italic;
}

.calorie-remain {
  font-size: 12px;
  color: #0F766E;
  font-weight: 500;
  margin-top: 2px;
}
.calorie-remain.over {
  color: #DC2626;
}

.meal-tags {
  display: flex;
  gap: 6px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.meal-tag {
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 20px;
  font-weight: 500;
}

.empty-tag {
  background: #F5F0EB;
  color: #A8A29E;
}

/* ═══════════════ 收支小结 ═══════════════ */
.finance-rows {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.finance-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.finance-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}
.income-icon { background: #ECFDF5; color: #0F766E; }
.expense-icon { background: #FEF2F2; color: #DC2626; }
.balance-icon { background: #F5F0EB; color: #44403C; }

.finance-label {
  font-size: 13px;
  color: #78716C;
  flex: 1;
}

.finance-val {
  font-size: 15px;
  font-weight: 700;
  color: #1C1917;
}
.finance-val.income-val { color: #0F766E; }
.finance-val.expense-val { color: #DC2626; }
.finance-val.negative { color: #DC2626; }

.finance-divider {
  height: 0.5px;
  background: #E5E7EB;
  margin: 2px 0;
}

.balance-row .finance-val {
  font-size: 17px;
}

.budget-placeholder {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 12px;
  padding: 8px 12px;
  background: #F5F0EB;
  border-radius: 8px;
  font-size: 11px;
  color: #A8A29E;
}

/* ═══════════════ 本周打卡柱状图 ═══════════════ */
.weekly-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 130px;
  padding-top: 8px;
}

.weekly-bar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex: 1;
  max-width: 48px;
}

.bar-label {
  font-size: 10px;
  color: #A8A29E;
  font-weight: 500;
  min-height: 14px;
}

.bar-fill-wrap {
  width: 100%;
  height: 80px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.bar-fill {
  width: 24px;
  border-radius: 6px 6px 0 0;
  transition: height 0.4s ease;
  min-height: 4px;
}
.bar-fill.past {
  background: #E9D5FF;
}
.bar-fill.today {
  background: #C4B5FD;
  box-shadow: 0 2px 8px rgba(124, 58, 237, 0.15);
}
.bar-fill.future {
  background: #F3F4F6;
}

.bar-day {
  font-size: 11px;
  color: #A8A29E;
  font-weight: 500;
}

/* ═══════════════ Element Plus 覆写 ═══════════════ */
:deep(.el-loading-mask) {
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.6);
}
</style>
