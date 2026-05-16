<template>
  <div class="health-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">健康记录</h1>
        <p class="page-subtitle">追踪体重变化，记录每日饮食</p>
      </div>
    </div>

    <div class="tab-bar">
      <button
        :class="['tab-btn', { active: activeTab === 'weight' }]"
        @click="switchTab('weight')"
      >
        <el-icon :size="18"><Histogram /></el-icon>
        <span>体重记录</span>
      </button>
      <button
        :class="['tab-btn', { active: activeTab === 'diet' }]"
        @click="switchTab('diet')"
      >
        <el-icon :size="18"><KnifeFork /></el-icon>
        <span>饮食记录</span>
      </button>
    </div>

    <!-- ==================== 体重 Tab ==================== -->
    <div v-show="activeTab === 'weight'" class="tab-panel">
      <div class="chart-card">
        <div class="card-header">
          <h3>体重趋势</h3>
          <div class="trend-range">
            <button
              v-for="opt in trendOptions"
              :key="opt.value"
              :class="['range-btn', { active: trendDays === opt.value }]"
              @click="setTrendRange(opt.value)"
            >{{ opt.label }}</button>
          </div>
        </div>
        <div ref="weightChartRef" class="chart-box"></div>
      </div>

      <div class="data-card">
        <div class="card-header">
          <h3>体重记录</h3>
          <el-button type="primary" size="small" @click="openWeightDialog" class="add-btn">
            <el-icon :size="14"><Plus /></el-icon>
            新增记录
          </el-button>
        </div>
        <el-table :data="weightRecords" stripe style="width: 100%" v-loading="weightLoading" empty-text="暂无体重记录">
          <el-table-column prop="recordDate" label="日期" width="120" />
          <el-table-column label="时段" width="70">
            <template #default="{ row }">
              {{ timeSlotLabel(row.timeSlot) }}
            </template>
          </el-table-column>
          <el-table-column prop="weightKg" label="体重 (kg)" width="120">
            <template #default="{ row }">
              <span class="weight-value">{{ row.weightKg }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="bmi" label="BMI" width="80">
            <template #default="{ row }">
              {{ row.bmi || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="bodyFatRate" label="体脂率 (%)" width="120">
            <template #default="{ row }">
              {{ row.bodyFatRate || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="muscleMass" label="肌肉量 (kg)" width="120">
            <template #default="{ row }">
              {{ row.muscleMass || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="记录时间" min-width="160" />
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openWeightDialog(row)">
                <el-icon :size="16"><Edit /></el-icon>
              </el-button>
              <el-button type="danger" link size="small" @click="handleDeleteWeight(row.id)">
                <el-icon :size="16"><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap" v-if="weightTotal > weightPageSize">
          <el-pagination
            v-model:current-page="weightPage"
            :page-size="weightPageSize"
            :total="weightTotal"
            layout="prev, pager, next"
            @current-change="fetchWeights"
            small
          />
        </div>
      </div>
    </div>

    <!-- ==================== 饮食 Tab ==================== -->
    <div v-show="activeTab === 'diet'" class="tab-panel">
      <div class="chart-card">
        <div class="card-header">
          <h3>近7天热量摄入</h3>
          <el-date-picker
            v-model="dietChartDate"
            type="date"
            size="small"
            disabled
            style="width: 140px"
          />
        </div>
        <div ref="dietChartRef" class="chart-box"></div>
      </div>

      <div class="data-card">
        <div class="card-header">
          <h3>饮食记录</h3>
          <div class="card-header-right">
            <el-date-picker
              v-model="dietDate"
              type="date"
              size="small"
              placeholder="选择日期"
              @change="onDietDateChange"
              style="width: 150px; margin-right: 8px"
            />
            <el-button type="primary" size="small" @click="openDietDialog" class="add-btn">
              <el-icon :size="14"><Plus /></el-icon>
              新增记录
            </el-button>
          </div>
        </div>
        <el-table :data="dietRecords" stripe style="width: 100%" v-loading="dietLoading" empty-text="暂无饮食记录">
          <el-table-column prop="recordTime" label="时间" width="160" />
          <el-table-column label="餐类" width="80">
            <template #default="{ row }">
              <el-tag :type="mealTagType(row.mealType)" size="small" effect="light">
                {{ mealLabel(row.mealType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="foodName" label="食物" min-width="120">
            <template #default="{ row }">
              {{ row.foodName || '自定义' }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="calories" label="热量 (kcal)" width="110">
            <template #default="{ row }">
              <span class="calorie-value">{{ row.calories || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="protein" label="蛋白质 (g)" width="100">
            <template #default="{ row }">
              {{ row.protein || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="fat" label="脂肪 (g)" width="80">
            <template #default="{ row }">
              {{ row.fat || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="carbs" label="碳水 (g)" width="80">
            <template #default="{ row }">
              {{ row.carbs || '-' }}
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap" v-if="dietTotal > dietPageSize">
          <el-pagination
            v-model:current-page="dietPage"
            :page-size="dietPageSize"
            :total="dietTotal"
            layout="prev, pager, next"
            @current-change="fetchDiets"
            small
          />
        </div>
      </div>
    </div>

    <!-- ==================== 新增/编辑体重对话框 ==================== -->
    <el-dialog v-model="weightDialogVisible" :title="weightEditId ? '编辑体重记录' : '新增体重记录'" width="440px" destroy-on-close>
      <el-form ref="weightFormRef" :model="weightForm" :rules="weightRules" label-width="100px">
        <el-form-item label="体重 (kg)" prop="weightKg">
          <el-input-number v-model="weightForm.weightKg" :min="20" :max="300" :precision="1" controls-position="right" style="width: 100%" placeholder="请输入体重" />
        </el-form-item>
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker v-model="weightForm.recordDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="weightForm.timeSlot" placeholder="选择时段（选填）" clearable style="width: 100%">
            <el-option label="早" value="MORNING" />
            <el-option label="中" value="AFTERNOON" />
            <el-option label="晚" value="EVENING" />
          </el-select>
        </el-form-item>
        <el-form-item label="BMI">
          <el-input-number v-model="weightForm.bmi" :min="10" :max="60" :precision="1" controls-position="right" style="width: 100%" placeholder="自动计算或手动输入" />
        </el-form-item>
        <el-form-item label="体脂率 (%)">
          <el-input-number v-model="weightForm.bodyFatRate" :min="1" :max="60" :precision="1" controls-position="right" style="width: 100%" placeholder="选填" />
        </el-form-item>
        <el-form-item label="肌肉量 (kg)">
          <el-input-number v-model="weightForm.muscleMass" :min="10" :max="150" :precision="1" controls-position="right" style="width: 100%" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="weightDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitWeight" :loading="weightSubmitting">{{ weightEditId ? '保存修改' : '确认' }}</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 新增饮食对话框 ==================== -->
    <el-dialog v-model="dietDialogVisible" title="新增饮食记录" width="480px" destroy-on-close>
      <el-form ref="dietFormRef" :model="dietForm" :rules="dietRules" label-width="100px">
        <el-form-item label="餐类" prop="mealType">
          <el-select v-model="dietForm.mealType" placeholder="选择餐类" style="width: 100%">
            <el-option label="早餐" value="BREAKFAST" />
            <el-option label="午餐" value="LUNCH" />
            <el-option label="晚餐" value="DINNER" />
            <el-option label="零食" value="SNACK" />
          </el-select>
        </el-form-item>
        <el-form-item label="食物搜索">
          <el-select
            v-model="selectedFoodId"
            filterable
            remote
            :remote-method="searchFoods"
            :loading="foodSearchLoading"
            clearable
            placeholder="搜索食物（选填）"
            style="width: 100%"
            @change="onFoodSelect"
          >
            <el-option
              v-for="f in foodOptions"
              :key="f.id"
              :label="`${f.name} (${f.caloriesPerUnit}kcal/${f.unit})`"
              :value="f.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="dietForm.quantity" :min="0" :precision="1" controls-position="right" style="width: 100%" placeholder="食用数量" />
        </el-form-item>
        <el-form-item label="热量 (kcal)">
          <el-input-number v-model="dietForm.calories" :min="0" :precision="0" controls-position="right" style="width: 100%" placeholder="输入热量" />
        </el-form-item>
        <el-form-item label="蛋白质 (g)">
          <el-input-number v-model="dietForm.protein" :min="0" :precision="1" controls-position="right" style="width: 100%" placeholder="选填" />
        </el-form-item>
        <el-form-item label="脂肪 (g)">
          <el-input-number v-model="dietForm.fat" :min="0" :precision="1" controls-position="right" style="width: 100%" placeholder="选填" />
        </el-form-item>
        <el-form-item label="碳水 (g)">
          <el-input-number v-model="dietForm.carbs" :min="0" :precision="1" controls-position="right" style="width: 100%" placeholder="选填" />
        </el-form-item>
        <el-form-item label="时间" prop="recordTime">
          <el-date-picker v-model="dietForm.recordTime" type="datetime" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dietDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDiet" :loading="dietSubmitting">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Histogram, KnifeFork, Plus, Edit, Delete } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { healthApi } from '@/api/health'

// ==================== Tab ====================
const activeTab = ref('weight')
function switchTab(tab) {
  activeTab.value = tab
  nextTick(() => {
    if (tab === 'weight') initWeightChart()
    else initDietChart()
  })
}

// ==================== 体重趋势范围 ====================
const trendDays = ref(90)
const trendOptions = [
  { label: '近7天', value: 7 },
  { label: '近30天', value: 30 },
  { label: '近90天', value: 90 },
  { label: '近180天', value: 180 }
]
function setTrendRange(days) {
  trendDays.value = days
  fetchWeightTrend()
}

// ==================== 体重 ====================
const weightChartRef = ref(null)
let weightChart = null
const weightRecords = ref([])
const weightLoading = ref(false)
const weightTotal = ref(0)
const weightPage = ref(1)
const weightPageSize = ref(10)

async function fetchWeights() {
  weightLoading.value = true
  try {
    const res = await healthApi.getWeights({ page: weightPage.value, size: weightPageSize.value })
    weightRecords.value = res.data.records
    weightTotal.value = res.data.total
  } catch {} finally { weightLoading.value = false }
}

const weightDialogVisible = ref(false)
const weightEditId = ref(null)
const weightSubmitting = ref(false)
const weightFormRef = ref(null)
const weightForm = reactive({
  weightKg: null,
  bmi: null,
  bodyFatRate: null,
  muscleMass: null,
  recordDate: null,
  timeSlot: null
})
const weightRules = {
  weightKg: [{ required: true, message: '请输入体重', trigger: 'blur' }],
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

function openWeightDialog(row) {
  weightEditId.value = row?.id || null
  if (row) {
    weightForm.weightKg = row.weightKg
    weightForm.bmi = row.bmi
    weightForm.bodyFatRate = row.bodyFatRate
    weightForm.muscleMass = row.muscleMass
    weightForm.recordDate = row.recordDate
    weightForm.timeSlot = row.timeSlot
  } else {
    weightForm.weightKg = null
    weightForm.bmi = null
    weightForm.bodyFatRate = null
    weightForm.muscleMass = null
    weightForm.recordDate = null
    weightForm.timeSlot = null
  }
  weightDialogVisible.value = true
}

async function submitWeight() {
  const valid = await weightFormRef.value.validate().catch(() => false)
  if (!valid) return
  weightSubmitting.value = true
  try {
    if (weightEditId.value) {
      await healthApi.updateWeight(weightEditId.value, { ...weightForm })
      ElMessage.success('保存成功')
    } else {
      await healthApi.addWeight({ ...weightForm })
      ElMessage.success('添加成功')
    }
    weightDialogVisible.value = false
    weightPage.value = 1
    await fetchWeights()
    await fetchWeightTrend()
  } catch {} finally { weightSubmitting.value = false }
}

async function handleDeleteWeight(id) {
  try {
    await ElMessageBox.confirm('确定删除这条体重记录？', '确认删除', { type: 'warning' })
  } catch { return }
  try {
    await healthApi.deleteWeight(id)
    ElMessage.success('已删除')
    await fetchWeights()
    await fetchWeightTrend()
  } catch {}
}

async function fetchWeightTrend() {
  try {
    const res = await healthApi.getWeightTrend(trendDays.value)
    renderWeightChart(res.data)
  } catch {}
}

function renderWeightChart(data) {
  if (!weightChartRef.value) return
  if (!weightChart) {
    weightChart = echarts.init(weightChartRef.value)
  }
  const dates = data.map(d => d.date)
  const weights = data.map(d => d.weightKg)
  const hasData = weights.some(w => w != null)

  weightChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e2e8e6',
      textStyle: { color: '#2c3e2d', fontSize: 13 },
      formatter: (params) => {
        const v = params[0].value
        return `${params[0].axisValue}<br/>体重: <b>${v != null ? v + ' kg' : '无记录'}</b>`
      }
    },
    grid: { left: 12, right: 24, top: 16, bottom: 12 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#d4dcd6' } },
      axisTick: { show: false },
      axisLabel: {
        color: '#7a8a7c',
        fontSize: 10,
        formatter: (v) => v.slice(5)
      },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'value',
      name: 'kg',
      min: (val) => Math.floor((val.min - 2) / 5) * 5,
      max: (val) => Math.ceil((val.max + 2) / 5) * 5,
      axisLabel: { color: '#7a8a7c', fontSize: 11 },
      splitLine: { lineStyle: { color: '#eef1ee', type: 'dashed' } },
      nameTextStyle: { color: '#9aab9c', fontSize: 10 }
    },
    series: [{
      type: 'line',
      data: weights,
      smooth: true,
      symbol: 'circle',
      symbolSize: 3,
      lineStyle: { color: '#4a9c6d', width: 2 },
      itemStyle: { color: '#4a9c6d' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(74,156,109,0.25)' },
          { offset: 1, color: 'rgba(74,156,109,0.02)' }
        ])
      },
      connectNulls: true
    }]
  }, true)
}

// ==================== 饮食 ====================
const dietChartRef = ref(null)
let dietChart = null
const dietRecords = ref([])
const dietLoading = ref(false)
const dietTotal = ref(0)
const dietPage = ref(1)
const dietPageSize = ref(10)
const dietDate = ref(new Date())
const dietChartDate = ref(new Date())

const dietDialogVisible = ref(false)
const dietSubmitting = ref(false)
const dietFormRef = ref(null)
const dietForm = reactive({
  mealType: null,
  quantity: null,
  calories: null,
  protein: null,
  fat: null,
  carbs: null,
  recordTime: null
})
const dietRules = {
  mealType: [{ required: true, message: '请选择餐类', trigger: 'change' }]
}
const selectedFoodId = ref(null)
const foodOptions = ref([])
const foodSearchLoading = ref(false)

function timeSlotLabel(slot) {
  const map = { MORNING: '早', AFTERNOON: '中', EVENING: '晚' }
  return map[slot] || ''
}
function mealLabel(type) {
  const map = { BREAKFAST: '早餐', LUNCH: '午餐', DINNER: '晚餐', SNACK: '零食' }
  return map[type] || type
}
function mealTagType(type) {
  const map = { BREAKFAST: '', LUNCH: 'success', DINNER: 'warning', SNACK: 'info' }
  return map[type] || ''
}

async function fetchDiets() {
  dietLoading.value = true
  try {
    const dateStr = dietDate.value ? formatDate(dietDate.value) : null
    const res = await healthApi.getDiets({ date: dateStr, page: dietPage.value, size: dietPageSize.value })
    dietRecords.value = res.data.records
    dietTotal.value = res.data.total
  } catch {} finally { dietLoading.value = false }
}

function onDietDateChange() {
  dietPage.value = 1
  fetchDiets()
}

async function fetchDietChart() {
  try {
    const days = []
    for (let i = 6; i >= 0; i--) {
      const d = new Date()
      d.setDate(d.getDate() - i)
      days.push(formatDate(d))
    }
    const results = await Promise.all(days.map(d => healthApi.getDietStats(d)))
    const calories = results.map(r => r.data.totalCalories || 0)
    renderDietChart(days, calories)
  } catch {}
}

function renderDietChart(dates, calories) {
  if (!dietChartRef.value) return
  if (!dietChart) {
    dietChart = echarts.init(dietChartRef.value)
  }
  dietChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e2e8e6',
      textStyle: { color: '#2c3e2d', fontSize: 13 },
      formatter: (params) => `${params[0].axisValue}<br/>热量: <b>${params[0].value} kcal</b>`
    },
    grid: { left: 12, right: 24, top: 16, bottom: 12 },
    xAxis: {
      type: 'category',
      data: dates.map(d => d.slice(5)),
      axisLine: { lineStyle: { color: '#d4dcd6' } },
      axisTick: { show: false },
      axisLabel: { color: '#7a8a7c', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      name: 'kcal',
      axisLabel: { color: '#7a8a7c', fontSize: 11 },
      splitLine: { lineStyle: { color: '#eef1ee', type: 'dashed' } },
      nameTextStyle: { color: '#9aab9c', fontSize: 10 }
    },
    series: [{
      type: 'bar',
      data: calories,
      barWidth: 24,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#f0a85a' },
          { offset: 1, color: '#f0c27a' }
        ])
      },
      emphasis: {
        itemStyle: { color: '#e8963a' }
      }
    }]
  }, true)
}

async function searchFoods(query) {
  if (!query) { foodOptions.value = []; return }
  foodSearchLoading.value = true
  try {
    const res = await healthApi.searchFoods({ name: query })
    foodOptions.value = res.data || []
  } catch {} finally { foodSearchLoading.value = false }
}

function onFoodSelect(foodId) {
  if (!foodId) return
  const food = foodOptions.value.find(f => f.id === foodId)
  if (food) {
    dietForm.quantity = 1
    dietForm.calories = food.caloriesPerUnit
    dietForm.protein = food.protein
    dietForm.fat = food.fat
    dietForm.carbs = food.carbs
  }
}

function openDietDialog() {
  dietForm.mealType = null
  dietForm.quantity = null
  dietForm.calories = null
  dietForm.protein = null
  dietForm.fat = null
  dietForm.carbs = null
  dietForm.recordTime = null
  selectedFoodId.value = null
  foodOptions.value = []
  dietDialogVisible.value = true
}

async function submitDiet() {
  const valid = await dietFormRef.value.validate().catch(() => false)
  if (!valid) return
  dietSubmitting.value = true
  try {
    await healthApi.addDiet({
      ...dietForm,
      foodId: selectedFoodId.value,
      recordTime: dietForm.recordTime ? formatDateTime(dietForm.recordTime) : null
    })
    ElMessage.success('添加成功')
    dietDialogVisible.value = false
    dietPage.value = 1
    await fetchDiets()
    await fetchDietChart()
  } catch {} finally { dietSubmitting.value = false }
}

// ==================== Utils ====================
function formatDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function formatDateTime(d) {
  const date = new Date(d)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}:${s}`
}

// ==================== Lifecycle ====================
let resizeHandler

onMounted(async () => {
  resizeHandler = () => {
    weightChart?.resize()
    dietChart?.resize()
  }
  window.addEventListener('resize', resizeHandler)

  await fetchWeights()
  await fetchWeightTrend()
  await fetchDiets()
  await fetchDietChart()
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeHandler)
  weightChart?.dispose()
  dietChart?.dispose()
})
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@600;700&family=Nunito:wght@400;500;600;700&display=swap');
</style>

<style scoped>
.health-page {
  padding: 28px 32px;
  min-height: 100vh;
  background: #f9faf7;
  font-family: 'Nunito', sans-serif;
}

/* Header */
.page-header {
  margin-bottom: 24px;
}
.page-title {
  font-family: 'Playfair Display', serif;
  font-size: 28px;
  font-weight: 700;
  color: #2c3e2d;
  margin: 0 0 4px;
  letter-spacing: -0.3px;
}
.page-subtitle {
  margin: 0;
  color: #8a9e8c;
  font-size: 14px;
  font-weight: 500;
}

/* Tab Bar */
.tab-bar {
  display: flex;
  gap: 4px;
  background: #eef1ea;
  padding: 4px;
  border-radius: 14px;
  width: fit-content;
  margin-bottom: 24px;
}
.tab-btn {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 9px 22px;
  border: none;
  background: transparent;
  border-radius: 11px;
  font-size: 14px;
  font-weight: 600;
  color: #7a8a7c;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  font-family: 'Nunito', sans-serif;
}
.tab-btn:hover {
  color: #4a7c5c;
}
.tab-btn.active {
  background: #fff;
  color: #3d6b4f;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
}

/* Tab Panel */
.tab-panel {
  animation: fadeSlideIn 0.35s ease;
}
@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Chart Card */
.chart-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 2px rgba(44,62,45,0.04);
  border: 1px solid #eef1ea;
}
.chart-box {
  width: 100%;
  height: 300px;
}

/* Data Card */
.data-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 1px 2px rgba(44,62,45,0.04);
  border: 1px solid #eef1ea;
}

/* Card Header */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.card-header h3 {
  font-family: 'Playfair Display', serif;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e2d;
  margin: 0;
}
.card-badge {
  font-size: 12px;
  color: #8a9e8c;
  background: #f3f6f1;
  padding: 3px 10px;
  border-radius: 20px;
  font-weight: 600;
}
.trend-range {
  display: flex;
  gap: 2px;
  background: #eef1ea;
  padding: 3px;
  border-radius: 10px;
}
.range-btn {
  border: none;
  background: transparent;
  padding: 4px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #7a8a7c;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Nunito', sans-serif;
}
.range-btn:hover {
  color: #4a7c5c;
}
.range-btn.active {
  background: #fff;
  color: #3d6b4f;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.card-header-right {
  display: flex;
  align-items: center;
}

.add-btn {
  background: #4a9c6d !important;
  border-color: #4a9c6d !important;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.2s;
}
.add-btn:hover {
  background: #3d845a !important;
  border-color: #3d845a !important;
}

/* Table tweaks */
.weight-value {
  font-weight: 700;
  color: #4a9c6d;
  font-size: 15px;
}
.calorie-value {
  font-weight: 700;
  color: #e8963a;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

:deep(.el-table) {
  --el-table-header-bg-color: #f7f9f5;
  --el-table-tr-bg-color: #fff;
  --el-table-row-hover-bg-color: #f8faf6;
  border-radius: 10px;
  font-family: 'Nunito', sans-serif;
}
:deep(.el-table th.el-table__cell) {
  color: #5a6b5c;
  font-weight: 700;
  font-size: 13px;
  border-bottom: 2px solid #eef1ea;
}
:deep(.el-table td.el-table__cell) {
  color: #4a5b4c;
  font-size: 13px;
}
:deep(.el-pagination) {
  --el-color-primary: #4a9c6d;
}
:deep(.el-button--primary) {
  --el-button-bg-color: #4a9c6d;
  --el-button-border-color: #4a9c6d;
  --el-button-hover-bg-color: #3d845a;
  --el-button-hover-border-color: #3d845a;
}
:deep(.el-dialog) {
  border-radius: 16px;
}
:deep(.el-input-number) {
  width: 100%;
}
</style>
