<template>
  <div class="page-container">
    <h2>积分流水</h2>

    <el-card class="points-card">
      <div class="points-balance">
        <span class="label">当前积分</span>
        <span class="value">{{ currentPoints }}</span>
      </div>
    </el-card>

    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="变动类型">
          <el-select v-model="filterType" placeholder="全部" clearable @change="loadLogs">
            <el-option label="全部" value=""></el-option>
            <el-option label="订单获得" value="ORDER_EARN"></el-option>
            <el-option label="订单抵扣" value="ORDER_DEDUCT"></el-option>
            <el-option label="评价奖励" value="REVIEW_BONUS"></el-option>
            <el-option label="积分兑换" value="EXCHANGE"></el-option>
            <el-option label="管理员调整" value="ADMIN_ADJUST"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="logs" border v-loading="loading">
      <el-table-column prop="reason" label="变动类型" width="120">
        <template #default="{ row }">
          <el-tag :type="getTypeTag(row.type)" size="small">{{ formatReason(row.reason) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="变动积分" width="120">
        <template #default="{ row }">
          <span :class="row.type === 'EARN' || row.type === 'REFUND' ? 'points-add' : 'points-sub'">
            {{ row.type === 'EARN' || row.type === 'REFUND' ? '+' : '-' }}{{ row.points }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="balance" label="变动后余额" width="120" />
      <el-table-column prop="remark" label="备注" min-width="150">
        <template #default="{ row }">
          {{ row.remark || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      class="pagination"
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pointsAPI } from '@/api'

const currentPoints = ref(0)
const logs = ref([])
const loading = ref(false)
const filterType = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getTypeTag = (type) => {
  if (type === 'EARN' || type === 'REFUND') return 'success'
  if (type === 'DEDUCT' || type === 'EXPIRE') return 'danger'
  return 'warning'
}

const formatReason = (reason) => {
  const map = {
    'ORDER_EARN': '订单获得',
    'ORDER_DEDUCT': '订单抵扣',
    'REVIEW_BONUS': '评价奖励',
    'EXCHANGE': '积分兑换',
    'ADMIN_ADJUST': '管理员调整',
    'REFUND': '退款返还'
  }
  return map[reason] || reason || '-'
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const loadPoints = async () => {
  try {
    const res = await pointsAPI.getMyPoints()
    currentPoints.value = res.data?.points || 0
  } catch (error) {
    console.error(error)
  }
}

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterType.value) {
      params.reason = filterType.value
    }
    const res = await pointsAPI.getLogs(params)
    logs.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error(error)
    ElMessage.error('加载积分记录失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadLogs()
}

onMounted(() => {
  loadPoints()
  loadLogs()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.points-card {
  margin-bottom: 20px;
}

.points-balance {
  display: flex;
  align-items: center;
  gap: 16px;
}

.points-balance .label {
  font-size: 16px;
  color: #666;
}

.points-balance .value {
  font-size: 32px;
  font-weight: bold;
  color: #ff4d4f;
}

.filter-card {
  margin-bottom: 20px;
}

.points-add {
  color: #67c23a;
  font-weight: bold;
}

.points-sub {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
