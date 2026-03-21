<template>
  <div class="page-container">
    <h2>财务报表</h2>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>营收统计</span>
          </template>
          <div class="stat-item">
            <span>总收入：</span>
            <span class="value">￥{{ finance.revenue || 0 }}</span>
          </div>
          <div class="stat-item">
            <span>总成本：</span>
            <span class="value">￥{{ finance.cost || 0 }}</span>
          </div>
          <div class="stat-item">
            <span>利润：</span>
            <span class="value profit">￥{{ ((finance.revenue || 0) - (finance.cost || 0)).toFixed(2) }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminAPI } from '@/api'

const finance = ref({})

onMounted(() => {
  loadFinance()
})

const loadFinance = async () => {
  try {
    const res = await adminAPI.getFinanceData()
    finance.value = res.data || {}
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
  font-size: 16px;
}
.stat-item:last-child {
  border-bottom: none;
}
.value {
  font-weight: bold;
  font-size: 20px;
}
.profit {
  color: #67c23a;
}
</style>
