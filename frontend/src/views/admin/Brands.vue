<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <div class="page-title">品牌管理</div>
        <div class="page-desc">维护品牌名称、Logo、描述和启用状态，支持编辑与删除。</div>
      </div>
      <div class="toolbar-actions">
        <el-select v-model="statusFilter" clearable placeholder="筛选状态" style="width: 140px" @change="load">
          <el-option label="全部状态" value="" />
          <el-option label="启用" value="active" />
          <el-option label="停用" value="inactive" />
        </el-select>
        <el-button type="primary" @click="openAdd">新增品牌</el-button>
      </div>
    </div>

    <el-card shadow="never" class="panel-card">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="brandId" label="ID" width="80" />
        <el-table-column label="品牌信息" min-width="280">
          <template #default="{ row }">
            <div class="brand-cell">
              <el-image
                v-if="row.logoUrl"
                :src="toAssetUrl(row.logoUrl)"
                fit="cover"
                class="brand-logo"
                preview-teleported
                :preview-src-list="[toAssetUrl(row.logoUrl)]"
              />
              <div v-else class="brand-logo placeholder">{{ (row.brandName || 'B').slice(0, 1) }}</div>
              <div>
                <div class="brand-name">{{ row.brandName }}</div>
                <div class="brand-meta">关联商品：{{ row.productCount ?? 0 }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="edit(row)">编辑</el-button>
            <el-button size="small" @click="toggleStatus(row)">
              {{ row.status === 'active' ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" :title="form.brandId ? '编辑品牌' : '新增品牌'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="品牌名称" prop="brandName">
          <el-input v-model="form.brandName" placeholder="请输入品牌名称" />
        </el-form-item>
        <el-form-item label="Logo 地址" prop="logoUrl">
          <div class="upload-field">
            <el-input v-model="form.logoUrl" placeholder="请输入图片 URL（可选）" />
            <el-upload
              :show-file-list="false"
              :auto-upload="false"
              accept="image/*"
              :on-change="handleLogoSelect"
            >
              <el-button :loading="uploading">上传本地图片</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item v-if="form.logoUrl" label="Logo 预览">
          <el-image :src="toAssetUrl(form.logoUrl)" class="logo-preview" fit="cover" preview-teleported />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="品牌描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入品牌简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { brandAPI, uploadAPI } from '@/api'

const list = ref([])
const loading = ref(false)
const visible = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const statusFilter = ref('')
const formRef = ref()
const form = reactive({
  brandId: null,
  brandName: '',
  logoUrl: '',
  description: '',
  status: 'active'
})

const rules = {
  brandName: [{ required: true, message: '请输入品牌名称', trigger: 'blur' }]
}

const normalizeUploadUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('/uploads/')) return `/api${url}`
  return url
}

const toAssetUrl = (url) => normalizeUploadUrl(url)

const resetForm = () => {
  Object.assign(form, {
    brandId: null,
    brandName: '',
    logoUrl: '',
    description: '',
    status: 'active'
  })
}

const load = async () => {
  loading.value = true
  try {
    const res = await brandAPI.adminGetList({ status: statusFilter.value || undefined })
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  resetForm()
  visible.value = true
}

const edit = (row) => {
  Object.assign(form, {
    brandId: row.brandId,
    brandName: row.brandName,
    logoUrl: row.logoUrl,
    description: row.description,
    status: row.status || 'active'
  })
  visible.value = true
}

const handleLogoSelect = async (uploadFile) => {
  if (!uploadFile?.raw) return
  uploading.value = true
  try {
    const res = await uploadAPI.uploadImage(uploadFile.raw)
    form.logoUrl = normalizeUploadUrl(res.data?.url || '')
    ElMessage.success('Logo 上传成功')
  } finally {
    uploading.value = false
  }
}

const save = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      brandName: form.brandName,
      logoUrl: form.logoUrl,
      description: form.description,
      status: form.status
    }
    if (form.brandId) {
      await brandAPI.update(form.brandId, payload)
      ElMessage.success('品牌信息已更新')
    } else {
      await brandAPI.create(payload)
      ElMessage.success('品牌已新增')
    }
    visible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row) => {
  await brandAPI.update(row.brandId, {
    ...row,
    status: row.status === 'active' ? 'inactive' : 'active'
  })
  ElMessage.success('状态已更新')
  load()
}

const del = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除品牌“${row.brandName}”吗？`, '提示', { type: 'warning' })
    await brandAPI.delete(row.brandId)
    ElMessage.success('品牌已删除')
    load()
  } catch (e) {
    if (e !== 'cancel') throw e
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

.brand-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #eef4fb;
}

.placeholder {
  display: grid;
  place-items: center;
  color: #5073a6;
  font-weight: 700;
}

.brand-name {
  font-weight: 700;
  color: #20304a;
}

 .brand-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #7f8ea5;
}

.upload-field {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  width: 100%;
}

.logo-preview {
  width: 72px;
  height: 72px;
  border-radius: 12px;
  background: #eef4fb;
}
</style>
