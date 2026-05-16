<template>
  <div class="ledger-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">账本管理</h1>
        <p class="page-subtitle">追踪收支，掌握财务健康</p>
      </div>
      <div class="header-actions">
        <div class="month-nav">
          <button class="month-btn" @click="prevMonth"><el-icon><ArrowLeft /></el-icon></button>
          <span class="month-label">{{ yearMonthLabel }}</span>
          <button class="month-btn" @click="nextMonth"><el-icon><ArrowRight /></el-icon></button>
        </div>
        <el-button class="export-btn" @click="handleExportCSV" size="small">
          <el-icon :size="14"><Download /></el-icon>
          导出 CSV
        </el-button>
      </div>
    </div>

    <!-- Stat Cards -->
    <div class="stat-row">
      <div class="stat-card income-card">
        <div class="stat-icon-wrap income-icon-wrap">
          <el-icon :size="18"><Top /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-label">总收入</span>
          <span class="stat-value income-value">{{ fmtMoney(stats.totalIncome) }}</span>
        </div>
      </div>
      <div class="stat-card expense-card">
        <div class="stat-icon-wrap expense-icon-wrap">
          <el-icon :size="18"><Bottom /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-label">总支出</span>
          <span class="stat-value expense-value">{{ fmtMoney(stats.totalExpense) }}</span>
        </div>
      </div>
      <div class="stat-card balance-card">
        <div class="stat-icon-wrap" :class="balanceClass">
          <el-icon :size="18"><Wallet /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-label">结余</span>
          <span class="stat-value" :class="balanceTextClass">{{ fmtSignedMoney(stats.balance) }}</span>
        </div>
      </div>
    </div>

    <!-- Charts Row -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <h3>支出分类占比</h3>
        </div>
        <div ref="expenseChartRef" class="chart-box"></div>
        <div v-if="!stats.expenseCategories || stats.expenseCategories.length === 0" class="chart-empty">暂无数据</div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3>收入分类占比</h3>
        </div>
        <div ref="incomeChartRef" class="chart-box"></div>
        <div v-if="!stats.incomeCategories || stats.incomeCategories.length === 0" class="chart-empty">暂无数据</div>
      </div>
    </div>

    <!-- Records Table -->
    <div class="data-card">
      <div class="card-header">
        <h3>收支记录</h3>
        <div class="card-header-right">
          <el-select v-model="filterType" placeholder="全部类型" clearable size="small" style="width: 110px; margin-right: 8px" @change="onFilterChange">
            <el-option label="支出" :value="0" />
            <el-option label="收入" :value="1" />
          </el-select>
          <el-select v-model="filterCategory" placeholder="全部分类" clearable size="small" style="width: 130px; margin-right: 8px" @change="onFilterChange">
            <el-option v-for="c in allCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-date-picker
            v-model="filterDate"
            type="date"
            size="small"
            placeholder="全部日期"
            clearable
            style="width: 140px; margin-right: 8px"
            @change="onFilterChange"
          />
          <el-button type="primary" size="small" @click="openAddDialog" class="add-btn">
            <el-icon :size="14"><Plus /></el-icon>
            新增记录
          </el-button>
        </div>
      </div>
      <el-table :data="records" stripe style="width: 100%" v-loading="loading" empty-text="暂无收支记录">
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column label="类型" width="70">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'danger'" size="small" effect="light">
              {{ row.type === 1 ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column label="金额" width="130">
          <template #default="{ row }">
            <span :class="row.type === 1 ? 'income-value' : 'expense-value'">
              {{ row.type === 1 ? '+' : '-' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="payment" label="支付方式" width="100">
          <template #default="{ row }">
            {{ paymentLabel(row.payment) }}
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" min-width="140">
          <template #default="{ row }">
            {{ row.note || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">
              <el-icon :size="15"><Edit /></el-icon>
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row.id)">
              <el-icon :size="15"><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchRecords"
          small
        />
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑记录' : '新增记录'" width="460px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio-button :value="0">支出</el-radio-button>
            <el-radio-button :value="1">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" controls-position="right" style="width: 100%" placeholder="请输入金额" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option
              v-for="c in filteredCategories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="form.payment" placeholder="选择支付方式" clearable style="width: 100%">
            <el-option label="现金" value="cash" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="微信" value="wechat" />
            <el-option label="银行卡" value="card" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" placeholder="备注（选填）" maxlength="200" />
        </el-form-item>
        <el-form-item label="日期" prop="recordDate">
          <el-date-picker v-model="form.recordDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Top, Bottom, Wallet, Plus, Edit, Delete, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ledgerApi } from '@/api/ledger'

// ==================== Month ====================
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)
const yearMonthLabel = computed(() => `${currentYear.value}年${currentMonth.value}月`)

function prevMonth() {
  if (currentMonth.value === 1) { currentMonth.value = 12; currentYear.value-- }
  else currentMonth.value--
  refreshAll()
}
function nextMonth() {
  if (currentMonth.value === 12) { currentMonth.value = 1; currentYear.value++ }
  else currentMonth.value++
  refreshAll()
}

// ==================== Stats ====================
const stats = reactive({ totalIncome: 0, totalExpense: 0, balance: 0, incomeCategories: [], expenseCategories: [] })
const balanceClass = computed(() => stats.balance >= 0 ? 'balance-icon-positive' : 'balance-icon-negative')
const balanceTextClass = computed(() => stats.balance >= 0 ? 'income-value' : 'expense-value')

function fmtMoney(val) {
  if (val == null) return '0.00'
  return Number(val).toFixed(2)
}
function fmtSignedMoney(val) {
  if (val == null) return '0.00'
  const n = Number(val)
  return (n >= 0 ? '+' : '') + n.toFixed(2)
}

async function fetchStats() {
  try {
    const res = await ledgerApi.getStats({ year: currentYear.value, month: currentMonth.value })
    Object.assign(stats, res.data)
    nextTick(() => { renderExpenseChart(); renderIncomeChart() })
  } catch {}
}

// ==================== Charts ====================
const expenseChartRef = ref(null)
const incomeChartRef = ref(null)
let expenseChart = null
let incomeChart = null

const expensePalette = ['#c9a96e', '#d4a76a', '#b8945c', '#a4844f', '#e8d5a3', '#c17b4a']
const incomePalette = ['#3b6b8c', '#4a7f9e', '#5a94b1', '#6da8c4', '#38709a', '#5090b0']

function renderPieChart(chart, refEl, categories, palette) {
  if (!refEl.value) return
  if (!chart) chart = echarts.init(refEl.value)
  if (!categories || categories.length === 0) {
    chart.setOption({ series: [{ type: 'pie', data: [] }] }, true)
    return chart
  }
  chart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#e9e8e4',
      textStyle: { color: '#1a1f36', fontSize: 12, fontFamily: 'DM Sans, sans-serif' },
      formatter: '{b}: {c} ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: ['52%', '80%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 3, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 12, fontWeight: 600, fontFamily: 'DM Sans, sans-serif' },
        scaleSize: 6
      },
      data: categories.map((c, i) => ({
        name: c.categoryName,
        value: Number(c.amount)
      })),
      color: palette
    }]
  }, true)
  return chart
}

function renderExpenseChart() {
  expenseChart = renderPieChart(expenseChart, expenseChartRef, stats.expenseCategories, expensePalette)
}
function renderIncomeChart() {
  incomeChart = renderPieChart(incomeChart, incomeChartRef, stats.incomeCategories, incomePalette)
}

// ==================== Records ====================
const records = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const filterType = ref(null)
const filterCategory = ref(null)
const filterDate = ref(null)

function onFilterChange() {
  page.value = 1
  fetchRecords()
}

async function fetchRecords() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    if (filterType.value != null) params.type = filterType.value
    if (filterCategory.value) params.categoryId = filterCategory.value
    if (filterDate.value) params.date = formatDate(filterDate.value)
    const res = await ledgerApi.getRecords(params)
    records.value = res.data.records
    total.value = res.data.total
  } catch {} finally { loading.value = false }
}

async function handleDelete(id) {
  try { await ElMessageBox.confirm('确定删除这条记录？', '确认删除', { type: 'warning' }) } catch { return }
  try {
    await ledgerApi.deleteRecord(id)
    ElMessage.success('已删除')
    await fetchRecords()
    await fetchStats()
  } catch {}
}

// ==================== Categories ====================
const allCategories = ref([])

const filteredCategories = computed(() => {
  if (form.type == null) return allCategories.value
  return allCategories.value.filter(c => c.type === form.type)
})

async function fetchCategories() {
  try {
    const res = await ledgerApi.getCategories()
    allCategories.value = res.data || []
  } catch {}
}

// ==================== Dialog ====================
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  type: 0,
  amount: null,
  categoryId: null,
  payment: null,
  note: '',
  recordDate: null
})
const rules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

function resetForm() {
  form.type = 0
  form.amount = null
  form.categoryId = null
  form.payment = null
  form.note = ''
  form.recordDate = null
  editId.value = null
}

function openAddDialog() {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  form.type = row.type
  form.amount = row.amount
  form.categoryId = row.categoryId
  form.payment = row.payment
  form.note = row.note || ''
  form.recordDate = row.recordDate
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = {
      type: form.type,
      amount: form.amount,
      categoryId: form.categoryId,
      payment: form.payment,
      note: form.note,
      recordDate: formatDate(form.recordDate)
    }
    if (isEdit.value) {
      await ledgerApi.updateRecord(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await ledgerApi.addRecord(payload)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    page.value = 1
    await fetchRecords()
    await fetchStats()
  } catch {} finally { submitting.value = false }
}

// ==================== CSV Export ====================
async function handleExportCSV() {
  try {
    const res = await ledgerApi.exportCSV({ year: currentYear.value, month: currentMonth.value })
    const bom = '﻿'
    const blob = new Blob([bom + res.data], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `ledger-${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}.csv`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {}
}

// ==================== Utils ====================
function formatDate(d) {
  if (!d) return null
  const date = new Date(d)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function paymentLabel(p) {
  const map = { cash: '现金', alipay: '支付宝', wechat: '微信', card: '银行卡' }
  return map[p] || p || '-'
}

// ==================== Lifecycle ====================
function refreshAll() {
  fetchStats()
  fetchRecords()
}

let resizeHandler

onMounted(async () => {
  resizeHandler = () => { expenseChart?.resize(); incomeChart?.resize() }
  window.addEventListener('resize', resizeHandler)
  await fetchCategories()
  await fetchStats()
  await fetchRecords()
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeHandler)
  expenseChart?.dispose()
  incomeChart?.dispose()
})
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@500;600;700&family=DM+Sans:wght@400;500;600;700&display=swap');
</style>

<style scoped>
.ledger-page {
  padding: 28px 32px;
  min-height: 100vh;
  background: #fafaf8;
  font-family: 'DM Sans', sans-serif;
}

/* Header */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
}
.page-title {
  font-family: 'Cormorant Garamond', serif;
  font-size: 30px;
  font-weight: 700;
  color: #1a1f36;
  margin: 0 0 4px;
  letter-spacing: -0.5px;
}
.page-subtitle {
  margin: 0;
  color: #8a8d9c;
  font-size: 14px;
  font-weight: 500;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.month-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 1px solid #e9e8e4;
  border-radius: 10px;
  padding: 6px 14px;
}
.month-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 6px;
  color: #6b6d7a;
  cursor: pointer;
  transition: all 0.15s;
}
.month-btn:hover {
  background: #f3f3f0;
  color: #1a1f36;
}
.month-label {
  font-size: 14px;
  font-weight: 600;
  color: #1a1f36;
  min-width: 80px;
  text-align: center;
  letter-spacing: 0.3px;
}

.export-btn {
  border-color: #c9a96e !important;
  color: #a88b4f !important;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.2s;
}
.export-btn:hover {
  background: #fdf8f0 !important;
  border-color: #b8944f !important;
  color: #8b6e3a !important;
}

/* Stat Cards */
.stat-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #eeedea;
  box-shadow: 0 1px 2px rgba(26,31,54,0.03);
  transition: box-shadow 0.25s, transform 0.25s;
  animation: cardReveal 0.5s ease backwards;
}
.income-card { animation-delay: 0s; }
.expense-card { animation-delay: 0.08s; }
.balance-card { animation-delay: 0.16s; }
@keyframes cardReveal {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.stat-card:hover {
  box-shadow: 0 4px 12px rgba(26,31,54,0.06);
  transform: translateY(-1px);
}
.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.income-icon-wrap { background: #eaf5f0; color: #2d8a5e; }
.expense-icon-wrap { background: #fdf0ed; color: #c95a4a; }
.balance-icon-positive { background: #eef3fa; color: #3b6b8c; }
.balance-icon-negative { background: #fdf0ed; color: #c95a4a; }
.stat-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.stat-label {
  font-size: 12px;
  color: #8a8d9c;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.stat-value {
  font-family: 'DM Sans', sans-serif;
  font-size: 22px;
  font-weight: 700;
  color: #1a1f36;
  letter-spacing: -0.3px;
}
.income-value { color: #2d8a5e; }
.expense-value { color: #c95a4a; }

/* Charts Row */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}
.chart-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  border: 1px solid #eeedea;
  box-shadow: 0 1px 2px rgba(26,31,54,0.03);
  position: relative;
}
.chart-header {
  margin-bottom: 8px;
}
.chart-header h3 {
  font-family: 'Cormorant Garamond', serif;
  font-size: 17px;
  font-weight: 600;
  color: #1a1f36;
  margin: 0;
}
.chart-box {
  width: 100%;
  height: 240px;
}
.chart-empty {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #b0b2bd;
  font-size: 13px;
}

/* Data Card */
.data-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  border: 1px solid #eeedea;
  box-shadow: 0 1px 2px rgba(26,31,54,0.03);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}
.card-header h3 {
  font-family: 'Cormorant Garamond', serif;
  font-size: 18px;
  font-weight: 600;
  color: #1a1f36;
  margin: 0;
}
.card-header-right {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.add-btn {
  background: #1a1f36 !important;
  border-color: #1a1f36 !important;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.2s;
}
.add-btn:hover {
  background: #2d334f !important;
  border-color: #2d334f !important;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* Table tweaks */
:deep(.el-table) {
  --el-table-header-bg-color: #f8f8f5;
  --el-table-tr-bg-color: #fff;
  --el-table-row-hover-bg-color: #f9f8f5;
  border-radius: 10px;
  font-family: 'DM Sans', sans-serif;
}
:deep(.el-table th.el-table__cell) {
  color: #6b6d7a;
  font-weight: 700;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  border-bottom: 2px solid #eeedea;
}
:deep(.el-table td.el-table__cell) {
  color: #3a3d4c;
  font-size: 13px;
}
:deep(.el-pagination) {
  --el-color-primary: #1a1f36;
}
:deep(.el-button--primary) {
  --el-button-bg-color: #1a1f36;
  --el-button-border-color: #1a1f36;
  --el-button-hover-bg-color: #2d334f;
  --el-button-hover-border-color: #2d334f;
}
:deep(.el-dialog) {
  border-radius: 16px;
}
:deep(.el-input-number) {
  width: 100%;
}
:deep(.el-radio-button__inner) {
  font-family: 'DM Sans', sans-serif;
}
</style>
