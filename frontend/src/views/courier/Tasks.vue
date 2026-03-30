<template>
  <div class="page">
    <h2>配送任务</h2>
    <el-button type="primary" @click="load">刷新</el-button>
    <el-table :data="tasks" border class="mt" v-loading="loading">
      <el-table-column prop="taskId" label="任务ID" width="100" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { courierAPI } from '@/api'

const tasks = ref([])
const loading = ref(false)

onMounted(() => load())
const load = async () => {
  loading.value = true
  try {
    const res = await courierAPI.getTasks()
    tasks.value = res.data?.records || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { padding: 20px; }
.mt { margin-top: 16px; }
</style>
