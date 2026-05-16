<template>
  <div class="task-detail-page">
    <!-- Header with breadcrumb -->
    <div class="detail-header">
      <div class="breadcrumb-row">
        <el-breadcrumb separator="›">
          <el-breadcrumb-item :to="{ path: '/tasks' }">
            <el-icon><List /></el-icon> 任务列表
          </el-breadcrumb-item>
          <el-breadcrumb-item>{{ task?.title || '加载中...' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="header-actions">
          <el-button text type="primary" @click="showEditDialog = true">
            <el-icon><Edit /></el-icon> 编辑
          </el-button>
          <el-button text type="danger" @click="handleDelete">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
        </div>
      </div>
    </div>

    <div v-if="task" class="detail-body">
      <!-- Hero: streak display -->
      <div class="streak-hero">
        <div class="streak-main">
          <div class="streak-current">
            <span class="streak-big-num">{{ task.streakDays }}</span>
            <span class="streak-big-label">天连续打卡</span>
          </div>
          <div class="streak-divider"></div>
          <div class="streak-max">
            <span class="streak-max-num">{{ task.maxStreakDays }}</span>
            <span class="streak-max-label">历史最长</span>
          </div>
        </div>
        <div class="streak-flame-icon" :class="{ burning: task.streakDays > 0 }">
          {{ task.streakDays > 0 ? '🔥' : '🕯️' }}
        </div>
      </div>

      <!-- Task Info Card -->
      <div class="info-card">
        <div class="info-left">
          <h1 class="task-name">{{ task.title }}</h1>
          <p class="task-desc-text" v-if="task.description">{{ task.description }}</p>
          <div class="task-meta">
            <el-tag :type="repeatTagType(task.repeatType)" effect="light" round size="small">
              {{ repeatLabel(task.repeatType) }}
            </el-tag>
            <span class="meta-date">
              <el-icon><Calendar /></el-icon>
              {{ task.startDate }} 起
              <template v-if="task.endDate"> ~ {{ task.endDate }}</template>
            </span>
            <span class="meta-date" v-if="task.remindTime">
              <el-icon><AlarmClock /></el-icon>
              {{ task.remindTime?.substring(0, 5) }}
            </span>
          </div>
        </div>
        <div class="info-right">
          <div class="status-switch">
            <span class="status-label">状态</span>
            <el-switch
              :model-value="task.status === 1"
              active-text="进行中"
              inactive-text="暂停"
              inline-prompt
              size="small"
              @change="toggleStatus"
            />
          </div>
        </div>
      </div>

      <!-- Checkin Calendar Placeholder -->
      <div class="calendar-section">
        <div class="section-header">
          <h3>打卡日历</h3>
          <el-button size="small" round @click="showCheckinDialog = true">
            <el-icon><CirclePlus /></el-icon> 打卡
          </el-button>
        </div>
        <div class="calendar-placeholder">
          <div class="calendar-grid">
            <div
              v-for="day in calendarDays"
              :key="day.date"
              class="cal-day"
              :class="day.level"
              :title="day.date + (day.checked ? ' ✓' : '')"
            >
              <span class="cal-day-num">{{ day.day }}</span>
            </div>
          </div>
          <div class="calendar-legend">
            <span>少</span>
            <span class="legend-dot level-0"></span>
            <span class="legend-dot level-1"></span>
            <span class="legend-dot level-2"></span>
            <span class="legend-dot level-3"></span>
            <span>多</span>
          </div>
        </div>
      </div>

      <!-- Checkin History Timeline -->
      <div class="timeline-section">
        <div class="section-header">
          <h3>打卡记录</h3>
        </div>
        <el-timeline v-if="records.length > 0">
          <el-timeline-item
            v-for="r in records"
            :key="r.id"
            :timestamp="r.checkinDate"
            placement="top"
            type="success"
            size="large"
            :icon="CircleCheck"
          >
            <div class="timeline-content">
              <span v-if="r.note">{{ r.note }}</span>
              <span v-else class="no-note">打卡</span>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="还没有打卡记录" :image-size="80" />
      </div>
    </div>

    <!-- Skeleton while loading -->
    <div v-else class="loading-skeleton">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- Edit Dialog -->
    <el-dialog v-model="showEditDialog" title="编辑任务" width="480px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" label-position="top">
        <el-form-item label="任务名称" prop="title">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="重复类型">
              <el-select v-model="editForm.repeatType" style="width: 100%">
                <el-option label="一次性" :value="0" />
                <el-option label="每日" :value="1" />
                <el-option label="每周" :value="2" />
                <el-option label="每月" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提醒时间">
              <el-time-picker v-model="editForm.remindTime" format="HH:mm" value-format="HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="editForm.startDate" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="editForm.endDate" type="date" style="width: 100%" placeholder="永久" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="updating">保存</el-button>
      </template>
    </el-dialog>

    <!-- Checkin Dialog -->
    <el-dialog v-model="showCheckinDialog" title="打卡" width="380px" destroy-on-close>
      <el-form :model="checkinForm" label-position="top">
        <el-form-item label="打卡日期">
          <el-date-picker
            v-model="checkinForm.date"
            type="date"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkinForm.note" placeholder="可选，记录一下今天的情况" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCheckinDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCheckin" :loading="checkingIn">确认打卡</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  List, Edit, Delete, Calendar, AlarmClock, CirclePlus, CircleCheck
} from '@element-plus/icons-vue'
import { taskApi } from '@/api/tasks'

const route = useRoute()
const router = useRouter()

const task = ref(null)
const records = ref([])
const showEditDialog = ref(false)
const showCheckinDialog = ref(false)
const updating = ref(false)
const checkingIn = ref(false)

const editForm = reactive({
  title: '', description: '', repeatType: 1, remindTime: null, startDate: '', endDate: null, status: 1
})

const checkinForm = reactive({ date: new Date(), note: '' })

const repeatLabel = (type) => ({ 0: '一次性', 1: '每日', 2: '每周', 3: '每月' })[type] || '未知'
const repeatTagType = (type) => ({ 0: 'info', 1: '', 2: 'warning', 3: 'danger' })[type] || 'info'

const disabledDate = (time) => {
  const d = new Date(time)
  const today = new Date()
  today.setHours(23, 59, 59, 999)
  const threeDaysAgo = new Date(today)
  threeDaysAgo.setDate(threeDaysAgo.getDate() - 3)
  threeDaysAgo.setHours(0, 0, 0, 0)
  return d.getTime() > today.getTime() || d.getTime() < threeDaysAgo.getTime()
}

// Build 84-day calendar grid for heatmap
const calendarDays = computed(() => {
  if (!task.value) return []
  const checkinDates = new Set(records.value.map(r => r.checkinDate))
  const days = []
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  for (let i = 83; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(d.getDate() - i)
    const dateStr = d.toISOString().substring(0, 10)
    const checked = checkinDates.has(dateStr)
    days.push({
      date: dateStr,
      day: d.getDate(),
      checked,
      level: checked ? 'level-3' : 'level-0'
    })
  }
  return days
})

const loadTask = async () => {
  const id = route.params.id
  const res = await taskApi.detail(id)
  task.value = res.data
  Object.assign(editForm, {
    title: task.value.title,
    description: task.value.description || '',
    repeatType: task.value.repeatType,
    remindTime: task.value.remindTime,
    startDate: task.value.startDate,
    endDate: task.value.endDate,
    status: task.value.status
  })
}

const loadRecords = async () => {
  const res = await taskApi.records(route.params.id)
  records.value = res.data
}

const toggleStatus = async (val) => {
  try {
    await taskApi.update(route.params.id, { status: val ? 1 : 0 })
    task.value.status = val ? 1 : 0
    ElMessage.success(val ? '任务已恢复' : '任务已暂停')
  } catch (e) {
    // handled
  }
}

const handleUpdate = async () => {
  updating.value = true
  try {
    await taskApi.update(route.params.id, editForm)
    ElMessage.success('任务已更新')
    showEditDialog.value = false
    await loadTask()
  } catch (e) {
    // handled
  } finally {
    updating.value = false
  }
}

const handleDelete = async () => {
  await ElMessageBox.confirm('确定要删除这个任务吗？', '确认删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await taskApi.delete(route.params.id)
  ElMessage.success('任务已删除')
  router.push('/tasks')
}

const handleCheckin = async () => {
  checkingIn.value = true
  try {
    const dateStr = checkinForm.date
      ? new Date(checkinForm.date).toISOString().substring(0, 10)
      : null
    await taskApi.checkin(route.params.id, { note: checkinForm.note, date: dateStr })
    ElMessage.success('打卡成功')
    showCheckinDialog.value = false
    checkinForm.note = ''
    checkinForm.date = new Date()
    await loadTask()
    await loadRecords()
  } catch (e) {
    // handled
  } finally {
    checkingIn.value = false
  }
}

onMounted(() => {
  loadTask()
  loadRecords()
})
</script>

<style scoped>
.task-detail-page {
  max-width: 780px;
  margin: 0 auto;
  padding: 28px 24px;
}

/* Breadcrumb Header */
.detail-header {
  margin-bottom: 24px;
}

.breadcrumb-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 4px;
}

/* Streak Hero */
.streak-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 18px;
  padding: 28px 32px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 6px 16px rgba(0,0,0,0.03);
}

.streak-main {
  display: flex;
  align-items: center;
  gap: 28px;
}

.streak-current {
  display: flex;
  flex-direction: column;
}

.streak-big-num {
  font-size: 52px;
  font-weight: 800;
  color: #f59e0b;
  line-height: 1;
  letter-spacing: -2px;
}

.streak-big-label {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 6px;
}

.streak-divider {
  width: 1px;
  height: 48px;
  background: #e2e8f0;
}

.streak-max {
  display: flex;
  flex-direction: column;
}

.streak-max-num {
  font-size: 28px;
  font-weight: 700;
  color: #94a3b8;
  line-height: 1;
}

.streak-max-label {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}

.streak-flame-icon {
  font-size: 56px;
  opacity: 0.4;
  transition: opacity 0.3s, transform 0.3s;
}

.streak-flame-icon.burning {
  opacity: 1;
  animation: flamePulse 2s ease-in-out infinite;
}

@keyframes flamePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

/* Info Card */
.info-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: #fff;
  border-radius: 14px;
  padding: 22px 28px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.03);
}

.task-name {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px;
}

.task-desc-text {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 12px;
  line-height: 1.5;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-date {
  font-size: 13px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-switch {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.status-label {
  font-size: 13px;
  color: #94a3b8;
}

/* Section Header */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #334155;
}

/* Calendar Section */
.calendar-section {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.03);
}

.calendar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(14, 1fr);
  gap: 3px;
  width: 100%;
}

.cal-day {
  aspect-ratio: 1;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 500;
  color: transparent;
  transition: all 0.15s;
  cursor: default;
}

.cal-day.level-0 {
  background: #f1f5f9;
}

.cal-day.level-1 {
  background: #bbf7d0;
}

.cal-day.level-2 {
  background: #4ade80;
  color: #fff;
}

.cal-day.level-3 {
  background: #16a34a;
  color: #fff;
}

.calendar-legend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #94a3b8;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-dot.level-0 { background: #f1f5f9; }
.legend-dot.level-1 { background: #bbf7d0; }
.legend-dot.level-2 { background: #4ade80; }
.legend-dot.level-3 { background: #16a34a; }

/* Timeline Section */
.timeline-section {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.03);
}

.timeline-content {
  font-size: 14px;
  color: #334155;
}

.no-note {
  color: #94a3b8;
  font-style: italic;
}

/* Skeleton */
.loading-skeleton {
  background: #fff;
  border-radius: 14px;
  padding: 28px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

@media (max-width: 640px) {
  .streak-hero {
    padding: 20px;
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .streak-main {
    gap: 16px;
  }

  .streak-big-num {
    font-size: 40px;
  }

  .info-card {
    flex-direction: column;
    gap: 16px;
  }

  .calendar-grid {
    grid-template-columns: repeat(7, 1fr);
  }
}
</style>
