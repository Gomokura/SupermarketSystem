<template>
  <div class="page-container">
    <div class="page-header">
      <h2>积分流水</h2>
    </div>

    <el-card class="balance-card">
      <div class="balance-display">
        <div class="balance-label">当前积分</div>
        <div class="balance-value">{{ balance }}</div>
      </div>
    </el-card>

    <el-card class="mt-20">
      <div class="filter-bar">
        <el-select v-model="filterType" placeholder="全部类型" clearable style="width: 160px" @change="loadLogs">
          <el-option label="全部类型" value="" />
          <el-option label="订单获得" value="order" />
          <el-option label="订单扣减" value="order_deduct" />
          <el-option label="积分兑换" value="redeem" />
          <el-option label="管理员调整" value="admin" />
          <el-option label="退款返还" value="refund" />
        </el-select>
        <el-button @click="loadLogs">刷新</el-button>
      </div>

      <el-table :data="logs" border v-loading="loading" class="mt-16">
        <el-table-column label="变动类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.changeType)">{{ getTypeText(row.changeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动积分" width="120">
          <template #default="{ row }">
            <span :class="row.changeAmount > 0 ? 'text-green' : 'text-red'">
              {{ row.changeAmount > 0 ? '+' : '' }}{{ row.changeAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动后余额" width="120">
          <template #default="{ row }">
            <span class="text-bold">{{ row.balanceAfter }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="说明" min-width="200" />
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="mt-16"
        @current-change="loadLogs"
        @size-change="loadLogs"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pointsAPI } from '@/api'

const balance = ref(0)
const logs = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterType = ref('')

onMounted(() => {
  loadBalance()
  loadLogs()
})

const loadBalance = async () => {
  try {
    const res = await pointsAPI.getMyPoints()
    balance.value = res.data?.balance ?? 0
  } catch (error) {
    console.error(error)
  }
}

const loadLogs = async () => {
  loading.value = true
  try {
    const res = await pointsAPI.getLogs({
      page: page.value,
      pageSize: pageSize.value,
      reason: filterType.value || undefined
    })
    logs.value = res.data?.records || res.data || []
    total.value = res.data?.total || logs.value.length
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getTagType = (type) => {
  const map = {
    'order': 'success',
    'order_deduct': 'danger',
    'redeem': 'warning',
    'admin': 'info',
    'refund': 'success'
  }
  return map[type] || 'info'
}

const getTypeText = (type) => {
  const map = {
    'order': '订单获得',
    'order_deduct': '订单扣减',
    'redeem': '积分兑换',
    'admin': '管理员调整',
    'refund': '退款返还'
  }
  return map[type] || type || '其他'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  margin-bottom: 0;
}
.page-header h2 {
  margin: 0;
}
.balance-card {
  margin-top: 20px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border: none;
}
.balance-card :deep(.el-card__body) {
  padding: 30px;
}
.balance-display {
  text-align: center;
  color: #fff;
}
.balance-label {
  font-size: 14px;
  opacity: 0.9;
}
.balance-value {
  font-size: 48px;
  font-weight: bold;
  margin-top: 8px;
}
.mt-20 {
  margin-top: 20px;
}
.mt-16 {
  margin-top: 16px;
}
.text-green {
  color: #67c23a;
  font-weight: bold;
}
.text-red {
  color: #f56c6c;
  font-weight: bold;
}
.text-bold {
  font-weight: bold;
}
.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
