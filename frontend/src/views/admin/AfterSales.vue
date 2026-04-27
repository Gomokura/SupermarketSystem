<template>
  <div class="page-container">
    <div class="header">
      <h2>售后管理</h2>
    </div>
    <!-- 筛选 -->
    <el-card shadow="never" style="margin-bottom: 20px;">
      <el-form :inline="true">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" style="width: 150px" clearable>
            <el-option label="待处理" value="PENDING" />
            <el-option label="已同意" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <!-- 列表 -->
    <el-table :data="tableData" border style="width: 100%" v-loading="loading">
      <el-table-column prop="asId" label="ID" width="80" />
      <el-table-column prop="orderId" label="关联订单ID" width="120" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="asType" label="类型" width="100">
        <template #default="{ row }">
          {{ row.asType === 'REFUND' ? '退款' : '退货退款' }}
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="申请原因" min-width="150" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="refundAmount" label="退款金额" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="primary" link size="small" @click="openHandleDialog(row)">处理</el-button>
          <el-button v-if="row.status === 'APPROVED'" type="success" link size="small" @click="openRefundDialog(row)">退款</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
        :total="total" layout="total, prev, pager, next" @current-change="loadData" />
    </div>
    <!-- 处理售后 -->
    <el-dialog title="售后处理" v-model="handleDialogVisible" width="400px">
      <el-form>
        <el-form-item label="操作">
          <el-radio-group v-model="handleForm.action">
            <el-radio label="APPROVED">同意</el-radio>
            <el-radio label="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="handleForm.remark" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认</el-button>
      </template>
    </el-dialog>
    <!-- 退款弹窗 -->
    <el-dialog title="确认退款" v-model="refundDialogVisible" width="400px">
      <el-form>
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.refundAmount" :precision="2" :min="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from "vue"
import { afterSalesAPI } from "@/api"
import { ElMessage } from "element-plus"
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  status: ""
})
const getStatusName = (st) => {
  const map = { PENDING: "待处理", APPROVED: "已同意", REJECTED: "已拒绝", COMPLETED: "已完成" }
  return map[st] || st
}
const getStatusType = (st) => {
  const map = { PENDING: "warning", APPROVED: "primary", REJECTED: "danger", COMPLETED: "success" }
  return map[st] || "default"
}
const loadData = async () => {
  loading.value = true
  try {
    const res = await afterSalesAPI.getAdminList(queryParams.value)
    tableData.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}
const handleSearch = () => { queryParams.value.pageNum = 1; loadData() }
const resetSearch = () => { queryParams.value.status = ""; handleSearch() }
// Handle Approvals
const handleDialogVisible = ref(false)
const currentAsId = ref(null)
const handleForm = ref({ action: "APPROVED", remark: "" })
const openHandleDialog = (row) => {
  currentAsId.value = row.asId
  handleForm.value = { action: "APPROVED", remark: "" }
  handleDialogVisible.value = true
}
const submitHandle = async () => {
  try {
    await afterSalesAPI.handleApply(currentAsId.value, handleForm.value.action, handleForm.value.remark)
    ElMessage.success("处理成功")
    handleDialogVisible.value = false
    loadData()
  } catch (e) {}
}
const refundDialogVisible = ref(false)
const refundForm = ref({ refundAmount: 0 })
const openRefundDialog = (row) => {
  currentAsId.value = row.asId
  refundForm.value.refundAmount = row.refundAmount || 0
  refundDialogVisible.value = true
}
const submitRefund = async () => {
  try {
    await afterSalesAPI.handleRefund(currentAsId.value, refundForm.value.refundAmount)
    ElMessage.success("退款成功，售后已结单")
    refundDialogVisible.value = false
    loadData()
  } catch (e) {}
}
onMounted(() => loadData())
</script>
<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
