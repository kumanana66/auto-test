<template>
  <section class="profile-section">
    <h2 class="section-title">个人中心</h2>
    <el-tabs v-model="activeTab" class="profile-tabs">
      <el-tab-pane label="基本信息" name="basic"></el-tab-pane>
      <el-tab-pane label="安全设置" name="security"></el-tab-pane>
    </el-tabs>
    <profile-basic
        v-if="activeTab === 'basic'"
        v-model:avatar="localBasicForm.avatar"
        :basic-form="localBasicForm"
        :avatar="displayAvatar"
        @submit="submitBasicForm"
        @update:avatar="handleAvatarUpdate"
    />
    <profile-security v-if="activeTab === 'security'" :security-form="securityForm" :is-email-bound="isEmailBound" @submit="submitSecurityForm" @send-code="sendVerificationCode" @verify="verifyEmail" />
  </section>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import ProfileBasic from '../components/ProfileBasic.vue';
import ProfileSecurity from '../components/ProfileSecurity.vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/auth';

const props = defineProps({
  username: {
    type: String,
    default: ''
  },
  basicForm: {
    type: Object,
    default: () => ({
      email: '',
      avatar: ''
    })
  },
  securityForm: {
    type: Object,
    default: () => ({})
  },
  isEmailBound: {
    type: Boolean,
    default: false
  }
});

const emits = defineEmits(['update-success']);
const activeTab = ref('basic');
const authStore = useAuthStore();

// 创建深度响应式副本
const localBasicForm = ref({
  ...props.basicForm,
  avatar: authStore.user?.avatar ? authStore.user.avatar : props.basicForm.avatar
});
// 监听props变化并深度合并
watch(() => props.basicForm, (newVal) => {
  // 手动深度克隆
  localBasicForm.value = {
    ...localBasicForm.value,
    ...newVal
  };
}, { deep: true });

// 处理头像更新
const handleAvatarUpdate = (newAvatarUrl) => {
  localBasicForm.value.avatar = newAvatarUrl;
  if (authStore.user) {
    authStore.user.avatar = newAvatarUrl.replace(/\?t=\d+$/, ''); // 存储原始URL
  }
};

const submitBasicForm = async (formData) => {
  const result = await authStore.updateUserProfile(formData);
  if (result.success) {
    ElMessage.success(result.message);
    // 拉取数据库最新数据
    await authStore.fetchUserInfo();
    // 把 Pinia user.avatar 最新值同步到本地表单
    localBasicForm.value.avatar = authStore.user?.avatar
        ? authStore.user.avatar + '?t=' + Date.now()
        : '';
  } else {
    ElMessage.error(result.message);
  }
};

const displayAvatar = computed(() => {
  return authStore.user?.avatar
      ? authStore.user.avatar + '?t=' + Date.now()
      : (props.basicForm.avatar || '');
});

const submitSecurityForm = (formData) => {
  // 安全设置提交逻辑由子组件处理
  emits('update-success');
};

const sendVerificationCode = (email) => {
  // 发送验证码逻辑由子组件处理
};

const verifyEmail = (email, code) => {
  // 验证邮箱逻辑由子组件处理
};
</script>

<style scoped>
.profile-section {
  background-color: #fff;
  border-radius: 8px;
  width: 100%;
  height: 100%;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow-y: auto;
  box-sizing: border-box;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
  margin-top: 0;
}

.profile-tabs {
  margin-bottom: 20px;
}
</style>