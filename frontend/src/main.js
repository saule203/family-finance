import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'


const app = createApp(App)

const pinia = createPinia()

app.use(ElementPlus)  // ✅ 全局引入 Element Plus
app.use(pinia)
app.use(router)

app.mount('#app')
