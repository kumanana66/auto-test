import request from './request'

// 请求拦截器：自动给需要的接口加 Token
request.interceptors.request.use(config => {
    const token = localStorage.getItem('auth_token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});


// 注册
export function register(data) {
    return request({
        url: '/api/auth/register',
        method: 'post',
        data
    })
}

// 登录
export function login(data) {
    return request({
        url: '/api/auth/login',
        method: 'post',
        data
    })
}

//获取当前用户信息
export function getUserInfo() {
    return request({
        url: '/api/auth/userinfo',
        method: 'get'
    });
}

//用户信息更新
export function updateUserInfo(data) {
    return request({
        url: '/api/auth/userinfo',
        method: 'put',
        data
    })
}

// 头像上传
export function uploadAvatar(file) {
    const formData = new FormData();
    formData.append('file', file); // 将文件添加到FormData中

    return request({
        url: '/api/auth/upload/avatar',
        method: 'post',
        data: formData, // 添加data字段
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}

//发送验证码
export function sendVerifyCode(email) {
    return request({
        url: '/api/auth/send-verify-code',
        method: 'post',
        params: { email }
    })
}

// 获取任务列表
export function getCrawlerTasks(params) {
    return request({
        url: '/api/crawler/tasks',
        method: 'get',
        params
    });
}

// 创建任务
export function postCrawlerTask(data) {
    return request({
        url: '/api/crawler/tasks',
        method: 'post',
        data
    });
}

// 保存草稿
export function saveCrawlerTaskDraft(data) {
    return request({
        url: '/api/crawler/tasks/draft',
        method: 'post',
        data
    });
}

// 删除任务
export function deleteCrawlerTask(id) {
    return request({
        url: `/api/crawler/tasks/${id}`,
        method: 'delete'
    });
}

// 执行任务
export function runCrawlerTask(id) {
    return request({
        url: `/api/crawler/tasks/${id}/run`,
        method: 'post'
    });
}

// 暂停任务
export function pauseCrawlerTask(id) {
    return request({
        url: `/api/crawler/tasks/${id}/pause`,
        method: 'post'
    });
}