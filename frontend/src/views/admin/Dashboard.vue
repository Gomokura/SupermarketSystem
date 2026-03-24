<template>
  <div class="page-container">
    <h2>数据概览</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.userCount || 0 }}</div>
          <div class="stat-label">用户总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.productCount || 0 }}</div>
          <div class="stat-label">商品总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.orderCount || 0 }}</div>
          <div class="stat-label">订单总数</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminAPI } from '@/api'

const stats = ref({})

onMounted(() => {
  loadStatistics()
})

const loadStatistics = async () => {
  try {
    const res = await adminAPI.getStatistics()
    stats.value = res.data || {}
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.stat-card {
  text-align: center;
  margin-bottom: 20px;
}
.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
}
.stat-label {
  margin-top: 10px;
  color: #666;
}
</style>
