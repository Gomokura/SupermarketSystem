<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>轮播图管理</h2>
        <p>维护首页轮播图，支持排序、跳转类型和启停。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增轮播图</el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="banners" border v-loading="loading">
        <el-table-column prop="bannerId" label="ID" width="80" />
        <el-table-column label="图片" min-width="220">
          <template #default="{ row }">
            <div class="image-cell">
              <el-image v-if="row.imageUrl" :src="row.imageUrl" fit="cover" class="banner-preview" />
              <span>{{ row.imageUrl || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="linkType" label="跳转类型" width="120" />
        <el-table-column prop="linkTarget" label="跳转目标" min-width="140" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="toggleBanner(row)">
              {{ row.status === 'active' ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="removeBanner(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑轮播图' : '新增轮播图'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="图片地址" prop="imageUrl">
          <el-input v-model="form.imageUrl" />
        </el-form-item>
        <el-form-item label="跳转类型" prop="linkType">
          <el-select v-model="form.linkType">
            <el-option label="NONE" value="NONE" />
            <el-option label="PRODUCT" value="PRODUCT" />
            <el-option label="CATEGORY" value="CATEGORY" />
            <el-option label="ACTIVITY" value="ACTIVITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="跳转目标">
          <el-input v-model="form.linkTarget" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="99" />
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
        <el-button type="primary" :loading="submitting" @click="submitBanner">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bannerAPI } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const banners = ref([])
const formRef = ref()

const form = reactive({
  imageUrl: '',
  linkType: 'NONE',
  linkTarget: '',
  sortOrder: 1,
  status: 'active'
})

const rules = {
  imageUrl: [{ required: true, message: '请输入图片地址', trigger: 'blur' }],
  linkType: [{ required: true, message: '请选择跳转类型', trigger: 'change' }]
}

const resetForm = () => {
  editingId.value = null
  Object.assign(form, {
    imageUrl: '',
    linkType: 'NONE',
    linkTarget: '',
    sortOrder: 1,
    status: 'active'
  })
}

const normalizeBanner = (row) => ({
  ...row,
  status: row.status || (row.isActive === 1 ? 'active' : 'inactive')
})

const loadBanners = async () => {
  loading.value = true
  try {
    const res = await bannerAPI.adminGetList()
    banners.value = (res.data || []).map(normalizeBanner)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.bannerId
  Object.assign(form, {
    imageUrl: row.imageUrl,
    linkType: row.linkType || 'NONE',
    linkTarget: row.linkTarget || '',
    sortOrder: row.sortOrder || 1,
    status: row.status || 'active'
  })
  dialogVisible.value = true
}

const submitBanner = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await bannerAPI.update(editingId.value, { ...form })
      ElMessage.success('轮播图已更新')
    } else {
      await bannerAPI.create({ ...form })
      ElMessage.success('轮播图已创建')
    }
    dialogVisible.value = false
    await loadBanners()
  } finally {
    submitting.value = false
  }
}

const toggleBanner = async (row) => {
  const nextActive = row.status !== 'active'
  await bannerAPI.toggle(row.bannerId, nextActive ? 1 : 0)
  ElMessage.success('状态已更新')
  await loadBanners()
}

const removeBanner = async (row) => {
  await ElMessageBox.confirm(`确认删除轮播图 #${row.bannerId} 吗？`, '提示', { type: 'warning' })
  await bannerAPI.delete(row.bannerId)
  ElMessage.success('轮播图已删除')
  await loadBanners()
}

onMounted(() => {
  loadBanners()
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

.image-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.banner-preview {
  width: 72px;
  height: 40px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f4f4f5;
}
</style>
