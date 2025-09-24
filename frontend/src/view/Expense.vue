<template>
  <div class="expense-container">
    <h2>支出管理</h2>

    <!-- 打开表单按钮 -->
    <button class="btn-add" @click="openModal">
      ➕ 新增支出
    </button>

    <!-- 弹窗表单 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3>{{ form.id ? '修改支出' : '新增支出' }}</h3>

        <form @submit.prevent="submitExpense">
          <div>
            <label>金额：</label>
            <input type="number" step="0.01" min="0.01" v-model="form.amount" required />
          </div>
          <div>
            <label>备注：</label>
            <input type="text" v-model="form.description" />
          </div>
          <div>
            <label>日期：</label>
            <input type="date" v-model="form.date" required />
          </div>
          <div>
            <label>分类：</label>
            <select v-model="form.categoryId" required>
              <option value="">请选择分类</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div>
            <label>账户：</label>
            <select v-model="form.accountId" required>
              <option value="">请选择账户</option>
              <option v-for="a in accounts" :key="a.id" :value="a.id">{{ a.name }}</option>
            </select>
          </div>
          <div class="modal-actions">
            <button type="submit">{{ form.id ? '修改' : '新增' }}</button>
            <button type="button" class="cancel-btn" @click="closeModal">取消</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 删除确认 Modal -->
    <div v-if="deleteId !== null" class="modal-overlay">
      <div class="modal-content">
        <h3>⚠ 确认删除</h3>
        <p>确定要删除这条支出记录吗？</p>
        <div class="modal-actions">
          <button class="btn-confirm" @click="confirmDelete">确认</button>
          <button class="btn-cancel" @click="deleteId = null">取消</button>
        </div>
      </div>
    </div>


    <!-- 年份 + 月份筛选 -->
    <div class="filters">
      <label>年份：</label>
      <select v-model="selectedYear" @change="fetchExpenses">
        <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
      </select>

      <label>月份：</label>
      <select v-model="selectedMonth" @change="fetchExpenses">
        <option v-for="m in 12" :key="m" :value="m">{{ m }}</option>
      </select>

      <button type="button" @click="resetToCurrentMonth">返回当前月</button>

      <!-- 查询 + 排序 -->
      <div class="search-sort">
        <input v-model="searchKeyword" placeholder="搜索备注或分类" @input="applyFilters" />

        <label>排序：</label>
        <select v-model="sortField" @change="applyFilters">
          <option value="">默认</option>
          <option value="amount">按金额</option>
          <option value="date">按日期</option>
          <option value="category">按分类</option>
        </select>

        <select v-model="sortOrder" @change="applyFilters">
          <option value="asc">升序</option>
          <option value="desc">降序</option>
        </select>
      </div>
    </div>

    <!-- 预算提醒 -->
    <div v-if="budgetMessage" :class="['budget-alert', overBudget ? 'over' : 'ok']">
      {{ budgetMessage }}
    </div>

    <!-- 支出列表 -->
    <div class="expense-table-container">
      <table class="expense-table">
        <thead>
          <tr>
            <th>序号</th><th>日期</th><th>金额</th><th>备注</th><th>分类</th><th>账户</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(e,index) in  filteredExpenses" :key="e.id">
            <td>{{ index + 1 }}</td>
            <td>{{ formatDate(e.date) }}</td>
            <td>{{ e.amount }}</td>
            <td>{{ e.description }}</td>
            <td>{{ e.category?.name }}</td>
            <td>{{ e.account?.name }}</td>
            <td>
              <button @click="editExpense(e)">编辑</button>
              <button @click="openDeleteModal(e.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <!-- 图表 -->
    <Charts
      :incomeCategories="[]" 
      :expenseCategories="categoryData.length ? categoryData : [{name:'暂无',value:1}]"
      :lineData="[]" 
      :dailyExpenseData="dailyChartData.length ? dailyChartData : [{day:1,value:0}]"
    />
  </div>
</template>


<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";
import Charts from "@/components/ExpenseCharts.vue";

// -------------------- 响应式数据 --------------------
const expenses = ref([]);
const categories = ref([]);
const accounts = ref([]);
const budgets = ref([]);

const form = ref({ id: null, amount: '', description: '', date: '', categoryId: '', accountId: '' });
const budgetMessage = ref('');
const overBudget = ref(false);
const categoryData = ref([]);
const dailyChartData = ref([]);
const deleteId = ref(null);

const userId = ref(localStorage.getItem("userId"));

// 添加弹窗
const showModal = ref(false);
const openModal = () => (showModal.value = true);
const closeModal = () => {
  showModal.value = false;
  resetForm();
};

// 年份+月份筛选
const selectedYear = ref(new Date().getFullYear());
const selectedMonth = ref(new Date().getMonth() + 1);
const years = ref([]);

// 搜索 + 排序相关
const searchKeyword = ref('');
const sortField = ref('');
const sortOrder = ref('asc');
const filteredExpenses = ref([]);

// -------------------- 页面初始化 --------------------
onMounted(async () => {
  initYears();
  await loadCategories();
  await loadAccounts();
  await loadBudgets();
  await fetchExpenses();
});

// 初始化年份选择列表（最近5年）
const initYears = () => {
  const now = new Date().getFullYear();
  years.value = [];
  for (let y = now; y >= now - 5; y--) years.value.push(y);
}

// -------------------- 数据加载 --------------------
const loadCategories = async () => {
  const res = await axios.get(`http://localhost:8080/api/categories/user/${userId.value}/type/支出`);
  categories.value = res.data;
}

const loadAccounts = async () => {
  const res = await axios.get('http://localhost:8080/api/accounts');
  accounts.value = res.data;
}

const loadBudgets = async () => {
  if (!userId.value) return;
  const res = await axios.get(`http://localhost:8080/api/budgets/user/${userId.value}`);
  budgets.value = res.data;
}

// -------------------- 支出数据处理 --------------------
const fetchExpenses = async () => {
  if (!userId.value) return;
  const res = await axios.get(`http://localhost:8080/api/expenses/user/${userId.value}`);
  let list = res.data;

  // 按年份 + 月份筛选
  const y = String(selectedYear.value);
  const m = String(selectedMonth.value).padStart(2,'0');
  list = list.filter(e => e.date?.startsWith(`${y}-${m}`));

  expenses.value = list;

  // 支出分类饼图
  const map = {};
  expenses.value.forEach(e => {
    const cname = e.category?.name || "未分类";
    map[cname] = (map[cname] || 0) + Number(e.amount);
  });
  const arr = Object.entries(map).map(([name,value]) => ({ name, value }));
  categoryData.value = arr.length ? arr : [{ name:'暂无', value:1 }];

  // 每日柱状图
  const daysInMonth = new Date(selectedYear.value, selectedMonth.value, 0).getDate();
  const dailyArr = Array.from({length: daysInMonth}, (_, i) => ({ day: i+1, value: 0 }));
  expenses.value.forEach(e => {
    const day = new Date(e.date).getDate();
    dailyArr[day-1].value += Number(e.amount);
  });
  dailyChartData.value = dailyArr.length ? dailyArr : [{ day:1, value:0 }];

  checkMonthlyBudget();
  applyFilters();
}

// 返回当前月
const resetToCurrentMonth = () => {
  const now = new Date();
  selectedYear.value = now.getFullYear();
  selectedMonth.value = now.getMonth() + 1;
  fetchExpenses();
}

const applyFilters = () => {
  let list = [...expenses.value];

  // 搜索备注或分类
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase();
    list = list.filter(e =>
      e.description?.toLowerCase().includes(keyword) ||
      e.category?.name?.toLowerCase().includes(keyword)
    );
  }

  // 排序
  if (sortField.value) {
    list.sort((a, b) => {
      let valA, valB;
      if (sortField.value === 'amount') {
        valA = Number(a.amount); valB = Number(b.amount);
      } else if (sortField.value === 'date') {
        valA = new Date(a.date); valB = new Date(b.date);
      } else if (sortField.value === 'category') {
        valA = a.category?.name || ''; valB = b.category?.name || '';
      }
      if (valA < valB) return sortOrder.value === 'asc' ? -1 : 1;
      if (valA > valB) return sortOrder.value === 'asc' ? 1 : -1;
      return 0;
    });
  }

  filteredExpenses.value = list;
}

// -------------------- 表单操作 --------------------
const submitExpense = async () => {
  try {
    if (!form.value.amount || form.value.amount <= 0) {
      alert("支出金额必须大于 0");
      return;
    };
    const payload = {
      amount: form.value.amount,
      description: form.value.description,
      date: form.value.date,
      category: { id: form.value.categoryId },
      account: { id: form.value.accountId },
      user: { id: userId.value }
    };
    if (form.value.id) {
      await axios.put(`http://localhost:8080/api/expenses/${form.value.id}`, payload);
    } else {
      await axios.post('http://localhost:8080/api/expenses', payload);
    }
    await fetchExpenses();
    resetForm();
    showModal.value = false;
    window.dispatchEvent(new Event('refresh-budget'));
  } catch (err) {
    console.error(err);
    alert('提交失败');
  }
}

const editExpense = (e) => {
  form.value = {
    id: e.id,
    amount: e.amount,
    description: e.description,
    date: e.date?.substring(0,10) || '',
    categoryId: e.category?.id || '',
    accountId: e.account?.id || ''
  };
  openModal();
}

// 打开删除确认弹窗
const openDeleteModal = (id) => {
  deleteId.value = id;
};

// 确认删除
const confirmDelete = async () => {
  if (deleteId.value) {
    await axios.delete(`http://localhost:8080/api/expenses/${deleteId.value}`);
    await fetchExpenses();
    window.dispatchEvent(new Event("refresh-budget"));
    deleteId.value = null;
  }
};

const resetForm = () => {
  form.value = { id: null, amount: '', description: '', date: '', categoryId: '', accountId: '' };
}

const formatDate = dateTime => dateTime ? dateTime.split('T')[0] : '';

// -------------------- 预算提醒 --------------------
const checkMonthlyBudget = () => {
  budgetMessage.value = '';
  overBudget.value = false;
  const y = selectedYear.value;
  const m = selectedMonth.value;
  const monthlyBudgets = budgets.value.filter(b => b.year === y && b.month === m);

  if (monthlyBudgets.length === 0) {
    budgetMessage.value = `⚠ ${y}年${m}月未设置预算，可自由支出`;
    return;
  }

  monthlyBudgets.forEach(b => {
    const spent = expenses.value
      .filter(e => e.category?.id === b.category?.id)
      .reduce((sum,e) => sum + Number(e.amount), 0);
    if (spent > b.amount) {
      overBudget.value = true;
      budgetMessage.value += `⚠ ${b.year}年${b.month}月 [${b.category?.name}] 已超预算！ `;
    }
  });
}
</script>

<style scoped>
.expense-container {
  position: relative; /* 相对定位，确保在背景之上 */
  z-index: 1; 
  max-width: 1100px;
  margin: auto;
  padding: 20px;
  font-family: "Microsoft YaHei", sans-serif;
  color: #333;
}

/* 标题 */
h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #2c3e50;
}

/* 卡片风格 */
form, .filters, table, .budget-alert {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

/* 表单 */
form div {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
form label {
  width: 70px;
  font-weight: bold;
  color: #555;
}
form input, form select {
  flex: 1;
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  outline: none;
  transition: all 0.2s;
}
form input:focus, form select:focus {
  border-color: #3498db;
  box-shadow: 0 0 4px rgba(52,152,219,0.3);
}

/* 表单按钮 */
form button {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
form button[type="submit"] {
  background: #2ecc71;
  color: #fff;
}
form button[type="button"] {
  background: #95a5a6;
  color: #fff;
}
form button:hover {
  opacity: 0.9;
}

/* 筛选区 */
.filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}
.filters select, .filters input {
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
}

/* 搜索 + 排序 */
.search-sort {
  margin-left: auto; /* 靠右 */
  display: flex;
  align-items: center;
  gap: 10px;
}
.search-sort input, .search-sort select {
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
}

/* 预算提醒 */
.budget-alert {
  font-weight: bold;
  border-left: 6px solid;
}
.budget-alert.ok {
  background-color: #eafaf1;
  color: #27ae60;
  border-color: #27ae60;
}
.budget-alert.over {
  background-color: #fdecea;
  color: #c0392b;
  border-color: #c0392b;
}

/* 表格整体容器 */
.expense-table-container {
  overflow-x: auto; /* 支持小屏水平滚动 */
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  background: #fff;
  padding: 12px;
  margin-bottom: 20px;
}
.expense-table{ width:100%; border-collapse:collapse; margin-top:10px; }
.expense-table th{ background:#d6e9ff; padding:12px; font-weight:600; color:#333; text-align:center; }
.expense-table td{ padding:10px 8px; border-top:1px solid #e0e0e0; text-align:center; }
.expense-table tr:nth-child(even){ background:#f9f9f9; }
.expense-table tr:hover{ background:#f1faff; }
.expense-table td:nth-child(3){ text-align:right; color:#27ae60; font-weight:600; }
/* 操作按钮 */
td button {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  transition: all 0.2s ease;
  border: none;
  cursor: pointer;
}
td button:first-child {
  background: #3498db;
  color: #fff;
}
td button:last-child {
  background: #e74c3c;
  color: #fff;
}
td button:hover {
  transform: scale(1.05);
  opacity: 0.9;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.btn-add {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  font-size: 15px;
  padding: 10px 18px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}
.btn-add:hover {
  background: linear-gradient(135deg, #2980b9, #1f6391);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

/* Modal 通用样式 */
.modal-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0,0,0,0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-content {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  width: 360px;
  max-width: 90%;
  box-shadow: 0 6px 18px rgba(0,0,0,0.2);
}
.modal-content h3 {
  margin-bottom: 12px;
  color: #2c3e50;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}
.btn-confirm {
  background: #2ecc71;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
}
.btn-confirm:hover {
  background: #27ae60;
}
.btn-cancel {
  background: #95a5a6;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
}
.btn-cancel:hover {
  background: #7f8c8d;
}
</style>