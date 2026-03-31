<template>
  <div class="page-container">
    <h2>评价管理</h2>
    <el-button type="primary" @click="load">刷新</el-button>
    <el-table :data="list" border class="mt" v-loading="loading">
      <el-table-column prop="reviewId" label="ID" width="80" />
      <el-table-column prop="productId" label="商品" width="90" />
      <el-table-column prop="rating" label="评分" width="80" />
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="reply(row)">回复</el-button>
          <el-button size="small" @click="toggle(row)">显隐</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reviewAPI } from '@/api'

const list = ref([])
const loading = ref(false)

onMounted(() => load())
const load = async () => {
  loading.value = true
  try {
    const res = await reviewAPI.adminGetList({ pageNum: 1, pageSize: 50 })
    list.value = res.data?.records || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const reply = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('回复内容', '回复评价', { inputValue: '感谢您的评价！' })
    await reviewAPI.adminReply(row.reviewId, value)
    ElMessage.success('已回复')
    load()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const toggle = async (row) => {
  try {
    const h = row.isHidden === 1 ? 0 : 1
    await reviewAPI.adminToggleHidden(row.reviewId, h)
    ElMessage.success('已更新')
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
