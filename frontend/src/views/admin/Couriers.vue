<template>
  <div class="page-container">
    <div class="header-bar">
      <h2>配送员管理</h2>
      <el-button type="primary" @click="openAdd">新增配送员</el-button>
    </div>

    <el-table :data="list" border v-loading="loading" class="mt">
      <el-table-column prop="courierId" label="ID" width="80" />
      <el-table-column prop="courierName" label="姓名" min-width="120" show-overflow-tooltip />
      <el-table-column prop="phone" label="手机" width="140" />
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTag(row)" size="small">{{ statusText(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="今日单数" width="100" align="center">
        <template #default="{ row }">{{ row.todayCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="累计单数" width="100" align="center">
        <template #default="{ row }">{{ row.totalCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.isDisabled" size="small" type="success" @click="toggleStatus(row)">启用</el-button>
          <el-button v-else size="small" type="danger" @click="toggleStatus(row)">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      class="pagination"
      @current-change="loadList"
    />

    <!-- 新增配送员弹窗 -->
    <el-dialog v-model="addVisible" title="新增配送员" width="420px">
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="90px">
        <el-form-item label="姓名" prop="courierName">
          <el-input v-model="addForm.courierName" placeholder="请输入姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addForm.phone" placeholder="作为登录账号" maxlength="11" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password">
          <el-input v-model="addForm.password" placeholder="请输入密码" show-password maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAdd">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { courierAPI } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const res = await courierAPI.adminGetList({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) { /* ignore */ } finally {
    loading.value = false
  }
}

loadList()

const statusText = (row) => {
  if (row.isDisabled) return '已禁用'
  if (row.isOnline) return '在线'
  return '离线'
}

const statusTag = (row) => {
  if (row.isDisabled) return 'danger'
  if (row.isOnline) return 'success'
  return 'info'
}

const toggleStatus = async (row) => {
  const action = row.isDisabled ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确认${action}配送员「${row.courierName}」？`, '提示')
    await courierAPI.adminUpdateStatus(row.courierId, row.isDisabled ? false : true)
    ElMessage.success(`${action}成功`)
    loadList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

// 新增
const addVisible = ref(false)
const addFormRef = ref()
const addForm = ref({ courierName: '', phone: '', password: '' })
const addRules = {
  courierName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const openAdd = () => {
  addForm.value = { courierName: '', phone: '', password: '' }
  addFormRef.value?.clearValidate()
  addVisible.value = true
}

const submitAdd = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await courierAPI.adminCreate(addForm.value)
      ElMessage.success('配送员创建成功')
      addVisible.value = false
      loadList()
    } catch (e) { /* interceptor handles */ } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.page-container { padding: 20px; }
.header-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.header-bar h2 { margin: 0; font-size: 18px; }
.mt { margin-top: 14px; }
.pagination { justify-content: center; margin-top: 16px; }
</style>
