<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-left">
        <span class="title">收银台</span>
      </div>
      <div class="header-right">
        <span class="admin-name">{{ adminName }}</span>
        <el-button text type="primary" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-main><router-view /></el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const adminName = ref('收银员')

onMounted(async () => {
  try {
    const res = await authAPI.getAdminInfo()
    adminName.value = res.data?.username || '收银员'
  } catch {
    // ignore
  }
})

const logout = () => {
  localStorage.removeItem('adminToken')
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; display: flex; flex-direction: column; }
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #409eff;
  color: #fff;
  padding: 0 20px;
  flex-shrink: 0;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.title { font-size: 18px; font-weight: bold; }
.header-right { display: flex; align-items: center; gap: 12px; }
.admin-name { font-size: 14px; }
.el-main { padding: 12px; overflow: auto; background: #f5f5f5; flex: 1; }
</style>
