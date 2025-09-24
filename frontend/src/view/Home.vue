<template>
  <div class="home-container">
    <!-- 主体内容 -->
    <main class="main-content">
      <div class="title-section">
        <h2 class="main-title">家庭财务管理系统</h2>
        <p class="subtitle">探索财务状况，规划幸福生活</p>
      </div>

      <!-- 功能按钮区 -->
      <div class="button-grid">
        <button class="function-button blue" @click="goTo('Income')">💰 收入记录</button>
        <button class="function-button green" @click="goTo('Expense')">🛒 支出记录</button>
        <button class="function-button purple" @click="goTo('Category')">📂 分类管理</button>
        <button class="function-button orange" @click="goTo('Budget')">📊 预算设置</button>
        <button class="function-button gray" @click="goTo('Member')">👨‍👩‍👧‍👦 家庭管理</button>
      </div>
    </main>

    <!-- 图表 -->
    <section class="charts-section">
      <h3 class="charts-title">年度财务概览</h3>
      <div class="charts-card">
        <YearlyCharts />
      </div>
    </section>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import YearlyCharts from "@/components/YearlyCharts.vue";

const router = useRouter();

const goTo = (page) => {
  router.push({ name: page });
};
</script>

<style scoped>
.home-container {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  z-index: 1; /* 确保内容在背景之上 */
}

/* 主体 */
.main-content {
  flex: 1;
  padding: 2rem;
  text-align: center;
}

.title-section {
  margin-bottom: 2rem;
}

.main-title {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.subtitle {
  font-size: 16px;
  color: #555;
  margin-top: 0.5rem;
}

/* 按钮网格 */
.button-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 1.2rem;
  margin-top: 2rem;
}

.function-button {
  min-width: 150px;
  padding: 1rem 1.5rem;
  border: none;
  border-radius: 14px;
  color: white;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.function-button:hover {
  transform: translateY(-6px) rotate(-1deg);
  box-shadow: 0 8px 16px rgba(0,0,0,0.25);
  opacity: 0.98;
}

/* 按钮色彩 */
.blue { background: linear-gradient(135deg, #4da6ff, #1e90ff); }
.green { background: linear-gradient(135deg, #66cc99, #33b27b); }
.purple { background: linear-gradient(135deg, #b366ff, #8a2be2); }
.orange { background: linear-gradient(135deg, #ffa64d, #ff7f24); }
.gray { background: linear-gradient(135deg, #999, #666); }

/* 图表区 */
.charts-section {
  padding: 2rem;
}

.charts-title {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #333;
}

.charts-card {
  padding: 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #ffffff, #f0f6ff);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
</style>
