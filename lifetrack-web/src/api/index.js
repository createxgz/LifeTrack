import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

function getToken() {
  return localStorage.getItem('token') || sessionStorage.getItem('token')
}

function clearToken() {
  localStorage.removeItem('token')
  sessionStorage.removeItem('token')
}

http.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    const status = error.response?.status
    if (status === 401 || status === 403) {
      clearToken()
      router.push('/auth/login')
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export const authApi = {
  login: (data) => http.post('/auth/login', data),
  register: (data) => http.post('/auth/register', data),
  forgotPassword: (email) => http.post('/auth/forgot-password', { email }),
  resetPassword: (data) => http.post('/auth/reset-password', data)
}

export const userApi = {
  getProfile: () => http.get('/user/profile'),
  updateProfile: (data) => http.put('/user/profile', data),
  changePassword: (data) => http.put('/user/password', data)
}

export default http
