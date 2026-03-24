<template>
  <div class="page-container">
    <h2>订单详情</h2>
    <el-card v-if="order" class="order-card">
      <template #header>
        <div class="card-header">
          <span>订单号: {{ order.orderId }}</span>
          <el-tag :type="getStatusType(order.orderStatus)">{{ order.orderStatus }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="下单时间">{{ formatDate(order.orderTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ order.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">￥{{ order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">{{ order.orderStatus }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="items-card" v-if="items.length">
      <template #header>
        <span>商品明细</span>
      </template>
      <el-table :data="items" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="price" label="单价" width="120">
          <template #default="{ row }">￥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            <span class="subtotal">￥{{ (row.price * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="actions">
      <el-button @click="goBack">返回列表</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { orderAPI } from '@/api'

const router = useRouter()
const route = useRoute()
const order = ref(null)
const items = ref([])

onMounted(() => {
  loadOrderDetail()
})

const loadOrderDetail = async () => {
  try {
    const res = await orderAPI.getDetail(route.params.id)
    if (res.data) {
      order.value = res.data.order || res.data
      items.value = res.data.items || []
    }
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

const goBack = () => {
  router.push('/orders')
}
</script>

<style scoped>
.order-card, .items-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subtotal {
  color: #f56c6c;
  font-weight: bold;
}

.actions {
  margin-top: 20px;
}
</style>
