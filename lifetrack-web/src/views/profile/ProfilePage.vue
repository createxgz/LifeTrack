<template>
  <div class="profile-page">
    <!-- 顶部用户信息卡片 -->
    <div class="profile-hero">
      <div class="hero-inner">
        <div class="avatar-ring">
          <div class="avatar">
            {{ user?.nickname?.charAt(0)?.toUpperCase() || '?' }}
          </div>
        </div>
        <div class="hero-info">
          <h1 class="hero-name">{{ user?.nickname || '用户' }}</h1>
          <p class="hero-email">{{ user?.email || '' }}</p>
          <div class="hero-meta">
            <span class="meta-tag">
              <el-icon><Calendar /></el-icon>
              注册于 {{ formatJoinDate }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab 区域 -->
    <div class="profile-body">
      <div class="tab-container">
        <div class="tab-nav">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            :class="['tab-btn', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >
            <el-icon><component :is="tab.icon" /></el-icon>
            {{ tab.label }}
          </button>
        </div>

        <!-- 个人信息 Tab -->
        <div v-show="activeTab === 'info'" class="tab-panel">
          <div class="panel-card">
            <div class="panel-header">
              <h2 class="panel-title">个人信息</h2>
              <p class="panel-desc">管理您的个人资料信息</p>
            </div>

            <el-form
              ref="infoFormRef"
              :model="infoForm"
              :rules="infoRules"
              label-position="top"
              class="profile-form"
            >
              <div class="form-grid">
                <el-form-item label="邮箱" class="form-item">
                  <div class="readonly-field">
                    <el-icon><Message /></el-icon>
                    <span>{{ infoForm.email }}</span>
                    <span class="readonly-badge">不可修改</span>
                  </div>
                </el-form-item>

                <el-form-item label="昵称" prop="nickname" class="form-item">
                  <el-input
                    v-model="infoForm.nickname"
                    placeholder="请输入昵称"
                    size="large"
                    :prefix-icon="User"
                  />
                </el-form-item>

                <el-form-item label="性别" prop="gender" class="form-item">
                  <el-select
                    v-model="infoForm.gender"
                    placeholder="请选择性别"
                    size="large"
                    style="width: 100%"
                  >
                    <el-option :value="1" label="男" />
                    <el-option :value="2" label="女" />
                    <el-option :value="0" label="保密" />
                  </el-select>
                </el-form-item>

                <el-form-item label="身高 (cm)" prop="height" class="form-item">
                  <el-input-number
                    v-model="infoForm.height"
                    :min="50"
                    :max="250"
                    :precision="1"
                    :step="0.5"
                    size="large"
                    style="width: 100%"
                    placeholder="170.0"
                  />
                </el-form-item>

                <el-form-item label="目标体重 (kg)" prop="targetWeight" class="form-item">
                  <el-input-number
                    v-model="infoForm.targetWeight"
                    :min="20"
                    :max="200"
                    :precision="1"
                    :step="0.5"
                    size="large"
                    style="width: 100%"
                    placeholder="65.0"
                  />
                </el-form-item>
              </div>

              <div class="form-actions">
                <el-button
                  type="primary"
                  size="large"
                  :loading="infoSaving"
                  @click="handleSaveInfo"
                  class="save-btn"
                >
                  <el-icon><Check /></el-icon>
                  保存修改
                </el-button>
              </div>
            </el-form>
          </div>
        </div>

        <!-- 修改密码 Tab -->
        <div v-show="activeTab === 'password'" class="tab-panel">
          <div class="panel-card">
            <div class="panel-header">
              <h2 class="panel-title">修改密码</h2>
              <p class="panel-desc">定期更换密码有助于保护账户安全</p>
            </div>

            <el-form
              ref="pwdFormRef"
              :model="pwdForm"
              :rules="pwdRules"
              label-position="top"
              class="profile-form"
            >
              <div class="form-grid form-grid-narrow">
                <el-form-item label="当前密码" prop="oldPassword" class="form-item">
                  <el-input
                    v-model="pwdForm.oldPassword"
                    type="password"
                    placeholder="请输入当前密码"
                    size="large"
                    show-password
                    :prefix-icon="Lock"
                  />
                </el-form-item>

                <el-form-item label="新密码" prop="newPassword" class="form-item">
                  <el-input
                    v-model="pwdForm.newPassword"
                    type="password"
                    placeholder="8-20位，含大小写字母和数字"
                    size="large"
                    show-password
                    :prefix-icon="Lock"
                  />
                </el-form-item>

                <el-form-item label="确认新密码" prop="confirmPassword" class="form-item">
                  <el-input
                    v-model="pwdForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    size="large"
                    show-password
                    :prefix-icon="Lock"
                  />
                </el-form-item>
              </div>

              <div class="form-actions">
                <el-button
                  type="primary"
                  size="large"
                  :loading="pwdSaving"
                  @click="handleChangePassword"
                  class="save-btn"
                >
                  <el-icon><Key /></el-icon>
                  修改密码
                </el-button>
              </div>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User, Lock, Check, Key, Message, Calendar,
  UserFilled, Setting
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth.js'
import { userApi } from '@/api/index.js'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const activeTab = ref('info')
const infoSaving = ref(false)
const pwdSaving = ref(false)
const infoFormRef = ref(null)
const pwdFormRef = ref(null)

const tabs = [
  { key: 'info', label: '个人信息', icon: UserFilled },
  { key: 'password', label: '修改密码', icon: Setting }
]

const formatJoinDate = computed(() => {
  if (!user.value?.createdAt) return '--'
  const d = new Date(user.value.createdAt)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
})

// 个人信息表单
const infoForm = reactive({
  email: '',
  nickname: '',
  gender: null,
  height: null,
  targetWeight: null
})

const infoRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度2-20个字符', trigger: 'blur' }
  ]
}

// 密码表单
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度8-20位', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/, message: '需含大小写字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

// 加载用户数据
async function loadProfile() {
  try {
    const data = await userApi.getProfile()
    authStore.setUserInfo(data.data)
    infoForm.email = data.email || ''
    infoForm.nickname = data.nickname || ''
    infoForm.gender = data.gender ?? null
    infoForm.height = data.height ?? null
    infoForm.targetWeight = data.targetWeight ?? null
  } catch {
    // handled by interceptor
  }
}

// 保存个人信息
async function handleSaveInfo() {
  const valid = await infoFormRef.value?.validate().catch(() => false)
  if (!valid) return
  infoSaving.value = true
  try {
    await userApi.updateProfile({
      nickname: infoForm.nickname,
      gender: infoForm.gender,
      height: infoForm.height,
      targetWeight: infoForm.targetWeight
    })
    // 刷新本地用户数据
    authStore.setUserInfo({
      ...authStore.user,
      nickname: infoForm.nickname,
      gender: infoForm.gender,
      height: infoForm.height,
      targetWeight: infoForm.targetWeight
    })
    ElMessage.success('个人信息已更新')
  } catch {
    // handled by interceptor
  } finally {
    infoSaving.value = false
  }
}

// 修改密码
async function handleChangePassword() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  pwdSaving.value = true
  try {
    await userApi.changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdFormRef.value?.resetFields()
  } catch {
    // handled by interceptor
  } finally {
    pwdSaving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  padding: 0;
  font-family: 'PingFang SC', 'Helvetica Neue', -apple-system, sans-serif;
}

/* ── 顶部用户卡片 ── */
.profile-hero {
  background: linear-gradient(135deg, #C2410C 0%, #9A3412 100%);
  border-radius: 16px;
  padding: 40px 48px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
}

.profile-hero::before {
  content: '';
  position: absolute;
  top: -60px;
  right: -40px;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 50%;
}

.profile-hero::after {
  content: '';
  position: absolute;
  bottom: -80px;
  left: 30%;
  width: 280px;
  height: 280px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 50%;
}

.hero-inner {
  display: flex;
  align-items: center;
  gap: 28px;
  position: relative;
  z-index: 1;
}

.avatar-ring {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.75rem;
  font-weight: 600;
  color: #FFF7ED;
  letter-spacing: -0.02em;
}

.hero-info {
  flex: 1;
}

.hero-name {
  font-size: 1.5rem;
  font-weight: 600;
  color: #FFF7ED;
  margin: 0 0 4px;
  letter-spacing: -0.01em;
}

.hero-email {
  font-size: 0.875rem;
  color: rgba(255, 247, 237, 0.7);
  margin: 0 0 12px;
}

.hero-meta {
  display: flex;
  gap: 16px;
}

.meta-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 0.8rem;
  color: rgba(255, 247, 237, 0.6);
  background: rgba(255, 255, 255, 0.08);
  padding: 4px 12px;
  border-radius: 20px;
}

/* ── Tab 区域 ── */
.profile-body {
  padding: 0;
}

.tab-container {
  background: #FFFDF9;
  border-radius: 14px;
  border: 1px solid rgba(194, 65, 12, 0.06);
  overflow: hidden;
}

.tab-nav {
  display: flex;
  border-bottom: 1px solid rgba(194, 65, 12, 0.06);
  padding: 0 8px;
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 16px 24px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #78716C;
  background: transparent;
  border: none;
  cursor: pointer;
  position: relative;
  transition: color 0.25s;
  font-family: inherit;
}

.tab-btn:hover {
  color: #C2410C;
}

.tab-btn.active {
  color: #C2410C;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 24px;
  right: 24px;
  height: 2px;
  background: #C2410C;
  border-radius: 1px 1px 0 0;
}

.tab-panel {
  padding: 32px 36px 36px;
}

/* ── 面板卡片 ── */
.panel-card {
  max-width: 680px;
}

.panel-header {
  margin-bottom: 28px;
}

.panel-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: #1C1917;
  margin: 0 0 4px;
}

.panel-desc {
  font-size: 0.8rem;
  color: #A8A29E;
  margin: 0;
}

/* ── 表单 ── */
.profile-form {
  width: 100%;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 28px;
}

.form-grid-narrow {
  max-width: 400px;
  grid-template-columns: 1fr;
}

.form-item {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  font-size: 0.8rem;
  font-weight: 500;
  color: #57534E;
  padding-bottom: 4px;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px rgba(194, 65, 12, 0.1);
  transition: box-shadow 0.25s;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(194, 65, 12, 0.25);
}

:deep(.el-input__wrapper:focus-within),
:deep(.el-select__wrapper:focus-within) {
  box-shadow: 0 0 0 2px rgba(194, 65, 12, 0.15);
}

:deep(.el-input-number) {
  width: 100%;
}

/* 只读邮箱字段 */
.readonly-field {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #FAFAF8;
  border: 1px solid rgba(194, 65, 12, 0.06);
  border-radius: 8px;
  font-size: 0.9rem;
  color: #57534E;
}

.readonly-field .el-icon {
  color: #A8A29E;
}

.readonly-badge {
  margin-left: auto;
  font-size: 0.7rem;
  color: #A8A29E;
  background: rgba(168, 162, 158, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}

/* ── 操作按钮 ── */
.form-actions {
  margin-top: 28px;
  padding-top: 24px;
  border-top: 1px solid rgba(194, 65, 12, 0.06);
}

.save-btn {
  background: #C2410C;
  border-color: #C2410C;
  border-radius: 8px;
  font-weight: 500;
  padding: 10px 28px;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.save-btn:hover {
  background: #B03A0A;
  border-color: #B03A0A;
  transform: translateY(-1px);
}

.save-btn:active {
  transform: scale(0.97);
}

/* ── 响应式 ── */
@media (max-width: 768px) {
  .profile-hero {
    padding: 28px 24px;
    border-radius: 12px;
  }

  .hero-inner {
    gap: 20px;
  }

  .avatar-ring {
    width: 60px;
    height: 60px;
  }

  .avatar {
    width: 48px;
    height: 48px;
    font-size: 1.25rem;
  }

  .hero-name {
    font-size: 1.25rem;
  }

  .tab-panel {
    padding: 24px 20px 28px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .tab-btn {
    padding: 14px 16px;
    font-size: 0.85rem;
  }
}
</style>
