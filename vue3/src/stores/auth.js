// src/stores/auth.js
import { defineStore } from 'pinia'
import {
    getUserInfo,
    login,
    register,
    sendVerifyCode,
    updateUserInfo,
    uploadAvatar as apiUploadAvatar
} from '../api/auth'
import { setToken, getToken, removeToken } from '../utils/token'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: null, // 用户信息
        token: getToken(),
        isAuthenticated: false
    }),

    // 正确的 getters 位置
    getters: {
        username: (state) => state.user?.username || '游客'
    },

    actions: {
        // 初始化认证状态，在应用启动时调用
        initAuth() {
            this.token = getToken();
            this.isAuthenticated = !!this.token;
        },

        // 登录
        async login(username, password) {
            try {
                const response = await login({ username, password });
                // 根据实际响应结构提取Token
                const token = response?.data?.token || response.token;

                if (!token) {
                    throw new Error("登录失败：未获取到Token");
                }

                // 1. 存储 Token 到 localStorage
                setToken(token);
                // 2. 更新 Pinia 状态
                this.token = token;
                this.isAuthenticated = true;
                // 3. 登录成功后，立即拉取用户信息
                await this.fetchUserInfo();

                return { success: true, message: '登录成功' };
            } catch (error) {
                // 登录失败，清空状态
                this.isAuthenticated = false;
                this.token = '';
                removeToken();

                // 错误处理
                let status = error.response?.status || 500;
                const data = error.response?.data || {};
                let message = data.message || '用户名或密码错误，请稍后再试';

                // 提取详细错误信息
                const errorData = data.data || {};
                const remainingAttempts = errorData.remainingAttempts;
                const lockMinutes = errorData.lockMinutes;

                if (status === 401) {
                    // 处理密码错误，包含剩余尝试次数
                    if (remainingAttempts !== undefined && remainingAttempts >= 0) {
                        message = `密码错误，还可重试${remainingAttempts}次`;
                    }
                    return { success: false, message, remainingAttempts };
                } else if (status === 423) {
                    // 处理账号锁定，包含锁定时间
                    if (lockMinutes !== undefined) {
                        message = `账号已锁定，请${lockMinutes}分钟后再试`;
                    }
                    return { success: false, message, isAccountLocked: true, lockMinutes };
                } else if (status === 500) {
                    return { success: false, message, isSystemError: true };
                }

                return { success: false, message, isSystemError: true };
            }
        },

        // 注册
        async register(username, password, email, verifyCode) {
            try {
                const response = await register({
                    username,
                    password,
                    email,
                    verifyCode
                });
                return { success: true, message: '注册成功' };
            } catch (error) {
                const status = error.response?.status;
                const data = error.response?.data || {};
                // let status = error.response?.status || 500;

                let message = data.message || '注册失败，请稍后再试';
                let errorType = 'system';

                // if (error.response && error.response.data) {
                //     message = error.response.data.message || message;
                // }

                // 根据状态码区分业务错误
                if (status === 409) {
                    return {
                        success: false,
                        message: '用户名已被注册，请重新输入',
                        isUsernameConflict: true
                    };
                } else if(status === 400){
                    return {
                        success: false,
                        message: data.message || '验证码错误或已过期',
                        isValidationError: true
                    };
                } else {
                    return {
                        success: false,
                        message: message,
                        isSystemError: true
                    };
                }
            }
        },

        // 获取用户信息
        async fetchUserInfo() {
            try {
                const res = await getUserInfo();
                console.log('用户信息响应:', res); // 调试：打印原始响应

                // 检查响应格式是否符合预期
                if (res && res.data) {
                    this.user = res.data; // 标准格式：{ data: { username: ... } }
                } else if (res) {
                    this.user = res; // 非标准格式：直接返回用户数据
                } else {
                    throw new Error('用户信息为空');
                }

                this.isAuthenticated = true;
            } catch (error) {
                console.error('fetchUserInfo 失败:', error);
                this.user = null;
                this.isAuthenticated = false;
                removeToken(); // 清空无效 token
            }
        },

        // 更新用户信息方法
        async updateUserProfile(data) {
            try {
                const response = await updateUserInfo(data);
                this.user = response.data; // 更新本地用户信息
                return { success: true, message: '个人信息更新成功' };
            } catch (error) {
                console.error('更新用户信息失败:', error);
                return {
                    success: false,
                    message: error.response?.data?.message || '更新失败，请稍后再试'
                };
            }
        },

        // 上传头像
        async uploadAvatar(file) {
            try {
                const response = await apiUploadAvatar(file);
                const avatarUrl = response.data;

                if (this.user) {
                    this.user.avatar = avatarUrl;
                }
                return { success: true, message: '头像上传成功', avatarUrl }; // avatarUrl 一定不再是 undefined
            } catch (error) {
                console.error('上传头像失败:', error);
                return {
                    success: false,
                    message: error.response?.data?.message || '上传失败，请稍后再试'
                };
            }
        },

        // 发送验证码
        async sendVerifyCode(email) {
            return await sendVerifyCode(email);
        },

        // 退出登录
        logout() {
            removeToken();
            this.token = null;
            this.isAuthenticated = false;
        },

        // 检查登录状态
        checkAuth() {
            this.isAuthenticated = !!this.token;
        }
    }
});