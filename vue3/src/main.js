// main.js
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import App from './App.vue';
import router from './router';
import './assets/css/style.css';
import { useAuthStore } from './stores/auth';
import { ElMessage, ElLoading } from 'element-plus';
import { setPiniaInstance } from '../src/api/request';

const app = createApp(App);

// 注册 ElementPlus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
}

// 创建并使用 Pinia
const pinia = createPinia();

setPiniaInstance(pinia);

app.use(pinia);

// 初始化认证状态（关键：确保应用启动时同步 Token）
const authStore = useAuthStore(pinia);
authStore.initAuth();

// 其他逻辑（路由、ElementPlus 等）
app.use(router);
app.component(ElLoading.name, ElLoading);
app.provide('alertMessage', {
    showSuccess: (message) => ElMessage.success({ message, duration: 2000 }),
    showError: (message) => ElMessage.error({ message, duration: 2000 }),
    showWarning: (message) => ElMessage.warning({ message, duration: 2000 }),
    showInfo: (message) => ElMessage.info({ message, duration: 2000 }),
});
app.use(ElementPlus);

// 全局时间格式化函数
app.config.globalProperties.$formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

app.mount('#app');