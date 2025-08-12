<template>
  <div class="login-container">
    <div class="login-form">
      <h3 class="login-title">系统登录</h3>
      <!-- 引入 AlertMessage 用于提示，实际使用注入的方法 -->
<!--      <AlertMessage />-->
      <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-width="80px"
      >
        <!-- 使用自定义 InputField 组件 -->
        <InputField
            v-model="loginForm.username"
            label="用户名"
            prop="username"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
        />
        <InputField
            v-model="loginForm.password"
            label="密码"
            prop="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            type="password"
        />
        <el-form-item style="text-align: center;">
          <!-- 使用自定义 Button 组件 -->
          <Button
              type="primary"
              :loading="loading"
              @click="handleLogin"
              :custom-style="{ width: '100%' }"
          >
            登录
          </Button>
        </el-form-item>
      </el-form>
      <div class="login-register-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted , inject} from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
// 引入自定义组件
import InputField from '../components/InputField.vue';
import Button from '../components/Button.vue';

const router = useRouter();
const authStore = useAuthStore();
const loginFormRef = ref(null);
const loading = ref(false);
const errorMessage = ref('');

// 注入提示方法
const alertMessage = inject('alertMessage', {
  showSuccess: () => {},
  showError: () => {}
});

const loginForm = reactive({
  username: '',
  password: ''
});

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      errorMessage.value = ''; // 清空之前的错误信息

      try {
        // 调用登录，等 login 里的 fetchUserInfo 完成
        const res = await authStore.login(loginForm.username, loginForm.password);
        if (res.success) {
          alertMessage.showSuccess(res.message);

          // 跳转
          router.push('/');

        } else {
          alertMessage.showError(res.message);
        }
      } catch (error) {
        console.error('登录异常:', error);
        alertMessage.showError('用户名或密码错误，请稍后再试');
      } finally {
        loading.value = false;
      }
    }
  });
};

const clearErrorMessage = () => {
  errorMessage.value = '';
};

onMounted(() => {
  // 清除可能存在的残留错误信息
  clearErrorMessage();
});

</script>

<style scoped>
.login-container {
  width: 100%;
  max-width: 400px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin: 50px auto; /* 居中显示，可根据布局调整 */
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  font-weight: 500;
}

.login-register-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.login-register-link a {
  color: #409EFF;
  text-decoration: none;
}

.login-register-link a:hover {
  text-decoration: underline;
}
</style>