<template>
  <div class="charts-container">
    <!-- 收入分类饼图 -->
    <div ref="incomePieRef" class="chart"></div>

    <!-- 年度收入折线图 -->
    <div ref="lineChartRef" class="chart"></div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted, onBeforeUnmount } from "vue";
import * as echarts from "echarts";

const props = defineProps({
  incomeCategories: { type: Array, default: () => [] },
  lineData: { type: Array, default: () => [] }
});

const totalIncome = ref(0);

const incomePieRef = ref(null);
const lineChartRef = ref(null);
let incomePieChart = null;
let lineChart = null;

const renderCharts = async () => {
  await nextTick();

  // 计算总收入
  totalIncome.value = props.incomeCategories.reduce((sum, d) => sum + d.value, 0);

  // --- 饼图 ---
  if (incomePieRef.value) {
    if (!incomePieChart) incomePieChart = echarts.init(incomePieRef.value);
    incomePieChart.clear();
    if (props.incomeCategories.length) {
      incomePieChart.setOption({
        title: {
          text: "收入分类",
          left: "center",
          textStyle: { fontSize: 20, color: '#333' }
        },
        tooltip: { trigger: "item", formatter: '{b}: {c} 元 ({d}%)' },
        legend: { bottom: 10, textStyle: { fontSize: 12, color: '#555' }, type: 'scroll' },
        series: [
          {
            type: "pie",
            radius: ["40%", "65%"],
            center: ["50%", "50%"],
            data: props.incomeCategories.map((d, i) => ({
              ...d,
              itemStyle: { color: `hsl(${i * 60 % 360}, 70%, 60%)` }
            })),
            emphasis: { itemStyle: { shadowBlur: 15, shadowColor: 'rgba(0,0,0,0.3)' } },
            label: { fontSize: 12 }
          }
        ],
        graphic: {
          type: 'text',
          left: 'center',
          top: 'center',
          style: {
            text: `总收入\n${totalIncome.value} 元`,
            font: 'bold 16px Arial',
            fill: '#333',
            align: 'center'
          }
        }
      });
    }
  }

    // --- 折线图 ---
  if (lineChartRef.value) {
    if (!lineChart) lineChart = echarts.init(lineChartRef.value);
    lineChart.clear();
    if (props.lineData.length) {
      const months = props.lineData.map(d => d.month + "月");
      const values = props.lineData.map(d => d.value);

      // 计算折线图总收入（各个月份收入之和）
      const lineTotal = values.reduce((sum, v) => sum + Number(v), 0);
      const lineTotalRounded = lineTotal.toFixed(2);

      lineChart.setOption({
        title: { text: "年度收入趋势", left: "center", textStyle: { fontSize: 20, color: '#333' } },
        tooltip: { trigger: "axis", formatter: '{b}<br/>收入: {c} 元' },
        xAxis: {
          type: "category",
          data: months,
          axisLine: { lineStyle: { color: '#aaa' } },
          axisLabel: { fontSize: 12 }
        },
        yAxis: {
          type: "value",
          axisLine: { lineStyle: { color: '#aaa' } },
          splitLine: { lineStyle: { type: 'dashed', color: '#eee' } },
          axisLabel: { fontSize: 12 }
        },
        series: [
          {
            name: "收入",
            type: "line",
            data: values,
            smooth: true,
            lineStyle: { width: 3, color: '#5470C6' },
            itemStyle: { color: '#5470C6' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(84,112,198,0.4)' },
                { offset: 1, color: 'rgba(84,112,198,0)' }
              ])
            }
          }
        ],
        graphic: [
          {
            type: 'text',
            left: 'right',
            top: 10,
            style: {
              text: `总收入: ${lineTotalRounded} 元`,
              font: 'bold 14px Arial',
              fill: '#333',
              align: 'right'
            }
          }
        ]
      });
    }
  }

};

// 页面挂载后初始化
onMounted(renderCharts);

// 监听数据更新，刷新图表
watch(
  () => [props.incomeCategories, props.lineData],
  renderCharts,
  { deep: true }
);

// 页面卸载时销毁图表
onBeforeUnmount(() => {
  if (incomePieChart) incomePieChart.dispose();
  if (lineChart) lineChart.dispose();
});
</script>

<style scoped>
.charts-container {
  display: flex;
  flex-wrap: wrap;   /* 支持换行 */
  justify-content: space-between;
  gap: 20px;
  padding: 10px 0;
}

.chart {
  flex: 1 1 45%;     /* 自由调整大小，可换行 */
  min-width: 350px;  /* 最小宽度保证显示效果 */
  height: 400px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  padding: 10px;
}

</style>
