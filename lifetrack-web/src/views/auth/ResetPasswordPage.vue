<template>
  <div class="reset-page">
    <div class="reset-card">
      <div class="card-header">
        <div class="logo-icon">
          <el-icon :size="20" color="#FFF7ED"><List /></el-icon>
        </div>
        <h1 class="title">重置密码</h1>
        <p class="subtitle">请设置您的新密码</p>
      </div>

      <el-form
        v-if="!resetSuccess"
        ref="formRef"
        :model="form"
        :rules="rules"
        @submit.prevent="handleSubmit"
        class="form"
      >
        <el-form-item prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="新密码（8-20位，含大小写字母和数字）"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认新密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          @click="handleSubmit"
          class="submit-btn"
        >
          重置密码
        </el-button>
      </el-form>

      <div v-else class="success-state">
        <div class="success-icon">
          <el-icon :size="48" color="#16A34A"><CircleCheck /></el-icon>
        </div>
        <h2 class="success-title">密码已重置</h2>
        <p class="success-desc">您的密码已成功重置，请使用新密码登录。</p>
        <el-button type="primary" @click="router.push('/auth/login')" class="login-btn">
          前往登录
        </el-button>
      </div>

      <div class="back-link">
        <router-link to="/auth/login">← 返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { List, Lock, CircleCheck } from '@element-plus/icons-vue'
import { authApi } from '@/api/index.js'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)
const resetSuccess = ref(false)

const token = route.query.token || ''
const email = route.query.email || ''

const form = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度8-20位', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/, message: '需含大小写字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

onMounted(() => {
  if (!token || !email) {
    ElMessage.error('重置链接无效，请重新申请')
    router.push('/auth/forgot-password')
  }
})

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authApi.resetPassword({ email, token, newPassword: form.newPassword })
    resetSuccess.value = true
    ElMessage.success('密码重置成功')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reset-page {
  min-height: 100vh;
  background: #FDF6EE;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  font-family: 'PingFang SC', 'Helvetica Neue', -apple-system, sans-serif;
}

.reset-card {
  background: #FFFDF9;
  border-radius: 16px;
  padding: 48px 40px 36px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #C2410C 0%, #B03A0A 100%);
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1C1917;
  margin: 0 0 8px;
}

.subtitle {
  font-size: 0.875rem;
  color: #78716C;
  margin: 0;
}

.form {
  margin-bottom: 16px;
}

.submit-btn {
  width: 100%;
  background: #C2410C;
  border-color: #C2410C;
  border-radius: 8px;
  font-weight: 500;
}
.submit-btn:hover {
  background: #B03A0A;
  border-color: #B03A0A;
}

.success-state {
  text-align: center;
  padding: 16px 0;
}

.success-icon {
  margin-bottom: 16px;
}

.success-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1C1917;
  margin: 0 0 12px;
}

.success-desc {
  font-size: 0.875rem;
  color: #57534E;
  margin: 0 0 24px;
  line-height: 1.6;
}

.login-btn {
  background: #C2410C;
  border-color: #C2410C;
  border-radius: 8px;
}
.login-btn:hover {
  background: #B03A0A;
  border-color: #B03A0A;
}

.back-link {
  text-align: center;
  margin-top: 24px;
}
.back-link a {
  color: #C2410C;
  text-decoration: none;
  font-size: 0.875rem;
}
.back-link a:hover {
  text-decoration: underline;
}
</style>
