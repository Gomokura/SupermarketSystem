<template>
  <el-container class="layout">
    <el-header class="header">
      <span>配送员</span>
      <el-button text type="primary" @click="logout">退出</el-button>
    </el-header>
    <el-main><router-view /></el-main>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const logout = () => {
  localStorage.removeItem('courierToken')
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #67c23a;
  color: #fff;
  padding: 0 20px;
}
</style>
