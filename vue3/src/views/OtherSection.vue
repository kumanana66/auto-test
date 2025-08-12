<template>
  <section class="other-section">
    <h2>{{ title }}</h2>
    <p>{{ message }}</p>
  </section>
</template>

<script setup>
import { defineProps } from 'vue';

const props = defineProps({
  title: {
    type: String,
    default: '功能开发中'
  },
  message: {
    type: String,
    default: '此功能正在开发中...'
  }
});
</script>

<style scoped>
.other-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
</style>