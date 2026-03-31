<template>
  <div class="page-container">
    <h2>售后管理</h2>
    <el-button type="primary" @click="load">刷新</el-button>
    <el-table :data="list" border class="mt" v-loading="loading">
      <el-table-column prop="afterSaleId" label="ID" width="80" />
      <el-table-column prop="orderId" label="订单" width="90" />
      <el-table-column prop="asType" label="类型" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="handle(row, 'approve')">同意</el-button>
          <el-button size="small" type="danger" @click="handle(row, 'reject')">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { afterSaleAPI } from '@/api'

const list = ref([])
const loading = ref(false)

onMounted(() => load())
const load = async () => {
  loading.value = true
  try {
    const res = await afterSaleAPI.adminGetList({ pageNum: 1, pageSize: 50 })
    list.value = res.data?.records || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handle = async (row, action) => {
  try {
    await afterSaleAPI.adminHandle(row.afterSaleId, action, action === 'approve' ? '同意' : '拒绝')
    ElMessage.success('已处理')
    load()
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
.page-container { padding: 20px; }
.mt { margin-top: 16px; }
</style>
