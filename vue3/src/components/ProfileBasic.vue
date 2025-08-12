<template>
  <div class="profile-form">
    <el-form
        ref="formRef"
        :model="localForm"
        label-width="120px"
        class="form-wrapper"
    >
      <el-form-item label="头像">
        <div class="avatar-container">
          <el-avatar
              :size="100"
              :src="localForm.avatar || ''"
              class="avatar">
            <!-- 加载中 -->
            <template #loading>
              <el-icon><Loading /></el-icon>
            </template>
            <!-- 头像加载失败时显示默认图标 -->
            <template #error>
              <el-icon><User /></el-icon>
              <div v-if="localForm.avatar" style="color: red;">
                头像加载失败，请检查 URL：{{ localForm.avatar }}
              </div>
            </template>
          </el-avatar>
          <div class="avatar-upload">
            <el-upload
                class="avatar-uploader"
                :http-request="customUpload"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :headers="uploadHeaders"
            >
              <el-button size="small" type="primary">更换头像</el-button>
            </el-upload>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="用户名">
        <el-input
            v-model="authStore.username"
            disabled
            class="read-only-input"
        />
      </el-form-item>

      <el-form-item label="邮箱">
        <el-input
            :model-value="authStore.user?.email"
            disabled
            class="read-only-input"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm">保存修改</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();

const uploadHeaders = computed(() => {
  return authStore.token ? { Authorization: `Bearer ${authStore.token}` } : {};
});

const formRef = ref(null);

// 创建本地表单副本
const localForm = reactive({
  avatar: ''
});

// 初始化本地表单数据
onMounted(() => {
  localForm.avatar = authStore.user?.avatar
      ? authStore.user.avatar + '?t=' + Date.now()
      : '';
});

const submitForm = () => {
  // 如果邮箱不可编辑，则直接更新头像
  if (localForm.avatar !== (authStore.user?.avatar || '')) {
    authStore.updateUserProfile({ avatar: localForm.avatar })
        .then(result => {
          if (result.success) {
            ElMessage.success('头像更新成功');
          } else {
            ElMessage.error(result.message || '更新失败，请稍后再试');
          }
        })
        .catch(() => {
          ElMessage.error('更新失败，请稍后再试');
        });
  } else {
    ElMessage.info('没有需要保存的修改');
  }
};

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg';
  const isPNG = file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG && !isPNG) {
    ElMessage.error('上传头像只能是JPG或PNG格式!');
  }
  if (!isLt2M) {
    ElMessage.error('上传头像大小不能超过2MB!');
  }
  return isJPG || isPNG && isLt2M;
};

const customUpload = async (params) => {
  try {
    const result = await authStore.uploadAvatar(params.file);
    console.log('后端返回avatarUrl:', result.avatarUrl);

    if (result.success && result.avatarUrl) {
      const avatarUrlWithTs = result.avatarUrl + '?t=' + Date.now();
      localForm.avatar = avatarUrlWithTs;
      ElMessage.success('头像更新成功');
    } else {
      ElMessage.error(result.message || '头像上传失败');
    }
  } catch (error) {
    ElMessage.error('上传失败，请稍后再试');
  }
};

// 监听用户信息变化，更新表单
watch(() => authStore.user, (newUser) => {
  if (newUser) {
    localForm.avatar = newUser.avatar ? newUser.avatar + '?t=' + Date.now() : '';
  }
}, { deep: true });
</script>

<style scoped>
.profile-form {
  padding: 20px;
  border-radius: 4px;
  background-color: #f9fafc;
}

.avatar-container {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.avatar {
  margin-right: 20px;
}

.avatar-upload {
  margin-top: 10px;
}

.read-only-input {
  background-color: #f5f7fa;
  cursor: not-allowed;
  border-color: #dcdfe6;
  color: #606266;
}
</style>