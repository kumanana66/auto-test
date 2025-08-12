<template>
  <el-form-item :label="label" :prop="prop">
    <el-input
        v-model="currentValue"
        :placeholder="placeholder"
        :prefix-icon="prefixIcon"
        @input="handleInput"
        :type="type"
    >
      <!-- 仅当有append内容时渲染 -->
      <template v-if="$slots.append" #append>
        <slot name="append"></slot>
      </template>
    </el-input>
    <template v-if="tips && tips.length > 0">
      <div class="custom-tips">
        <el-tag
            v-for="(tip, index) in tips"
            :key="index"
            :type="tip.type"
            size="small"
        >
          {{ tip.content }}
        </el-tag>
      </div>
    </template>
  </el-form-item>
</template>

<script setup>
import { computed, watch } from 'vue';
import { ElTag } from 'element-plus';

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  label: {
    type: String,
    required: true,
  },
  prop: {
    type: String,
    required: true,
  },
  placeholder: {
    type: String,
    default: '',
  },
  prefixIcon: {
    type: String,
    default: '',
  },
  tips: {
    type: Array,
    default: () => [],
  },
  type: {
    type: String,
    default: 'text'
  }
});

const emit = defineEmits(['update:modelValue']);

// 使用计算属性实现双向绑定，确保与 modelValue 同步
const currentValue = computed({
  get() {
    return props.modelValue;
  },
  set(value) {
    emit('update:modelValue', value);
  }
});

const handleInput = (value) => {
  currentValue.value = value;
};
</script>

<style scoped>
.custom-tips {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

</style>