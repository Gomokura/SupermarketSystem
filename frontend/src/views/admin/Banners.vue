<template>
  <div class="page-container">
    <h2>轮播图管理</h2>
    <el-button type="primary" @click="load">刷新</el-button>
    <el-table :data="list" border class="mt" v-loading="loading">
      <el-table-column prop="bannerId" label="ID" width="80" />
      <el-table-column prop="imageUrl" label="图片" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="isActive" label="启用" width="80" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { bannerAPI } from '@/api'

const list = ref([])
const loading = ref(false)

onMounted(() => load())
const load = async () => {
  loading.value = true
  try {
    const res = await bannerAPI.adminGetList()
    list.value = res.data || []
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
