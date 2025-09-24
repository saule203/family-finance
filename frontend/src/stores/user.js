// src/stores/user.js
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: null,
    username: '',
    token: '',
    // 你可以根据需要添加更多字段
  }),
  actions: {
    setUser(data) {
      this.userId = data.userId
      this.username = data.username
      this.token = data.token
    },
    clearUser() {
      this.userId = null
      this.username = ''
      this.token = ''
    }
  },
  persist: true  // 若你使用 pinia-plugin-persistedstate 插件，可以保留登录状态
})
