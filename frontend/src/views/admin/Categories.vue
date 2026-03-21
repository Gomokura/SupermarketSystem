<template>
  <div class="page-container">
    <h2>分类管理</h2>
    <el-button type="primary" @click="showDialog = true; form = {}">添加分类</el-button>
    <el-table :data="categories" border style="margin-top: 20px">
      <el-table-column prop="categoryId" label="ID" width="80" />
      <el-table-column prop="categoryName" label="分类名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="editCategory(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteCategory(row.categoryId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑分类' : '添加分类'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="form.categoryName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { productAPI } from '@/api'

const categories = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const form = ref({})

onMounted(() => {
  loadCategories()
})

const loadCategories = async () => {
  try {
    const res = await productAPI.getCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const editCategory = (row) => {
  isEdit.value = true
  form.value = { ...row }
  showDialog.value = true
}

const saveCategory = async () => {
  try {
    if (isEdit.value) {
      await productAPI.updateCategory(form.value)
    } else {
      await productAPI.addCategory(form.value)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadCategories()
  } catch (error) {
    console.error(error)
  }
}

const deleteCategory = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个分类吗？', '提示', { type: 'warning' })
    await productAPI.deleteCategory(id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}
</script>
