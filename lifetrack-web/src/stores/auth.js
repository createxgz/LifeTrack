import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi, userApi } from '../api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  async function login(email, password) {
    const res = await authApi.login({ email, password })
    setToken(res.data.token)
    return res.data
  }

  async function register(email, password) {
    return await authApi.register({ email, password })
  }

  async function fetchProfile() {
    const res = await userApi.getProfile()
    user.value = res.data
    return res.data
  }

  return { token, user, setToken, logout, login, register, fetchProfile }
})
