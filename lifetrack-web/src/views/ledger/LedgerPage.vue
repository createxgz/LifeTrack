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

    <!-- Wallet Card -->
    <div class="wallet-card">
      <div class="wallet-summary" @click="walletExpanded = !walletExpanded">
        <div class="wallet-left">
          <div class="wallet-icon-wrap">
            <el-icon :size="20"><Coin /></el-icon>
          </div>
          <div class="wallet-info">
            <span class="wallet-label">钱包总额</span>
            <span class="wallet-value">{{ fmtMoney(walletTotal) }}</span>
          </div>
        </div>
        <div class="wallet-right">
          <el-button class="wallet-edit-btn" size="small" @click.stop="openWalletDialog">
            <el-icon :size="14"><Edit /></el-icon>
            编辑
          </el-button>
          <el-icon :class="['wallet-chevron', { expanded: walletExpanded }]"><ArrowDown /></el-icon>
        </div>
      </div>
      <div v-if="walletExpanded && walletAccounts.length > 0" class="wallet-breakdown">
        <div v-for="acct in walletAccounts" :key="acct.id" class="wallet-account-row">
          <span class="account-type-tag" :class="'tag-' + (acct.accountType || 'other')">{{ accountTypeLabel(acct.accountType) }}</span>
          <span class="account-name">{{ acct.name }}</span>
          <span class="account-amount">{{ fmtMoney(acct.amount) }}</span>
        </div>
      </div>
      <div v-if="walletExpanded && walletAccounts.length === 0" class="wallet-empty">
        暂无账户，点击「编辑」添加
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

    <!-- Budget Card -->
    <div class="data-card budget-card">
      <div class="card-header">
        <h3>预算管理</h3>
        <el-button size="small" @click="openBudgetDialog">
          <el-icon :size="14"><Edit /></el-icon>
          编辑预算
        </el-button>
      </div>
      <div v-if="budgets.length === 0" class="budget-empty">
        暂无预算设定，点击「编辑预算」为支出分类设置月度限额
      </div>
      <div v-else class="budget-list">
        <div v-for="b in budgets" :key="b.id" class="budget-row">
          <span class="budget-cat">{{ b.categoryName }}</span>
          <span class="budget-spent">已花 ¥{{ fmtMoney(b.spent) }}</span>
          <div class="budget-bar-wrap">
            <div class="budget-bar-fill" :style="{ width: Math.min(Number(b.percentage), 100) + '%', background: Number(b.percentage) > 100 ? '#DC2626' : Number(b.percentage) > 80 ? '#F59E0B' : '#0F766E' }"></div>
          </div>
          <span class="budget-limit">/ ¥{{ fmtMoney(b.monthlyLimit) }}</span>
          <span class="budget-pct" :class="{ over: Number(b.percentage) > 100 }">{{ Math.round(Number(b.percentage)) }}%</span>
        </div>
      </div>
      <div class="budget-summary" v-if="budgets.length > 0">
        <span>合计预算 ¥{{ fmtMoney(totalBudget) }}</span>
        <span>已用 ¥{{ fmtMoney(totalBudgetSpent) }}</span>
        <span :class="totalBudgetRemaining >= 0 ? 'income-value' : 'expense-value'">
          剩余 {{ fmtSignedMoney(totalBudgetRemaining) }}
        </span>
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
          <el-select v-model="filterCategory" placeholder="全部分类" clearable size="small" style="width: 130px; margin-right: 4px" @change="onFilterChange">
            <el-option v-for="c in allCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-button size="small" @click="openCategoryDialog" style="margin-right: 8px">
            <el-icon :size="14"><Setting /></el-icon>
          </el-button>
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

    <!-- Wallet Dialog -->
    <el-dialog v-model="walletDialogVisible" title="编辑钱包账户" width="520px" destroy-on-close>
      <div class="wallet-dialog-total">
        <span>总余额</span>
        <strong>{{ fmtMoney(walletTotal) }}</strong>
      </div>
      <el-table :data="walletAccounts" stripe style="width: 100%" empty-text="暂无账户，请在下方添加">
        <el-table-column label="账户名称" min-width="120">
          <template #default="{ row, $index }">
            <template v-if="walletEditingIdx === $index">
              <el-input v-model="walletEditForm.name" size="small" placeholder="账户名称" />
            </template>
            <template v-else>{{ row.name }}</template>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row, $index }">
            <template v-if="walletEditingIdx === $index">
              <el-select v-model="walletEditForm.accountType" size="small" style="width:100%">
                <el-option label="银行卡" value="bank" />
                <el-option label="支付宝" value="alipay" />
                <el-option label="微信" value="wechat" />
                <el-option label="现金" value="cash" />
                <el-option label="投资" value="investment" />
                <el-option label="其他" value="other" />
              </el-select>
            </template>
            <template v-else>
              <span class="account-type-tag" :class="'tag-' + (row.accountType || 'other')">{{ accountTypeLabel(row.accountType) }}</span>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row, $index }">
            <template v-if="walletEditingIdx === $index">
              <el-input-number v-model="walletEditForm.amount" :min="0" :precision="2" size="small" controls-position="right" style="width:100%" />
            </template>
            <template v-else>{{ fmtMoney(row.amount) }}</template>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row, $index }">
            <template v-if="walletEditingIdx === $index">
              <el-button type="primary" link size="small" @click="saveWalletEdit(row.id)">保存</el-button>
              <el-button link size="small" @click="walletEditingIdx = null">取消</el-button>
            </template>
            <template v-else>
              <el-button type="primary" link size="small" @click="startEditWallet(row, $index)">
                <el-icon :size="15"><Edit /></el-icon>
              </el-button>
              <el-button type="danger" link size="small" @click="handleDeleteWallet(row.id)">
                <el-icon :size="15"><Delete /></el-icon>
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div class="wallet-add-row" v-if="walletEditingIdx === -1">
        <el-input v-model="walletEditForm.name" size="small" placeholder="账户名称" style="width:140px" />
        <el-select v-model="walletEditForm.accountType" size="small" placeholder="类型" style="width:90px">
          <el-option label="银行卡" value="bank" />
          <el-option label="支付宝" value="alipay" />
          <el-option label="微信" value="wechat" />
          <el-option label="现金" value="cash" />
          <el-option label="投资" value="investment" />
          <el-option label="其他" value="other" />
        </el-select>
        <el-input-number v-model="walletEditForm.amount" :min="0" :precision="2" size="small" controls-position="right" placeholder="金额" style="width:130px" />
        <el-button type="primary" size="small" @click="saveWalletEdit(null)">添加</el-button>
        <el-button size="small" @click="walletEditingIdx = null">取消</el-button>
      </div>
      <template #footer>
        <el-button @click="startAddWallet" :disabled="walletEditingIdx != null">新增账户</el-button>
        <el-button @click="walletDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Category Management Dialog -->
    <el-dialog v-model="categoryDialogVisible" title="管理分类" width="600px" destroy-on-close>
      <el-tabs v-model="categoryTab">
        <el-tab-pane label="支出分类" :name="0" />
        <el-tab-pane label="收入分类" :name="1" />
      </el-tabs>
      <el-table :data="categorizedCategories" stripe style="width: 100%" empty-text="暂无分类">
        <el-table-column label="分类名" min-width="140">
          <template #default="{ row }">
            <template v-if="categoryEditingId === row.id">
              <el-input v-model="categoryEditForm.name" size="small" placeholder="分类名称" />
            </template>
            <template v-else>
              {{ row.name }}
              <el-tag v-if="row.isDefault === 1" size="small" type="info" effect="plain" style="margin-left:6px">系统</el-tag>
              <el-tag v-else size="small" type="warning" effect="plain" style="margin-left:6px">自定义</el-tag>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <template v-if="row.isDefault === 1">
              <span class="text-muted">—</span>
            </template>
            <template v-else>
              <template v-if="categoryEditingId === row.id">
                <el-button type="primary" link size="small" @click="saveCategoryEdit(row.id)">保存</el-button>
                <el-button link size="small" @click="categoryEditingId = null">取消</el-button>
              </template>
              <template v-else>
                <el-button type="primary" link size="small" @click="startEditCategory(row)">
                  <el-icon :size="15"><Edit /></el-icon>
                </el-button>
                <el-button type="danger" link size="small" @click="handleDeleteCategory(row)">
                  <el-icon :size="15"><Delete /></el-icon>
                </el-button>
              </template>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="categoryEditingId === -1" class="category-add-row">
        <el-input v-model="categoryEditForm.name" size="small" placeholder="新分类名称" style="width:160px" />
        <el-button type="primary" size="small" @click="saveCategoryEdit(null)">添加</el-button>
        <el-button size="small" @click="categoryEditingId = null">取消</el-button>
      </div>
      <template #footer>
        <el-button @click="startAddCategory" :disabled="categoryEditingId != null">新增分类</el-button>
        <el-button @click="categoryDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Budget Management Dialog -->
    <el-dialog v-model="budgetDialogVisible" title="编辑月度预算" width="560px" destroy-on-close>
      <p class="budget-dialog-hint">{{ yearMonthLabel }} · 为支出分类设置预算上限</p>
      <el-table :data="budgetEditList" stripe style="width: 100%" empty-text="暂无支出分类">
        <el-table-column label="分类" min-width="100">
          <template #default="{ row }">{{ row.categoryName }}</template>
        </el-table-column>
        <el-table-column label="当月已花" width="110">
          <template #default="{ row }">
            <span :class="row.spent > row.limitInput ? 'expense-value' : ''">¥{{ fmtMoney(row.spent) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预算上限" min-width="160">
          <template #default="{ row, $index }">
            <el-input-number
              v-model="row.limitInput"
              :min="0"
              :precision="2"
              controls-position="right"
              size="small"
              style="width: 130px"
              placeholder="不设预算"
            />
            <el-button v-if="row.budgetId" type="danger" link size="small" style="margin-left:4px" @click="handleDeleteBudget(row.budgetId, $index)">
              <el-icon :size="14"><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="budgetDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAllBudgets" :loading="budgetSaving">保存全部</el-button>
      </template>
    </el-dialog>

    <!-- Category Delete Migration Dialog -->
    <el-dialog v-model="migrateDialogVisible" title="迁移记录" width="440px" destroy-on-close>
      <p class="migrate-text">删除分类 <strong>{{ deletingCategory?.name }}</strong> 前，请选择将已有的收支记录迁移到哪个分类：</p>
      <el-select v-model="migrateTargetId" placeholder="选择目标分类" style="width: 100%">
        <el-option v-for="c in migrateOptions" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <template #footer>
        <el-button @click="migrateDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDeleteCategory" :disabled="!migrateTargetId">确认删除并迁移</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, ArrowDown, Top, Bottom, Wallet, Plus, Edit, Delete, Download, Coin, Setting } from '@element-plus/icons-vue'
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

// ==================== Wallet ====================
const walletTotal = ref(0)
const walletAccounts = ref([])
const walletExpanded = ref(false)
const walletDialogVisible = ref(false)
const walletEditingIdx = ref(null)
const walletEditForm = reactive({ name: '', accountType: 'bank', amount: 0 })

function accountTypeLabel(type) {
  const map = { bank: '银行卡', alipay: '支付宝', wechat: '微信', cash: '现金', investment: '投资', other: '其他' }
  return map[type] || '其他'
}

async function fetchWallets() {
  try {
    const res = await ledgerApi.getWallets()
    walletTotal.value = res.data.total || 0
    walletAccounts.value = res.data.accounts || []
  } catch {}
}

function openWalletDialog() {
  walletEditingIdx.value = null
  walletDialogVisible.value = true
}

function startAddWallet() {
  walletEditForm.name = ''
  walletEditForm.accountType = 'bank'
  walletEditForm.amount = 0
  walletEditingIdx.value = -1
}

function startEditWallet(row, idx) {
  walletEditForm.name = row.name
  walletEditForm.accountType = row.accountType || 'bank'
  walletEditForm.amount = row.amount
  walletEditingIdx.value = idx
}

async function saveWalletEdit(id) {
  if (!walletEditForm.name.trim()) { ElMessage.warning('请输入账户名称'); return }
  if (!walletEditForm.amount || walletEditForm.amount <= 0) { ElMessage.warning('请输入金额'); return }
  try {
    const payload = {
      name: walletEditForm.name.trim(),
      accountType: walletEditForm.accountType,
      amount: walletEditForm.amount
    }
    if (id != null) {
      await ledgerApi.updateWallet(id, payload)
      ElMessage.success('已更新')
    } else {
      await ledgerApi.addWallet(payload)
      ElMessage.success('已添加')
    }
    walletEditingIdx.value = null
    await fetchWallets()
  } catch {}
}

async function handleDeleteWallet(id) {
  try { await ElMessageBox.confirm('确定删除该钱包账户？', '确认删除', { type: 'warning' }) } catch { return }
  try {
    await ledgerApi.deleteWallet(id)
    ElMessage.success('已删除')
    await fetchWallets()
  } catch {}
}

// ==================== Categories ====================
const allCategories = ref([])
const categoryDialogVisible = ref(false)
const categoryTab = ref(0)
const categoryEditingId = ref(null)
const categoryEditForm = reactive({ name: '' })
const migrateDialogVisible = ref(false)
const deletingCategory = ref(null)
const migrateTargetId = ref(null)

const filteredCategories = computed(() => {
  if (form.type == null) return allCategories.value
  return allCategories.value.filter(c => c.type === form.type)
})

const categorizedCategories = computed(() => {
  return allCategories.value.filter(c => c.type === categoryTab.value)
})

const migrateOptions = computed(() => {
  if (!deletingCategory.value) return []
  return allCategories.value.filter(c => c.type === deletingCategory.value.type && c.id !== deletingCategory.value.id)
})

async function fetchCategories() {
  try {
    const res = await ledgerApi.getCategories()
    allCategories.value = res.data || []
  } catch {}
}

function openCategoryDialog() {
  categoryEditingId.value = null
  categoryDialogVisible.value = true
}

function startAddCategory() {
  categoryEditForm.name = ''
  categoryEditingId.value = -1
}

function startEditCategory(row) {
  categoryEditForm.name = row.name
  categoryEditingId.value = row.id
}

async function saveCategoryEdit(id) {
  if (!categoryEditForm.name.trim()) { ElMessage.warning('请输入分类名称'); return }
  try {
    if (id != null) {
      await ledgerApi.updateCategory(id, { name: categoryEditForm.name.trim() })
      ElMessage.success('已更新')
    } else {
      await ledgerApi.addCategory({ name: categoryEditForm.name.trim(), type: categoryTab.value })
      ElMessage.success('已添加')
    }
    categoryEditingId.value = null
    await fetchCategories()
  } catch {}
}

function handleDeleteCategory(row) {
  deletingCategory.value = row
  migrateTargetId.value = null
  migrateDialogVisible.value = true
}

async function confirmDeleteCategory() {
  if (!migrateTargetId.value) return
  try {
    await ledgerApi.deleteCategory(deletingCategory.value.id, migrateTargetId.value)
    ElMessage.success('已删除并迁移')
    migrateDialogVisible.value = false
    deletingCategory.value = null
    await fetchCategories()
  } catch {}
}

// ==================== Budgets ====================
const budgets = ref([])
const budgetDialogVisible = ref(false)
const budgetSaving = ref(false)
const budgetEditList = ref([])

const totalBudget = computed(() => {
  return budgets.value.reduce((sum, b) => sum + Number(b.monthlyLimit || 0), 0)
})
const totalBudgetSpent = computed(() => {
  return budgets.value.reduce((sum, b) => sum + Number(b.spent || 0), 0)
})
const totalBudgetRemaining = computed(() => {
  return totalBudget.value - totalBudgetSpent.value
})

async function fetchBudgets() {
  try {
    const res = await ledgerApi.getBudgets({ year: currentYear.value, month: currentMonth.value })
    budgets.value = res.data || []
  } catch {}
}

function openBudgetDialog() {
  const expenseCats = allCategories.value.filter(c => c.type === 0)
  const budgetMap = {}
  budgets.value.forEach(b => { budgetMap[b.categoryId] = b })
  budgetEditList.value = expenseCats.map(c => ({
    categoryId: c.id,
    categoryName: c.name,
    spent: budgetMap[c.id] ? Number(budgetMap[c.id].spent || 0) : 0,
    limitInput: budgetMap[c.id] ? Number(budgetMap[c.id].monthlyLimit || 0) : null,
    budgetId: budgetMap[c.id] ? budgetMap[c.id].id : null
  }))
  budgetDialogVisible.value = true
}

async function saveAllBudgets() {
  budgetSaving.value = true
  try {
    for (const row of budgetEditList.value) {
      if (row.budgetId) {
        if (row.limitInput > 0) {
          await ledgerApi.updateBudget(row.budgetId, { monthlyLimit: row.limitInput })
        } else {
          await ledgerApi.deleteBudget(row.budgetId)
        }
      } else if (row.limitInput > 0) {
        await ledgerApi.saveBudget({
          categoryId: row.categoryId,
          monthlyLimit: row.limitInput,
          year: currentYear.value,
          month: currentMonth.value
        })
      }
    }
    ElMessage.success('预算已保存')
    budgetDialogVisible.value = false
    await fetchBudgets()
  } catch {} finally { budgetSaving.value = false }
}

async function handleDeleteBudget(budgetId, index) {
  try {
    await ledgerApi.deleteBudget(budgetId)
    budgetEditList.value[index].limitInput = null
    budgetEditList.value[index].budgetId = null
    ElMessage.success('预算已删除')
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
  fetchWallets()
  fetchStats()
  fetchRecords()
  fetchBudgets()
}

let resizeHandler

onMounted(async () => {
  resizeHandler = () => { expenseChart?.resize(); incomeChart?.resize() }
  window.addEventListener('resize', resizeHandler)
  await Promise.all([fetchCategories(), fetchWallets(), fetchStats(), fetchRecords(), fetchBudgets()])
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

/* ── Wallet Card ── */
.wallet-card {
  background: linear-gradient(135deg, #1a1f36 0%, #2d334f 100%);
  border-radius: 14px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(26,31,54,0.12);
}
.wallet-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  cursor: pointer;
  user-select: none;
  transition: background 0.2s;
}
.wallet-summary:hover {
  background: rgba(255,255,255,0.03);
}
.wallet-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.wallet-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(201,169,110,0.2);
  color: #c9a96e;
  display: flex;
  align-items: center;
  justify-content: center;
}
.wallet-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.wallet-label {
  font-size: 12px;
  color: rgba(255,255,255,0.55);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.wallet-value {
  font-family: 'DM Sans', sans-serif;
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.5px;
}
.wallet-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.wallet-edit-btn {
  border-color: rgba(201,169,110,0.5) !important;
  color: #c9a96e !important;
  background: rgba(201,169,110,0.1) !important;
  font-weight: 600;
  border-radius: 8px;
}
.wallet-edit-btn:hover {
  background: rgba(201,169,110,0.2) !important;
  border-color: #c9a96e !important;
  color: #dbb87a !important;
}
.wallet-chevron {
  color: rgba(255,255,255,0.5);
  font-size: 16px;
  transition: transform 0.25s;
}
.wallet-chevron.expanded {
  transform: rotate(180deg);
}
.wallet-breakdown {
  padding: 0 24px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.wallet-account-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: rgba(255,255,255,0.05);
  border-radius: 8px;
}
.account-type-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  letter-spacing: 0.3px;
  flex-shrink: 0;
}
.tag-bank { background: #e8f0fe; color: #3b6b8c; }
.tag-alipay { background: #e6f0ff; color: #1677ff; }
.tag-wechat { background: #e6f9e8; color: #07c160; }
.tag-cash { background: #fff7e6; color: #d48806; }
.tag-investment { background: #f9e8ff; color: #722ed1; }
.tag-other { background: #f3f3f0; color: #6b6d7a; }
.account-name {
  flex: 1;
  font-size: 13px;
  color: rgba(255,255,255,0.8);
}
.account-amount {
  font-family: 'DM Sans', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #c9a96e;
}
.wallet-empty {
  padding: 0 24px 16px;
  font-size: 13px;
  color: rgba(255,255,255,0.4);
  text-align: center;
}

/* ── Wallet Dialog ── */
.wallet-dialog-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f8f5;
  border-radius: 10px;
  margin-bottom: 14px;
  font-size: 14px;
  color: #6b6d7a;
}
.wallet-dialog-total strong {
  font-family: 'DM Sans', sans-serif;
  font-size: 20px;
  color: #1a1f36;
}
.wallet-add-row {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  align-items: center;
}

/* ── Category Dialog ── */
.category-add-row {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  align-items: center;
}
.text-muted {
  color: #c0c2cc;
}

/* ── Budget Card ── */
.budget-card {
  margin-bottom: 20px;
}
.budget-empty {
  font-size: 13px;
  color: #b0b2bd;
  text-align: center;
  padding: 16px 0;
}
.budget-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.budget-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}
.budget-row:not(:last-child) {
  border-bottom: 0.5px solid #f3f3f0;
}
.budget-cat {
  width: 80px;
  font-size: 13px;
  font-weight: 600;
  color: #1a1f36;
  flex-shrink: 0;
}
.budget-spent {
  font-size: 12px;
  color: #6b6d7a;
  font-weight: 500;
  min-width: 80px;
  flex-shrink: 0;
}
.budget-bar-wrap {
  flex: 1;
  height: 8px;
  background: #f3f3f0;
  border-radius: 4px;
  overflow: hidden;
  min-width: 60px;
}
.budget-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
  min-width: 0;
}
.budget-limit {
  font-size: 12px;
  color: #8a8d9c;
  flex-shrink: 0;
}
.budget-pct {
  font-size: 13px;
  font-weight: 600;
  color: #0F766E;
  min-width: 40px;
  text-align: right;
  flex-shrink: 0;
}
.budget-pct.over { color: #DC2626; }

.budget-summary {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #eeedea;
  font-size: 13px;
  color: #6b6d7a;
  font-weight: 500;
}

/* ── Budget Dialog ── */
.budget-dialog-hint {
  font-size: 13px;
  color: #8a8d9c;
  margin: 0 0 14px;
}

/* ── Migrate Dialog ── */
.migrate-text {
  font-size: 14px;
  color: #4a4d5c;
  margin: 0 0 14px;
  line-height: 1.6;
}
</style>
