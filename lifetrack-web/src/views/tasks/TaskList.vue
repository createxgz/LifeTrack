<template>
  <div class="task-list-page">
    <!-- Stats Row -->
    <div class="stats-row">
      <div class="stat-card stat-total">
        <div class="stat-icon">
          <el-icon :size="22"><List /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalTasks }}</span>
          <span class="stat-label">全部任务</span>
        </div>
      </div>
      <div class="stat-card stat-done">
        <div class="stat-icon">
          <el-icon :size="22"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.todayCheckins }}<small>/{{ stats.todayTotal }}</small></span>
          <span class="stat-label">今日打卡</span>
        </div>
      </div>
      <div class="stat-card stat-weekly">
        <div class="stat-icon">
          <el-icon :size="22"><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ (stats.weekRate * 100).toFixed(0) }}<small>%</small></span>
          <span class="stat-label">本周打卡率</span>
        </div>
      </div>
      <div class="stat-card stat-streak">
        <div class="stat-icon">
          <el-icon :size="22"><Timer /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ maxStreak }}<small>天</small></span>
          <span class="stat-label">最长连续</span>
        </div>
      </div>
    </div>

    <!-- Toolbar -->
    <div class="toolbar">
      <div class="filters">
        <el-select v-model="filterStatus" placeholder="全部状态" clearable size="default" @change="loadTasks">
          <el-option label="进行中" :value="1" />
          <el-option label="已暂停" :value="0" />
          <el-option label="已完成" :value="2" />
        </el-select>
        <el-select v-model="filterRepeat" placeholder="全部类型" clearable size="default" @change="loadTasks">
          <el-option label="每日" :value="1" />
          <el-option label="每周" :value="2" />
          <el-option label="每月" :value="3" />
          <el-option label="一次性" :value="0" />
        </el-select>
        <el-select v-model="filterParent" placeholder="全部层级" size="default" @change="loadTasks">
          <el-option label="全部任务" :value="null" />
          <el-option label="仅顶层任务" :value="0" />
        </el-select>
      </div>
      <el-button type="primary" @click="openCreateDialog(null)">
        <el-icon><Plus /></el-icon>
        新建任务
      </el-button>
    </div>

    <!-- Task Cards -->
    <div v-if="tasks.length > 0" class="task-grid">
      <div
        v-for="task in tasks"
        :key="task.id"
        class="task-card"
        :class="{ 'checked-today': task.checkedToday, 'is-subtask': task.parentTaskId }"
        @click="goDetail(task.id)"
      >
        <div class="task-card-top">
          <div class="task-title-row">
            <span v-if="task.parentTaskId" class="sub-indicator">
              <el-icon :size="14"><Share /></el-icon>
            </span>
            <h3 class="task-title">{{ task.title }}</h3>
            <el-tag
              size="small"
              :type="repeatTagType(task.repeatType)"
              effect="light"
              round
            >
              {{ repeatLabel(task.repeatType) }}
            </el-tag>
          </div>
          <p class="task-desc" v-if="task.description">{{ task.description }}</p>
        </div>

        <div class="task-card-bottom">
          <div class="task-streak" :class="{ active: task.streakDays > 0 }">
            <span class="streak-flame">{{ task.streakDays > 0 ? '🔥' : '⚡' }}</span>
            <span class="streak-num">{{ task.streakDays }}</span>
            <span class="streak-unit">天连续</span>
          </div>

          <div class="task-week-ring">
            <el-progress
              type="circle"
              :percentage="Math.round(task.weekRate * 100)"
              :width="52"
              :stroke-width="5"
              :color="task.checkedToday ? '#67c23a' : '#c0c4cc'"
            >
              <span class="ring-label">{{ Math.round(task.weekRate * 100) }}%</span>
            </el-progress>
          </div>

          <el-button
            v-if="!task.checkedToday"
            class="checkin-btn"
            type="success"
            size="small"
            round
            @click.stop="quickCheckin(task)"
            :loading="checkingIn === task.id"
          >
            打卡
          </el-button>
          <el-tag v-else type="success" effect="dark" round size="small">已打卡</el-tag>
        </div>

        <!-- Subtask count badge -->
        <div v-if="task.subtaskCount > 0" class="subtask-badge" @click.stop="goDetail(task.id)">
          <el-icon :size="12"><List /></el-icon>
          {{ task.subtaskCount }} 个子任务
        </div>

        <!-- Add subtask quick action -->
        <div v-if="!task.parentTaskId" class="add-subtask-btn" @click.stop="openCreateDialog(task.id)">
          <el-icon :size="14"><Plus /></el-icon>
        </div>

        <!-- Status Indicator Dot -->
        <div class="status-dot" :class="statusClass(task.status)"></div>
      </div>
    </div>

    <el-empty v-else description="还没有任务，开始创建吧" />

    <!-- Pagination -->
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="loadTasks"
      />
    </div>

    <!-- Create / Edit Task Dialog -->
    <el-dialog v-model="showCreateDialog" :title="isEditMode ? '编辑任务' : '新建任务'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="父任务" v-if="parentTasks.length > 0 || form.parentTaskId">
          <el-select v-model="form.parentTaskId" placeholder="无（顶层任务）" clearable style="width: 100%">
            <el-option
              v-for="pt in parentTasks"
              :key="pt.id"
              :label="pt.title"
              :value="pt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务名称" prop="title">
          <el-input v-model="form.title" placeholder="例如：每天阅读30分钟" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="可选描述" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="重复类型" prop="repeatType">
              <el-select v-model="form.repeatType" style="width: 100%">
                <el-option label="一次性" :value="0" />
                <el-option label="每日" :value="1" />
                <el-option label="每周" :value="2" />
                <el-option label="每月" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提醒时间" prop="remindTime">
              <el-time-picker
                v-model="form.remindTime"
                format="HH:mm"
                value-format="HH:mm:ss"
                placeholder="可选"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="form.endDate" type="date" style="width: 100%" placeholder="永久" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">
          {{ isEditMode ? '保存修改' : '创建任务' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { List, CircleCheck, TrendCharts, Timer, Plus, Share } from '@element-plus/icons-vue'
import { taskApi } from '@/api/tasks'

const router = useRouter()

const tasks = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 12
const filterStatus = ref(null)
const filterRepeat = ref(null)
const filterParent = ref(null)
const checkingIn = ref(null)
const saving = ref(false)
const showCreateDialog = ref(false)
const formRef = ref(null)
const editTaskId = ref(null)
const parentTasks = ref([])

const stats = reactive({
  totalTasks: 0,
  todayCheckins: 0,
  todayTotal: 0,
  weekRate: 0,
  monthRate: 0
})

const isEditMode = computed(() => editTaskId.value !== null)

const maxStreak = computed(() => {
  let max = 0
  tasks.value.forEach(t => { if (t.maxStreakDays > max) max = t.maxStreakDays })
  return max
})

const form = reactive({
  title: '',
  description: '',
  parentTaskId: null,
  repeatType: 1,
  remindTime: null,
  startDate: '',
  endDate: null
})

const rules = {
  title: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  repeatType: [{ required: true, message: '请选择重复类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }]
}

const repeatLabel = (type) => {
  const map = { 0: '一次性', 1: '每日', 2: '每周', 3: '每月' }
  return map[type] || '未知'
}

const repeatTagType = (type) => {
  const map = { 0: 'info', 1: '', 2: 'warning', 3: 'danger' }
  return map[type] || 'info'
}

const statusClass = (status) => {
  const map = { 0: 'paused', 1: 'active', 2: 'done' }
  return map[status] || 'paused'
}

const loadTasks = async () => {
  const res = await taskApi.list({
    status: filterStatus.value,
    repeatType: filterRepeat.value,
    parentTaskId: filterParent.value,
    page: currentPage.value,
    size: pageSize
  })
  tasks.value = res.data.records
  total.value = res.data.total
}

const loadStats = async () => {
  const res = await taskApi.stats()
  Object.assign(stats, res.data)
}

const loadParentTasks = async () => {
  // Fetch top-level tasks for parent selector (up to 50)
  const res = await taskApi.list({ parentTaskId: 0, page: 1, size: 50 })
  parentTasks.value = res.data.records || []
}

const openCreateDialog = (parentTaskId) => {
  editTaskId.value = null
  Object.assign(form, {
    title: '',
    description: '',
    parentTaskId: parentTaskId || null,
    repeatType: 1,
    remindTime: null,
    startDate: '',
    endDate: null
  })
  showCreateDialog.value = true
  loadParentTasks()
}

const openEditDialog = async (task) => {
  editTaskId.value = task.id
  loadParentTasks()
  Object.assign(form, {
    title: task.title,
    description: task.description || '',
    parentTaskId: task.parentTaskId || null,
    repeatType: task.repeatType,
    remindTime: task.remindTime,
    startDate: task.startDate,
    endDate: task.endDate
  })
  showCreateDialog.value = true
}

const quickCheckin = async (task) => {
  checkingIn.value = task.id
  try {
    await taskApi.checkin(task.id, { note: '', date: null })
    ElMessage.success('打卡成功')
    await loadTasks()
    await loadStats()
  } catch (e) {
    // error handled by interceptor
  } finally {
    checkingIn.value = null
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEditMode.value) {
      await taskApi.update(editTaskId.value, {
        title: form.title,
        description: form.description,
        parentTaskId: form.parentTaskId,
        repeatType: form.repeatType,
        remindTime: form.remindTime,
        startDate: form.startDate,
        endDate: form.endDate
      })
      ElMessage.success('任务已更新')
    } else {
      await taskApi.create({
        title: form.title,
        description: form.description,
        parentTaskId: form.parentTaskId,
        repeatType: form.repeatType,
        remindTime: form.remindTime,
        startDate: form.startDate,
        endDate: form.endDate
      })
      ElMessage.success('任务已创建')
    }
    showCreateDialog.value = false
    Object.assign(form, {
      title: '', description: '', parentTaskId: null, repeatType: 1, remindTime: null, startDate: '', endDate: null
    })
    editTaskId.value = null
    await loadTasks()
    await loadStats()
  } catch (e) {
    // error handled by interceptor
  } finally {
    saving.value = false
  }
}

const goDetail = (id) => {
  router.push(`/tasks/${id}`)
}

onMounted(() => {
  loadTasks()
  loadStats()
})
</script>

<style scoped>
.task-list-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 28px 24px;
}

/* Stats Row */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.03);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.06), 0 12px 24px rgba(0,0,0,0.04);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-total .stat-icon {
  background: #eef2ff;
  color: #6366f1;
}

.stat-done .stat-icon {
  background: #ecfdf5;
  color: #10b981;
}

.stat-weekly .stat-icon {
  background: #fef3c7;
  color: #f59e0b;
}

.stat-streak .stat-icon {
  background: #fce7f3;
  color: #ec4899;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.3;
  letter-spacing: -0.5px;
}

.stat-value small {
  font-size: 14px;
  font-weight: 500;
  color: #94a3b8;
}

.stat-label {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

/* Toolbar */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filters {
  display: flex;
  gap: 10px;
}

.filters .el-select {
  width: 140px;
}

/* Task Grid */
.task-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.task-card {
  position: relative;
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.03);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  overflow: hidden;
  border: 1.5px solid transparent;
}

.task-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.06), 0 16px 32px rgba(0,0,0,0.04);
  border-color: #e2e8f0;
}

.task-card.is-subtask {
  border-left: 3px solid #818cf8;
  padding-left: 17px;
}

.task-card.checked-today {
  background: linear-gradient(135deg, #fafffe 0%, #f0fdf4 100%);
}

.task-card-top {
  margin-bottom: 16px;
}

.task-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.sub-indicator {
  color: #818cf8;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.task-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-desc {
  font-size: 13px;
  color: #94a3b8;
  margin: 4px 0 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.task-streak {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.task-streak.active .streak-num {
  color: #f59e0b;
}

.streak-flame {
  font-size: 15px;
}

.streak-num {
  font-size: 18px;
  font-weight: 700;
  color: #cbd5e1;
}

.streak-unit {
  font-size: 11px;
  color: #94a3b8;
  margin-left: 2px;
}

.ring-label {
  font-size: 11px;
  color: #64748b;
  font-weight: 600;
}

.checkin-btn {
  font-weight: 600;
}

/* Subtask badge */
.subtask-badge {
  position: absolute;
  bottom: 10px;
  left: 20px;
  font-size: 11px;
  color: #818cf8;
  display: flex;
  align-items: center;
  gap: 3px;
  cursor: pointer;
  background: #eef2ff;
  padding: 2px 8px;
  border-radius: 10px;
  transition: background 0.2s;
}

.subtask-badge:hover {
  background: #e0e7ff;
}

/* Add subtask button */
.add-subtask-btn {
  position: absolute;
  bottom: 12px;
  right: 20px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #eef2ff;
  color: #818cf8;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s, background 0.2s;
}

.task-card:hover .add-subtask-btn {
  opacity: 1;
}

.add-subtask-btn:hover {
  background: #c7d2fe;
}

/* Status Dot */
.status-dot {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.active {
  background: #22c55e;
  box-shadow: 0 0 6px rgba(34,197,94,0.4);
}

.status-dot.paused {
  background: #f59e0b;
  box-shadow: 0 0 6px rgba(245,158,11,0.4);
}

.status-dot.done {
  background: #94a3b8;
}

/* Pagination */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-bottom: 24px;
}

/* Responsive */
@media (max-width: 960px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .task-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
  .task-grid {
    grid-template-columns: 1fr;
  }
  .toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
}
</style>
