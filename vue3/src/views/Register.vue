<template>
  <div class="register-container">
    <div class="register-form">
      <h3 class="register-title">用户注册</h3>
      <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="80px"
      >
        <InputField
            v-model="registerForm.username"
            label="用户名"
            prop="username"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
            :tips="usernameTips"
        />
        <InputField
            v-model="registerForm.email"
            label="邮箱"
            prop="email"
            placeholder="请输入邮箱"
            prefix-icon="el-icon-envelope"
            :tips="emailTips"
        />
        <div class="code-field">
          <InputField
              v-model="registerForm.verifyCode"
              label="验证码"
              prop="verifyCode"
              placeholder="请输入验证码"
              prefix-icon="el-icon-key"
              :tips="verifyCodeTips"
          />
          <Button
              :disabled="isSending || countdown > 0"
              @click="sendVerifyCode"
              :custom-style="{ width: '120px', marginLeft: '10px' }"
          >
            {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
          </Button>
        </div>
        <InputField
            v-model="registerForm.password"
            label="密码"
            prop="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            :tips="passwordTips"
            type="password"
        />
        <InputField
            v-model="registerForm.confirmPassword"
            label="确认密码"
            prop="confirmPassword"
            placeholder="请再次输入密码"
            prefix-icon="el-icon-lock"
            :tips="confirmPasswordTips"
            type="password"
        />
        <el-form-item style="text-align: center;">
          <Button
              type="primary"
              :loading="loading"
              @click="handleRegister"
              :custom-style="{ width: '100%' }"
          >
            注册
          </Button>
        </el-form-item>
      </el-form>
      <div class="register-login-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, inject } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { validateUsername, validatePassword, isWeakPassword, validateEmail } from '../utils/validate';
import InputField from '../components/InputField.vue';
import Button from '../components/Button.vue';
import request from "../api/request.js";

const router = useRouter();
const authStore = useAuthStore();
const registerFormRef = ref(null);
const loading = ref(false);
const isSending = ref(false);
const countdown = ref(0);

// tips提示
const usernameTips = ref([]);
const emailTips = ref([]);
const verifyCodeTips = ref([]);
const passwordTips = ref([]);
const confirmPasswordTips = ref([]);

// 全局消息
const alertMessage = inject('alertMessage', {
  showSuccess: () => {},
  showError: () => {}
});

const registerForm = reactive({
  username: '',
  email: '',
  verifyCode: '',
  password: '',
  confirmPassword: ''
});

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

// 防抖函数（避免频繁请求）
const debounce = (fn, delay) => {
  let timer;
  return (...args) => {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn(...args);
    }, delay);
  };
};

// ========== 自动实时提示 ==========
// 监听邮箱变化，实时验证格式
watch(() => registerForm.email, debounce((val) => {
  emailTips.value = [];
  if (!val) return;

  if (!validateEmail(val)) {
    emailTips.value = [{ type: 'error', content: '请输入正确的邮箱格式' }];
  } else {
    emailTips.value = [{ type: 'success', content: '邮箱格式正确' }];
  }
}, 300));

// 发送验证码
const sendVerifyCode = async () => {
  if (!registerForm.email) {
    alertMessage.showError('请先输入邮箱');
    return;
  }

  if (!validateEmail(registerForm.email)) {
    alertMessage.showError('请输入正确的邮箱格式');
    return;
  }

  isSending.value = true;
  try {
    await authStore.sendVerifyCode(registerForm.email);
    alertMessage.showSuccess('验证码已发送至您的邮箱');
    startCountdown();
  } catch (error) {
    const message = error.response?.data?.message || '发送验证码失败，请稍后重试';
    alertMessage.showError(message);
  } finally {
    isSending.value = false;
  }
};

// 倒计时
const startCountdown = () => {
  countdown.value = 60;
  const timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
    }
  }, 1000);
};

// 监听用户名变化，实时检查是否已存在
watch(() => registerForm.username, debounce(async (val) => {
  usernameTips.value = [];
  if (!val) {
    return;
  } else if (!validateUsername(val)) {
    usernameTips.value = [
      { type: 'error', content: '用户名需6-20位，可使用中文、英文、数字、下划线' }
    ];
  } else {
    // 调用后端接口检查用户名是否存在
    try {
      const response = await fetch(`${apiBaseUrl}/api/auth/check-username?username=${val}`);
      const data = await response.json();
      if (data.exists) {
        usernameTips.value = [
          { type: 'error', content: data.message } // 显示后端返回的“用户名已被注册”等消息
        ];
      } else {
        usernameTips.value = [
          { type: 'success', content: '用户名格式正确且可用' }
        ];
      }
    } catch (error) {
      console.error('检查用户名失败:', error);
      alertMessage.showError('网络异常，请稍后再试');
    }
  }
}, 300));

watch(() => registerForm.password, (val) => {
  passwordTips.value = [];
  if (!val) return;

  // 先检查长度
  if (val.length < 8 || val.length > 20) {
    passwordTips.value.push({
      type: 'error',
      content: '密码长度需8-20位'
    });
    // 长度不满足时，无需继续检查其他条件
    return;
  }

  // 检查空格
  if (val.includes(' ')) {
    passwordTips.value.push({
      type: 'error',
      content: '密码不能包含空格'
    });
    return;
  }

  // 检查特殊字符
  if (/[^a-zA-Z0-9]/.test(val)) {
    passwordTips.value.push({
      type: 'error',
      content: '密码只能包含字母和数字'
    });
    return;
  }

  // 检查字符类型组合
  if (!validatePassword(val)) {
    passwordTips.value.push({
      type: 'error',
      content: '密码需至少包含大小写字母和数字中的两类'
    });
  } else if (isWeakPassword(val)) {
    passwordTips.value.push({
      type: 'warning',
      content: '密码为常见弱密码，请更换'
    });
  } else {
    passwordTips.value.push({
      type: 'success',
      content: '密码强度符合要求'
    });
  }
});

watch(
    () => [registerForm.password, registerForm.confirmPassword],
    ([pwd, confirmPwd]) => {
      if (!pwd && !confirmPwd) {
        confirmPasswordTips.value = [];
      } else if (pwd !== confirmPwd) {
        confirmPasswordTips.value = [
          { type: 'error', content: '两次输入的密码不一致' }
        ];
      } else {
        confirmPasswordTips.value = [
          { type: 'success', content: '密码一致' }
        ];
      }
    }
);

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
  ]
};

// ========== 注册处理 ==========
const handleRegister = async () => {
  if (!registerFormRef.value) return;

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) {
      alertMessage.showError('请完善表单信息');
      return false;
    }

    // 先校验验证码
    loading.value = true;
    try {
      // 1. 校验验证码
      // const resCheck = await request({
      //   url: '/api/auth/verify-email-code',
      //   method: 'post',
      //   data: {
      //     email: registerForm.email,
      //     code: registerForm.verifyCode
      //   }
      // });

      // if (!resCheck.success) {
      //   verifyCodeTips.value = [{ type: 'error', content: resCheck.message || '验证码错误或已过期' }];
      //   alertMessage.showError(resCheck.message || '验证码错误或已过期');
      //   loading.value = false;
      //   return;
      // }

      // 2. 校验通过，再注册
      const res = await authStore.register(
          registerForm.username,
          registerForm.password,
          registerForm.email,
          registerForm.verifyCode
      );

      if (res.success) {
        alertMessage.showSuccess(res.message);
        router.push('/login');
      } else {
        alertMessage.showError(res.message);
      }
    } catch (error) {
      const message = error.response?.data?.message || error.message || '注册失败，请稍后重试';
      alertMessage.showError(message);
    } finally {
      loading.value = false;
    }
  });
};

// 组件销毁时清除定时器
onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer);
  if (debounceTimer) clearTimeout(debounceTimer);
});

</script>

<style scoped>
.register-container {
  width: 100%;
  max-width: 400px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin: 50px auto;
}
.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  font-weight: 500;
}
.register-login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}
.register-login-link a {
  color: #409EFF;
  text-decoration: none;
}
.register-login-link a:hover {
  text-decoration: underline;
}
.password-tips {
  margin-top: 5px;
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}
</style>
