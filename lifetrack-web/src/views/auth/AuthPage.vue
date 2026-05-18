<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <!-- 装饰元素 -->
      <div class="deco-ring ring-top" />
      <div class="deco-ring ring-mid" />
      <div class="deco-ring ring-bottom" />
      <div class="grain-overlay" />

      <div class="brand-content">
        <!-- Logo -->
        <div class="logo">
          <div class="logo-icon">
            <el-icon :size="17" color="#FFF7ED"><List /></el-icon>
          </div>
          <span class="logo-name">LifeTrack</span>
        </div>

        <!-- 标语 -->
        <h1 class="headline">
          <span class="headline-line">记录每一天，</span>
          <span class="headline-line">成为更好的自己</span>
        </h1>
        <p class="subline">
          <span class="subline-pill">任务打卡</span>
          <span class="subline-dot">·</span>
          <span class="subline-pill">健康记录</span>
          <span class="subline-dot">·</span>
          <span class="subline-pill">账本管理</span>
        </p>
        <p class="subline-tail">一个专属于你的生活管理空间</p>

        <!-- 特性列表 -->
        <div class="features">
          <div
            class="feature-item"
            v-for="(feat, i) in features"
            :key="feat.title"
            :style="{ animationDelay: `${0.6 + i * 0.12}s` }"
          >
            <div class="feat-icon" :style="{ background: feat.bg }">
              <el-icon :size="14" :color="feat.color"><component :is="feat.icon" /></el-icon>
            </div>
            <div class="feat-text">
              <div class="feat-title">{{ feat.title }}</div>
              <div class="feat-desc">{{ feat.desc }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="brand-footer">
        <span class="footer-dot" />
        数据存储在你自己的服务器
        <span class="footer-dot" />
        完全掌控
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-panel">
      <div class="form-inner">
        <!-- Tab 切换 -->
        <div class="tab-row">
          <span
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-item"
            :class="{ active: activeTab === tab.key }"
            @click="switchTab(tab.key)"
          >
            {{ tab.label }}
            <span v-if="activeTab === tab.key" class="tab-underline" />
          </span>
        </div>

        <!-- 表单切换动画 -->
        <Transition name="form-fade" mode="out-in">
          <!-- 登录表单 -->
          <el-form
            v-if="activeTab === 'login'"
            key="login"
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="email">
              <label class="field-label">邮箱</label>
              <div class="input-wrap">
                <el-icon class="input-icon"><Message /></el-icon>
                <el-input
                  v-model="loginForm.email"
                  placeholder="your@email.com"
                  type="email"
                  class="custom-input"
                />
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <label class="field-label">密码</label>
              <div class="input-wrap">
                <el-icon class="input-icon"><Lock /></el-icon>
                <el-input
                  v-model="loginForm.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="输入密码"
                  class="custom-input"
                >
                  <template #suffix>
                    <el-icon class="eye-icon" @click="showPassword = !showPassword">
                      <View v-if="!showPassword" />
                      <Hide v-else />
                    </el-icon>
                  </template>
                </el-input>
              </div>
            </el-form-item>

            <div class="form-row">
              <el-checkbox v-model="loginForm.remember" class="remember-check">
                记住我
              </el-checkbox>
              <a class="forgot-link" @click="handleForgotPassword">忘记密码？</a>
            </div>

            <button
              class="submit-btn"
              :class="{ 'is-loading': loading }"
              :disabled="loading"
              @click="handleLogin"
              type="submit"
            >
              <span class="submit-btn-text">登录 LifeTrack</span>
              <span class="submit-btn-shimmer" />
            </button>
          </el-form>

          <!-- 注册表单 -->
          <el-form
            v-else
            key="register"
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            @submit.prevent="handleRegister"
          >
            <el-form-item prop="nickname">
              <label class="field-label">昵称</label>
              <div class="input-wrap">
                <el-icon class="input-icon"><User /></el-icon>
                <el-input
                  v-model="registerForm.nickname"
                  placeholder="怎么称呼你？"
                  class="custom-input"
                />
              </div>
            </el-form-item>

            <el-form-item prop="email">
              <label class="field-label">邮箱</label>
              <div class="input-wrap">
                <el-icon class="input-icon"><Message /></el-icon>
                <el-input
                  v-model="registerForm.email"
                  placeholder="your@email.com"
                  type="email"
                  class="custom-input"
                />
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <label class="field-label">密码</label>
              <div class="input-wrap">
                <el-icon class="input-icon"><Lock /></el-icon>
                <el-input
                  v-model="registerForm.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="8-20位，含大小写字母和数字"
                  class="custom-input"
                >
                  <template #suffix>
                    <el-icon class="eye-icon" @click="showPassword = !showPassword">
                      <View v-if="!showPassword" />
                      <Hide v-else />
                    </el-icon>
                  </template>
                </el-input>
              </div>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <label class="field-label">确认密码</label>
              <div class="input-wrap">
                <el-icon class="input-icon"><Lock /></el-icon>
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="再输一次"
                  class="custom-input"
                />
              </div>
            </el-form-item>

            <button
              class="submit-btn"
              :class="{ 'is-loading': loading }"
              :disabled="loading"
              @click="handleRegister"
              type="submit"
            >
              <span class="submit-btn-text">注册并开始使用</span>
              <span class="submit-btn-shimmer" />
            </button>
          </el-form>
        </Transition>

        <!-- 分割线 -->
        <div class="divider">
          <span class="divider-line" />
          <span class="divider-text">或</span>
          <span class="divider-line" />
        </div>

        <!-- 第三方登录 -->
        <div class="social-row">
          <button class="social-btn" @click="handleWechatLogin">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="#07C160" aria-hidden="true">
              <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.295.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.601-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.927a.272.272 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-7.062-6.122zm-3.318 2.554c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982zm4.540 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982z"/>
            </svg>
            微信登录
          </button>
          <button class="social-btn" @click="handleMiniProgram">
            <el-icon :size="15" color="#C2410C"><Iphone /></el-icon>
            小程序扫码
          </button>
        </div>

        <div class="switch-tip">
          <template v-if="activeTab === 'login'">
            还没有账号？
            <span class="switch-link" @click="switchTab('register')">立即注册</span>
            <span class="switch-dot">·</span>
            免费使用
          </template>
          <template v-else>
            已有账号？
            <span class="switch-link" @click="switchTab('login')">直接登录</span>
          </template>
        </div>

        <div class="security-tip">
          <el-icon :size="12"><CircleCheck /></el-icon>
          数据加密存储，仅你可见
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  List, Message, Lock, View, Hide, User, Iphone, CircleCheck
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const activeTab = ref(route.path === '/auth/register' ? 'register' : 'login')
const loading = ref(false)
const showPassword = ref(false)

// sync tab with route
watch(() => route.path, (path) => {
  activeTab.value = path === '/auth/register' ? 'register' : 'login'
})

const tabs = [
  { key: 'login', label: '登录' },
  { key: 'register', label: '注册账号' },
]

const features = [
  {
    icon: 'Stopwatch',
    title: '任务打卡提醒',
    desc: '邮件准时送达，不再遗忘计划',
    bg: '#FEF3C7',
    color: '#C2410C',
  },
  {
    icon: 'TrendCharts',
    title: '健康数据追踪',
    desc: '体重 · 热量 · 趋势图一目了然',
    bg: '#ECFDF5',
    color: '#0F766E',
  },
  {
    icon: 'Wallet',
    title: '账本收支管理',
    desc: '分类预算，知道钱去哪了',
    bg: '#EFF6FF',
    color: '#0369A1',
  },
]

function switchTab(key) {
  activeTab.value = key
  router.replace(key === 'register' ? '/auth/register' : '/auth/login')
}

// 登录表单
const loginFormRef = ref()
const loginForm = reactive({ email: '', password: '', remember: false })
const loginRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少8位', trigger: 'blur' },
  ],
}

// 注册表单
const registerFormRef = ref()
const registerForm = reactive({ nickname: '', email: '', password: '', confirmPassword: '' })
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}
const registerRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称 2-20 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码 8-20 位', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/,
      message: '需包含大小写字母和数字',
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

async function handleLogin() {
  await loginFormRef.value?.validate()
  loading.value = true
  try {
    const data = await authStore.login(loginForm.email, loginForm.password)
    authStore.setToken(data.token, loginForm.remember)
    await authStore.fetchProfile()
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    // 错误由 axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  await registerFormRef.value?.validate()
  loading.value = true
  try {
    await authStore.register(registerForm.email, registerForm.password, registerForm.nickname)
    ElMessage.success('注册成功，请登录')
    loginForm.email = registerForm.email
    switchTab('login')
  } catch {
    // 错误由 axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}

function handleWechatLogin() {
  ElMessage.info('微信登录功能开发中')
}

function handleMiniProgram() {
  ElMessage.info('小程序扫码功能开发中')
}

function handleForgotPassword() {
  router.push('/auth/forgot-password')
}
</script>

<style scoped>
/* ============================================
   排版变量 — 1.25 Modular Scale
   ============================================ */
:root {
  --text-xs: 0.7rem;     /* 11.2px */
  --text-sm: 0.8125rem;  /* 13px */
  --text-base: 0.9375rem;/* 15px */
  --text-md: 1.0625rem;  /* 17px */
  --text-lg: 1.25rem;    /* 20px */
  --text-xl: 1.5rem;     /* 24px */
  --text-2xl: 1.875rem;  /* 30px */
  --leading-tight: 1.35;
  --leading-normal: 1.6;
  --leading-relaxed: 1.75;
}

/* ── 页面根容器 ── */
.login-page {
  display: grid;
  grid-template-columns: 1fr 1fr;
  width: 100vw;
  min-height: 100vh;
  margin-left: calc(50% - 50vw);
  font-family: 'PingFang SC', 'Helvetica Neue', -apple-system, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* ── 左侧品牌区 ── */
.brand-panel {
  background: #FDF6EE;
  padding: 60px 56px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
  isolation: isolate;
}

/* 纸纹纹理叠加 */
.grain-overlay {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  opacity: 0.35;
  background-image:
    radial-gradient(circle at 15% 25%, rgba(210, 160, 130, 0.12) 0%, transparent 50%),
    radial-gradient(circle at 75% 60%, rgba(194, 120, 80, 0.08) 0%, transparent 40%),
    radial-gradient(circle at 40% 85%, rgba(230, 190, 150, 0.10) 0%, transparent 50%);
}

/* 装饰圆环 */
.deco-ring {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.ring-top {
  width: 320px;
  height: 320px;
  top: -160px;
  right: -130px;
  border: 1px solid rgba(251, 191, 116, 0.18);
}
.ring-mid {
  width: 120px;
  height: 120px;
  top: 38%;
  right: -80px;
  border: 0.75px solid rgba(251, 191, 116, 0.12);
}
.ring-bottom {
  width: 240px;
  height: 240px;
  bottom: -120px;
  left: -120px;
  border: 1px solid rgba(251, 191, 116, 0.10);
}

.brand-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 11px;
  margin-bottom: 48px;
}
.logo-icon {
  width: 38px;
  height: 38px;
  background: linear-gradient(135deg, #C2410C 0%, #B03A0A 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(194, 65, 12, 0.18);
}
.logo-name {
  font-size: var(--text-md);
  font-weight: 650;
  color: #1C1917;
  letter-spacing: -0.025em;
}

/* 主标题 */
.headline {
  margin: 0 0 18px;
}
.headline-line {
  display: block;
  font-size: var(--text-2xl);
  font-weight: 600;
  color: #1C1917;
  line-height: 1.45;
  letter-spacing: -0.025em;
}

/* 副标题 */
.subline {
  font-size: var(--text-sm);
  color: #78716C;
  line-height: var(--leading-relaxed);
  margin: 0 0 10px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 2px;
}
.subline-pill {
  display: inline-block;
  padding: 2px 8px;
  background: rgba(194, 65, 12, 0.06);
  border-radius: 4px;
  font-size: var(--text-xs);
  color: #92400E;
  font-weight: 500;
}
.subline-dot {
  color: #D6D3D1;
  margin: 0 2px;
}
.subline-tail {
  font-size: var(--text-xs);
  color: #A8A29E;
  margin: 0;
  line-height: var(--leading-relaxed);
}

/* 特性列表 */
.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 36px;
}
.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 13px;
  padding: 4px 10px 4px 0;
  border-radius: 8px;
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  animation: featSlideIn 0.6s cubic-bezier(0.22, 0.61, 0.36, 1) both;
}
.feature-item:hover {
  transform: translateX(4px);
}
.feat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 1px;
  transition: transform 0.2s ease;
}
.feature-item:hover .feat-icon {
  transform: scale(1.08);
}
.feat-title {
  font-size: var(--text-sm);
  font-weight: 550;
  color: #1C1917;
  letter-spacing: -0.01em;
}
.feat-desc {
  font-size: var(--text-xs);
  color: #78716C;
  margin-top: 2px;
  line-height: 1.4;
}

@keyframes featSlideIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 底部信息 */
.brand-footer {
  font-size: var(--text-xs);
  color: #A8A29E;
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  letter-spacing: 0.01em;
}
.footer-dot {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #D6D3D1;
}

/* ── 右侧表单区 ── */
.form-panel {
  background: #FFFDF9;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 56px 48px;
}
.form-inner {
  width: 100%;
  max-width: 380px;
}

/* Tab 切换 */
.tab-row {
  display: flex;
  border-bottom: 1px solid #F0EDE8;
  margin-bottom: 28px;
  gap: 4px;
}
.tab-item {
  font-size: var(--text-base);
  padding: 0 4px 14px;
  margin-right: 24px;
  cursor: pointer;
  color: #A8A29E;
  position: relative;
  transition: color 0.3s ease;
  user-select: none;
  font-weight: 450;
}
.tab-item.active {
  color: #C2410C;
  font-weight: 550;
}
.tab-underline {
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: #C2410C;
  border-radius: 1px;
  animation: tabSlideIn 0.35s cubic-bezier(0.22, 0.61, 0.36, 1);
}
@keyframes tabSlideIn {
  from {
    transform: scaleX(0.4);
    opacity: 0;
  }
  to {
    transform: scaleX(1);
    opacity: 1;
  }
}

/* 表单切换动画 */
.form-fade-enter-active,
.form-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.form-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.form-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* 表单字段 */
.field-label {
  display: block;
  font-size: var(--text-xs);
  color: #78716C;
  margin-bottom: 7px;
  font-weight: 500;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}
.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}
.input-icon {
  position: absolute;
  left: 13px;
  color: #B8B0A8;
  font-size: 16px;
  z-index: 1;
  pointer-events: none;
  transition: color 0.25s ease, transform 0.25s ease;
}
.custom-input:focus-within ~ .input-icon,
.input-wrap:has(.custom-input:focus-within) .input-icon {
  color: #C2410C;
  transform: scale(1.1);
}
.eye-icon {
  cursor: pointer;
  color: #B8B0A8;
  transition: color 0.2s, transform 0.2s;
}
.eye-icon:hover {
  color: #78716C;
  transform: scale(1.1);
}

/* Element Plus 输入框覆写 */
.custom-input :deep(.el-input__wrapper) {
  padding-left: 38px;
  border-radius: 10px;
  border: 1px solid #E8E2D8;
  background: #FFFDF9;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
  transition: border-color 0.25s ease, box-shadow 0.25s ease, background 0.25s ease;
  height: 44px;
}
.custom-input :deep(.el-input__wrapper):hover {
  border-color: #D4C4B0;
  background: #FFFBF7;
}
.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #C2410C;
  background: #FFFBF7;
  box-shadow:
    0 0 0 4px rgba(194, 65, 12, 0.06),
    0 2px 8px rgba(194, 65, 12, 0.04);
}
.custom-input :deep(.el-input__inner) {
  font-size: var(--text-sm);
  color: #1C1917;
}
.custom-input :deep(.el-input__inner::placeholder) {
  color: #C4BFB8;
}

:deep(.el-form-item) { margin-bottom: 15px; }
:deep(.el-form-item__error) {
  font-size: var(--text-xs);
  padding-top: 4px;
  color: #DC2626;
}

/* 记住我 & 忘记密码 */
.form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.remember-check :deep(.el-checkbox__label) {
  font-size: var(--text-xs);
  color: #78716C;
}
.remember-check :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: #FEF3C7;
  border-color: #F59E0B;
}
.remember-check :deep(.el-checkbox__inner::after) {
  border-color: #92400E;
}
.forgot-link {
  font-size: var(--text-xs);
  color: #C2410C;
  text-decoration: none;
  cursor: pointer;
  font-weight: 500;
  transition: opacity 0.2s;
}
.forgot-link:hover { opacity: 0.7; }

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 46px;
  background: #C2410C;
  border: none;
  border-radius: 11px;
  color: #FFF7ED;
  font-size: var(--text-base);
  font-weight: 550;
  cursor: pointer;
  letter-spacing: 0.015em;
  font-family: inherit;
  position: relative;
  overflow: hidden;
  transition: background 0.25s ease, transform 0.15s ease, box-shadow 0.25s ease;
  box-shadow: 0 2px 4px rgba(194, 65, 12, 0.12), 0 4px 12px rgba(194, 65, 12, 0.08);
}
.submit-btn:hover {
  background: #B03A0A;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(194, 65, 12, 0.16), 0 6px 16px rgba(194, 65, 12, 0.10);
}
.submit-btn:active {
  transform: translateY(0) scale(0.985);
  box-shadow: 0 1px 2px rgba(194, 65, 12, 0.10);
}
.submit-btn:disabled {
  background: #C2410C;
  cursor: wait;
  opacity: 0.85;
}
.submit-btn-shimmer {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 247, 237, 0.15) 50%,
    transparent 100%
  );
  transition: left 0.6s ease;
}
.submit-btn:hover .submit-btn-shimmer {
  left: 100%;
}
.submit-btn-text {
  position: relative;
  z-index: 1;
}

/* 分割线 */
.divider {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 20px 0;
}
.divider-line {
  flex: 1;
  height: 0.75px;
  background: #EBE6DE;
}
.divider-text {
  font-size: var(--text-xs);
  color: #B8B0A8;
  font-weight: 500;
}

/* 第三方登录 */
.social-row {
  display: flex;
  gap: 10px;
}
.social-btn {
  flex: 1;
  height: 40px;
  border: 1px solid #E8E2D8;
  border-radius: 10px;
  background: #FFFDF9;
  font-size: var(--text-xs);
  color: #44403C;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-family: inherit;
  transition: border-color 0.2s, background 0.2s, transform 0.15s;
}
.social-btn:hover {
  border-color: #D4C4B0;
  background: #FFFBF7;
  transform: translateY(-1px);
}
.social-btn:active {
  transform: translateY(0);
}

/* 底部提示 */
.switch-tip {
  text-align: center;
  font-size: var(--text-xs);
  color: #78716C;
  margin-top: 22px;
  line-height: 1.6;
}
.switch-link {
  color: #C2410C;
  font-weight: 550;
  cursor: pointer;
  transition: opacity 0.2s;
}
.switch-link:hover { opacity: 0.75; }
.switch-dot {
  color: #D6D3D1;
  margin: 0 3px;
}

.security-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  font-size: var(--text-xs);
  color: #B8B0A8;
  margin-top: 12px;
}

/* ── 响应式 ── */
@media (max-width: 768px) {
  .login-page { grid-template-columns: 1fr; }
  .brand-panel { display: none; }
  .form-panel { padding: 40px 28px; }
  .form-inner { max-width: 100%; }
}
</style>
