<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <div class="page-title">轮播图管理</div>
        <div class="page-desc">配置首页 Banner 图片、跳转目标、排序和展示状态。</div>
      </div>
      <div class="toolbar-actions">
        <el-button @click="load">刷新</el-button>
        <el-button type="primary" @click="openAdd">新增轮播图</el-button>
      </div>
    </div>

    <el-card shadow="never" class="panel-card">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="bannerId" label="ID" width="80" />
        <el-table-column label="图片" width="120">
          <template #default="{ row }">
            <el-image
              :src="toAssetUrl(row.imageUrl)"
              fit="cover"
              class="banner-thumb"
              preview-teleported
              :preview-src-list="[toAssetUrl(row.imageUrl)]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="imageUrl" label="图片地址" min-width="240" show-overflow-tooltip />
        <el-table-column prop="linkType" label="跳转类型" width="120" />
        <el-table-column prop="linkTarget" label="跳转目标" min-width="140" />
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="edit(row)">编辑</el-button>
            <el-button size="small" @click="toggle(row)">
              {{ row.status === 'active' ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" :title="form.bannerId ? '编辑轮播图' : '新增轮播图'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="图片地址" prop="imageUrl">
          <div class="upload-field">
            <el-input v-model="form.imageUrl" placeholder="请输入 Banner 图片 URL" />
            <el-upload
              :show-file-list="false"
              :auto-upload="false"
              accept="image/*"
              :on-change="handleImageSelect"
            >
              <el-button :loading="uploading">上传本地图片</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item v-if="form.imageUrl" label="图片预览">
          <el-image :src="toAssetUrl(form.imageUrl)" fit="cover" class="banner-preview" preview-teleported />
        </el-form-item>
        <el-form-item label="跳转类型">
          <el-select v-model="form.linkType" style="width: 100%">
            <el-option label="无跳转" value="NONE" />
            <el-option label="商品" value="PRODUCT" />
            <el-option label="分类" value="CATEGORY" />
            <el-option label="活动" value="ACTIVITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="跳转目标">
          <el-input v-model="form.linkTarget" placeholder="填写商品ID / 分类ID / 活动ID" />
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
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bannerAPI, uploadAPI } from '@/api'

const list = ref([])
const loading = ref(false)
const visible = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const formRef = ref()

const form = reactive({
  bannerId: null,
  imageUrl: '',
  linkType: 'NONE',
  linkTarget: '',
  sortOrder: 99,
  status: 'active'
})

const rules = {
  imageUrl: [{ required: true, message: '请输入图片地址', trigger: 'blur' }]
}

const normalizeUploadUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('/uploads/')) return `/api${url}`
  return url
}

const toAssetUrl = (url) => normalizeUploadUrl(url)

const resetForm = () => {
  Object.assign(form, {
    bannerId: null,
    imageUrl: '',
    linkType: 'NONE',
    linkTarget: '',
    sortOrder: 99,
    status: 'active'
  })
}

const load = async () => {
  loading.value = true
  try {
    const res = await bannerAPI.adminGetList()
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
    bannerId: row.bannerId,
    imageUrl: row.imageUrl,
    linkType: row.linkType || 'NONE',
    linkTarget: row.linkTarget || '',
    sortOrder: row.sortOrder ?? 99,
    status: row.status || 'active'
  })
  visible.value = true
}

const handleImageSelect = async (uploadFile) => {
  if (!uploadFile?.raw) return
  uploading.value = true
  try {
    const res = await uploadAPI.uploadImage(uploadFile.raw)
    form.imageUrl = normalizeUploadUrl(res.data?.url || '')
    ElMessage.success('图片上传成功')
  } finally {
    uploading.value = false
  }
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      imageUrl: form.imageUrl,
      linkType: form.linkType,
      linkTarget: form.linkTarget,
      sortOrder: form.sortOrder,
      status: form.status
    }
    if (form.bannerId) {
      await bannerAPI.update(form.bannerId, payload)
      ElMessage.success('轮播图已更新')
    } else {
      await bannerAPI.create(payload)
      ElMessage.success('轮播图已新增')
    }
    visible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

const toggle = async (row) => {
  const isActive = row.status === 'active' ? 0 : 1
  await bannerAPI.toggle(row.bannerId, isActive)
  ElMessage.success(isActive ? '已启用' : '已停用')
  load()
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该轮播图吗？', '提示', { type: 'warning' })
    await bannerAPI.delete(row.bannerId)
    ElMessage.success('轮播图已删除')
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
}

.panel-card {
  border: none;
  border-radius: 18px;
  box-shadow: 0 14px 36px rgba(42, 68, 110, 0.08);
}

.banner-thumb {
  width: 72px;
  height: 42px;
  border-radius: 8px;
  background: #eef4fb;
}

.upload-field {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  width: 100%;
}

.banner-preview {
  width: 180px;
  height: 90px;
  border-radius: 10px;
  background: #eef4fb;
}
</style>
