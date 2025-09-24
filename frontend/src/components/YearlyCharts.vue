<template>
  <div class="yearly-charts">
    <h2 class="chart-title">{{ year }} 年收支统计</h2>

    <!-- 控制区 -->
    <div class="controls">
      <label class="label">选择年份：</label>
      <select v-model="year" class="year-select">
        <option v-for="y in availableYears" :key="y" :value="y">{{ y }}</option>
      </select>

      <button @click="toggleMode" class="toggle-btn">
        {{ mode === 'combined' ? '切换为分开展示' : '切换为合并展示' }}
      </button>
    </div>

    <!-- 单图模式 -->
    <div v-if="mode === 'combined'" class="chart-container">
      <Line
        :key="year + mode"
        v-if="chartDataCombined"
        :data="chartDataCombined"
        :options="chartOptionsCombined"
        :plugins="[infoBoxPluginCombined]"
      />
    </div>

    <!-- 双图模式 -->
    <div v-else class="chart-container">
      <Line
        :key="year + 'income'"
        v-if="chartDataIncome"
        :data="chartDataIncome"
        :options="chartOptionsIncome"
        :plugins="[infoBoxPluginIncome]"
      />
      <Line
        :key="year + 'expense'"
        v-if="chartDataExpense"
        :data="chartDataExpense"
        :options="chartOptionsExpense"
        :plugins="[infoBoxPluginExpense]"
      />
    </div>
  </div>
</template>

<script setup>
/* 保持你原有的 script setup 部分不变，仅在 fetchData 里加渐变色 */
import { ref, onMounted, watch, computed, nextTick } from 'vue'
import axios from 'axios'
import { Chart as ChartJS, Title, Tooltip, Legend, LineElement, CategoryScale, LinearScale, PointElement } from 'chart.js'
import { Line } from 'vue-chartjs'

ChartJS.register(Title, Tooltip, Legend, LineElement, CategoryScale, LinearScale, PointElement)

const year = ref(new Date().getFullYear())
const availableYears = ref([2023, 2024, 2025])
const mode = ref('combined')

const chartDataCombined = ref(null)
const chartDataIncome = ref(null)
const chartDataExpense = ref(null)

const totalIncome = ref(0)
const totalExpense = ref(0)
const aspectRatio = ref(1.5)

function toggleMode() {
  mode.value = mode.value === 'combined' ? 'separate' : 'combined'
}

function updateAspectRatio() {
  nextTick(() => {
    const container = document.querySelector('.chart-container')
    if (container) {
      const width = container.clientWidth
      const height = Math.min(container.clientHeight / (mode.value === 'combined' ? 1 : 2), 250)
      aspectRatio.value = width / Math.max(height, 150)
    }
  })
}

// 插件：信息框
function createInfoBoxPlugin(chartDataRef) {
  return {
    id: 'infoBoxPlugin',
    afterDraw(chart) {
      if (!chart || !chartDataRef.value || !chartDataRef.value.datasets) return
      const { ctx, chartArea: { top, right } } = chart
      ctx.save()

      const padding = 10
      const lineHeight = 22
      const boxWidth = 190
      const boxHeight = lineHeight * 3 + padding
      const x = right - boxWidth - 10
      const y = top + 10

      const incomeColor = chartDataRef.value.datasets.find(d => d.label === '收入')?.borderColor || 'green'
      const expenseColor = chartDataRef.value.datasets.find(d => d.label === '支出')?.borderColor || 'red'
      const netIncome = totalIncome.value - totalExpense.value
      const netColor = netIncome >= 0 ? 'green' : 'red'

      // 背景改毛玻璃效果
      ctx.fillStyle = 'rgba(255,255,255,0.6)'
      ctx.fillRect(x, y, boxWidth, boxHeight)
      ctx.strokeStyle = 'rgba(0,0,0,0.15)'
      ctx.strokeRect(x, y, boxWidth, boxHeight)

      ctx.font = 'bold 14px Arial'
      ctx.textAlign = 'left'
      let offsetY = padding + lineHeight / 1.5

      if (chartDataRef.value.datasets.some(d => d.label === '收入')) {
        ctx.fillStyle = incomeColor
        ctx.fillText(`总收入: ${totalIncome.value.toLocaleString()}`, x + padding, y + offsetY)
        offsetY += lineHeight
      }
      if (chartDataRef.value.datasets.some(d => d.label === '支出')) {
        ctx.fillStyle = expenseColor
        ctx.fillText(`总支出: ${totalExpense.value.toLocaleString()}`, x + padding, y + offsetY)
        offsetY += lineHeight
      }
      ctx.fillStyle = netColor
      ctx.fillText(`净收入: ${netIncome.toLocaleString()}`, x + padding, y + offsetY)

      ctx.restore()
    }
  }
}

const infoBoxPluginCombined = computed(() => createInfoBoxPlugin(chartDataCombined))
const infoBoxPluginIncome = computed(() => createInfoBoxPlugin(chartDataIncome))
const infoBoxPluginExpense = computed(() => createInfoBoxPlugin(chartDataExpense))

const chartOptionsCombined = computed(() => ({
  responsive: true,
  maintainAspectRatio: true,
  aspectRatio: aspectRatio.value,
  plugins: { legend: { labels: { font: { size: 13 } } } },
  scales: {
    y: { beginAtZero: true, ticks: { stepSize: 500, font: { size: 12 } }, maxTicksLimit: 10 }
  }
}))
const chartOptionsIncome = chartOptionsCombined
const chartOptionsExpense = chartOptionsCombined

async function fetchData() {
  try {
    const userId = localStorage.getItem('userId')
    const res = await axios.get(`http://localhost:8080/api/charts/yearly`, {
      params: { year: year.value, userId }
    })
    const data = res.data

    totalIncome.value = data.income.reduce((sum, val) => sum + val, 0)
    totalExpense.value = data.expense.reduce((sum, val) => sum + val, 0)

    // 渐变色线条
    const ctx = document.createElement("canvas").getContext("2d")
    const gradientIncome = ctx.createLinearGradient(0, 0, 0, 400)
    gradientIncome.addColorStop(0, "#4caf50")
    gradientIncome.addColorStop(1, "#81c784")

    const gradientExpense = ctx.createLinearGradient(0, 0, 0, 400)
    gradientExpense.addColorStop(0, "#f44336")
    gradientExpense.addColorStop(1, "#e57373")

    chartDataCombined.value = {
      labels: data.months,
      datasets: [
        { label: "收入", data: data.income, borderColor: gradientIncome, fill: false, tension: 0.3 },
        { label: "支出", data: data.expense, borderColor: gradientExpense, fill: false, tension: 0.3 }
      ]
    }
    chartDataIncome.value = {
      labels: data.months,
      datasets: [
        { label: "收入", data: data.income, borderColor: gradientIncome, fill: false, tension: 0.3 }
      ]
    }
    chartDataExpense.value = {
      labels: data.months,
      datasets: [
        { label: "支出", data: data.expense, borderColor: gradientExpense, fill: false, tension: 0.3 }
      ]
    }

    updateAspectRatio()
  } catch (err) {
    console.error("获取图表数据失败:", err)
  }
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', updateAspectRatio)
})
watch([year, mode], fetchData)
</script>

<style scoped>
.yearly-charts {
  padding: 1.5rem;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 3px 10px rgba(0,0,0,0.1);
  width: 100%;
  box-sizing: border-box;
  margin-top: 1rem;
}

.chart-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 1rem;
}

/* 控制栏 */
.controls {
  display: flex;
  gap: 1rem;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.label {
  font-weight: bold;
  color: #555;
}

.year-select {
  padding: 0.4rem 0.8rem;
  border-radius: 8px;
  border: 1px solid #ccc;
  font-size: 14px;
}

.toggle-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #4da6ff, #1e90ff);
  color: #fff;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
}
.toggle-btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

/* 图表容器 */
.chart-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 100%;
}

.chart-container canvas {
  width: 100% !important;
  height: auto !important;
}
</style>
