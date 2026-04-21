<template>
  <el-container class="layout">
    <el-header class="header">
      <span>收银台</span>
      <el-button text type="primary" @click="logout">退出</el-button>
    </el-header>
    <el-main><router-view /></el-main>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #409eff;
  color: #fff;
  padding: 0 20px;
}
</style>
