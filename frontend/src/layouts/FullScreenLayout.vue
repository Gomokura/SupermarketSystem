<template>
  <div class="fullscreen-layout">
    <el-button class="floating-back" :icon="ArrowLeft" @click="goBack">返回</el-button>
    <slot />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push(userStore.currentRole === 'cashier' ? '/login?role=cashier' : '/admin')
}
</script>

<style scoped>
.fullscreen-layout {
  position: relative;
  min-height: 100vh;
  background: #f5f7fa;
}

.floating-back {
  position: fixed;
  top: 12px;
  left: 12px;
  z-index: 500;
  height: 34px;
  background: rgba(255, 255, 255, .92);
  box-shadow: 0 6px 18px rgba(15, 23, 42, .12);
}
</style>
