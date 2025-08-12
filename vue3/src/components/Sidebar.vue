<template>
  <aside class="sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
    <div class="header-container">
      <div class="avatar-container" v-if="!isCollapsed">
        <el-avatar :size="36" :src="authStore.user?.avatar || ''">
          <template #error>
            <el-icon><User /></el-icon>
          </template>
        </el-avatar>
      </div>
      <div class="toggle-icon" @click="toggleCollapse">
        <el-icon v-if="!isCollapsed"><Expand /></el-icon>
        <el-icon v-else class="fold-icon"><Fold /></el-icon>
      </div>
    </div>

    <el-menu
        :default-active="currentMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        class="el-menu-vertical"
        @open="handleOpen"
        @close="handleClose"
        @select="handleSelect"
    >
      <el-menu-item index="1">
        <el-icon><User /></el-icon>
        <span v-if="!isCollapsed">个人中心</span>
      </el-menu-item>

      <el-sub-menu index="2">
        <template #title>
          <el-icon><SetUp /></el-icon>
          <span v-if="!isCollapsed">爬虫管理</span>
        </template>
        <el-menu-item index="2-1">任务列表</el-menu-item>
        <el-menu-item index="2-2">任务创建</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="3">
        <template #title>
          <el-icon><DataAnalysis /></el-icon>
          <span v-if="!isCollapsed">商品管理</span>
        </template>
        <el-menu-item index="3-1">销量分析</el-menu-item>
        <el-menu-item index="3-2">库存分析</el-menu-item>
      </el-sub-menu>

    </el-menu>

    <div class="logout-container" v-if="!isCollapsed">
      <el-button
          type="primary"
          size="small"
          class="logout-btn"
          @click="logout"
      >
        <el-icon><SwitchButton /></el-icon>
        <span>退出登录</span>
      </el-button>
    </div>
    <div class="logout-container-collapsed" v-else>
      <el-button
          type="primary"
          size="small"
          class="logout-btn"
          @click="logout"
      >
        <el-icon><SwitchButton /></el-icon>
      </el-button>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue';
import { defineProps, defineEmits } from 'vue';
import { User, SetUp, DataAnalysis, Expand, Fold, SwitchButton } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const props = defineProps({
  currentMenu: {
    type: String,
    required: true
  }
});

const emits = defineEmits(['select', 'open', 'close', 'collapse-change']);
const router = useRouter();
const authStore = useAuthStore();
const isCollapsed = ref(false);

const handleOpen = (key, keyPath) => {
  emits('open', key, keyPath);
};

const handleClose = (key, keyPath) => {
  emits('close', key, keyPath);
};

const handleSelect = (key) => {
  emits('select', key);
};

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
  emits('collapse-change', isCollapsed.value);
};

const logout = () => {
  authStore.logout();
  router.push('/login');
  emits('logout');
};
</script>

<style scoped>
.sidebar {
  width: 220px;
  background-color: #fff;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  height: 100vh;
  transition: width 0.3s ease;
}

.sidebar-collapsed {
  width: 64px;
}

.header-container {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  background-color: #f8f9fa;
  transition: padding 0.3s ease;
}

.sidebar-collapsed .header-container {
  padding: 15px 10px;
}

.avatar-container {
  display: flex;
  align-items: center;
  flex: 1;
}

.toggle-icon {
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.toggle-icon:hover {
  background-color: #ebeef5;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
  border-bottom: none;
}

.logout-container {
  padding: 20px;
  display: flex;
  justify-content: center;
  transition: padding 0.3s ease;
}

.sidebar-collapsed .logout-container {
  padding: 20px 10px;
}

.logout-container-collapsed {
  padding: 20px 10px;
  display: flex;
  justify-content: center;
}

.logout-btn {
  width: 100%;
  font-size: 13px;
  padding: 10px 0;
  transition: width 0.3s ease;
}

.sidebar-collapsed .logout-btn {
  width: 40px;
  padding: 10px 0;
}

.sidebar-collapsed .logout-btn span {
  display: none;
}
.fold-icon {
  margin-left: 5px;
}
</style>