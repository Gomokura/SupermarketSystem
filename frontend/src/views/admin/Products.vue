<template>
  <div class="page-container">
    <h2>商品管理</h2>

    <!-- 搜索栏 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="商品名/条码" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" placeholder="全部" clearable style="width:140px">
            <el-option v-for="c in flatCategories" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
            <el-option label="上架" value="active" />
            <el-option label="下架" value="off_shelf" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="openAdd" style="margin-left:8px">添加商品</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-table :data="products" border v-loading="loading">
      <el-table-column prop="productId" label="ID" width="70" />
      <el-table-column label="主图" width="80">
        <template #default="{ row }">
          <el-image v-if="row.coverImage" :src="row.coverImage" style="width:50px;height:50px;object-fit:cover" />
          <span v-else style="color:#ccc">无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="barcode" label="条码" width="130" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="brandName" label="品牌" width="90" />
      <el-table-column prop="price" label="售价" width="90">
        <template #default="{ row }">￥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="costPrice" label="成本价" width="90">
        <template #default="{ row }">￥{{ row.costPrice || '—' }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="salesCount" label="销量" width="80" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'info'">
            {{ row.status === 'active' ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 'active' ? 'warning' : 'success'"
            @click="toggleStatus(row)">{{ row.status === 'active' ? '下架' : '上架' }}</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.productId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50]" @current-change="loadProducts" @size-change="loadProducts" />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑商品' : '添加商品'" width="700px" top="5vh">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="form" label-width="90px">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="商品名称" required>
                  <el-input v-model="form.productName" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="条形码">
                  <el-input v-model="form.barcode" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="分类">
                  <el-select v-model="form.categoryId" style="width:100%" filterable>
                    <el-option v-for="c in flatCategories" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="品牌">
                  <el-select v-model="form.brandId" style="width:100%" clearable filterable>
                    <el-option v-for="b in brands" :key="b.brandId" :label="b.brandName" :value="b.brandId" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="售价" required>
                  <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="原价">
                  <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="成本价">
                  <el-input-number v-model="form.costPrice" :min="0" :precision="2" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="库存">
                  <el-input-number v-model="form.stock" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="预警库存">
                  <el-input-number v-model="form.stockWarning" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="单位">
                  <el-input v-model="form.unit" placeholder="如：件/箱/kg" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="主图URL">
              <el-input v-model="form.coverImage" placeholder="图片地址" />
            </el-form-item>
            <el-form-item label="首页推荐">
              <el-switch v-model="form.isRecommend" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio value="active">上架</el-radio>
                <el-radio value="off_shelf">下架</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="商品描述">
              <el-input v-model="form.description" type="textarea" :rows="3" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="SKU规格" name="sku" v-if="isEdit">
          <div style="margin-bottom:10px">
            <el-button type="primary" size="small" @click="addSkuRow">添加规格</el-button>
          </div>
          <el-table :data="skuList" border size="small">
            <el-table-column label="规格名" min-width="120">
              <template #default="{ row }"><el-input v-model="row.skuName" size="small" /></template>
            </el-table-column>
            <el-table-column label="规格描述" min-width="120">
              <template #default="{ row }"><el-input v-model="row.skuSpec" size="small" /></template>
            </el-table-column>
            <el-table-column label="售价" width="110">
              <template #default="{ row }"><el-input-number v-model="row.price" :min="0" :precision="2" size="small" style="width:100%" /></template>
            </el-table-column>
            <el-table-column label="库存" width="90">
              <template #default="{ row }"><el-input-number v-model="row.stock" :min="0" size="small" style="width:100%" /></template>
            </el-table-column>
            <el-table-column label="条码" width="130">
              <template #default="{ row }"><el-input v-model="row.barcode" size="small" /></template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row, $index }">
                <el-button size="small" type="danger" link @click="removeSkuRow($index, row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top:10px">
            <el-button type="primary" @click="saveSkus">保存SKU</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProduct">保存基本信息</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { productAPI, brandAPI } from '@/api'

const products = ref([])
const allCategories = ref([])
const brands = ref([])
const loading = ref(false)
const total = ref(0)
const showDialog = ref(false)
const isEdit = ref(false)
const activeTab = ref('basic')
const form = ref({})
const skuList = ref([])

const query = reactive({ keyword: '', categoryId: null, status: '', pageNum: 1, pageSize: 10 })

const flatCategories = computed(() => {
  const result = []
  function walk(list, prefix = '') {
    for (const c of list) {
      result.push({ ...c, categoryName: prefix + c.categoryName })
      if (c.children?.length) walk(c.children, prefix + c.categoryName + ' / ')
    }
  }
  walk(allCategories.value)
  return result
})

onMounted(() => {
  loadProducts()
  loadCategories()
  loadBrands()
})

async function loadProducts() {
  loading.value = true
  try {
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.categoryId) params.categoryId = query.categoryId
    if (query.status) params.status = query.status
    const res = await productAPI.getList(params)
    products.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function loadCategories() {
  try {
    const res = await productAPI.getCategories()
    allCategories.value = res.data || []
  } catch (e) { console.error(e) }
}

async function loadBrands() {
  try {
    const res = await brandAPI.getList({ pageSize: 200 })
    brands.value = res.data?.records || res.data || []
  } catch (e) { console.error(e) }
}

function doSearch() { query.pageNum = 1; loadProducts() }
function resetQuery() { Object.assign(query, { keyword: '', categoryId: null, status: '', pageNum: 1 }); loadProducts() }

function openAdd() {
  isEdit.value = false
  activeTab.value = 'basic'
  form.value = { status: 'active', isRecommend: 0, stock: 0, stockWarning: 5 }
  showDialog.value = true
}

async function openEdit(row) {
  isEdit.value = true
  activeTab.value = 'basic'
  form.value = { ...row }
  showDialog.value = true
  loadSkus(row.productId)
}

async function loadSkus(productId) {
  try {
    const res = await productAPI.getSkus(productId)
    skuList.value = res.data || []
  } catch (e) { skuList.value = [] }
}

async function saveProduct() {
  if (!form.value.productName) { ElMessage.warning('请填写商品名称'); return }
  try {
    if (isEdit.value) {
      await productAPI.update(form.value.productId, form.value)
    } else {
      await productAPI.add(form.value)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadProducts()
  } catch (e) { console.error(e) }
}

async function toggleStatus(row) {
  const next = row.status === 'active' ? 'off_shelf' : 'active'
  try {
    await productAPI.updateStatus(row.productId, next)
    ElMessage.success('状态已更新')
    loadProducts()
  } catch (e) { console.error(e) }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', { type: 'warning' })
    await productAPI.delete(id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

function addSkuRow() {
  skuList.value.push({ skuName: '', skuSpec: '', price: 0, stock: 0, barcode: '', status: 'active' })
}

async function removeSkuRow(index, row) {
  if (row.skuId) {
    try {
      await productAPI.deleteSku(row.skuId)
      ElMessage.success('已删除')
    } catch (e) { console.error(e) }
  }
  skuList.value.splice(index, 1)
}

async function saveSkus() {
  const productId = form.value.productId
  const items = skuList.value.filter(s => s.skuName)
  try {
    await productAPI.saveSkus(productId, items)
    ElMessage.success('SKU已保存')
    loadSkus(productId)
  } catch (e) { console.error(e) }
}
</script>

<style scoped>
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
