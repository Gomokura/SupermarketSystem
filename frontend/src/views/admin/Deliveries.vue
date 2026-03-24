<template>
  <div class="page-container">
    <h2>配送管理</h2>
    <el-table :data="deliveries" border>
      <el-table-column prop="deliveryId" label="配送单号" width="120" />
      <el-table-column prop="orderId" label="订单号" width="120" />
      <el-table-column prop="receiver" label="收货人" />
      <el-table-column prop="address" label="地址" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag>{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="courierName" label="快递员" width="120" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="updateStatus(row, '配送中')">开始配送</el-button>
          <el-button size="small" type="success" @click="updateStatus(row, '已完成')">完成配送</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadDeliveries"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAPI } from '@/api'

const deliveries = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadDeliveries()
})

const loadDeliveries = async () => {
  try {
    const res = await adminAPI.getDeliveries({ pageNum: pageNum.value, pageSize: pageSize.value })
    deliveries.value = res.data.records || res.data || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const updateStatus = async (row, status) => {
  try {
    await adminAPI.updateDeliveryStatus(row.deliveryId, status)
    ElMessage.success('更新成功')
    loadDeliveries()
  } catch (error) {
    console.error(error)
  }
}
</script>
