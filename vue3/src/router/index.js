import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import { useAuthStore } from '../stores/auth'
import TaskList from '../views/TaskList.vue'
import CrawlerTaskForm from '../views/CrawlerTaskForm.vue';
import Profile from '../views/Profile.vue';
import Welcome from '../views/Welcome.vue';
import OtherSection from '../views/OtherSection.vue';
import TaskDetail  from '../components/TaskDetail.vue'

const routes = [
  {
    path: '/',
    component: Home,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Welcome',
        component: Welcome
      },
      {
        path: 'crawler/tasks',
        name: 'TaskList',
        component: TaskList
      },
      {
        path: 'crawler/tasks/create',
        name: 'TaskCreate',
        component: CrawlerTaskForm
      },
      {
        path: 'profile',
        name: 'Profile',
        component: Profile
      },
      {
        path: 'sales',
        name: 'SalesAnalysis',
        component: () => OtherSection,
        props: { title: '销量分析', message: '此功能待开发...' }
      },
      {
        path: 'inventory',
        name: 'InventoryAnalysis',
        component: () => OtherSection,
        props: { title: '库存分析', message: '此功能待开发...' }
      },
      {
        path: '/crawler/tasks/:id',
        name: 'TaskDetail',
        component: TaskDetail,
        props: true
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫 - 使用store中的认证状态
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore() // 在守卫内部获取store实例

  // 检查目标路由是否需要认证
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router