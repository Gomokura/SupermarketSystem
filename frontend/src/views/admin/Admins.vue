<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <div class="page-title">管理员账号管理</div>
        <div class="page-desc">集中维护后台账号、角色分配、状态与密码重置。</div>
      </div>
      <div class="toolbar-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名 / 姓名 / 手机"
          clearable
          style="width: 260px"
          @keyup.enter="load"
          @clear="load"
        >
          <template #append>
            <el-button @click="load">查询</el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="openCreate">新增管理员</el-button>
      </div>
    </div>

    <el-card shadow="never" class="panel-card">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="adminId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="手机" min-width="140" />
        <el-table-column prop="role" label="角色" min-width="140">
          <template #default="{ row }">
            <el-tag>{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLogin" label="最后登录时间" min-width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="toggleStatus(row)">
              {{ row.status === 'active' ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="warning" @click="openResetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          background
          layout="total, prev, pager, next"
          :total="total"
          @current-change="load"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑管理员' : '新增管理员'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入管理员姓名" />
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetVisible" title="重置管理员密码" width="420px">
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="90px">
        <el-form-item label="管理员">
          <el-input :model-value="resetTarget?.username || ''" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="warning" :loading="resetting" @click="submitResetPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAPI } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const resetting = ref(false)
const dialogVisible = ref(false)
const resetVisible = ref(false)
const isEdit = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const formRef = ref()
const resetFormRef = ref()
const resetTarget = ref(null)

const roleOptions = [
  { label: '超级管理员', value: 'SUPER_ADMIN' },
  { label: '店长', value: 'MANAGER' },
  { label: '商品专员', value: 'PRODUCT' },
  { label: '财务专员', value: 'FINANCE' },
  { label: '客服专员', value: 'SERVICE' },
  { label: '仓库管理员', value: 'WAREHOUSE' },
  { label: '收银员', value: 'CASHIER' }
]

const form = reactive({
  adminId: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  role: 'MANAGER',
  status: 'active'
})

const resetForm = reactive({
  newPassword: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const resetRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少 6 位', trigger: 'blur' }
  ]
}

const roleText = (role) => roleOptions.find(item => item.value === role)?.label || role || '-'

const resetFormState = () => {
  Object.assign(form, {
    adminId: null,
    username: '',
    password: '',
    realName: '',
    phone: '',
    role: 'MANAGER',
    status: 'active'
  })
}

const load = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getAdmins({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  isEdit.value = false
  resetFormState()
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    adminId: row.adminId,
    username: row.username,
    password: '',
    realName: row.realName,
    phone: row.phone,
    role: row.role,
    status: row.status || 'active'
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      username: form.username,
      password: form.password,
      realName: form.realName,
      phone: form.phone,
      role: form.role,
      status: form.status
    }
    if (isEdit.value) {
      delete payload.password
      await adminAPI.updateAdmin(form.adminId, payload)
      ElMessage.success('管理员信息已更新')
    } else {
      await adminAPI.createAdmin(payload)
      ElMessage.success('管理员已创建')
    }
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row) => {
  const nextStatus = row.status === 'active' ? 'inactive' : 'active'
  await adminAPI.updateAdminStatus(row.adminId, nextStatus)
  ElMessage.success(nextStatus === 'active' ? '已启用' : '已停用')
  load()
}

const openResetPassword = (row) => {
  resetTarget.value = row
  resetForm.newPassword = ''
  resetVisible.value = true
}

const submitResetPassword = async () => {
  await resetFormRef.value.validate()
  resetting.value = true
  try {
    await adminAPI.resetAdminPassword(resetTarget.value.adminId, resetForm.newPassword)
    ElMessage.success('密码已重置')
    resetVisible.value = false
  } finally {
    resetting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page-shell {
  display: grid;
  gap: 18px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  color: #22324d;
}

.page-desc {
  margin-top: 4px;
  color: #6f7f97;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.panel-card {
  border: none;
  border-radius: 18px;
  box-shadow: 0 14px 36px rgba(42, 68, 110, 0.08);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}
</style>
