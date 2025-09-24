<template>
  <div class="member-container">
    <h2>家庭成员管理</h2>

    <!-- 操作栏 -->
    <div class="toolbar">
      <button @click="openForm()">新增成员</button>

      <input v-model="searchKeyword" placeholder="搜索姓名或角色" />
      <select v-model="sortField" @change="applySort">
        <option value="default">默认</option>
        <option value="name">按姓名</option>
        <option value="role">按角色</option>
        <option value="birthDate">按出生日期</option>
      </select>
      <select v-model="sortAsc" @change="applySort">
        <option :value="true">升序</option>
        <option :value="false">降序</option>
      </select>
      <button @click="resetSearch">重置</button>

      <label>人均收入模式：</label>
      <select v-model="incomeMode" @change="loadPerCapitaIncome">
        <option value="thisYear">今年</option>
        <option value="lastYear">去年</option>
        <option value="last12">近12个月</option>
      </select>

      <span>年人均收入：￥{{ perCapitaIncome.toFixed(2) }}</span>
    </div>

    <!-- 成员卡片列表 -->
    <div class="cards">
      <div v-for="m in filteredAndSortedMembers" :key="m.id" class="card">
        <div class="card-header">
          <strong>{{ m.name }}</strong> ({{ m.gender }})
        </div>
        <div class="card-body">
          <p>角色: {{ m.role }}</p>
          <p>出生日期: {{ m.birthDate }}</p>
          <p>年龄: {{ calculateAge(m.birthDate) }}</p>
          <p>联系方式: {{ m.phone || '-' }}</p>
          <p>备注: {{ m.remark || '-' }}</p>
        </div>
        <div class="card-actions">
          <button class="edit-btn" @click="openForm(m)">编辑</button>
          <button class="delete-btn" @click="deleteMember(m.id)">删除</button>
        </div>
      </div>
      <div v-if="filteredAndSortedMembers.length === 0" class="no-members">暂无成员</div>
    </div>

    <!-- 弹窗表单 -->
    <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
      <div class="modal">
        <h3>{{ form.id ? '修改成员' : '新增成员' }}</h3>
        <form @submit.prevent="submitMember">
          <div class="form-row">
            <label>姓名：</label>
            <input type="text" v-model="form.name" required />
          </div>
          <div class="form-row">
            <label>角色：</label>
            <select v-model="form.role" required>
              <option value="">请选择角色</option>
              <option>父亲</option>
              <option>母亲</option>
              <option>孩子</option>
              <option>其他</option>
            </select>
          </div>
          <div class="form-row">
            <label>性别：</label>
            <select v-model="form.gender" required>
              <option value="">请选择性别</option>
              <option>男</option>
              <option>女</option>
            </select>
          </div>
          <div class="form-row">
            <label>出生日期：</label>
            <input type="date" v-model="form.birthDate" required />
          </div>
          <div class="form-row">
            <label>联系方式：</label>
            <input type="text" v-model="form.phone" />
          </div>
          <div class="form-row">
            <label>备注：</label>
            <input type="text" v-model="form.remark" />
          </div>

          <div class="modal-actions">
            <button type="submit">{{ form.id ? '修改' : '新增' }}</button>
            <button type="button" @click="closeForm">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

const currentUserId = Number(localStorage.getItem("userId") || 0)

const members = ref([])
const form = ref({ id: null, name: '', role: '', gender: '', birthDate: '', phone: '', remark: '' })
const showForm = ref(false)

// 查询与排序
const searchKeyword = ref("")
const sortField = ref("default")
const sortAsc = ref(true)

// 年人均收入
const incomeMode = ref("thisYear")
const perCapitaIncome = ref(0)

// 计算年龄
const calculateAge = (birthDateStr) => {
  if (!birthDateStr) return ''
  const birth = new Date(birthDateStr)
  const today = new Date()
  let age = today.getFullYear() - birth.getFullYear()
  const m = today.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--
  return age
}

// 加载成员列表
const loadMembers = async () => {
  if (!currentUserId) return
  try {
    const res = await axios.get(`http://localhost:8080/api/members/user/${currentUserId}`)
    members.value = res.data.map(m => ({
      ...m,
      birthDate: m.birthDate ? new Date(m.birthDate).toISOString().slice(0,10) : ''
    }))
  } catch (err) {
    console.error('加载成员失败', err)
    alert('加载成员失败')
  }
}

// 加载人均收入
const loadPerCapitaIncome = async () => {
  if (!currentUserId) return
  try {
    const res = await axios.get(`http://localhost:8080/api/members/user/${currentUserId}/perCapitaIncome`, {
      params: { mode: incomeMode.value }
    })
    perCapitaIncome.value = Number(res.data) || 0
  } catch (err) {
    console.error('加载人均收入失败', err)
    perCapitaIncome.value = 0
  }
}

// 弹窗操作
const openForm = (m=null) => {
  if (m) form.value = { ...m, user: { id: currentUserId } }
  else form.value = { id: null, name: '', role: '', gender: '', birthDate: '', phone: '', remark: '' }
  showForm.value = true
}
const closeForm = () => showForm.value = false

// 新增/修改成员
const submitMember = async () => {
  if (!currentUserId) return alert("请先登录")
  try {
    const payload = { ...form.value, user: { id: currentUserId } }
    if (form.value.id) await axios.put(`http://localhost:8080/api/members/${form.value.id}`, payload)
    else await axios.post('http://localhost:8080/api/members', payload)
    closeForm()
    await loadMembers()
    await loadPerCapitaIncome()
  } catch (err) {
    console.error('提交失败', err)
    alert(err.response?.data || '提交失败')
  }
}

// 删除成员
const deleteMember = async (id) => {
  if (!confirm('确定删除该成员吗？')) return
  try {
    await axios.delete(`http://localhost:8080/api/members/${id}`)
    await loadMembers()
    await loadPerCapitaIncome()
  } catch (err) {
    console.error('删除失败', err)
    alert(err.response?.data || '删除失败')
  }
}

// 查询 + 排序计算
const filteredAndSortedMembers = computed(() => {
  let list = [...members.value]
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    list = list.filter(m => m.name.toLowerCase().includes(kw) || m.role.toLowerCase().includes(kw))
  }
  if (sortField.value !== "default") {
    list.sort((a,b) => {
      let va = a[sortField.value], vb = b[sortField.value]
      if (typeof va === 'string') va = va.toLowerCase()
      if (typeof vb === 'string') vb = vb.toLowerCase()
      if (va < vb) return sortAsc.value ? -1 : 1
      if (va > vb) return sortAsc.value ? 1 : -1
      return 0
    })
  }
  return list
})

const resetSearch = () => { searchKeyword.value = "" }
const applySort = () => {}

onMounted(() => {
  loadMembers()
  loadPerCapitaIncome()
})
</script>

<style scoped>
.member-container {
  position: relative; /* 相对定位，确保在背景之上 */
  z-index: 1; 
  max-width: 1000px;
  margin: auto;
  padding: 20px;
  font-family: "Microsoft YaHei", sans-serif;
}
h2 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 20px;
}

/* 工具栏 */
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}
.toolbar input, .toolbar select {
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
}
.toolbar button {
  padding: 6px 16px;
  border-radius: 6px;
  border: none;
  color: #fff;
  background-color: #1890ff;
  cursor: pointer;
}
.toolbar button:hover { background-color: #40a9ff; }
.toolbar span { font-weight: bold; color: #2c3e50; }

/* 卡片列表 */
.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 15px;
}
.card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  padding: 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: transform 0.2s;
}
.card:hover { transform: translateY(-4px); }
.card-header { font-weight: bold; font-size: 16px; margin-bottom: 6px; color: #1890ff; }
.card-body p { margin: 4px 0; font-size: 14px; color: #555; }
.card-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 8px;
}
.card-actions button {
  padding: 4px 8px;
  border-radius: 4px;
  color: #fff;
  border: none;
  cursor: pointer;
}
.card-actions .edit-btn { background-color: #1890ff; }
.card-actions .edit-btn:hover { background-color: #40a9ff; }
.card-actions .delete-btn { background-color: #ff4d4f; }
.card-actions .delete-btn:hover { background-color: #d9363e; }

.no-members { grid-column: 1/-1; text-align: center; color: #999; font-size: 16px; }

/* 弹窗表单 */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0,0,0,0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}
.modal {
  background: #fff;
  border-radius: 10px;
  width: 400px;
  max-width: 90%;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.3);
}
.modal h3 { margin-bottom: 15px; color: #2c3e50; }
.form-row { display: flex; align-items: center; margin-bottom: 12px; }
.form-row label { width: 90px; font-weight: bold; margin-right: 10px; text-align: right; }
.form-row input, .form-row select { flex: 1; padding: 6px 10px; border-radius: 6px; border: 1px solid #ccc; }
.modal-actions { display: flex; justify-content: center; gap: 15px; margin-top: 12px; }
.modal-actions button { padding: 6px 16px; border-radius: 6px; border: none; color: #fff; cursor: pointer; }
.modal-actions button[type="submit"] { background-color: #1890ff; }
.modal-actions button[type="submit"]:hover { background-color: #40a9ff; }
.modal-actions button[type="button"] { background-color: #aaa; }
.modal-actions button[type="button"]:hover { background-color: #888; }
</style>
