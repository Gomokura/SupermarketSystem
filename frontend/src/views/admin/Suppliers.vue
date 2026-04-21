<template>
  <div class="page-container">
    <h2>供应商管理</h2>

    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增供应商</el-button>
      </div>

      <el-table :data="suppliers" border v-loading="loading">
        <el-table-column prop="supplierId" label="ID" width="80" />
        <el-table-column prop="name" label="供应商名称" min-width="150" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="手机" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="settlementPeriod" label="结算账期" width="100">
          <template #default="{ row }">
            {{ row.settlementPeriod || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        class="pagination"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑供应商' : '新增供应商'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" type="textarea" rows="2" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="结算账期">
          <el-input v-model="form.settlementPeriod" placeholder="如：月结30天" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api'

const suppliers = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const form = reactive({
  supplierId: null,
  name: '',
  contact: '',
  phone: '',
  email: '',
  address: '',
  settlementPeriod: ''
})

const rules = {
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const formRef = ref()

const loadSuppliers = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getSuppliers({ page: currentPage.value, pageSize: pageSize.value })
    suppliers.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.keys(form).forEach(key => {
    if (key === 'supplierId') form[key] = null
    else form[key] = ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await adminAPI.updateSupplier(form.supplierId, form)
      ElMessage.success('编辑成功')
    } else {
      await adminAPI.createSupplier(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadSuppliers()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该供应商吗？删除后不可恢复。', '警告', { type: 'warning' })
    await adminAPI.deleteSupplier(row.supplierId)
    ElMessage.success('删除成功')
    loadSuppliers()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadSuppliers()
}

onMounted(() => {
  loadSuppliers()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
