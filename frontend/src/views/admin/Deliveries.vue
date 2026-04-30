<template>
  <div class="page-container">
    <h2>配送管理</h2>

    <el-card style="margin-bottom:16px">
      <el-form :inline="true">
        <el-form-item label="状态">
          <el-select v-model="statusFilter" placeholder="全部" clearable style="width:140px" @change="loadDeliveries">
            <el-option label="待接单" value="PENDING" />
            <el-option label="配送中" value="DELIVERING" />
            <el-option label="已完成" value="DELIVERED" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadDeliveries">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="deliveries" border>
      <el-table-column prop="deliveryId" label="配送单号" width="100" />
      <el-table-column prop="orderId" label="订单号" width="100" />
      <el-table-column prop="receiver" label="收货人" width="100" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="courierName" label="配送员" width="110" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openAssign(row)">分配骑手</el-button>
          <el-button size="small" type="danger" v-if="row.status === 'PENDING'" @click="markDelivering(row)">开始配送</el-button>
          <el-button size="small" type="success" v-if="row.status === 'DELIVERING'" @click="markDelivered(row)">完成配送</el-button>
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

    <!-- 分配骑手对话框 -->
    <el-dialog v-model="assignDialog" title="分配骑手" width="400px">
      <el-form label-width="80px">
        <el-form-item label="配送员">
          <el-select v-model="assignCourierId" placeholder="选择配送员" style="width:100%">
            <el-option v-for="c in couriers" :key="c.courierId" :label="c.courierName + '（' + c.phone + '）'" :value="c.courierId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAPI, courierAPI } from '@/api'

const deliveries = ref([])
const couriers = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')
const assignDialog = ref(false)
const assignCourierId = ref(null)
const currentDelivery = ref(null)

onMounted(() => {
  loadDeliveries()
  loadCouriers()
})

async function loadDeliveries() {
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await adminAPI.getDeliveries(params)
    deliveries.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) { console.error(e) }
}

async function loadCouriers() {
  try {
    const res = await courierAPI.adminGetList({ pageNum: 1, pageSize: 100 })
    couriers.value = res.data?.records || res.data || []
  } catch (e) { console.error(e) }
}

function openAssign(row) {
  currentDelivery.value = row
  assignCourierId.value = null
  assignDialog.value = true
}

async function confirmAssign() {
  if (!assignCourierId.value) { ElMessage.warning('请选择配送员'); return }
  try {
    await adminAPI.assignCourier(currentDelivery.value.deliveryId, assignCourierId.value)
    ElMessage.success('分配成功')
    assignDialog.value = false
    loadDeliveries()
  } catch (e) { console.error(e) }
}

async function markDelivering(row) {
  try {
    await adminAPI.updateDeliveryStatus(row.deliveryId, 'DELIVERING')
    ElMessage.success('已更新为配送中')
    loadDeliveries()
  } catch (e) { console.error(e) }
}

async function markDelivered(row) {
  try {
    await adminAPI.updateDeliveryStatus(row.deliveryId, 'DELIVERED')
    ElMessage.success('已标记为完成')
    loadDeliveries()
  } catch (e) { console.error(e) }
}

function statusLabel(s) {
  return { PENDING: '待接单', DELIVERING: '配送中', DELIVERED: '已完成', FAILED: '失败' }[s] || s
}
function statusType(s) {
  return { PENDING: 'warning', DELIVERING: 'primary', DELIVERED: 'success', FAILED: 'danger' }[s] || 'info'
}
</script>

<style scoped>
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
