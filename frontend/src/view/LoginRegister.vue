<template>
  <div class="auth-container">
    <!-- 粒子背景 -->
    <canvas ref="particleCanvas" class="particle-canvas"></canvas>

    <div 
      class="auth-card" 
      ref="card" 
      :style="cardStyle"
      @mousemove="handleMouseMove"
      @mouseleave="handleMouseLeave"
    >
      <!-- 提示浮层 -->
      <transition name="message-fade">
        <div v-if="message.text" :class="['message-box', message.type]">
          {{ message.text }}
        </div>
      </transition>

      <!-- 卡片标题 -->
      <div class="auth-card-title">
        <h2>{{ activeTab === 'login' ? '欢迎登录' : '创建账户' }}</h2>
      </div>

      <!-- Tab Header -->
      <div class="tab-header">
        <div class="tab" :class="{ active: activeTab === 'login' }" @click="switchTab('login')">登录</div>
        <div class="tab" :class="{ active: activeTab === 'register' }" @click="switchTab('register')">注册</div>
        <div class="tab-indicator" :style="indicatorStyle"></div>
      </div>

      <!-- 表单提示文字 -->
      <p class="form-tip" v-if="activeTab === 'login'">请输入您的账户信息登录系统</p>
      <p class="form-tip" v-else>注册后即可开始管理家庭财务</p>

      <!-- Forms -->
      <transition name="fade" mode="out-in">
        <!-- 登录 -->
        <form v-if="activeTab === 'login'" key="login" @submit.prevent="handleLogin">
          <input v-model="loginForm.username" placeholder="请输入用户名" class="input" autocomplete="username" />
          <input v-model="loginForm.password" type="password" placeholder="请输入密码" class="input" autocomplete="current-password" />
          <button type="submit" class="btn btn-login">登录</button>
        </form>

        <!-- 注册 -->
        <div v-else key="register">
          <input v-model="registerForm.username" placeholder="设置用户名（唯一）" class="input" autocomplete="username" />
          <input v-model="registerForm.password" type="password" placeholder="设置密码" class="input" autocomplete="new-password" />
          <button @click="handleRegister" class="btn btn-register">注册</button>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import { register, login } from '@/api/user'
import { onMounted, ref } from 'vue'

export default {
  data() {
    return {
      activeTab: 'login',
      registerForm: { username: '', password: '' },
      loginForm: { username: '', password: '' },
      rotateX: 0,
      rotateY: 0,
      shineX: 50,
      shineY: 50,
      message: { text: '', type: '' } // 弹窗消息
    }
  },
  computed: {
    indicatorStyle() {
      return { transform: this.activeTab === 'login' ? 'translateX(0%)' : 'translateX(100%)' }
    },
    cardStyle() {
      return {
        transform: `rotateX(${this.rotateX}deg) rotateY(${this.rotateY}deg)`,
      }
    }
  },
  methods: {
    switchTab(tab) {
      if (this.activeTab !== tab) this.activeTab = tab
    },
    handleMouseMove(e) {
      const card = this.$refs.card
      const rect = card.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top
      const halfW = rect.width / 2
      const halfH = rect.height / 2

      this.rotateY = (x - halfW) / halfW * 8
      this.rotateX = -(y - halfH) / halfH * 8

      this.shineX = (x / rect.width) * 100
      this.shineY = (y / rect.height) * 100
      card.style.setProperty('--shine-x', `${this.shineX}%`)
      card.style.setProperty('--shine-y', `${this.shineY}%`)
    },
    handleMouseLeave() {
      this.rotateX = 0
      this.rotateY = 0
      this.$refs.card.style.setProperty('--shine-x', `50%`)
      this.$refs.card.style.setProperty('--shine-y', `50%`)
    },
    showMessage(text, type='info', duration=2500) {
      this.message = { text, type }
      setTimeout(() => this.message = { text: '', type: '' }, duration)
    },
    async handleRegister() {
      try {
        await register(this.registerForm)
        this.showMessage('注册成功', 'success')
        this.switchTab('login')
      } catch (err) {
        if (err.response && err.response.status === 409) {
          this.showMessage('用户名已存在，请更换用户名', 'warning')
        } else {
          this.showMessage('注册失败，请稍后重试', 'error')
        }
      }
    },
    async handleLogin() {
      try {
        const res = await login(this.loginForm)
        localStorage.setItem('userId', res.data.id)
        localStorage.setItem('username', res.data.username)
        this.$router.push('/home')
      } catch (err) {
        if (err.response && err.response.status === 401) {
          this.showMessage(err.response.data, 'warning')
        } else {
          this.showMessage('登录失败', 'error')
        }
      }
    },
    initParticles() {
      const canvas = this.$refs.particleCanvas
      const ctx = canvas.getContext('2d')
      canvas.width = window.innerWidth
      canvas.height = window.innerHeight

      const particles = []
      const num = 80
      for (let i = 0; i < num; i++) {
        particles.push({
          x: Math.random() * canvas.width,
          y: Math.random() * canvas.height,
          r: Math.random() * 2 + 1,
          dx: (Math.random() - 0.5) * 0.8,
          dy: (Math.random() - 0.5) * 0.8
        })
      }

      const animate = () => {
        ctx.clearRect(0,0,canvas.width,canvas.height)
        particles.forEach(p => {
          ctx.beginPath()
          ctx.arc(p.x, p.y, p.r, 0, Math.PI*2)
          ctx.fillStyle = 'rgba(255,255,255,0.6)'
          ctx.fill()

          p.x += p.dx
          p.y += p.dy

          if(p.x<0 || p.x>canvas.width) p.dx *= -1
          if(p.y<0 || p.y>canvas.height) p.dy *= -1
        })
        requestAnimationFrame(animate)
      }
      animate()
      window.addEventListener('resize', () => {
        canvas.width = window.innerWidth
        canvas.height = window.innerHeight
      })
    }
  },
  mounted() {
    this.initParticles()
  }
}
</script>

<style scoped>
/* 页面背景与 3D 透视 */
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  perspective: 1000px;
  padding: 20px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #74ebd5, #acb6e5);
}

/* 粒子背景画布 */
.particle-canvas {
  position: absolute;
  inset: 0;
  z-index: 0;
}

/* 卡片主体 + 微浮动 + 光泽随鼠标 */
.auth-card {
  background: #fff;
  padding: 40px 30px;
  border-radius: 14px;
  width: 340px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.2);
  position: relative;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  animation: float 6s ease-in-out infinite;
  will-change: transform;
  --shine-x: 50%;
  --shine-y: 50%;
  z-index: 1;
}

/* 卡片主体 + 微浮动 + 光泽随鼠标 */
.auth-card {
  background: #fff;
  padding: 40px 30px;
  border-radius: 14px;
  width: 340px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.2);
  position: relative;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  animation: float 6s ease-in-out infinite;
  will-change: transform;
  --shine-x: 50%;
  --shine-y: 50%;
}

/* 卡片微浮动动画 */
@keyframes float {
  0%,100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

/* 光泽：跟随鼠标的柔和高光 */
.auth-card::after {
  content: "";
  position: absolute;
  inset: -50%;
  background: radial-gradient(
    circle at var(--shine-x) var(--shine-y),
    rgba(255,255,255,0.25),
    transparent 60%
  );
  pointer-events: none;
  transition: background-position 0.1s;
}

/* 卡片标题 */
.auth-card-title {
  text-align: center;
  font-size: 22px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}

/* 顶部 Tab 与弹性指示条 */
.tab-header {
  display: flex;
  position: relative;
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-weight: 700;
  cursor: pointer;
  color: #666;
  user-select: none;
  transition: color 0.3s;
}

.tab.active {
  color: #36d1dc;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  width: 50%;
  height: 3px;
  background: #36d1dc;
  transition: transform 0.5s cubic-bezier(0.68,-0.55,0.27,1.55);
}

/* 表单提示文字 */
.form-tip {
  font-size: 13px;
  color: #666;
  margin-bottom: 10px;
  text-align: center;
}

/* 输入框 */
.input {
  width: 90%;
  padding: 10px 15px;
  margin-bottom: 15px;
  border-radius: 10px;
  border: 1px solid #dcdcdc;
  outline: none;
  transition: border 0.2s, transform 0.2s, box-shadow 0.2s;
}

.input:focus {
  border-color: #74ebd5;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.12);
}

.input::placeholder {
  color: #aaa;
  font-style: italic;
}

/* 按钮 */
.btn {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  color: #fff;
  cursor: pointer;
  transition: background 0.25s, transform 0.1s, box-shadow 0.2s;
  box-shadow: 0 10px 20px rgba(0,0,0,0.12);
}

.btn:active { transform: scale(0.97); }

.btn-login {
  background: linear-gradient(90deg, #f7971e, #ffd200);
}

.btn-login:hover {
  background: linear-gradient(90deg, #ffd200, #f7971e);
  box-shadow: 0 12px 24px rgba(0,0,0,0.2);
}

.btn-register {
  background: linear-gradient(90deg, #36d1dc, #5b86e5);
}

.btn-register:hover {
  background: linear-gradient(90deg, #5b86e5, #36d1dc);
  box-shadow: 0 12px 24px rgba(0,0,0,0.2);
}

/* 提示弹窗 */
.message-box {
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  padding: 10px 18px;
  border-radius: 8px;
  font-weight: 500;
  z-index: 10;
  text-align: center;
  color: #fff;
}
.message-box.success { background: #2ecc71; }
.message-box.warning { background: #f39c12; }
.message-box.error { background: #e74c3c; }

/* 提示动画 */
.message-fade-enter-active, .message-fade-leave-active {
  transition: opacity 0.35s, transform 0.35s;
}
.message-fade-enter-from {
  opacity: 0;
  transform: translate(-50%, -10px);
}
.message-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -10px);
}

/* 表单淡入淡出切换 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.35s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
