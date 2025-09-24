<template>
  <header class="top-nav">
    <!-- 左侧 LOGO + 系统名 -->
    <div class="nav-left">
      <img src="@/assets/logo.png" alt="logo" class="logo" />
      <h1 class="system-title">家庭财务管理系统</h1>
    </div>

    <!-- 中间导航按钮 -->
    <div class="nav-center">
      <button 
        v-for="item in navItems"
        :key="item.page"
        class="nav-button"
        :class="{ active: isActive(item.page) }"
        @click="goTo(item.page)"
      >
        {{ item.label }}
      </button>
    </div>

    <!-- 右侧 用户信息 + 功能 -->
    <div class="nav-right">
      <span class="time">{{ currentTime }}</span>
      <span class="username">你好，{{ username }}</span>
       <button class="logout-button" @click="logout">退出</button>
    </div>
  </header>
</template>

<script>
export default {
  name: "TopNav",
  data() {
    return {
      username: "",
      currentTime: "",
      timer: null,
      showDropdown: false,
      navItems: [
        { page: "Home", label: "主页" },
        { page: "Income", label: "收入" },
        { page: "Expense", label: "支出" },
        { page: "Category", label: "分类" },
        { page: "Budget", label: "预算" },
        { page: "Member", label: "家庭" },
      ],
    };
  },
  mounted() {
    this.username = localStorage.getItem("username") || "访客";
    this.updateTime();
    this.timer = setInterval(this.updateTime, 1000);
  },
  beforeUnmount() {
    clearInterval(this.timer);
  },
  methods: {
    goTo(page) {
      this.$router.push({ name: page });
      this.showDropdown = false;
    },
    logout() {
      localStorage.removeItem("username");
      localStorage.removeItem("userId");
      this.$router.push({ name: "Register" });
    },
    isActive(page) {
      return this.$route.name === page;
    },
    updateTime() {
      const now = new Date();
      this.currentTime = now.toLocaleString("zh-CN", {
        hour12: false,
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      });
    },
  },
};
</script>

<style scoped>
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(90deg, #e6f2ff, #f9fcff);
  padding: 0.8rem 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  width: 28px;
  height: 28px;
}

.system-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.nav-center {
  flex: 1;
  text-align: center;
}

.nav-button {
  background: none;
  border: none;
  margin: 0 10px;
  font-size: 15px;
  font-weight: bold;
  color: #333;
  cursor: pointer;
  transition: color 0.3s, border-bottom 0.3s;
  padding: 6px 10px;
}

.nav-button:hover {
  color: #4da6ff;
}

.nav-button.active {
  color: #4da6ff;
  border-bottom: 2px solid #4da6ff;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.username {
  font-size: 14px;
  color: #555;
}

.time {
  font-size: 13px;
  color: #666;
  background: #f1f7ff;
  padding: 3px 8px;
  border-radius: 6px;
}
/* 退出按钮 */
.logout-button {
  padding: 6px 14px;
  background-color: #ff6b6b;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  transition: background 0.3s, transform 0.2s;
}

.logout-button:hover {
  background-color: #e65555;
  transform: translateY(-2px);
}
</style>
