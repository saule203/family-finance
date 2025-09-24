<template>
  <div class="charts-container">
    <!-- 支出分类饼图 -->
    <div v-if="expenseCategories.length" ref="expensePieRef" class="chart"></div>

    <!-- 每日支出柱状图 -->
    <div v-if="dailyExpenseData.length" ref="dailyChartRef" class="chart"></div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted } from "vue";
import * as echarts from "echarts";

const props = defineProps({
  expenseCategories: { type: Array, default: () => [] }, 
  dailyExpenseData: { type: Array, default: () => [] }
});

const expensePieRef = ref(null);
const dailyChartRef = ref(null);
let expensePieChart, dailyChart;

const renderCharts = async () => {
  await nextTick();

// ==== 支出分类饼图 (内环显示总支出) ====
if (expensePieRef.value) {
  if (!expensePieChart) expensePieChart = echarts.init(expensePieRef.value);

  const colors = [
    '#FF6F61', '#6B5B95', '#88B04B', '#F7CAC9', '#92A8D1', '#955251', '#B565A7', '#009B77'
  ];

  // 1) 清洗数据：过滤占位项“暂无”和非正数
  const raw = Array.isArray(props.expenseCategories) ? props.expenseCategories : [];
  const data = raw.filter(d => d && d.name !== '暂无' && Number(d.value) > 0);

  // 2) 正确计算总额（只统计有效项）
  const totalAmount = data.reduce((sum, i) => sum + Number(i.value || 0), 0);
  const hasData = totalAmount > 0 && data.length > 0;

  const series = [];

  // 3) 有数据时渲染外环
  if (hasData) {
    series.push({
      type: "pie",
      radius: ["45%", "65%"],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: "{b}\n{c} 元\n({d}%)", fontSize: 12, fontWeight: 500 },
      emphasis: { scale: true, scaleSize: 0.1, itemStyle: { shadowBlur: 15, shadowColor: 'rgba(0,0,0,0.25)' } },
      data: data.map((item, idx) => ({
        ...item,
        itemStyle: { color: colors[idx % colors.length] }
      })),
      labelLine: { smooth: true, length: 15, length2: 15 }
    });
  }

  // 4) 内环始终存在，只显示居中文字
  series.push({
    type: 'pie',
    radius: ['0%', '40%'],
    silent: true,
    tooltip: { show: false },     // 关闭内环提示
    label: {
      position: 'center',
      formatter: () => hasData ? `总支出\n${totalAmount} 元` : '暂无支出',
      fontSize: 16,
      fontWeight: 600,
      color: '#333'
    },
    data: [{ value: 1, itemStyle: { color: '#f5f5f5' } }] // 仅用于绘制背景圆盘
  });

  expensePieChart.setOption({
    title: {
      text: "支出分类",
      left: "center",
      top: 10,
      textStyle: { fontSize: 18, fontWeight: 600 }
    },
    tooltip: hasData ? { trigger: "item", formatter: "{b}: {c} 元 ({d}%)" } : { show: false },
    legend: hasData
      ? { bottom: 10, left: 'center', itemWidth: 12, itemHeight: 12, textStyle: { fontSize: 12 } }
      : { show: false },
    series
  }, true); // notMerge=true，避免旧配置残留
}


  // ==== 每日支出柱状图 ====
  if (dailyChartRef.value && props.dailyExpenseData.length) {
    if (!dailyChart) dailyChart = echarts.init(dailyChartRef.value);

    const days = props.dailyExpenseData.map(d => d.day);
    const values = props.dailyExpenseData.map(d => d.value);
    const colorData = props.dailyExpenseData.map(d =>
      new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#f48fb1' }, { offset: 1, color: '#f44336' }])
    );

    dailyChart.setOption({
      title: { text: "每日支出", left: "center", top: 10, textStyle: { fontSize: 18, fontWeight: 600 } },
      tooltip: { trigger: "axis", formatter: params => `${params[0].axisValue}日: ${params[0].data} 元` },
      grid: { left: '3%', right: '3%', bottom: '10%', containLabel: true },
      xAxis: { type: "category", data: days, axisTick: { alignWithLabel: true }, axisLine: { lineStyle: { color: '#ccc' } }, axisLabel: { fontSize: 12 } },
      yAxis: { type: "value", axisLine: { lineStyle: { color: '#ccc' } }, splitLine: { lineStyle: { type: 'dashed', color: '#eee' } } },
      series: [
        {
          type: "bar",
          data: values,
          barWidth: "40%",
          itemStyle: { borderRadius: [6,6,0,0], color: (params) => colorData[params.dataIndex] },
          label: { show: true, position: 'top', fontSize:12 }
        }
      ],
      animationDuration: 800
    });
  }
};

onMounted(renderCharts);
watch(() => [props.expenseCategories, props.dailyExpenseData], renderCharts, { deep: true });
</script>

<style scoped>
.charts-container {
  display: flex;
  flex-wrap: wrap; /* 自动换行 */
  justify-content: space-between;
  gap: 20px;
}
.chart {
  flex: 1 1 48%; /* 宽度自适应，最小48% */
  min-width: 280px;
  height: 400px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 3px 15px rgba(0,0,0,0.08);
  padding: 12px;
}
</style>
