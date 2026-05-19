import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref<any>(null)
  const token = ref(localStorage.getItem('token') || '')

  function setUser(u: any) {
    user.value = u
    localStorage.setItem('user', JSON.stringify(u))
  }

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('token', t)
  }

  function logout() {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  function loadUser() {
    const u = localStorage.getItem('user')
    if (u) user.value = JSON.parse(u)
  }

  loadUser()

  return { user, token, setUser, setToken, logout }
})
