<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>品牌管理</h2>
        <p>维护品牌名称、排序和描述信息。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增品牌</el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="brands" border v-loading="loading">
        <el-table-column prop="brandId" label="ID" width="80" />
        <el-table-column prop="brandName" label="品牌名称" min-width="180" />
        <el-table-column prop="logoUrl" label="Logo" min-width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
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
            <el-button size="small" type="danger" @click="removeBrand(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑品牌' : '新增品牌'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="品牌名称" prop="brandName">
          <el-input v-model="form.brandName" />
        </el-form-item>
        <el-form-item label="Logo 地址">
          <el-input v-model="form.logoUrl" />
        </el-form-item>
        <el-form-item label="品牌描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
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
        <el-button type="primary" :loading="submitting" @click="submitBrand">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { brandAPI } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const brands = ref([])
const formRef = ref()

const form = reactive({
  brandName: '',
  logoUrl: '',
  description: '',
  sortOrder: 0,
  status: 'active'
})

const rules = {
  brandName: [{ required: true, message: '请输入品牌名称', trigger: 'blur' }]
}

const resetForm = () => {
  editingId.value = null
  Object.assign(form, {
    brandName: '',
    logoUrl: '',
    description: '',
    sortOrder: 0,
    status: 'active'
  })
}

const loadBrands = async () => {
  loading.value = true
  try {
    const res = await brandAPI.getList()
    brands.value = res.data || []
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.brandId
  Object.assign(form, {
    brandName: row.brandName,
    logoUrl: row.logoUrl,
    description: row.description,
    sortOrder: row.sortOrder || 0,
    status: row.status || 'active'
  })
  dialogVisible.value = true
}

const submitBrand = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await brandAPI.update(editingId.value, { ...form })
      ElMessage.success('品牌已更新')
    } else {
      await brandAPI.create({ ...form })
      ElMessage.success('品牌已创建')
    }
    dialogVisible.value = false
    await loadBrands()
  } finally {
    submitting.value = false
  }
}

const removeBrand = async (row) => {
  await ElMessageBox.confirm(`确认删除品牌「${row.brandName}」吗？`, '提示', { type: 'warning' })
  await brandAPI.delete(row.brandId)
  ElMessage.success('品牌已删除')
  await loadBrands()
}

onMounted(() => {
  loadBrands()
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
</style>
