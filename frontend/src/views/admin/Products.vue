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
          <el-button @click="exportProducts" style="margin-left:8px">📊 导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 商品卡片网格 -->
    <div class="products-grid" v-loading="loading">
      <div class="product-card" v-for="product in products" :key="product.productId" @click="openEdit(product)">
        <!-- 图片容器 -->
        <div class="product-image-container">
          <img 
            class="product-image"
            :src="product.coverImage || getAiImage(product.productName)"
            :alt="product.productName"
          />
          <!-- 折扣角标 -->
          <div class="discount-badge" v-if="product.originalPrice && product.price < product.originalPrice">
            {{ Math.round((product.price / product.originalPrice) * 10) }}折
          </div>
          <!-- 状态标签 -->
          <div class="status-badge" :class="product.status">
            {{ product.status === 'active' ? '上架' : '下架' }}
          </div>
        </div>

        <!-- 卡片信息 -->
        <div class="product-info">
          <h3 class="product-name">{{ product.productName }}</h3>
          <div class="product-price">￥{{ product.price }}</div>
          <div class="product-meta">
            <span class="sales">销量: {{ product.salesCount || 0 }}</span>
            <span class="category">{{ product.categoryName }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="product-actions">
          <el-button size="small" type="primary" @click.stop="openEdit(product)">编辑</el-button>
          <el-button 
            size="small" 
            :type="product.status === 'active' ? 'warning' : 'success'"
            @click.stop="toggleStatus(product)"
          >
            {{ product.status === 'active' ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" type="danger" @click.stop="handleDelete(product.productId)">删除</el-button>
        </div>
      </div>
    </div>
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
import { productAPI, brandAPI, adminAPI } from '@/api'
import { exportToExcel } from '@/utils/exportUtils'

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
    const res = await adminAPI.getProducts(params)
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
    const res = await brandAPI.getAdminList({ pageSize: 200 })
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

function exportProducts() {
  if (products.value.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }
  
  const columns = [
    { label: '商品ID', prop: 'productId' },
    { label: '商品名称', prop: 'productName' },
    { label: '条码', prop: 'barcode' },
    { label: '分类', prop: 'categoryName' },
    { label: '品牌', prop: 'brandName' },
    { label: '售价', prop: 'price' },
    { label: '成本价', prop: 'costPrice' },
    { label: '库存', prop: 'stock' },
    { label: '销量', prop: 'salesCount' },
    { label: '状态', prop: 'status' }
  ]
  
  const exportData = products.value.map(product => ({
    ...product,
    status: product.status === 'active' ? '上架' : '下架'
  }))
  
  try {
    exportToExcel(exportData, columns, '商品管理')
    ElMessage.success('商品列表已导出')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

// 生成 Pollinations.ai 图片 URL
function getAiImage(productName) {
  const prompt = encodeURIComponent(`${productName} product photo white background realistic high quality`)
  return `https://image.pollinations.ai/prompt/${prompt}?width=300&height=300&nologo=true`
}
</script>

<style scoped>
.page-container {
  padding: 16px;
  background: #f5f7fa;
  min-height: 100vh;
}

h2 {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 600;
}

.pagination { 
  margin-top: 16px; 
  display: flex; 
  justify-content: flex-end; 
}

/* 商品卡片网格 */
.products-grid {
  display: grid;
  gap: 16px;
  margin-bottom: 16px;
  /* PC 端: 4-5 列 */
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
}

/* 平板端: 3 列 (768px - 1200px) */
@media (max-width: 1200px) {
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  }
}

/* 手机端: 2 列 */
@media (max-width: 768px) {
  .products-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 卡片样式 */
.product-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: all 0.3s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* 图片容器 */
.product-image-container {
  position: relative;
  width: 100%;
  height: 160px;
  background: #f0f0f0;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 折扣角标 */
.discount-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(255, 59, 48, 0.9);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  z-index: 10;
}

/* 状态标签 */
.status-badge {
  position: absolute;
  bottom: 8px;
  right: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  color: white;
  z-index: 10;
}

.status-badge.active {
  background: rgba(52, 211, 153, 0.9);
}

.status-badge.off_shelf {
  background: rgba(148, 163, 184, 0.9);
}

/* 卡片信息区 */
.product-info {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  margin: 0 0 8px 0;
  font-size: 13px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  height: 28px;
}

.product-price {
  font-size: 16px;
  color: #ff3b30;
  font-weight: 700;
  margin-bottom: 6px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  gap: 8px;
  flex-wrap: wrap;
}

.product-meta .sales {
  flex: 1;
}

.product-meta .category {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 操作按钮区 */
.product-actions {
  padding: 8px 12px;
  display: flex;
  gap: 4px;
  border-top: 1px solid #f0f0f0;
}

.product-actions :deep(.el-button) {
  flex: 1;
  padding: 4px 8px;
  font-size: 12px;
}

.product-actions :deep(.el-button--small) {
  height: 28px;
  line-height: 26px;
}
</style>
