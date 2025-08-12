<template>
  <div class="profile-form">
    <el-form
        ref="formRef"
        :model="localForm"
        :rules="rules"
        label-width="150px"
        class="form-wrapper"
    >
      <el-form-item label="当前密码" prop="oldPassword">
        <el-input
            v-model="localForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
        />
        <div class="password-tips" :class="{show: oldPasswordTips.length > 0}">
          <el-tag
              v-for="(tip, idx) in oldPasswordTips"
              :key="tip.id || idx"
              :type="getTipType(tip.type)"
          >{{ tip.content }}</el-tag>
        </div>
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="localForm.newPassword" type="password" placeholder="请输入新密码" />
        <div class="password-tips" :class="{show: passwordTips.length > 0}">
          <el-tag
              v-for="(tip, idx) in passwordTips"
              :key="tip.id || idx"
              :type="getTipType(tip.type)"
          >{{ tip.content }}</el-tag>
        </div>
      </el-form-item>

      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="localForm.confirmPassword" type="password" placeholder="请确认新密码" />
        <div class="confirm-password-tips" :class="{show: confirmPasswordTips.length > 0}">
          <el-tag
              v-for="(tip, idx) in confirmPasswordTips"
              :key="tip.id || idx"
              :type="getTipType(tip.type)"
          >{{ tip.content }}</el-tag>
        </div>
      </el-form-item>

      <el-form-item label="邮箱">
        <el-input
            :model-value="authStore.user?.email"
            disabled
            class="read-only-input"
        />
        <el-button
            :disabled="isSendingCode || countdown > 0"
            type="primary"
            @click="handleSendCode"
        >
          {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
        </el-button>
      </el-form-item>

      <el-form-item label="验证码" prop="verificationCode">
        <el-input
            v-model="localForm.verificationCode"
            placeholder="请输入验证码"
            style="width: 200px; display: inline-block; margin-right: 10px;"
        />
        <div v-if="emailVerificationTips.length > 0" class="email-verification-tips">
          <el-tag :type="getTipType(emailVerificationTips[0].type)">
            {{ emailVerificationTips[0].content }}
          </el-tag>
        </div>
      </el-form-item>

      <el-form-item class="save-button-item">
        <el-button
            type="primary"
            @click="submitForm"
            :disabled="isSubmitting"
            :loading="isSubmitting"
        >保存修改</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { defineProps, defineEmits } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/auth';
import request from '../api/request';

const props = defineProps({
  form: { type: Object, default: () => ({}) },
  isEmailBound: { type: Boolean, default: false },
  actualOldPassword: { type: String, default: '' }
});
const emits = defineEmits(['submit', 'send-code', 'verify']);

const formRef = ref();
const authStore = useAuthStore();
const isSendingCode = ref(false);
const isVerifyingEmail = ref(false);
const isSubmitting = ref(false);
const countdown = ref(0);
const oldPasswordStatus = ref(null);
const hasValidEmail = ref(false);

// 防止并发校验
const isCheckingOldPassword = ref(false);
const isCheckingNewPassword = ref(false);
const isCheckingConfirmPassword = ref(false);

const emailVerificationTips = ref([]);

let oldPasswordDebounceTimer = null;
let newPasswordDebounceTimer = null;
let confirmPasswordDebounceTimer = null;

const localForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  verificationCode: ''
});

watch(
    () => props.form,
    (form) => Object.assign(localForm, form || {}),
    { deep: true, immediate: true }
);

// 密码提示
const passwordTips = ref([]);
const confirmPasswordTips = ref([]);
const oldPasswordTips = ref([]);

const getTipType = type => ({
  error: 'danger',
  warning: 'warning',
  success: 'success',
  info: 'info'
})[type] || 'info';

// 密码规则
const rules = reactive({
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度至少为8位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码应为6位数字', trigger: 'blur' }
  ]
});

// 实时校验当前密码（防抖）
watch(() => localForm.oldPassword, (val) => {
  if (oldPasswordDebounceTimer) clearTimeout(oldPasswordDebounceTimer);
  oldPasswordDebounceTimer = setTimeout(async () => {
    oldPasswordTips.value = [];
    if (!val) return;
    if (isCheckingOldPassword.value) return;
    isCheckingOldPassword.value = true;
    try {
      const res = await request({
        url: '/api/auth/validate-password',
        method: 'post',
        data: { password: val }
      });
      if (res && res.success && res.data === true) {
        oldPasswordStatus.value = true;
        oldPasswordTips.value.push({ type: 'success', content: '当前密码验证通过' });
      } else {
        oldPasswordStatus.value = false;
        const errorMessage = res.data?.message || '当前密码不正确';
        oldPasswordTips.value.push({ type: 'error', content: errorMessage });
      }
    } catch (error) {
      oldPasswordTips.value.push({ type: 'error', content: '校验密码时发生错误' });
    } finally {
      isCheckingOldPassword.value = false;
    }
  }, 300);
});

// 实时校验新密码
watch(() => localForm.newPassword, (val) => {
  if (newPasswordDebounceTimer) clearTimeout(newPasswordDebounceTimer);
  newPasswordDebounceTimer = setTimeout(async () => {
    passwordTips.value = [];
    if (!val) return;
    if (val.length < 8) {
      passwordTips.value.push({ type: 'error', content: '密码长度需至少8位' });
      return;
    }
    if (isCheckingNewPassword.value) return;
    isCheckingNewPassword.value = true;
    try {
      const res = await request({
        url: '/api/auth/validate-password',
        method: 'post',
        data: { password: val }
      });
      if (res && res.success && res.data === true) {
        passwordTips.value.push({ type: 'error', content: '新密码不能与旧密码一致' });
      } else {
        passwordTips.value.push({ type: 'success', content: '密码验证通过' });
      }
    } catch (error) {
      passwordTips.value.push({ type: 'error', content: '校验密码时发生错误' });
    } finally {
      isCheckingNewPassword.value = false;
    }
  }, 300);
});

// 实时校验确认密码
watch([() => localForm.newPassword, () => localForm.confirmPassword], ([newPwd, confirmPwd]) => {
  if (confirmPasswordDebounceTimer) clearTimeout(confirmPasswordDebounceTimer);
  confirmPasswordDebounceTimer = setTimeout(async () => {
    confirmPasswordTips.value = [];
    if (!confirmPwd) return;
    if (newPwd !== confirmPwd) {
      confirmPasswordTips.value.push({ type: 'error', content: '两次输入的密码不一致，请重新输入' });
      return;
    }
    if (isCheckingConfirmPassword.value) return;
    isCheckingConfirmPassword.value = true;
    try {
      const res = await request({
        url: '/api/auth/validate-password',
        method: 'post',
        data: { password: confirmPwd }
      });
      if (res && res.success && res.data === true) {
        confirmPasswordTips.value.push({ type: 'error', content: '新密码不能与旧密码一致' });
      } else {
        confirmPasswordTips.value.push({ type: 'success', content: '密码验证通过' });
      }
    } catch (error) {
      confirmPasswordTips.value.push({ type: 'error', content: '校验密码时发生错误' });
    } finally {
      isCheckingConfirmPassword.value = false;
    }
  }, 300);
});

// 发送邮箱验证码
const handleSendCode = async () => {
  const email = authStore.user?.email;
  if (!email) {
    ElMessage.error('未获取到邮箱，请刷新页面重试');
    return;
  }
  isSendingCode.value = true;
  try {
    await authStore.sendVerifyCode(email); // 用当前登录用户的邮箱
    ElMessage.success('验证码已发送到您的邮箱');
    startCountdown();
  } catch (error) {
    const message = error.response?.data?.message || '发送验证码失败，请稍后重试';
    ElMessage.error(message);
  } finally {
    isSendingCode.value = false;
  }
};

const startCountdown = () => {
  countdown.value = 60;
  const timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) clearInterval(timer);
  }, 1000);
};

// 表单提交
const submitForm = async () => {
  try {
    await formRef.value.validate();
    if (oldPasswordStatus.value === false) {
      ElMessage.error('当前密码不正确');
      return;
    }
    if (passwordTips.value.some(tip => tip.type === 'error')) {
      ElMessage.error('新密码不符合要求');
      return;
    }
    if (confirmPasswordTips.value.some(tip => tip.type === 'error')) {
      ElMessage.error('确认密码不符合要求');
      return;
    }

    // 验证验证码
      if (!localForm.verificationCode) {
        ElMessage.error('请输入验证码');
        return;
      }

      // 新增：验证验证码是否正确（需要调用后端验证接口）
      try {
        const verifyRes = await request({
          url: '/api/auth/verify-email-code',
          method: 'post',
          data: {
            email: authStore.user?.email,
            code: localForm.verificationCode
          }
        });
        if (!verifyRes.success || !verifyRes.data) {
          ElMessage.error('验证码错误或已过期');
          return;
        }
      } catch (error) {
        ElMessage.error('验证码验证失败，请稍后重试');
        return;
      }

    isSubmitting.value = true;
    const res = await request({
      url: '/api/auth/userinfo',
      method: 'put',
      data: {
        oldPassword: localForm.oldPassword,
        newPassword: localForm.newPassword,
        securityEmail: authStore.user?.email,
        verificationCode: localForm.verificationCode
      }
    });
    if (res && res.success) {
      ElMessage.success('密码修改成功');
      emits('submit', { ...localForm });
    } else {
      ElMessage.error(res.message || '密码修改失败，请稍后再试');
    }
  } catch (err) {
    ElMessage.error('请完善必填项');

  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  return () => {
    if (oldPasswordDebounceTimer) clearTimeout(oldPasswordDebounceTimer);
    if (newPasswordDebounceTimer) clearTimeout(newPasswordDebounceTimer);
    if (confirmPasswordDebounceTimer) clearTimeout(confirmPasswordDebounceTimer);
  };
});
</script>

<style scoped>
.profile-form {
  padding: 20px;
  border-radius: 4px;
  background-color: #f9fafc;
}
.password-tips, .confirm-password-tips, .old-password-tips {
  margin-top: 5px;
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  min-height: 24px;
  height: 0;
  overflow: hidden;
  transition: height 0.3s ease;
}
.password-tips.show, .confirm-password-tips.show, .old-password-tips.show {
  height: auto;
}
.el-button {
  margin-top: 10px;
}
.el-form-item.save-button-item {
  margin-top: -15px;
}
</style>
