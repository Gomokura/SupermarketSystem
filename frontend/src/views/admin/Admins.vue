<template>
  <div class="page-container">
    <h2>管理员账号</h2>
    <el-button type="primary" @click="load">刷新</el-button>
    <el-table :data="list" border class="mt" v-loading="loading">
      <el-table-column prop="adminId" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="phone" label="手机" />
      <el-table-column prop="role" label="角色" />
      <el-table-column prop="status" label="状态" width="90" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminAPI } from '@/api'

const list = ref([])
const loading = ref(false)

onMounted(() => load())
const load = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getAdmins({ pageNum: 1, pageSize: 50 })
    list.value = res.data?.records || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page-container { padding: 20px; }
.mt { margin-top: 16px; }
</style>
