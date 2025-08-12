// 获取Token
export function getToken() {
    return localStorage.getItem('auth_token')
}

// 设置Token
export const setToken = (token) => {
    localStorage.setItem('auth_token', token);
};

// 移除Token
export function removeToken() {
    localStorage.removeItem('auth_token')
}

// 检查Token是否存在
export function hasToken() {
    return !!getToken()
}