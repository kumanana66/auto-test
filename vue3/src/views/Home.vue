<template>
  <div class="home-container">
    <!-- 加载态 -->
    <div v-if="loading" class="loading-container">
      <el-loading :fullscreen="true" :lock="true" text="加载中..." spinner="el-icon-loading"></el-loading>
    </div>
    <div v-else class="main-layout">
      <!-- 左侧菜单 -->
      <Sidebar :current-menu="currentMenu" @select="handleSelect" @open="handleOpen" @close="handleClose" />

      <!-- 右侧内容区 -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
// 导入组件
import { ref, computed, onMounted, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { getUserInfo } from '../api/auth';
import { ElMessage } from 'element-plus';
import Sidebar from '../components/Sidebar.vue';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const username = ref('');
const loading = ref(true);

// 根据当前路由自动计算currentMenu - 修正任务创建页面的映射
const currentMenu = computed(() => {
  const path = route.path;
  if (path.includes('/profile')) return '1';
  if (path.includes('/crawler/tasks') && !path.includes('create')) return '2-1'; // 任务列表
  if (path.includes('/crawler/tasks/create')) return '2-2'; // 任务创建
  if (path.includes('/crawler/analysis')) return '2-3'; // 竞品分析
  if (path.includes('/sales')) return '3-1';
  if (path.includes('/inventory')) return '3-2';
  return ''; // 默认欢迎页
});

// 表单数据
const basicForm = reactive({
  name: '',
  avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
  phone: '',
  email: ''
});

const securityForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  securityEmail: '',
  verificationCode: ''
});

const crawlerForm = reactive({
  processName: '',
  asinList: '',
  requiredInfo: [],
  timeCycle: 'daily'
});

// 模拟是否绑定邮箱
const isEmailBound = ref(false);

onMounted(async () => {
  await fetchUserInfo();
});

const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo();
    username.value = res.username || '管理员';

    // 填充基本信息
    basicForm.name = res.name || '';
    basicForm.phone = res.phone || '';
    basicForm.email = res.email || '';
    basicForm.avatar = res.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

    // 检查安全邮箱是否已绑定
    isEmailBound.value = !!res.securityEmail;
    securityForm.securityEmail = res.securityEmail || '';
  } catch (err) {
    console.error('获取用户信息失败:', err);
    if (err.response?.status === 401) {
      authStore.logout();
      router.push('/login');
    }
  } finally {
    loading.value = false;
  }
};

// 菜单事件
const handleOpen = (key, keyPath) => console.log('菜单展开:', key, keyPath);
const handleClose = (key, keyPath) => console.log('菜单收起:', key, keyPath);
// 菜单选择事件 - 更新路由
const handleSelect = (key) => {
  switch (key) {
    case '1':
      router.push('/profile');
      break;
    case '2-1':
      router.push('/crawler/tasks');
      break;
    case '2-2':
      router.push('/crawler/tasks/create');
      break;
    case '3-1':
      router.push('/sales');
      break;
    case '3-2':
      router.push('/inventory');
      break;
    default:
      router.push('/');
  }
};

// 爬虫表单事件
const handleCrawlerSubmit = (data) => {
  console.log('爬虫任务提交数据:', data);
  ElMessage.success('表单提交成功');
};

const handleCrawlerSave = (data) => {
  console.log('爬虫任务保存草稿:', data);
  ElMessage.info('已保存为草稿');
};

const logout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
/* 全局布局重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.home-container {
  width: 100vw;
  height: 100vh;
  background-color: #f5f7fa;
  overflow: hidden;
}

/* 加载态覆盖 */
.loading-container {
  position: fixed;
  z-index: 9999;
}

/* 主布局：左右弹性布局 */
.main-layout {
  display: flex;
  width: 100%;
  height: 100%;
}

/* 右侧内容区 */
.content {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}
</style>