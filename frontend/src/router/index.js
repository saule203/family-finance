import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/view/Home.vue'
import Income from "@/view/Income.vue";
import Expense from "@/view/Expense.vue";
import Budget from "@/view/Budget.vue";
import Category from "@/view/Category.vue";
import Member from "@/view/Member.vue";
import Register from '@/view/LoginRegister.vue'

const routes = [
  {
    path: '/',
    name: 'Register',
    component: Register
  },
  {
    path: '/Home',
    name: 'Home',
    component: Home
  },
  {
    path: '/income',
    name: 'Income',
    component: Income
  },
  {
    path: '/budget',
    name: 'Budget',
    component: Budget
  },
  {
    path: '/expense',
    name: 'Expense',
    component: Expense
  },
  {
    path: '/member',
    name: 'Member',
    component: Member
  },
    {
    path: '/category',
    name: 'Category',
    component: Category
  }
]

const router = createRouter({
history: createWebHistory(),  routes
})

export default router
