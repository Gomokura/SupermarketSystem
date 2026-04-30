<template>
  <div class="page-container">
    <h2>分类管理</h2>
    <el-button type="primary" @click="openAdd">添加分类</el-button>

    <el-table :data="categories" border style="margin-top:20px" row-key="categoryId" default-expand-all
      :tree-props="{ children: 'children' }">
      <el-table-column prop="categoryId" label="ID" width="70" />
      <el-table-column prop="categoryName" label="分类名称" min-width="160" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="parentId" label="父分类ID" width="100">
        <template #default="{ row }">
          {{ row.parentId ? row.parentId : '顶级' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.categoryId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑分类' : '添加分类'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="分类名称" required>
          <el-input v-model="form.categoryName" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-select v-model="form.parentId" placeholder="顶级分类" clearable style="width:100%">
            <el-option v-for="c in flatCategories" :key="c.categoryId"
              :label="c.categoryName" :value="c.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="图标URL">
          <el-input v-model="form.icon" placeholder="可选" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { productAPI } from '@/api'

const categories = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const form = ref({})

const flatCategories = computed(() => {
  const flat = []
  function walk(list) {
    for (const c of list) {
      flat.push(c)
      if (c.children?.length) walk(c.children)
    }
  }
  walk(categories.value)
  return flat
})

onMounted(loadCategories)

async function loadCategories() {
  try {
    const res = await productAPI.getCategories()
    categories.value = res.data || []
  } catch (e) { console.error(e) }
}

function openAdd() {
  isEdit.value = false
  form.value = { sortOrder: 0 }
  showDialog.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  showDialog.value = true
}

async function saveCategory() {
  if (!form.value.categoryName) { ElMessage.warning('请填写分类名称'); return }
  try {
    if (isEdit.value) {
      await productAPI.updateCategory(form.value)
    } else {
      await productAPI.addCategory(form.value)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadCategories()
  } catch (e) { console.error(e) }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个分类吗？', '提示', { type: 'warning' })
    await productAPI.deleteCategory(id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}
</script>
