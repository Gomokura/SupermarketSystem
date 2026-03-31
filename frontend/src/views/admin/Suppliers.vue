<template>
  <div class="page-container">
    <div class="page-header">
      <h2>供应商管理</h2>
      <el-button type="primary" @click="openDialog(false)">新增供应商</el-button>
    </div>

    <el-card class="mt-16">
      <div class="filter-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索供应商名称/联系人/手机"
          clearable
          style="width: 260px"
          @clear="loadList"
          @keyup.enter="loadList"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadList">搜索</el-button>
      </div>

      <el-table :data="list" border v-loading="loading" class="mt-16">
        <el-table-column prop="supplierId" label="ID" width="80" />
        <el-table-column prop="name" label="供应商名称" min-width="160" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="settlementPeriod" label="结算账期" width="100">
          <template #default="{ row }">
            {{ row.settlementPeriod || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(true, row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteSupplier(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="mt-16"
        @current-change="loadList"
        @size-change="loadList"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑供应商' : '新增供应商'" width="520px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="结算账期">
          <el-input v-model="form.settlementPeriod" placeholder="如：月结30天" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveSupplier">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminAPI } from '@/api'

const list = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()

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
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }]
}

onMounted(() => {
  loadList()
})

const loadList = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getSuppliers({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    })
    list.value = res.data?.records || res.data || []
    total.value = res.data?.total || list.value.length
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, { supplierId: null, name: '', contact: '', phone: '', email: '', address: '', settlementPeriod: '' })
}

const openDialog = (edit, row = null) => {
  if (edit && row) {
    isEdit.value = true
    Object.assign(form, row)
  } else {
    isEdit.value = false
    resetForm()
  }
  dialogVisible.value = true
}

const saveSupplier = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await adminAPI.updateSupplier(form.supplierId, form)
    } else {
      await adminAPI.createSupplier(form)
    }
    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
    dialogVisible.value = false
    loadList()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const deleteSupplier = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除供应商「${row.name}」吗？`, '提示', { type: 'warning' })
    await adminAPI.deleteSupplier(row.supplierId)
    ElMessage.success('删除成功')
    loadList()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-header h2 {
  margin: 0;
}
.mt-16 {
  margin-top: 16px;
}
.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
