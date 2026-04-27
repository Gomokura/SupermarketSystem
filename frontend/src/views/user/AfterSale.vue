<template>
  <div class="page-container">
    <h2>我的售后</h2>
    <el-table :data="asRecords" border style="width: 100%">
      <el-table-column prop="asId" label="售后ID" width="100" />
      <el-table-column prop="orderId" label="关联订单ID" width="120" />
      <el-table-column prop="asType" label="类型">
        <template #default="{ row }">
          <el-tag :type="row.asType === 'REFUND' ? 'warning' : 'info'">{{ row.asType === "REFUND" ? "退款" : "退货退款" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="申请原因" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="refundAmount" label="退款金额" />
      <el-table-column prop="adminRemark" label="管理员备注" />
    </el-table>
  </div>
</template>
<script setup>
import { ref, onMounted } from "vue"
import { afterSalesAPI } from "@/api"
const asRecords = ref([])
const getStatusName = (st) => {
  const map = { PENDING: "待处理", APPROVED: "已同意", REJECTED: "已拒绝", COMPLETED: "已完成" }
  return map[st] || st
}
const getStatusType = (st) => {
  const map = { PENDING: "warning", APPROVED: "success", REJECTED: "danger", COMPLETED: "info" }
  return map[st] || "default"
}
const loadData = async () => {
  try {
    const res = await afterSalesAPI.getMyList()
    asRecords.value = res.data?.records || res.data || []
  } catch (e) {}
}
onMounted(() => {
  loadData()
})
</script>
