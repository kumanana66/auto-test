<template>
  <el-button
      :type="type"
      :loading="loading"
      :disabled="disabled"
      @click="handleClick"
      :style="customStyle"
  >
    <slot></slot>
  </el-button>
</template>

<script setup>
import { ref } from 'vue';
import { ElButton } from 'element-plus';

const props = defineProps({
  type: {
    type: String,
    default: 'default',
  },
  loading: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  customStyle: {
    type: Object,
    default: () => ({ width: '100%' }), // 可根据需求调整默认样式
  },
});

const emit = defineEmits(['click']);

const handleClick = (event) => {
  if (!props.disabled) {
    emit('click', event);
  }
};
</script>

<style scoped>
/* 可根据需要自定义按钮样式，这里主要用 Element Plus 自带的样式体系 */
</style>
