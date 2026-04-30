<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>管理员账户</h2>
        <p>支持关键词搜索、新增、编辑和重置密码。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增管理员</el-button>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          clearable
          placeholder="按账号、姓名或手机号搜索"
          style="width: 320px"
          @keyup.enter="loadAdmins"
          @clear="loadAdmins"
        >
          <template #append>
            <el-button @click="loadAdmins">搜索</el-button>
          </template>
        </el-input>
      </div>

      <el-table :data="records" border v-loading="loading">
        <el-table-column prop="adminId" label="ID" width="80" />
        <el-table-column prop="username" label="账号" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column label="角色" width="130">
          <template #default="{ row }">{{ roleText(row.role) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近登录" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.lastLogin) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="toggleStatus(row)">
              {{ row.status === 'active' ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="warning" @click="resetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadAdmins"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑管理员' : '新增管理员'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="账号" prop="username" v-if="!editingId">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="初始密码" prop="password" v-if="!editingId">
          <el-input v-model="form.password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
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
const dialogVisible = ref(false)
const records = ref([])
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const editingId = ref(null)
const formRef = ref()

const roleOptions = [
  { label: '超级管理员', value: 'SUPER_ADMIN' },
  { label: '店长', value: 'MANAGER' },
  { label: '商品运营', value: 'PRODUCT' },
  { label: '财务', value: 'FINANCE' },
  { label: '客服', value: 'SERVICE' },
  { label: '仓管', value: 'WAREHOUSE' },
  { label: '收银员', value: 'CASHIER' }
]

const form = reactive({
  username: '',
  realName: '',
  phone: '',
  role: 'MANAGER',
  status: 'active',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
}

const roleText = (value) => roleOptions.find(item => item.value === value)?.label || value

const formatDateTime = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN')
}

const resetForm = () => {
  editingId.value = null
  Object.assign(form, {
    username: '',
    realName: '',
    phone: '',
    role: 'MANAGER',
    status: 'active',
    password: ''
  })
}

const loadAdmins = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getAdmins({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    })
    records.value = res.data?.records || []
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
  editingId.value = row.adminId
  Object.assign(form, {
    username: row.username,
    realName: row.realName,
    phone: row.phone,
    role: row.role,
    status: row.status,
    password: ''
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await adminAPI.updateAdmin(editingId.value, {
        realName: form.realName,
        phone: form.phone,
        role: form.role,
        status: form.status
      })
      ElMessage.success('管理员信息已更新')
    } else {
      await adminAPI.createAdmin({ ...form })
      ElMessage.success('管理员已创建')
    }
    dialogVisible.value = false
    await loadAdmins()
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row) => {
  await adminAPI.updateAdmin(row.adminId, {
    realName: row.realName,
    phone: row.phone,
    role: row.role,
    status: row.status === 'active' ? 'inactive' : 'active'
  })
  ElMessage.success('状态已更新')
  loadAdmins()
}

const resetPassword = async (row) => {
  const result = await ElMessageBox.prompt(`请输入 ${row.username} 的新密码`, '重置密码', {
    inputType: 'password',
    inputPattern: /^.{6,}$/,
    inputErrorMessage: '密码至少 6 位'
  })
  await adminAPI.resetAdminPassword(row.adminId, result.value)
  ElMessage.success('密码已重置')
}

onMounted(() => {
  loadAdmins()
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
