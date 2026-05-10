<template>
  <div class="page-container">
    <div class="hero">
      <div>
        <h2>积分明细</h2>
        <p>查看积分余额、来源和消费记录。</p>
      </div>
      <div class="balance-card">
        <span class="balance-label">当前积分</span>
        <span class="balance-value">{{ currentPoints }}</span>
      </div>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="reason" placeholder="全部类型" clearable @change="reloadLogs">
          <el-option label="订单奖励" value="ORDER_REWARD" />
          <el-option label="订单支付奖励" value="ORDER_PAY" />
          <el-option label="订单抵扣" value="ORDER_DEDUCT" />
          <el-option label="收银抵扣" value="CASHIER_DEDUCT" />
          <el-option label="管理员调整" value="ADMIN_ADJUST" />
          <el-option label="退款返还" value="REFUND_ROLLBACK" />
          <el-option label="注册赠送" value="REGISTER_GIFT" />
        </el-select>
        <el-button @click="reloadLogs">刷新</el-button>
      </div>

      <el-table :data="logs" border v-loading="loading">
        <el-table-column label="变动类型" min-width="160">
          <template #default="{ row }">
            <div class="reason-cell">
              <el-tag :type="reasonTag(row.reason)" size="small">{{ reasonText(row.reason) }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="变动值" width="120" align="center">
          <template #default="{ row }">
            <span :class="row.changeAmount >= 0 ? 'points-up' : 'points-down'">
              {{ row.changeAmount >= 0 ? '+' : '' }}{{ row.changeAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="余额" width="110" align="center" />
        <el-table-column prop="refId" label="关联单号" width="120" align="center">
          <template #default="{ row }">
            {{ row.refId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadLogs"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { pointsAPI } from '@/api'

const currentPoints = ref(0)
const logs = ref([])
const loading = ref(false)
const reason = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const reasonText = (value) => {
  const map = {
    ORDER_REWARD: '订单奖励',
    ORDER_PAY: '订单支付奖励',
    ORDER_DEDUCT: '订单抵扣',
    CASHIER_DEDUCT: '收银抵扣',
    ADMIN_ADJUST: '管理员调整',
    REFUND_ROLLBACK: '退款返还',
    REGISTER_GIFT: '注册赠送'
  }
  return map[value] || value || '-'
}

const reasonTag = (value) => {
  if (value === 'ORDER_DEDUCT' || value === 'CASHIER_DEDUCT') return 'danger'
  if (value === 'ADMIN_ADJUST') return 'warning'
  return 'success'
}

const formatDateTime = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN')
}

const loadPoints = async () => {
  const res = await pointsAPI.getMyPoints()
  currentPoints.value = Number(res.data || 0)
}

const loadLogs = async () => {
  loading.value = true
  try {
    const res = await pointsAPI.getLogs({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      reason: reason.value || undefined
    })
    logs.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const reloadLogs = () => {
  pageNum.value = 1
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

.hero {
  display: flex;
  justify-content: space-between;
  align-items: stretch;
  gap: 16px;
  margin-bottom: 20px;
}

.hero h2 {
  margin: 0 0 6px;
}

.hero p {
  margin: 0;
  color: #909399;
}

.balance-card {
  min-width: 220px;
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  border: 1px solid #fed7aa;
  border-radius: 8px;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.balance-label {
  color: #9a3412;
  font-size: 13px;
}

.balance-value {
  margin-top: 6px;
  font-size: 30px;
  line-height: 1;
  font-weight: 700;
  color: #c2410c;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reason-cell {
  display: flex;
  align-items: center;
}

.points-up {
  color: #16a34a;
  font-weight: 700;
}

.points-down {
  color: #dc2626;
  font-weight: 700;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
