<template>
  <div class="category-container">
    <h2>分类管理</h2>
    <div class="header-bar">
      <!-- 打开添加分类弹窗按钮 -->
      <button class="btn-add" @click="openModal">新增分类</button>

      <!-- 添加/修改分类弹窗 -->
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal-content">
          <h3>{{ editingCategory ? '修改分类' : '新增分类' }}</h3>
          <form @submit.prevent="submitCategory">
            <div class="form-row">
              <label>分类名称：</label>
              <input v-model="form.name" placeholder="分类名称" required />
            </div>
            <div class="form-row">
              <label>类型：</label>
              <select v-model="form.type" required>
                <option disabled value="">请选择类型</option>
                <option value="支出">支出</option>
                <option value="收入">收入</option>
              </select>
            </div>
            <div class="modal-actions">
              <button type="submit" class="btn-confirm">{{ editingCategory ? '修改' : '新增' }}</button>
              <button type="button" class="btn-cancel" @click="closeModal">取消</button>
            </div>
          </form>
        </div>
      </div>
    
      <!-- 搜索 + 排序 -->
      <div class="search-sort">
        <input v-model="searchKeyword" placeholder="搜索分类" />
        <button @click="resetSearch" class="btn-reset">重置</button>

        <select v-model="sortField" @change="applySort">
          <option value="default">默认</option>
          <option value="name">名称</option>
          <option value="type">类型</option>
        </select>

        <select v-model="sortAsc" @change="applySort">
          <option :value="true">升序</option>
          <option :value="false">降序</option>
        </select>
      </div>
      
    </div>

    <!-- 分类列表 -->
    <table>
      <thead>
        <tr>
          <th>序号</th>
          <th>名称</th>
          <th>类型</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(category,index) in filteredCategories" :key="category.id">
          <td>{{ index + 1 }}</td>
          <td>{{ category.name }}</td>
          <td>{{ category.type }}</td>
          <td>
            <button @click="editCategory(category)" class="btn-edit">编辑</button>
            <button @click="confirmDelete(category)" class="btn-delete">删除</button>
            <button @click="showUsage(category)" class="btn-usage">使用情况</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 使用情况弹窗 -->
    <div v-if="showUsageModal" class="modal-overlay" @click.self="showUsageModal=false">
      <div class="modal-content">
        <h3>分类 "{{ usageCategory.name }}" 使用情况</h3>

        <!-- 类型切换 -->
        <div class="usage-type-switch">
          <button @click="showUsage(usageCategory, '收入')" :class="{active: usageType==='收入'}">收入</button>
          <button @click="showUsage(usageCategory, '支出')" :class="{active: usageType==='支出'}">支出</button>
          <button @click="showUsage(usageCategory, '预算')" :class="{active: usageType==='预算'}">预算</button>
        </div>

        <div v-if="usageData.length === 0">
          <p>该分类暂无 {{ usageType }} 记录</p>
        </div>

        <table v-else class="usage-table">
          <thead>
            <tr>
              <th>年份</th>
              <th>月份</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in usageData" :key="item.year + '-' + item.month">
              <td>{{ item.year }}</td>
              <td>{{ item.month }}</td>
            </tr>
          </tbody>
        </table>

        <div class="modal-actions">
          <button @click="showUsageModal=false" class="btn-cancel">关闭</button>
        </div>
      </div>
    </div>


    <!-- 自定义删除弹窗 -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal=false">
      <div class="modal-content">
        <h3>确认删除</h3>
        <p>确定要删除分类 "{{ categoryToDelete.name }}" 吗？</p>
        <div class="modal-actions">
          <button @click="deleteCategory" class="btn-confirm">确认</button>
          <button @click="showDeleteModal=false" class="btn-cancel">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "Category",
  data() {
    return {
      categories: [],
      form: { name: "", type: "" },
      editingCategory: null,
      showModal: false,
      searchKeyword: "",
      sortField: "default",
      sortAsc: true,
      showDeleteModal: false,
      categoryToDelete: null,
      showUsageModal: false,
      usageCategory: null,
      usageData: [],
      usageType: '收入',     // 每页显示数量
    };
  },
  computed: {
    filteredCategories() {
      let list = this.categories;
      if (this.searchKeyword.trim()) {
        const kw = this.searchKeyword.trim().toLowerCase();
        list = list.filter(c => c.name.toLowerCase().includes(kw) || c.type.toLowerCase().includes(kw));
      }
      if (this.sortField !== "default") {
        list = list.slice().sort((a, b) => {
          const va = a[this.sortField] || "";
          const vb = b[this.sortField] || "";
          if (va < vb) return this.sortAsc ? -1 : 1;
          if (va > vb) return this.sortAsc ? 1 : -1;
          return 0;
        });
      }
      return list;
    },
    usagePagedData() {
      const start = (this.usagePage - 1) * this.usagePageSize;
      const end = start + this.usagePageSize;
      return this.usageData.slice(start, end);
    },
    usageTotalPages() {
      return Math.ceil(this.usageData.length / this.usagePageSize);
    }
  },
  methods: {
    async fetchCategories() {
      const userId = localStorage.getItem("userId");
      if (!userId) { alert("请先登录"); return; }
      try {
        const res = await axios.get(`http://localhost:8080/api/categories/user/${userId}`);
        this.categories = res.data;
      } catch (err) { console.error("获取分类失败", err); }
    },

    openModal() {
      this.showModal = true;
      this.editingCategory = null;
      this.form = { name: "", type: "" };
    },
    closeModal() { this.showModal = false; },

    editCategory(category) {
      this.editingCategory = category;
      this.form = { name: category.name, type: category.type };
      this.showModal = true;
    },

    async submitCategory() {
      const userId = localStorage.getItem("userId");
      if (!userId) { alert("请先登录"); return; }

      // 防重复
      const exists = this.categories.some(
        c => c.name.trim() === this.form.name.trim() && c.type === this.form.type
      );
      if (!this.editingCategory && exists) { alert("该分类已存在，请勿重复添加"); return; }

      try {
        const payload = { name: this.form.name.trim(), type: this.form.type, user: { id: userId } };
        if (this.editingCategory) {
          await axios.put(`http://localhost:8080/api/categories/${this.editingCategory.id}`, payload);
        } else {
          await axios.post("http://localhost:8080/api/categories", payload);
        }
        this.showModal = false;
        this.fetchCategories();
      } catch (err) { console.error("提交分类失败", err); }
    },

    confirmDelete(category) {
      this.categoryToDelete = category;
      this.showDeleteModal = true;
    },

    async deleteCategory() {
      try {
        await axios.delete(`http://localhost:8080/api/categories/${this.categoryToDelete.id}`);
        this.showDeleteModal = false;
        this.fetchCategories();
      } catch (err) {
        alert("删除失败：" + (err.response?.data || err.message));
      }
    },
    async showUsage(category, type = '收入') {
      this.usageCategory = category;
      this.usageType = type;   // 记录当前显示类型
      try {
        const res = await axios.get(`http://localhost:8080/api/categories/${category.id}/usage`);
        
        // 只保留当前类型的数据
        const filtered = res.data.filter(item => item.source === type);

        // 按年月排序
        this.usageData = filtered.sort((a, b) => {
          if (a.year !== b.year) return a.year - b.year;
          return a.month - b.month;
        });

        this.showUsageModal = true;
      } catch(err) {
        console.error("查询分类使用情况失败", err);
        alert("查询失败：" + err.message);
      }
    },

    resetSearch() { this.searchKeyword = ""; },
    applySort() { /* computed 自动更新 */ }
  },
  mounted() { this.fetchCategories(); },
  toggleUsageSort(field) {
    if (this.usageSortField === field) {
      this.usageSortAsc = !this.usageSortAsc;
    } else {
      this.usageSortField = field;
      this.usageSortAsc = true;
    }
    this.applyUsageSort();
    this.usagePage = 1; // 排序后回到第一页
  },
  usagePrevPage() {
    if (this.usagePage > 1) this.usagePage--;
  },
  usageNextPage() {
    if (this.usagePage < this.usageTotalPages) this.usagePage++;
  }
};
</script>

<style scoped>
.category-container {
  position: relative; /* 相对定位，确保在背景之上 */
  z-index: 1; 
  max-width: 700px;
  margin: auto;
  font-family: "Microsoft YaHei", sans-serif;
  color: #333;
}
h2 { text-align: center; margin-bottom: 20px; color: #2c3e50; }
/* 顶部工具栏布局 */
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
/* 打开弹窗按钮 */
.btn-add {
  padding: 8px 16px;
  background: #1890ff;
  color: #fff;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  margin-bottom: 15px;
  transition: all 0.2s;
}
.btn-add:hover { background: #40a9ff; }

/* 搜索排序（右侧） */
.search-sort {
  display: flex;
  gap: 6px;
  align-items: center;
}
.search-sort input {
  flex: 1;
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
}
.search-sort select {
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
}
.btn-reset {
  padding: 6px 12px;
  background: #52c41a;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.btn-reset:hover { background: #73d13d; }
/* 表格 */
table {
  width: 100%;
  border-collapse: collapse;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  border-radius: 8px;
  overflow: hidden;
}
thead { background: #f5f5f5; }
th, td { padding: 10px; text-align: center; }
tbody tr:nth-child(even) { background: #fafafa; }
tbody tr:hover { background: #f0f9ff; }
.btn-delete, .btn-edit,.btn-usage {
  padding: 4px 10px;
  border-radius: 6px;
  border: none;
  color: #fff;
  cursor: pointer;
  margin: 0 2px;
}
.btn-edit,.btn-usage { background: #1890ff; }
.btn-edit,.btn-usage:hover { background: #40a9ff; }
.btn-delete { background: #ff4d4f; }
.btn-delete:hover { background: #d9363e; }

/* 弹窗 */
.modal-overlay {
  position: fixed; top:0; left:0; width:100%; height:100%;
  background: rgba(0,0,0,0.3);
  display:flex; justify-content:center; align-items:center;
  z-index: 999;
}
.modal-content {
  background: #fff; padding: 20px 30px; border-radius: 12px; max-width: 400px; width: 90%;
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
  text-align: center;
}
.form-row { display:flex; align-items:center; gap:10px; margin-bottom:12px; }
.form-row label { width: 70px; font-weight:bold; text-align:right; }
.form-row input, .form-row select { flex:1; padding:6px 10px; border-radius:6px; border:1px solid #ccc; }

.modal-actions { display:flex; justify-content:center; gap:20px; margin-top:15px; }
.btn-confirm { padding:6px 16px; background:#1890ff; color:#fff; border:none; border-radius:6px; cursor:pointer; }
.btn-confirm:hover { background:#40a9ff; }
.btn-cancel { padding:6px 16px; background:#aaa; color:#fff; border:none; border-radius:6px; cursor:pointer; }
.btn-cancel:hover { background:#888; }
.usage-type-switch {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 10px;
}
.usage-type-switch button {
  padding: 4px 12px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  background: #ccc;
  color: #fff;
}
.usage-type-switch button.active {
  background: #1890ff;
}


</style>
