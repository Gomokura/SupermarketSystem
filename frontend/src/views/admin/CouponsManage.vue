<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>优惠券管理</h2>
        <p>支持创建、编辑、启停和批量发券。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增优惠券</el-button>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="status" placeholder="全部状态" clearable @change="reload">
          <el-option label="启用" value="active" />
          <el-option label="暂停" value="paused" />
          <el-option label="过期" value="expired" />
        </el-select>
      </div>

      <el-table :data="coupons" border v-loading="loading">
        <el-table-column prop="couponId" label="ID" width="80" />
        <el-table-column prop="couponName" label="名称" min-width="180" />
        <el-table-column prop="couponType" label="类型" width="120" />
        <el-table-column label="面值/折扣" width="110" align="center">
          <template #default="{ row }">
            {{ row.couponType === 'discount' ? `${row.faceValue} 折` : `￥${row.faceValue}` }}
          </template>
        </el-table-column>
        <el-table-column prop="minAmount" label="门槛" width="100" align="center" />
        <el-table-column label="已发放/总量" width="130" align="center">
          <template #default="{ row }">
            {{ row.issuedCount || 0 }} / {{ row.totalCount || '不限' }}
          </template>
        </el-table-column>
        <el-table-column label="有效期" min-width="200">
          <template #default="{ row }">
            {{ formatDate(row.startTime) }} - {{ formatDate(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="toggleCoupon(row)">{{ row.status === 'active' ? '暂停' : '启用' }}</el-button>
            <el-button size="small" type="success" @click="openBatchIssue(row)">批量发券</el-button>
            <el-button size="small" type="danger" @click="removeCoupon(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadCoupons"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑优惠券' : '新增优惠券'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="优惠券名称" prop="couponName">
          <el-input v-model="form.couponName" />
        </el-form-item>
        <el-form-item label="类型" prop="couponType">
          <el-select v-model="form.couponType">
            <el-option label="满减" value="full_reduction" />
            <el-option label="折扣" value="discount" />
            <el-option label="品类券" value="category" />
          </el-select>
        </el-form-item>
        <el-form-item label="面值/折扣" prop="faceValue">
          <el-input-number v-model="form.faceValue" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="使用门槛">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="总发放量">
          <el-input-number v-model="form.totalCount" :min="0" />
        </el-form-item>
        <el-form-item label="每人限领">
          <el-input-number v-model="form.perLimit" :min="-1" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCoupon">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="issueVisible" title="批量发券" width="520px">
      <el-form label-width="110px">
        <el-form-item label="目标优惠券">
          <div>{{ issueTarget?.couponName || '-' }}</div>
        </el-form-item>
        <el-form-item label="用户 ID 列表">
          <el-input
            v-model="userIdsText"
            type="textarea"
            :rows="6"
            placeholder="例如：1000,1001,1002"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueVisible = false">取消</el-button>
        <el-button type="primary" :loading="issuing" @click="submitBatchIssue">确认发放</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const issuing = ref(false)
const dialogVisible = ref(false)
const issueVisible = ref(false)
const coupons = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const status = ref('')
const editingId = ref(null)
const issueTarget = ref(null)
const userIdsText = ref('')
const formRef = ref()

const form = reactive({
  couponName: '',
  couponType: 'full_reduction',
  faceValue: 10,
  minAmount: 0,
  totalCount: 100,
  perLimit: 1,
  startTime: '',
  endTime: '',
  description: ''
})

const rules = {
  couponName: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  couponType: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }],
  faceValue: [{ required: true, message: '请输入面值或折扣', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const formatDate = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN')
}

const statusTag = (value) => {
  const map = { active: 'success', paused: 'warning', expired: 'info' }
  return map[value] || ''
}

const resetForm = () => {
  editingId.value = null
  Object.assign(form, {
    couponName: '',
    couponType: 'full_reduction',
    faceValue: 10,
    minAmount: 0,
    totalCount: 100,
    perLimit: 1,
    startTime: '',
    endTime: '',
    description: ''
  })
}

const loadCoupons = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getCoupons({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: status.value || undefined
    })
    coupons.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const reload = () => {
  pageNum.value = 1
  loadCoupons()
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.couponId
  Object.assign(form, {
    couponName: row.couponName,
    couponType: row.couponType,
    faceValue: row.faceValue,
    minAmount: row.minAmount,
    totalCount: row.totalCount,
    perLimit: row.perLimit,
    startTime: row.startTime,
    endTime: row.endTime,
    description: row.description
  })
  dialogVisible.value = true
}

const submitCoupon = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await adminAPI.updateCoupon(editingId.value, { ...form })
      ElMessage.success('优惠券已更新')
    } else {
      await adminAPI.createCoupon({ ...form })
      ElMessage.success('优惠券已创建')
    }
    dialogVisible.value = false
    await loadCoupons()
  } finally {
    submitting.value = false
  }
}

const toggleCoupon = async (row) => {
  await adminAPI.toggleCoupon(row.couponId, row.status === 'active' ? 'paused' : 'active')
  ElMessage.success('状态已更新')
  await loadCoupons()
}

const removeCoupon = async (row) => {
  await ElMessageBox.confirm(`确认删除优惠券「${row.couponName}」吗？`, '提示', { type: 'warning' })
  await adminAPI.deleteCoupon(row.couponId)
  ElMessage.success('优惠券已删除')
  await loadCoupons()
}

const openBatchIssue = (row) => {
  issueTarget.value = row
  userIdsText.value = ''
  issueVisible.value = true
}

const submitBatchIssue = async () => {
  const userIds = userIdsText.value
    .split(/[\s,，]+/)
    .map(value => Number(value))
    .filter(value => Number.isInteger(value) && value > 0)

  if (!userIds.length) {
    ElMessage.warning('请至少输入一个用户 ID')
    return
  }

  issuing.value = true
  try {
    await adminAPI.batchIssueCoupons(issueTarget.value.couponId, userIds)
    ElMessage.success('批量发券完成')
    issueVisible.value = false
    await loadCoupons()
  } finally {
    issuing.value = false
  }
}

onMounted(() => {
  loadCoupons()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 6px;
}

.page-header p {
  margin: 0;
  color: #909399;
}

.toolbar {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
