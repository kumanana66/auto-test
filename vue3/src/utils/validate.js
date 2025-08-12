// 用户名验证：6-20位，中文、英文、数字、下划线
export function validateUsername(username) {
    const reg = /^[a-zA-Z0-9_\u4e00-\u9fa5]{6,20}$/
    return reg.test(username)
}

// 密码验证：8-20位，至少包含大小写字母和数字中的两类
export function validatePassword(password) {
    if (password.length < 8 || password.length > 20) {
        return false
    }

    // 检查是否包含空格
    if (password.includes(' ')) {
        return false
    }

    // 检查是否包含特殊字符（非字母和数字）
    const hasSpecialChar = /[^a-zA-Z0-9]/.test(password)
    if (hasSpecialChar) {
        return false
    }

    const hasUpperCase = /[A-Z]/.test(password)
    const hasLowerCase = /[a-z]/.test(password)
    const hasDigit = /[0-9]/.test(password)

    return (hasUpperCase + hasLowerCase + hasDigit) >= 2
}

// 检查是否为弱密码
export function isWeakPassword(password) {
    const weakPasswords = ['123456', 'password', '12345678', 'qwerty', '12345', '123456789']
    return weakPasswords.includes(password)
}

export function validateEmail(email) {
    const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return reg.test(email);
}