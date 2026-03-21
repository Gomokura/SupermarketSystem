<template>
  <div class="page-container">
    <h2>商品管理</h2>
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索商品" style="width: 200px" clearable />
      <el-button @click="loadProducts">搜索</el-button>
      <el-button type="primary" @click="showDialog = true; isEdit = false; form = {}">添加商品</el-button>
    </div>
    <el-table :data="products" border>
      <el-table-column prop="productId" label="ID" width="80" />
      <el-table-column prop="productName" label="商品名称" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="editProduct(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteProduct(row.productId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadProducts"
      />
    </div>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑商品' : '添加商品'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名称">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="cat in categories" :key="cat.categoryId" :label="cat.categoryName" :value="cat.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="form.supplier" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProduct">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { productAPI } from '@/api'

const products = ref([])
const categories = ref([])
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showDialog = ref(false)
const isEdit = ref(false)
const form = reactive({})

onMounted(() => {
  loadProducts()
  loadCategories()
})

const loadProducts = async () => {
  try {
    const res = await productAPI.getList({ keyword: keyword.value, pageNum: pageNum.value, pageSize: pageSize.value })
    products.value = res.data.records || res.data || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const loadCategories = async () => {
  try {
    const res = await productAPI.getCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const editProduct = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  showDialog.value = true
}

const saveProduct = async () => {
  try {
    if (isEdit.value) {
      await productAPI.update(form)
    } else {
      await productAPI.add(form)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadProducts()
  } catch (error) {
    console.error(error)
  }
}

const deleteProduct = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', { type: 'warning' })
    await productAPI.delete(id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}
</script>
