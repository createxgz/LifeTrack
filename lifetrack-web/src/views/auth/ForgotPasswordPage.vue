<template>
  <div class="forgot-page">
    <div class="forgot-card">
      <div class="card-header">
        <div class="logo-icon">
          <el-icon :size="20" color="#FFF7ED"><List /></el-icon>
        </div>
        <h1 class="title">忘记密码</h1>
        <p class="subtitle">输入您的注册邮箱，我们将发送密码重置链接</p>
      </div>

      <el-form
        v-if="!emailSent"
        ref="formRef"
        :model="form"
        :rules="rules"
        @submit.prevent="handleSubmit"
        class="form"
      >
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入注册邮箱"
            size="large"
            :prefix-icon="Message"
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          @click="handleSubmit"
          class="submit-btn"
        >
          发送重置链接
        </el-button>
      </el-form>

      <div v-else class="success-state">
        <div class="success-icon">
          <el-icon :size="48" color="#16A34A"><CircleCheck /></el-icon>
        </div>
        <h2 class="success-title">邮件已发送</h2>
        <p class="success-desc">
          重置链接已发送到 <strong>{{ form.email }}</strong>，请在 24 小时内点击链接重置密码。
        </p>
        <p class="success-hint">没有收到邮件？请检查垃圾邮件文件夹</p>
      </div>

      <div class="back-link">
        <router-link to="/auth/login">← 返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { List, Message, CircleCheck } from '@element-plus/icons-vue'
import { authApi } from '@/api/index.js'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const emailSent = ref(false)

const form = reactive({ email: '' })
const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authApi.forgotPassword(form.email)
    emailSent.value = true
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.forgot-page {
  min-height: 100vh;
  background: #FDF6EE;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  font-family: 'PingFang SC', 'Helvetica Neue', -apple-system, sans-serif;
}

.forgot-card {
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
  line-height: 1.5;
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
  margin: 0 0 8px;
  line-height: 1.6;
}

.success-hint {
  font-size: 0.8rem;
  color: #A8A29E;
  margin: 0;
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
