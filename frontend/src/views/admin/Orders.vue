<template>
  <div class="page-container">
    <h2>订单管理</h2>
    <el-table :data="orders" border>
      <el-table-column prop="orderId" label="订单号" width="100" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column prop="totalAmount" label="金额" width="120" />
      <el-table-column prop="orderStatus" label="状态" width="120">
        <template #default="{ row }">
          <el-tag>{{ row.orderStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="paymentMethod" label="支付方式" width="120" />
      <el-table-column prop="orderTime" label="下单时间" width="180" />
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
import { orderAPI } from '@/api'

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
</script>
