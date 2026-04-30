<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>供应商管理</h2>
        <p>维护供应商资料，支持搜索、新增、编辑和删除。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增供应商</el-button>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          clearable
          placeholder="按供应商名称或联系人搜索"
          style="width: 320px"
          @keyup.enter="loadSuppliers"
          @clear="loadSuppliers"
        >
          <template #append>
            <el-button @click="loadSuppliers">搜索</el-button>
          </template>
        </el-input>
      </div>

      <el-table :data="pagedSuppliers" border v-loading="loading">
        <el-table-column prop="supplierId" label="ID" width="80" />
        <el-table-column prop="supplierName" label="供应商名称" min-width="180" />
        <el-table-column prop="contact" label="联系人" width="120" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="账期" width="120" align="center">
          <template #default="{ row }">
            {{ row.paymentPeriod ? `${row.paymentPeriod} 天` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeSupplier(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="filteredSuppliers.length"
          layout="total, prev, pager, next"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑供应商' : '新增供应商'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="form.supplierName" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="付款账期">
          <el-input-number v-model="form.paymentPeriod" :min="0" :max="180" />
        </el-form-item>
        <el-form-item label="银行账号">
          <el-input v-model="form.bankAccount" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitSupplier">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const suppliers = ref([])
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(8)
const editingId = ref(null)
const formRef = ref()

const form = reactive({
  supplierName: '',
  contact: '',
  phone: '',
  email: '',
  address: '',
  bankAccount: '',
  paymentPeriod: 30,
  status: 'active'
})

const rules = {
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const filteredSuppliers = computed(() => {
  if (!keyword.value) return suppliers.value
  const text = keyword.value.trim().toLowerCase()
  return suppliers.value.filter(item =>
    [item.supplierName, item.contact, item.phone]
      .filter(Boolean)
      .some(value => String(value).toLowerCase().includes(text))
  )
})

const pagedSuppliers = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredSuppliers.value.slice(start, start + pageSize.value)
})

const resetForm = () => {
  editingId.value = null
  Object.assign(form, {
    supplierName: '',
    contact: '',
    phone: '',
    email: '',
    address: '',
    bankAccount: '',
    paymentPeriod: 30,
    status: 'active'
  })
}

const loadSuppliers = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getSuppliers({ keyword: keyword.value || undefined })
    suppliers.value = res.data || []
    pageNum.value = 1
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.supplierId
  Object.assign(form, {
    supplierName: row.supplierName,
    contact: row.contact,
    phone: row.phone,
    email: row.email,
    address: row.address,
    bankAccount: row.bankAccount,
    paymentPeriod: row.paymentPeriod,
    status: row.status || 'active'
  })
  dialogVisible.value = true
}

const submitSupplier = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await adminAPI.updateSupplier(editingId.value, { ...form })
      ElMessage.success('供应商已更新')
    } else {
      await adminAPI.createSupplier({ ...form })
      ElMessage.success('供应商已创建')
    }
    dialogVisible.value = false
    await loadSuppliers()
  } finally {
    submitting.value = false
  }
}

const removeSupplier = async (row) => {
  await ElMessageBox.confirm(`确认删除供应商「${row.supplierName}」吗？`, '提示', { type: 'warning' })
  await adminAPI.deleteSupplier(row.supplierId)
  ElMessage.success('供应商已删除')
  await loadSuppliers()
}

onMounted(() => {
  loadSuppliers()
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
  display: flex;
  justify-content: space-between;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
