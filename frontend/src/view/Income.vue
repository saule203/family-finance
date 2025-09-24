<template>
  <div class="income-page">
    <h2>收入管理</h2>

    <!-- 新增收入按钮 -->
    <button class="btn-add" @click="openModal">➕ 新增收入</button>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3>{{ form.id ? '修改收入' : '新增收入' }}</h3>
        <form @submit.prevent="submitIncome">
          <div class="form-item">
            <label>金额：</label>
            <input type="number" step="0.01" min="0.01" v-model="form.amount" required />
          </div>
          <div class="form-item">
            <label>描述：</label>
            <input type="text" v-model="form.description" />
          </div>
          <div class="form-item">
            <label>日期：</label>
            <input type="date" v-model="form.date" required />
          </div>
          <div class="form-item">
            <label>分类：</label>
            <select v-model="form.categoryId" required>
              <option value="">请选择分类</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="form-item">
            <label>账户：</label>
            <select v-model="form.accountId" required>
              <option value="">请选择账户</option>
              <option v-for="a in accounts" :key="a.id" :value="a.id">{{ a.name }}</option>
            </select>
          </div>
          <div class="modal-actions">
            <button type="submit" class="btn-confirm">{{ form.id ? '修改' : '新增' }}</button>
            <button type="button" class="btn-cancel" @click="closeModal">取消</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div v-if="deleteId !== null" class="modal-overlay">
      <div class="modal-content">
        <h3>⚠ 确认删除</h3>
        <p>确定要删除这条收入记录吗？</p>
        <div class="modal-actions">
          <button class="btn-confirm" @click="confirmDelete">确认</button>
          <button class="btn-cancel" @click="deleteId = null">取消</button>
        </div>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filters-card">
      <div class="filter-left">
        <label>年份：</label>
        <select v-model="selectedYear" @change="fetchIncomes">
          <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
        </select>

        <label>月份：</label>
        <select v-model="selectedMonth" @change="fetchIncomes">
          <option v-for="m in 12" :key="m" :value="m">{{ m }}</option>
        </select>

        <button @click="resetToCurrentMonth">返回当前月</button>
      </div>

      <div class="search-sort-card">
        <div class="search-wrapper">
          <span class="search-icon">🔍</span>
          <input v-model="searchKeyword" placeholder="搜索描述或分类" @input="applyFilters" />
        </div>
        
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

    <!-- 收入表格 -->
    <table class="income-table">
      <thead>
        <tr>
          <th>序号</th>
          <th>日期</th>
          <th>金额</th>
          <th>描述</th>
          <th>分类</th>
          <th>账户</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(income, index) in filteredIncomes" :key="income.id">
          <td>{{ index + 1 }}</td>
          <td>{{ formatDate(income.date) }}</td>
          <td>{{ income.amount }}</td>
          <td>{{ income.description }}</td>
          <td>{{ income.category?.name }}</td>
          <td>{{ income.account?.name }}</td>
          <td>
            <button class="btn-edit" @click="editIncome(income)">编辑</button>
            <button class="btn-delete" @click="openDeleteModal(income.id)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 图表组件 -->
    <Charts 
      :incomeCategories="categoryData"
      :lineData="lineChartData"
    />
  </div>
</template>

<script>
import axios from "axios";
import Charts from "@/components/IncomeCharts.vue";

export default {
  name: "IncomePage",
  components: { Charts },
  data() {
    return {
      userId: null,
      incomes: [],
      categories: [],
      accounts: [],
      showModal: false,
      deleteId: null,
      searchKeyword: '',
      sortField: '',
      sortOrder: 'asc',
      filteredIncomes: [],
      form: { id:null, amount:'', description:'', date:'', categoryId:'', accountId:'' },
      categoryData: [],
      lineChartData: [],
      selectedYear: new Date().getFullYear(),
      selectedMonth: new Date().getMonth() + 1,
      years: Array.from({length:5}, (_,i) => new Date().getFullYear() - i)
    };
  },
  mounted() {
    this.userId = parseInt(localStorage.getItem("userId"),10);
    this.loadData();
    this.fetchIncomes();
  },
  methods: {
    formatDate(date) {
      const d = new Date(date);
      return `${d.getFullYear()}-${d.getMonth()+1}-${d.getDate()}`;
    },
    async loadData() {
      if(!this.userId) return;
      try {
        const [catRes, accRes] = await Promise.all([
          axios.get(`http://localhost:8080/api/categories/user/${this.userId}/type/收入`),
          axios.get("http://localhost:8080/api/accounts")
        ]);
        this.categories = catRes.data;
        this.accounts = accRes.data;
      } catch(err){ console.error(err); }
    },
    openModal() { this.showModal = true; },
    closeModal() { 
      this.showModal = false; 
      this.form = { id:null, amount:'', description:'', date:'', categoryId:'', accountId:'' };
    },
    resetToCurrentMonth() {
      const now = new Date();
      this.selectedYear = now.getFullYear();
      this.selectedMonth = now.getMonth() + 1;
      this.fetchIncomes();
    },
    async fetchIncomes() {
      if(!this.userId) return;
      try {
        const res = await axios.get(`http://localhost:8080/api/incomes/user/${this.userId}`);
        const allIncomes = res.data;
        this.incomes = allIncomes.filter(i => {
          const d = new Date(i.date);
          return d.getFullYear()===this.selectedYear && (d.getMonth()+1)===this.selectedMonth;
        });
        // 饼图数据
        const map = {};
        this.incomes.forEach(i=>{
          const name = i.category?.name||"未分类";
          map[name] = (map[name]||0)+Number(i.amount);
        });
        this.categoryData = Object.entries(map).map(([name,value])=>({name,value}));
        // 折线图数据
        this.lineChartData = Array.from({length:12},(_,idx)=>{
          const month = idx+1;
          const total = allIncomes
            .filter(i=>{
              const d=new Date(i.date);
              return d.getFullYear()===this.selectedYear && (d.getMonth()+1)===month;
            })
            .reduce((s,i)=>s+Number(i.amount),0);
          return { month, value: total };
        });
      } catch(err){ console.error(err); }
      this.applyFilters();
    },
    async submitIncome() {
      try {
        if (!this.form.amount || this.form.amount <= 0) {
          alert("收入金额必须大于0");
          return;
        };
        // 构造 payload，保证字段直接传 id 而不是嵌套对象
        const payload = {
          amount: this.form.amount,
          description: this.form.description,
          date: this.form.date,
          categoryId: Number(this.form.categoryId),
          accountId: Number(this.form.accountId),
          userId: this.userId
        };
        if(this.form.id){
          await axios.put(`http://localhost:8080/api/incomes/${this.form.id}`, payload);
        } else {
          await axios.post(`http://localhost:8080/api/incomes`, payload);
        }
        this.closeModal();
        this.fetchIncomes();
      } catch (err) {
        console.error(err);
        alert("提交失败，请检查输入或后端接口");
      }
    },
    editIncome(income){
      this.form = {
        id: income.id,
        amount: income.amount,
        description: income.description,
        date: income.date?.substring(0,10)||'',
        categoryId: income.category?.id||'',
        accountId: income.account?.id||''
      };
      this.openModal();
    },
    openDeleteModal(id){ this.deleteId = id; },
    async confirmDelete() {
      if(!this.deleteId) return;
      await axios.delete(`http://localhost:8080/api/incomes/${this.deleteId}`);
      this.deleteId = null;
      this.fetchIncomes();
    },
    applyFilters(){
      let list = [...this.incomes];
      if(this.searchKeyword.trim()){
        const kw=this.searchKeyword.trim().toLowerCase();
        list = list.filter(i => i.description.toLowerCase().includes(kw) || i.category?.name?.toLowerCase().includes(kw));
      }
      if(this.sortField){
        list.sort((a,b)=>{
          let va,vb;
          if(this.sortField==='amount'){ va=Number(a.amount); vb=Number(b.amount); }
          else if(this.sortField==='date'){ va=new Date(a.date); vb=new Date(b.date); }
          else if(this.sortField==='category'){ va=a.category?.name||''; vb=b.category?.name||''; }
          if(va<vb) return this.sortOrder==='asc'? -1:1;
          if(va>vb) return this.sortOrder==='asc'? 1:-1;
          return 0;
        });
      }
      this.filteredIncomes = list;
    }
  }
};
</script>

<style scoped>
.income-page {
  position: relative; /* 相对定位，确保在背景之上 */
  z-index: 1; 
  max-width: 1100px;
  margin: 20px auto;
  padding: 20px;
  border-radius:12px;
  box-shadow:0 4px 12px rgba(0,0,0,0.06);
}
h2{ text-align:center; color:#333; margin-bottom:20px; }
.btn-add {
  background: linear-gradient(135deg,#4da6ff,#1a8cff);
  color:#fff;
  padding:10px 20px;
  border:none; border-radius:10px;
  cursor:pointer; font-weight:600;
  transition:0.3s;
}
.btn-add:hover{ opacity:0.85; transform:scale(1.03); }

.modal-overlay{ position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.35); display:flex; justify-content:center; align-items:center; z-index:1000; }
.modal-content{ background:#fff; padding:24px; border-radius:12px; width:400px; max-width:90%; box-shadow:0 8px 20px rgba(0,0,0,0.2); }
.modal-actions{ display:flex; justify-content:flex-end; gap:12px; margin-top:20px; }
.btn-confirm{ background:#2ecc71; color:#fff; padding:8px 16px; border-radius:8px; cursor:pointer; transition:0.2s; }
.btn-confirm:hover{ opacity:0.85; transform:scale(1.05); }
.btn-cancel{ background:#e74c3c; color:#fff; padding:8px 16px; border-radius:8px; cursor:pointer; transition:0.2s; }
.btn-cancel:hover{ opacity:0.85; transform:scale(1.05); }

.filters-card{ display:flex; justify-content:space-between; align-items:center; padding:16px; background:#f0f6ff; border-radius:12px; margin-bottom:24px; flex-wrap:wrap; }
.filter-left,.search-sort-card{ display:flex; gap:12px; align-items:center; flex-wrap:wrap; }
.search-wrapper{ position:relative; display:flex; align-items:center; }
.search-wrapper .search-icon{ position:absolute; left:10px; font-size:16px; color:#888; }
.search-wrapper input{ padding:8px 12px 8px 30px; border-radius:10px; border:1px solid #ccc; min-width:180px; }
.search-sort-card select, .filter-left select{ padding:6px 12px; border-radius:8px; border:1px solid #ccc; cursor:pointer; }
.search-sort-card select:hover, .filter-left select:hover{ border-color:#4da6ff; box-shadow:0 0 3px rgba(77,166,255,0.3); }

.income-table{ width:100%; border-collapse:collapse; margin-top:10px; }
.income-table th{ background:#d6e9ff; padding:12px; font-weight:600; color:#333; text-align:center; }
.income-table td{ padding:10px 8px; border-top:1px solid #e0e0e0; text-align:center; }
.income-table tr:nth-child(even){ background:#f9f9f9; }
.income-table tr:hover{ background:#f1faff; }
.income-table td:nth-child(3){ text-align:right; color:#27ae60; font-weight:600; }
.btn-edit{ background:#4da6ff; color:#fff; padding:4px 10px; border-radius:6px; margin-right:4px; cursor:pointer; }
.btn-delete{ background:#e74c3c; color:#fff; padding:4px 10px; border-radius:6px; cursor:pointer; }
.form-item{ margin-bottom:12px; display:flex; flex-direction:column; }
.form-item label{ margin-bottom:4px; font-weight:500; color:#333; }
.form-item input, .form-item select{ padding:8px 12px; border-radius:8px; border:1px solid #ccc; }
</style>
