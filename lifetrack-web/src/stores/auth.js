import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi, userApi } from '../api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || sessionStorage.getItem('token') || '')
  const user = ref(null)

  function setToken(newToken, remember) {
    token.value = newToken
    if (remember === false) {
      sessionStorage.setItem('token', newToken)
      localStorage.removeItem('token')
    } else {
      localStorage.setItem('token', newToken)
      sessionStorage.removeItem('token')
    }
  }

  function setUserInfo(userInfo) {
    user.value = userInfo
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    sessionStorage.removeItem('token')
  }

  async function login(email, password) {
    const res = await authApi.login({ email, password })
    return res.data
  }

  async function register(email, password, nickname) {
    return await authApi.register({ email, password, nickname })
  }

  async function fetchProfile() {
    const res = await userApi.getProfile()
    user.value = res.data
    return res.data
  }

  return { token, user, setToken, setUserInfo, logout, login, register, fetchProfile }
})
