<template>
  <div class="page-container">
    <h2>我的订单</h2>
    <el-table :data="orders" border style="width: 100%">
      <el-table-column prop="orderId" label="订单号" width="100" />
      <el-table-column prop="orderTime" label="下单时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.orderTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="订单金额" width="120">
        <template #default="{ row }">
          <span class="amount">￥{{ row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="orderStatus" label="订单状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.orderStatus)">{{ row.orderStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="paymentMethod" label="支付方式" width="120" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="viewDetail(row.orderId)">查看详情</el-button>
          <el-button v-if="canCancel(row.orderStatus)" type="danger" size="small" @click="cancelOrder(row.orderId)">取消订单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadOrders"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderAPI } from '@/api'

const router = useRouter()
const orders = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadOrders()
})

const loadOrders = async () => {
  try {
    const res = await orderAPI.getList({ pageNum: pageNum.value, pageSize: pageSize.value })
    orders.value = res.data.records || res.data || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const map = {
    '待支付': 'warning',
    '待发货': 'info',
    '配送中': 'primary',
    '已完成': 'success',
    '已取消': 'danger'
  }
  return map[status] || ''
}

const canCancel = (status) => {
  return ['待支付', '待发货'].includes(status)
}

const viewDetail = (orderId) => {
  router.push(`/orders/${orderId}`)
}

const cancelOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', { type: 'warning' })
    await orderAPI.cancel(orderId)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}
</script>

<style scoped>
.amount {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
