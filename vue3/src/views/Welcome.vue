<template>
  <section class="welcome">
    <h1>欢迎回来，{{ authStore.username }}</h1>
  </section>
</template>

<script setup>
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
</script>

<style scoped>
.welcome {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  text-align: center;
}

.welcome h1 {
  margin-bottom: 20px;
  color: #333;
}
</style>