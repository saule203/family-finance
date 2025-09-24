<template>
  <div class="budget-container">
    <h2>预算管理</h2>

    <!-- 添加/修改预算按钮 + 弹窗 -->
    <button class="btn-add" @click="openModal">新增预算</button>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3>{{ form.id ? '修改预算' : '新增预算' }}</h3>
        <form @submit.prevent="submitBudget">
          <div class="form-row">
            <label>年份：</label>
            <select v-model="form.year" required>
              <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
            </select>
          </div>
          <div class="form-row">
            <label>月份：</label>
            <select v-model="form.month" required>
              <option v-for="m in 12" :key="m" :value="m">{{ m }}</option>
            </select>
          </div>
          <div class="form-row">
            <label>分类：</label>
            <select v-model="form.categoryId" required>
              <option value="">请选择分类</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="form-row">
            <label>预算金额：</label>
            <input type="number" step="0.01" min="0.01" v-model="form.amount" required />
          </div>
          <div class="modal-actions">
            <button type="submit" class="btn-confirm">{{ form.id ? '修改' : '新增' }}</button>
            <button type="button" class="btn-cancel" @click="closeModal">取消</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 筛选和排序 -->
    <div class="filters">
      <label>年份：</label>
      <select v-model="selectedYear" @change="onYearChange">
        <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
      </select>

      <label>月份：</label>
      <select v-model="selectedMonth" @change="onMonthChange">
        <option v-for="m in 12" :key="m" :value="m">{{ m }}</option>
      </select>

      <button class="btn-current" @click="resetToCurrentMonth">当前月</button>

      <div class="search-sort">
        <input v-model="searchKeyword" placeholder="搜索分类或金额" />
        <label>排序：</label>
        <select v-model="sortField" @change="applySort">
          <option value="default">默认</option>
          <option value="category">按分类</option>
          <option value="amount">按金额</option>
        </select>
        <select v-model="sortAsc" @change="applySort">
          <option :value="true">升序</option>
          <option :value="false">降序</option>
        </select>
        <button class="btn-reset" @click="resetSearch">重置</button>
      </div>
    </div>

    <!-- 预算表格 -->
    <table>
      <thead>
        <tr>
          <th>序号</th>
          <th>分类</th>
          <th>预算金额</th>
          <th>已用金额</th>
          <th>状态</th>
          <th>年份</th>
          <th>月份</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(b,index) in filteredAndSortedBudgets" :key="b.id">
          <td>{{ index + 1 }}</td>
          <td>{{ b.category?.name || '未知分类' }}</td>
          <td>{{ b.amount }}</td>
          <td>{{ getSpent(b.category?.id, b.year, b.month) }}</td>
          <td :class="{'over': isOver(b), 'ok': !isOver(b)}">
            {{ isOver(b) ? '超预算' : '正常' }}
          </td>
          <td>{{ b.year }}</td>
          <td>{{ b.month }}</td>
          <td>
            <button @click="editBudget(b)" class="btn-edit">编辑</button>
            <button @click="deleteBudget(b.id)" class="btn-delete">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 图表 -->
    <div class="charts-container">
      <div ref="barChartRef" class="chart"></div>
      <div ref="lineChartRef" class="chart"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'

// 数据
const categories = ref([])
const budgets = ref([])
const expensesSummary = ref({})
const monthlyData = ref([])
const yearlyData = ref([])

// 表单 + 弹窗
const form = ref({ id:null, categoryId:'', amount:'', year:new Date().getFullYear(), month:new Date().getMonth()+1 })
const showModal = ref(false)

// 筛选
const selectedYear = ref(new Date().getFullYear())
const selectedMonth = ref(new Date().getMonth()+1)
const years = ref([])
const searchKeyword = ref("")
const sortField = ref("default")
const sortAsc = ref(true)

// 图表引用
const lineChartRef = ref(null)
let lineChart = null
const barChartRef = ref(null)
let barChart = null

// 初始化年份
onMounted(() => {
  const now = new Date().getFullYear()
  for(let y=now; y>=now-5; y--) years.value.push(y)
  loadCategories()
  loadBudgets()
  loadExpensesSummary()
  loadYearlyChart(selectedYear.value)
  loadBarChart()
})

const loadCategories = async () => {
  const userId = localStorage.getItem("userId")
  const res = await axios.get(`http://localhost:8080/api/categories/user/${userId}/type/支出`)
  categories.value = res.data
}

const loadBudgets = async () => {
  const userId = localStorage.getItem("userId")
  if(!userId) return
  const res = await axios.get(`http://localhost:8080/api/budgets/user/${userId}`)
  budgets.value = res.data
  loadBarChart()
}

const loadExpensesSummary = async () => {
  const userId = localStorage.getItem("userId")
  if(!userId) return
  const res = await axios.get(`http://localhost:8080/api/expenses/user/${userId}`)
  const summary = {}
  res.data.forEach(e=>{
    if(!e.date || !e.category?.id) return
    const ym = e.date.slice(0,7)
    if(!summary[ym]) summary[ym]={}
    summary[ym][e.category.id] = (summary[ym][e.category.id]||0) + Number(e.amount)
  })
  expensesSummary.value = summary
}

// 表单操作
const submitBudget = async () => {
  try {
    if (!form.value.amount || form.value.amount <= 0) {
      alert("预算金额必须大于 0");
      return;
    };
    const userId = localStorage.getItem("userId")
    if(!userId) return alert("请先登录")
    const payload = { id:form.value.id, amount:form.value.amount, category:{id:form.value.categoryId}, user:{id:parseInt(userId)}, year:form.value.year, month:form.value.month }
    await axios.post('http://localhost:8080/api/budgets', payload)
    closeModal()
    await loadBudgets()
    await loadExpensesSummary()
    await loadYearlyChart(selectedYear.value)
    await loadBarChart()
  }catch(err){console.error(err); alert('提交失败')}
}

const editBudget = (b) => { 
  form.value = { id:b.id, categoryId:b.category?.id||'', amount:b.amount, year:b.year, month:b.month }
  showModal.value = true
}

const deleteBudget = async (id) => {
  if(!confirm('确定删除该预算吗？')) return
  await axios.delete(`http://localhost:8080/api/budgets/${id}`)
  await loadBudgets()
  await loadExpensesSummary()
  await loadYearlyChart(selectedYear.value)
  await loadBarChart()
}

const openModal = ()=>showModal.value=true
const closeModal = ()=>showModal.value=false

// 工具函数
const getSpent = (categoryId, year, month) => {
  const ym = `${year}-${String(month).padStart(2,'0')}`
  return expensesSummary.value[ym]?.[categoryId] || 0
}
const isOver = (b)=>getSpent(b.category?.id,b.year,b.month) > b.amount

// 筛选排序
const filteredAndSortedBudgets = computed(()=>{
  let list = budgets.value.filter(b=>b.year===selectedYear.value && b.month===selectedMonth.value)
  if(searchKeyword.value.trim()){
    const kw = searchKeyword.value.trim().toLowerCase()
    list = list.filter(b=>(b.category?.name?.toLowerCase().includes(kw)) || String(b.amount).includes(kw))
  }
  if(sortField.value!=='default'){
    list = list.slice().sort((a,b)=>{
      let va=sortField.value==='category'?a.category?.name:'amount' in a?a.amount:0
      let vb=sortField.value==='category'?b.category?.name:'amount' in b?b.amount:0
      if(va<vb) return sortAsc.value?-1:1
      if(va>vb) return sortAsc.value?1:-1
      return 0
    })
  }
  return list
})
const resetToCurrentMonth = () => {
  const now = new Date()
  selectedYear.value=now.getFullYear()
  selectedMonth.value=now.getMonth()+1
  loadBarChart()
  loadYearlyChart(selectedYear.value)
}
const onYearChange = ()=>{ loadBarChart(); loadYearlyChart(selectedYear.value) }
const onMonthChange = ()=>loadBarChart()
const resetSearch = ()=>searchKeyword.value=''
const applySort = ()=>{}

// 年度折线图
const loadYearlyChart = async (year) => {
  const userId = localStorage.getItem("userId")
  if(!userId) return
  const res = await axios.get(`http://localhost:8080/api/budgets/yearly/${userId}/${year}`)
  yearlyData.value = res.data.map(item => ({
    month: item.month+'月',
    budget: item.budget||0,
    spent: item.spent||0
  }))
  await nextTick()
  if(lineChartRef.value){
    if(!lineChart) lineChart=echarts.init(lineChartRef.value)
    else lineChart.clear()
    lineChart.setOption({
      title:{ text:`${selectedYear.value} 年预算/支出`, left:'center' },
      tooltip:{ trigger:'axis' },
      legend:{ data:['预算','支出'], top:30 },
      xAxis:{ type:'category', data:yearlyData.value.map(i=>i.month), axisLabel:{rotate:45,fontSize:12,color:'#333'} },
      yAxis:{ type:'value' },
      series:[
        {
          name:'预算',
          type:'line',
          smooth:true,
          data:yearlyData.value.map(i=>i.budget),
          symbol:'circle',
          symbolSize:8,
          lineStyle:{ width:3, color: { type:'linear', x:0, y:0, x2:1, y2:0, colorStops:[{offset:0,color:'#1890ff'},{offset:1,color:'#40a9ff'}]} },
          itemStyle:{ color:'#1890ff' },
          label:{ show:false } // 不显示默认标签
        },
        {
          name:'支出',
          type:'line',
          smooth:true,
          data:yearlyData.value.map(i=>i.spent),
          symbol:'circle',
          symbolSize:8,
          lineStyle:{ width:3, color: { type:'linear', x:0, y:0, x2:1, y2:0, colorStops:[{offset:0,color:'#52c41a'},{offset:1,color:'#73d13d'}]} },
          itemStyle:{ color:'#52c41a' },
          label:{ show:false } // 不显示默认标签
        }
      ]
    })
  }
}

// 月度柱状图
const loadBarChart = async ()=>{
  const userId = localStorage.getItem("userId")
  if(!userId) return
  const res = await axios.get(`http://localhost:8080/api/budgets/monthly/${userId}/${selectedYear.value}/${selectedMonth.value}`)
  monthlyData.value = res.data
  await nextTick()
  if(barChartRef.value){
    if(!barChart) barChart=echarts.init(barChartRef.value)
    else barChart.clear()
    barChart.setOption({
      title:{ text:`${selectedYear.value} 年 ${selectedMonth.value} 月各分类预算/支出`, left:'center' },
      tooltip:{ trigger:'axis' },
      legend:{ data:['预算','支出'], top:30 },
      xAxis:{ type:'category', data:monthlyData.value.map(i=>i.category), axisLabel:{rotate:30,fontSize:12,color:'#333'} },
      yAxis:{ type:'value' },
      series:[
        {
          name:'预算',
          type:'bar',
          data:monthlyData.value.map(i=>i.budget),
          barWidth:'35%',
          itemStyle:{
            color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#1890ff'},{offset:1,color:'#40a9ff'}]),
            barBorderRadius: [6,6,0,0] // 顶部圆角
          },
          label:{ show:false } // 不显示默认标签
        },
        {
          name:'支出',
          type:'bar',
          data:monthlyData.value.map(i=>i.expense),
          barWidth:'35%',
          itemStyle:{
            color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#52c41a'},{offset:1,color:'#73d13d'}]),
            barBorderRadius: [6,6,0,0] // 顶部圆角
          },
          label:{ show:false } // 不显示默认标签
        }
      ]
    })
  }
}
</script>
<style scoped>
.budget-container{
  position: relative;
  z-index: 1; 
  max-width:900px;
  margin:auto;
  padding:20px;
  font-family:"Microsoft YaHei",sans-serif;
}
h2{text-align:center;margin-bottom:20px;color:#2c3e50;}
button{margin-right:6px;}
.btn-add{padding:8px 16px;background:#1890ff;color:#fff;border-radius:6px;border:none;cursor:pointer;margin-bottom:15px;transition:0.2s;}
.btn-add:hover{background:#40a9ff;}
.filters{margin-bottom:20px;display:flex;align-items:center;gap:10px;}
.btn-current{padding:5px 12px;background:#52c41a;color:#fff;border:none;border-radius:6px;cursor:pointer;}
.btn-current:hover{background:#73d13d;}
.search-sort{margin-left:auto;display:flex;gap:6px;align-items:center;}
.search-sort input{flex:1;padding:5px;border:1px solid #ccc;border-radius:4px;}
.search-sort select{padding:5px 8px;border-radius:4px;border:1px solid #ccc;}
.btn-reset{padding:5px 12px;background:#1890ff;color:white;border:none;border-radius:4px;cursor:pointer;}
.btn-reset:hover{background:#40a9ff;}
table{width:100%;border-collapse:collapse;margin-bottom:20px;}
thead{background:#f5f5f5;}
td,th{border:1px solid #ddd;padding:8px;text-align:center;}
td.over{color:#c62828;font-weight:bold;}
td.ok{color:#2e7d32;font-weight:bold;}
button.btn-edit{padding:4px 8px;color:#fff;background:#1890ff;border:none;border-radius:4px;cursor:pointer;}
button.btn-edit:hover{background:#40a9ff;}
button.btn-delete{padding:4px 8px;color:#fff;background:#ff4d4f;border:none;border-radius:4px;cursor:pointer;}
button.btn-delete:hover{background:#d9363e;}
.charts-container{display:flex;flex-wrap:wrap;gap:20px;justify-content:flex-start;}
.chart{flex:1 1 300px;height:400px;margin-top:30px;}
.modal-overlay{
  position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.3);
  display:flex;justify-content:center;align-items:center;z-index:999;
}
.modal-content{
  background:#fff;padding:20px 30px;border-radius:12px;max-width:400px;width:90%;
  box-shadow:0 4px 15px rgba(0,0,0,0.2);text-align:center;
}
.form-row{display:flex;align-items:center;gap:10px;margin-bottom:12px;}
.form-row label{width:70px;font-weight:bold;text-align:right;}
.form-row input,.form-row select{flex:1;padding:6px 10px;border-radius:6px;border:1px solid #ccc;}
.modal-actions{display:flex;justify-content:center;gap:20px;margin-top:15px;}
.btn-confirm{padding:6px 16px;background:#1890ff;color:#fff;border:none;border-radius:6px;cursor:pointer;}
.btn-confirm:hover{background:#40a9ff;}
.btn-cancel{padding:6px 16px;background:#aaa;color:#fff;border:none;border-radius:6px;cursor:pointer;}
.btn-cancel:hover{background:#888;}
</style>
