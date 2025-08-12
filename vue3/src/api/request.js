import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/auth';
// 用于存储 pinia 实例
let pinia = null;

// 创建 axios 实例
const service = axios.create({
    baseURL: 'http://localhost:8080', // 后端 API 基础地址
    timeout: 10000 // 请求超时时间
});

// 新增：用于设置 pinia 实例的函数，供外部调用
export const setPiniaInstance = (instance) => {
    pinia = instance;
};

// 请求拦截器
service.interceptors.request.use(
    config => {
        if (pinia) {
            const authStore = useAuthStore(pinia);
            const token = authStore.token;

            if (token) {
                config.headers['Authorization'] = `Bearer ${token}`;
            }
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器：透传完整错误信息，让业务代码处理
service.interceptors.response.use(
    (response) => {
        return response.data;
    },
    (error) => {
        if (error.response && error.response.status === 401) {
            // 确保传递 pinia 实例
            if (pinia) {
                useAuthStore(pinia).logout();
            } else {
                localStorage.removeItem('auth_token');
            }
            router.push('/login');
        } else if (error.response && error.response.status === 500) {
            ElMessage.error('服务端异常，请稍后重试');
        }
        return Promise.reject(error);
    }
);


export default service;