<template>
  <div id="app">
    <!-- 全局背景 -->
    <div class="app-background">
      <div class="decor-circle circle1"></div>
      <div class="decor-circle circle2"></div>
      <div class="decor-circle circle3"></div>
      <div class="decor-circle circle4"></div>
      <div class="decor-circle circle5"></div>
    </div>

    <!-- 顶部导航 -->
    <TopNav v-if="$route.name !== 'Register'" :username="username" />

    <!-- 路由页面渲染入口 -->
    <router-view />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import TopNav from "@/components/TopNav.vue";

const username = ref('访客');

onMounted(() => {
  username.value = localStorage.getItem('username') || '访客';
});
</script>

<style>
#app {
  position: relative;
  min-height: 100vh;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

/* 全局背景容器 */
.app-background {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none; /* 避免遮挡交互 */
  background: linear-gradient(135deg, #f7f9fc, #eaf0f8);
}

/* 装饰圆圈 */
.decor-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(0,123,255,0.15);
  animation: floatCircle 20s ease-in-out infinite alternate, flicker 4s ease-in-out infinite;
}

.circle1 { width: 140px; height: 140px; top: -50px; left: -50px; animation-duration: 18s; }
.circle2 { width: 100px; height: 100px; bottom: 30px; right: -30px; animation-duration: 22s; }
.circle3 { width: 60px; height: 60px; top: 100px; right: 50px; animation-duration: 20s; }
.circle4 { width: 80px; height: 80px; bottom: 100px; left: 60px; animation-duration: 25s; }
.circle5 { width: 120px; height: 120px; top: 200px; left: 150px; animation-duration: 28s; }

@keyframes floatCircle {
  0% { transform: translate(0,0); }
  25% { transform: translate(15px,-10px); }
  50% { transform: translate(-10px,15px); }
  75% { transform: translate(5px,-5px); }
  100% { transform: translate(0,0); }
}

@keyframes flicker {
  0%,100% { opacity: 0.15; }
  50% { opacity: 0.25; }
}
</style>
