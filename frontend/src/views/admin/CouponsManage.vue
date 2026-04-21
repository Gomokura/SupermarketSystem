<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <div class="page-title">优惠券管理</div>
        <div class="page-desc">维护优惠券列表，并支持按用户批量发券。</div>
      </div>
      <div class="toolbar-actions">
        <el-select v-model="statusFilter" clearable placeholder="筛选状态" style="width: 150px" @change="load">
          <el-option label="全部状态" value="" />
          <el-option label="启用" value="active" />
          <el-option label="停用" value="inactive" />
        </el-select>
        <el-button type="primary" @click="openCreate">新增优惠券</el-button>
      </div>
    </div>

    <el-card shadow="never" class="panel-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="优惠券列表" name="list">
          <el-table :data="list" border stripe v-loading="loading">
            <el-table-column prop="couponId" label="ID" width="80" />
            <el-table-column prop="couponName" label="名称" min-width="160" />
            <el-table-column prop="couponType" label="类型" width="120" />
            <el-table-column prop="faceValue" label="面值/折扣" width="110" />
            <el-table-column prop="minAmount" label="门槛" width="100" />
            <el-table-column label="库存" width="150">
              <template #default="{ row }">{{ row.issuedCount || 0 }} / {{ row.totalCount || '不限' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" />
            <el-table-column label="操作" width="300" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="openEdit(row)">编辑</el-button>
                <el-button size="small" @click="toggle(row)">{{ row.status === 'active' ? '暂停' : '启用' }}</el-button>
                <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
                <el-button size="small" type="warning" @click="prefillIssue(row)">批量发券</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" background layout="total, prev, pager, next" :total="total" @current-change="load" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="批量发券" name="issue">
          <el-form ref="issueFormRef" :model="issueForm" :rules="issueRules" label-width="110px" class="issue-form">
            <el-form-item label="优惠券ID" prop="couponId">
              <el-input-number v-model="issueForm.couponId" :min="1" />
            </el-form-item>
            <el-form-item label="用户ID列表" prop="userIdsText">
              <el-input v-model="issueForm.userIdsText" type="textarea" :rows="5" placeholder="请输入用户ID，使用英文逗号分隔，如：1000,1001,1002" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="issuing" @click="submitIssue">确认发券</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.couponId ? '编辑优惠券' : '新增优惠券'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="券名称" prop="couponName"><el-input v-model="form.couponName" /></el-form-item>
        <el-form-item label="券类型" prop="couponType">
          <el-select v-model="form.couponType" style="width: 100%">
            <el-option label="满减券" value="FULL_REDUCE" />
            <el-option label="折扣券" value="DISCOUNT" />
            <el-option label="品类券" value="CATEGORY" />
          </el-select>
        </el-form-item>
        <el-form-item label="面值/折扣" prop="faceValue"><el-input-number v-model="form.faceValue" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="最低消费"><el-input-number v-model="form.minAmount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="总量"><el-input-number v-model="form.totalCount" :min="0" /></el-form-item>
        <el-form-item label="每人限领"><el-input-number v-model="form.perLimit" :min="-1" /></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCoupon">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api'

const activeTab = ref('list')
const statusFilter = ref('')
const loading = ref(false)
const submitting = ref(false)
const issuing = ref(false)
const dialogVisible = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const formRef = ref()
const issueFormRef = ref()

const form = reactive({
  couponId: null,
  couponName: '',
  couponType: 'FULL_REDUCE',
  faceValue: 10,
  minAmount: 0,
  totalCount: 100,
  perLimit: 1,
  startTime: '',
  endTime: '',
  description: ''
})

const issueForm = reactive({
  couponId: null,
  userIdsText: ''
})

const rules = {
  couponName: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  couponType: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }]
}

const issueRules = {
  couponId: [{ required: true, message: '请输入优惠券ID', trigger: 'blur' }],
  userIdsText: [{ required: true, message: '请输入用户ID列表', trigger: 'blur' }]
}

const resetForm = () => {
  Object.assign(form, {
    couponId: null,
    couponName: '',
    couponType: 'FULL_REDUCE',
    faceValue: 10,
    minAmount: 0,
    totalCount: 100,
    perLimit: 1,
    startTime: '',
    endTime: '',
    description: ''
  })
}

const load = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getCoupons({ pageNum: pageNum.value, pageSize: pageSize.value, status: statusFilter.value || undefined })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    couponId: row.couponId,
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
    const payload = {
      couponName: form.couponName,
      couponType: form.couponType,
      faceValue: form.faceValue,
      minAmount: form.minAmount,
      totalCount: form.totalCount,
      perLimit: form.perLimit,
      startTime: form.startTime,
      endTime: form.endTime,
      description: form.description
    }
    if (form.couponId) {
      await adminAPI.updateCoupon(form.couponId, payload)
      ElMessage.success('优惠券已更新')
    } else {
      await adminAPI.createCoupon(payload)
      ElMessage.success('优惠券已创建')
    }
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

const toggle = async (row) => {
  await adminAPI.toggleCoupon(row.couponId, row.status === 'active' ? 'inactive' : 'active')
  ElMessage.success('优惠券状态已更新')
  load()
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该优惠券吗？', '提示', { type: 'warning' })
    await adminAPI.deleteCoupon(row.couponId)
    ElMessage.success('优惠券已删除')
    load()
  } catch (e) {
    if (e !== 'cancel') throw e
  }
}

const prefillIssue = (row) => {
  issueForm.couponId = row.couponId
  activeTab.value = 'issue'
}

const submitIssue = async () => {
  await issueFormRef.value.validate()
  const userIds = issueForm.userIdsText
    .split(',')
    .map(item => Number(item.trim()))
    .filter(item => !Number.isNaN(item) && item > 0)
  if (!userIds.length) return ElMessage.warning('请输入有效的用户ID')
  issuing.value = true
  try {
    const res = await adminAPI.batchIssueCoupons(issueForm.couponId, userIds)
    ElMessage.success(`发券完成：成功 ${res.data?.successCount ?? 0} 张`)
  } finally {
    issuing.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page-shell { display: grid; gap: 18px; }
.page-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.page-title { font-size: 28px; font-weight: 800; color: #22324d; }
.page-desc { margin-top: 4px; color: #6f7f97; }
.toolbar-actions { display: flex; gap: 12px; align-items: center; }
.panel-card { border: none; border-radius: 18px; box-shadow: 0 14px 36px rgba(42, 68, 110, 0.08); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 18px; }
.issue-form { max-width: 680px; padding-top: 8px; }
</style>
