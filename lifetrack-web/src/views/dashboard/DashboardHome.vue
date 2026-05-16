<template>
  <div class="dashboard-home">
    <div class="page-header">
      <div>
        <h2 class="page-title">仪表盘</h2>
        <p class="page-date">{{ todayStr }}</p>
      </div>
    </div>

    <!-- Stat Cards -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card task-stat" @click="$router.push('/tasks')">
          <div class="stat-icon-box task-icon-box">
            <el-icon :size="20"><List /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.todayCheckins }}<span class="stat-unit">/{{ overview.totalTasks }}</span></div>
            <div class="stat-label">今日打卡</div>
          </div>
          <div class="stat-ring">
            <svg viewBox="0 0 60 60" class="ring-svg">
              <circle cx="30" cy="30" r="25" fill="none" stroke="#eef2ff" stroke-width="5" />
              <circle cx="30" cy="30" r="25" fill="none" stroke="#6366f1" stroke-width="5"
                :stroke-dasharray="taskRingDash"
                stroke-linecap="round" transform="rotate(-90 30 30)" />
            </svg>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card weight-stat" @click="$router.push('/health')">
          <div class="stat-icon-box weight-icon-box">
            <el-icon :size="20"><Histogram /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.latestWeight || '--' }}<span v-if="overview.latestWeight" class="stat-unit">kg</span></div>
            <div class="stat-label">最新体重</div>
          </div>
          <span v-if="overview.weightDirection" class="direction-tag" :class="'dir-' + overview.weightDirection">
            {{ directionLabel(overview.weightDirection) }}
          </span>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card diet-stat" @click="$router.push('/health')">
          <div class="stat-icon-box diet-icon-box">
            <el-icon :size="20"><KnifeFork /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.todayCalories || 0 }}<span class="stat-unit">kcal</span></div>
            <div class="stat-label">今日摄入</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card ledger-stat" @click="$router.push('/ledger')">
          <div class="stat-icon-box ledger-icon-box">
            <el-icon :size="20"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value" :class="{ negative: balanceNegative }">{{ balanceStr }}</div>
            <div class="stat-label">本月结余</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts Row -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-card-header">
            <h3>本周打卡趋势</h3>
          </div>
          <div ref="checkinChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-card-header">
            <h3>体重趋势（近7天）</h3>
          </div>
          <div ref="weightMiniChartRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <!-- Monthly Finance + Calendar -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-card-header">
            <h3>本月收支概览</h3>
            <div class="finance-summary">
              <span class="income-text">收 ¥{{ fmtNum(overview.monthlyIncome || 0) }}</span>
              <span class="divider">|</span>
              <span class="expense-text">支 ¥{{ fmtNum(overview.monthlyExpense || 0) }}</span>
            </div>
          </div>
          <div ref="financeChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-card-header">
            <h3>打卡日历</h3>
          </div>
          <div class="calendar-mini" v-loading="calendarLoading">
            <div class="cal-month-nav">
              <button class="cal-nav-btn" @click="calShiftMonth(-1)"><el-icon :size="14"><ArrowLeft /></el-icon></button>
              <span class="cal-month-label">{{ calYear }}年{{ calMonth }}月</span>
              <button class="cal-nav-btn" @click="calShiftMonth(1)"><el-icon :size="14"><ArrowRight /></el-icon></button>
            </div>
            <div class="cal-weekdays">
              <span v-for="d in weekHeaders" :key="d" class="cal-wd">{{ d }}</span>
            </div>
            <div class="cal-grid">
              <div
                v-for="(day, i) in calendarDays"
                :key="i"
                class="cal-cell"
                :class="{
                  'cal-empty': !day,
                  'cal-today': day && day.date === todayStr,
                  'cal-checkin': day && day.checkinCount > 0,
                  'cal-multi': day && day.checkinCount > 1
                }"
              >
                <span v-if="day" class="cal-num">{{ day.date?.slice(-2)?.replace(/^0/, '') }}</span>
                <span v-if="day" class="cal-dots">
                  <span v-if="day.checkinCount" class="cal-dot checkin-dot" :title="day.checkinCount + '次打卡'"></span>
                  <span v-if="day.hasExpense" class="cal-dot expense-dot" title="有支出"></span>
                  <span v-if="day.hasIncome" class="cal-dot income-dot" title="有收入"></span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { List, Histogram, KnifeFork, Money, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { dashboardApi } from '@/api/dashboard'

const todayStr = new Date().toISOString().slice(0, 10)

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
  balance: 0
})

const taskRingDash = computed(() => {
  const rate = overview.todayRate || 0
  const len = 2 * Math.PI * 25
  return `${len * rate} ${len}`
})

const balanceStr = computed(() => {
  const v = overview.balance || 0
  const n = Number(v)
  return (n >= 0 ? '+' : '') + n.toFixed(0)
})

const balanceNegative = computed(() => Number(overview.balance || 0) < 0)

function directionLabel(d) {
  const map = { up: '↑ 上升', down: '↓ 下降', stable: '→ 持平' }
  return map[d] || ''
}

function fmtNum(v) {
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

// ==================== Charts ====================
const checkinChartRef = ref(null)
const weightMiniChartRef = ref(null)
const financeChartRef = ref(null)
let checkinChart = null
let weightMiniChart = null
let financeChart = null

function renderCheckinChart(data) {
  if (!checkinChartRef.value) return
  if (!checkinChart) checkinChart = echarts.init(checkinChartRef.value)

  // Build last 7 days
  const days = []
  const vals = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const key = d.toISOString().slice(0, 10)
    days.push(key.slice(5))
    vals.push(data?.[key] ?? 0)
  }

  checkinChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e2e8f0',
      textStyle: { color: '#334155', fontSize: 12 },
      formatter: (p) => `${p[0].axisValue}<br/>打卡: <b>${p[0].value}</b> 次`
    },
    grid: { left: 8, right: 16, top: 12, bottom: 8 },
    xAxis: {
      type: 'category',
      data: days,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#94a3b8', fontSize: 10 },
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: vals,
      barWidth: 18,
      itemStyle: {
        borderRadius: [5, 5, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#818cf8' },
          { offset: 1, color: '#c7d2fe' }
        ])
      }
    }]
  }, true)
}

function renderWeightMiniChart(data) {
  if (!weightMiniChartRef.value) return
  if (!weightMiniChart) weightMiniChart = echarts.init(weightMiniChartRef.value)

  const dates = (data || []).map(d => d.date?.slice(5) || '')
  const weights = (data || []).map(d => d.weightKg)

  weightMiniChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e2e8f0',
      textStyle: { color: '#334155', fontSize: 12 },
      formatter: (p) => `${p[0].axisValue}<br/>体重: <b>${p[0].value ?? '--'}</b> kg`
    },
    grid: { left: 8, right: 16, top: 12, bottom: 8 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      name: 'kg',
      axisLabel: { color: '#94a3b8', fontSize: 10 },
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
      nameTextStyle: { color: '#94a3b8', fontSize: 9 }
    },
    series: [{
      type: 'line',
      data: weights,
      smooth: true,
      symbol: 'circle',
      symbolSize: 4,
      lineStyle: { color: '#10b981', width: 2 },
      itemStyle: { color: '#10b981' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(16,185,129,0.2)' },
          { offset: 1, color: 'rgba(16,185,129,0.02)' }
        ])
      },
      connectNulls: true
    }]
  }, true)
}

function renderFinanceChart(income, expense) {
  if (!financeChartRef.value) return
  if (!financeChart) financeChart = echarts.init(financeChartRef.value)

  const inc = Number(income || 0)
  const exp = Number(expense || 0)

  financeChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e2e8f0',
      textStyle: { color: '#334155', fontSize: 12 },
      formatter: (p) => {
        const v = p[0]
        return `${v.name}<br/><b>¥${Number(v.value).toLocaleString()}</b>`
      }
    },
    grid: { left: 8, right: 16, top: 16, bottom: 8 },
    xAxis: {
      type: 'category',
      data: ['收入', '支出'],
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { show: false },
      axisLabel: { color: '#64748b', fontSize: 12, fontWeight: 600 }
    },
    yAxis: {
      type: 'value',
      name: '¥',
      axisLabel: { color: '#94a3b8', fontSize: 10 },
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
      nameTextStyle: { color: '#94a3b8', fontSize: 9 }
    },
    series: [{
      type: 'bar',
      data: [
        { value: inc, itemStyle: { color: '#10b981', borderRadius: [6, 6, 0, 0] } },
        { value: exp, itemStyle: { color: '#f43f5e', borderRadius: [6, 6, 0, 0] } }
      ],
      barWidth: 40,
      label: {
        show: true,
        position: 'top',
        color: '#64748b',
        fontSize: 11,
        fontWeight: 600,
        formatter: (p) => '¥' + Number(p.value).toLocaleString()
      }
    }]
  }, true)
}

// ==================== Calendar ====================
const calendarLoading = ref(false)
const calYear = ref(new Date().getFullYear())
const calMonth = ref(new Date().getMonth() + 1)
const calendarDays = ref([])
const weekHeaders = ['一', '二', '三', '四', '五', '六', '日']

function calShiftMonth(delta) {
  let m = calMonth.value + delta
  let y = calYear.value
  if (m > 12) { m = 1; y++ }
  if (m < 1) { m = 12; y-- }
  calYear.value = y
  calMonth.value = m
  fetchCalendar()
}

function buildCalendarGrid(daysData) {
  const firstDay = new Date(calYear.value, calMonth.value - 1, 1)
  const lastDay = new Date(calYear.value, calMonth.value, 0)
  const totalDays = lastDay.getDate()
  // day of week: 0=Sun -> 6, 1=Mon -> 0, ..., 6=Sat -> 5
  let startDow = firstDay.getDay() - 1
  if (startDow < 0) startDow = 6

  const dataMap = {}
  if (daysData) {
    for (const d of daysData) {
      dataMap[d.date] = d
    }
  }

  const grid = []
  for (let i = 0; i < startDow; i++) grid.push(null)

  for (let d = 1; d <= totalDays; d++) {
    const dateStr = `${calYear.value}-${String(calMonth.value).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    grid.push(dataMap[dateStr] || { date: dateStr, checkinCount: 0, hasIncome: false, hasExpense: false })
  }

  while (grid.length % 7 !== 0) grid.push(null)
  calendarDays.value = grid
}

async function fetchCalendar() {
  calendarLoading.value = true
  try {
    const res = await dashboardApi.getCalendar({ year: calYear.value, month: calMonth.value })
    buildCalendarGrid(res.data?.days)
  } catch {} finally { calendarLoading.value = false }
}

// ==================== Lifecycle ====================
async function fetchOverview() {
  try {
    const res = await dashboardApi.getOverview()
    const d = res.data
    Object.assign(overview, d)

    await nextTick()
    renderCheckinChart(d.weekCheckins)
    renderWeightMiniChart(d.weightMiniTrend)
    renderFinanceChart(d.monthlyIncome, d.monthlyExpense)
  } catch {}
}

let resizeHandler

onMounted(async () => {
  resizeHandler = () => {
    checkinChart?.resize()
    weightMiniChart?.resize()
    financeChart?.resize()
  }
  window.addEventListener('resize', resizeHandler)

  await fetchOverview()
  await fetchCalendar()
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeHandler)
  checkinChart?.dispose()
  weightMiniChart?.dispose()
  financeChart?.dispose()
})
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');
</style>

<style scoped>
.dashboard-home {
  padding: 28px 32px;
  min-height: 100vh;
  background: #f5f6fa;
  font-family: 'Inter', sans-serif;
}

/* Header */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 2px;
  letter-spacing: -0.3px;
}
.page-date {
  margin: 0;
  color: #94a3b8;
  font-size: 13px;
  font-weight: 500;
}

/* Stat Cards */
.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  border-radius: 14px;
  border: 1px solid #edf0f5;
  transition: all 0.25s;
  overflow: hidden;
  position: relative;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 20px;
  gap: 14px;
}

.stat-icon-box {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.task-icon-box { background: #eef2ff; color: #6366f1; }
.weight-icon-box { background: #ecfdf5; color: #10b981; }
.diet-icon-box { background: #fef3c7; color: #f59e0b; }
.ledger-icon-box { background: #fce7f3; color: #ec4899; }

.stat-info {
  flex: 1;
  min-width: 0;
}
.stat-value {
  font-size: 22px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1.2;
  letter-spacing: -0.5px;
}
.stat-value.negative {
  color: #f43f5e;
}
.stat-unit {
  font-size: 12px;
  font-weight: 500;
  color: #94a3b8;
  margin-left: 2px;
}
.stat-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
  margin-top: 2px;
}

/* SVG ring for task card */
.stat-ring {
  flex-shrink: 0;
}
.ring-svg {
  width: 50px;
  height: 50px;
}

.direction-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 20px;
  flex-shrink: 0;
}
.dir-up { background: #fef2f2; color: #ef4444; }
.dir-down { background: #ecfdf5; color: #10b981; }
.dir-stable { background: #f1f5f9; color: #64748b; }

/* Chart Cards */
.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 22px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
  border: 1px solid #edf0f5;
}
.chart-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.chart-card-header h3 {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}
.chart-box {
  width: 100%;
  height: 200px;
}

.finance-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
}
.income-text { color: #10b981; }
.expense-text { color: #f43f5e; }
.divider { color: #e2e8f0; }

/* Bottom */
.bottom-row {
  margin-bottom: 0;
}

/* Mini Calendar */
.calendar-mini {
  min-height: 200px;
}
.cal-month-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 10px;
}
.cal-nav-btn {
  border: none;
  background: #f1f5f9;
  border-radius: 6px;
  padding: 4px 8px;
  cursor: pointer;
  color: #64748b;
  display: flex;
  align-items: center;
  transition: all 0.15s;
}
.cal-nav-btn:hover {
  background: #e2e8f0;
  color: #334155;
}
.cal-month-label {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
  min-width: 70px;
  text-align: center;
}
.cal-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
  margin-bottom: 4px;
}
.cal-wd {
  text-align: center;
  font-size: 11px;
  font-weight: 600;
  color: #94a3b8;
  padding: 2px 0;
}
.cal-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}
.cal-cell {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  position: relative;
  cursor: default;
}
.cal-cell.cal-empty {
  color: transparent;
}
.cal-cell.cal-today {
  background: #eef2ff;
  color: #6366f1;
}
.cal-cell.cal-checkin {
  background: #f0fdf4;
  color: #166534;
}
.cal-cell.cal-multi {
  background: #dcfce7;
  color: #15803d;
}
.cal-num {
  font-size: 11px;
  line-height: 1;
}
.cal-dots {
  display: flex;
  gap: 2px;
  position: absolute;
  bottom: 2px;
}
.cal-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
}
.checkin-dot { background: #22c55e; }
.expense-dot { background: #f43f5e; }
.income-dot { background: #3b82f6; }

:deep(.el-card) {
  border-radius: 14px;
}
:deep(.el-loading-mask) {
  border-radius: 14px;
}
</style>
