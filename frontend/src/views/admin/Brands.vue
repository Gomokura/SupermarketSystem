<template>
  <div class="page-container">
    <h2>品牌管理</h2>
    <el-button type="primary" @click="openAdd">新增品牌</el-button>
    <el-table :data="list" border class="mt" v-loading="loading">
      <el-table-column prop="brandId" label="ID" width="80" />
      <el-table-column prop="brandName" label="品牌名" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="edit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="visible" title="品牌" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.brandName" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { brandAPI } from '@/api'

const list = ref([])
const loading = ref(false)
const visible = ref(false)
const form = reactive({ brandId: null, brandName: '' })

onMounted(() => load())
const load = async () => {
  loading.value = true
  try {
    const res = await brandAPI.getList()
    list.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
const openAdd = () => {
  form.brandId = null
  form.brandName = ''
  visible.value = true
}
const edit = (row) => {
  form.brandId = row.brandId
  form.brandName = row.brandName
  visible.value = true
}
const save = async () => {
  try {
    if (form.brandId) await brandAPI.update(form.brandId, { brandName: form.brandName })
    else await brandAPI.create({ brandName: form.brandName })
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } catch (e) {
    console.error(e)
  }
}
const del = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await brandAPI.delete(row.brandId)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}
</script>

<style scoped>
.page-container { padding: 20px; }
.mt { margin-top: 16px; }
</style>
