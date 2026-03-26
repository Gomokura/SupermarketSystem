<template>
  <div class="page-container">
    <h2>商品列表</h2>

    <!-- 搜索筛选栏 -->
    <el-card class="filter-card">
      <el-row :gutter="12" align="middle">
        <el-col :span="5">
          <el-input v-model="filters.keyword" placeholder="搜索商品名称" clearable @keyup.enter="search" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="filters.categoryId" placeholder="商品分类" clearable>
            <el-option v-for="cat in categories" :key="cat.categoryId" :label="cat.categoryName" :value="cat.categoryId" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filters.brandId" placeholder="品牌" clearable>
            <el-option v-for="b in brands" :key="b.brandId" :label="b.brandName" :value="b.brandId" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-input-group>
            <el-input v-model="filters.minPrice" placeholder="最低价" type="number" style="width: 90px" />
            <span style="padding: 0 6px; line-height: 32px">-</span>
            <el-input v-model="filters.maxPrice" placeholder="最高价" type="number" style="width: 90px" />
          </el-input-group>
        </el-col>
        <el-col :span="4">
          <el-select v-model="sortKey" placeholder="排序方式">
            <el-option label="综合排序" value="" />
            <el-option label="价格从低到高" value="price_asc" />
            <el-option label="价格从高到低" value="price_desc" />
            <el-option label="销量优先" value="sales" />
            <el-option label="评分优先" value="rating" />
          </el-select>
        </el-col>
        <el-col :span="2">
          <el-button type="primary" @click="search">搜索</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 商品网格 -->
    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in products" :key="product.productId">
        <el-card class="product-card" shadow="hover" @click="goToDetail(product.productId)">
          <img :src="product.coverImage" class="product-img" />
          <div class="product-info">
            <div class="product-name">{{ product.productName }}</div>
            <div class="price-row">
              <span class="price">￥{{ product.price }}</span>
              <span v-if="product.originalPrice > product.price" class="original-price">￥{{ product.originalPrice }}</span>
            </div>
            <div class="meta-row">
              <span class="sales">已售 {{ product.salesCount || 0 }}</span>
              <el-rate :model-value="product.rating || 0" disabled size="small" />
            </div>
          </div>
          <el-button type="primary" size="small" class="cart-btn" @click.stop="addToCart(product)">加入购物车</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && products.length === 0" description="暂无商品" />

    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadProducts"
        @size-change="loadProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productAPI, brandAPI, cartAPI } from '@/api'

const router = useRouter()
const products = ref([])
const categories = ref([])
const brands = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const sortKey = ref('')

const filters = reactive({
  keyword: '',
  categoryId: '',
  brandId: '',
  minPrice: '',
  maxPrice: ''
})

const sortMap = {
  '': { sortBy: '', sortOrder: '' },
  price_asc: { sortBy: 'price', sortOrder: 'asc' },
  price_desc: { sortBy: 'price', sortOrder: 'desc' },
  sales: { sortBy: 'salesCount', sortOrder: 'desc' },
  rating: { sortBy: 'rating', sortOrder: 'desc' }
}

onMounted(() => {
  loadCategories()
  loadBrands()
  loadProducts()
})

const loadProducts = async () => {
  loading.value = true
  try {
    const sort = sortMap[sortKey.value] || {}
    const res = await productAPI.getList({
      keyword: filters.keyword || undefined,
      categoryId: filters.categoryId || undefined,
      brandId: filters.brandId || undefined,
      minPrice: filters.minPrice || undefined,
      maxPrice: filters.maxPrice || undefined,
      sortBy: sort.sortBy || undefined,
      sortOrder: sort.sortOrder || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await productAPI.getCategoryTree()
    // 展平树形分类
    const flatten = (nodes) => {
      let result = []
      nodes.forEach(n => {
        result.push(n)
        if (n.children) result = result.concat(flatten(n.children))
      })
      return result
    }
    categories.value = flatten(res.data || [])
  } catch (e) { console.error(e) }
}

const loadBrands = async () => {
  try {
    const res = await brandAPI.getList()
    brands.value = res.data || []
  } catch (e) { console.error(e) }
}

const search = () => {
  pageNum.value = 1
  loadProducts()
}

const goToDetail = (id) => router.push(`/products/${id}`)

const addToCart = async (product) => {
  try {
    await cartAPI.add(product.productId, 1)
    ElMessage.success('已加入购物车')
  } catch (e) { console.error(e) }
}
</script>

<style scoped>
.filter-card { margin-bottom: 20px; }
.product-card { margin-bottom: 16px; cursor: pointer; }
.product-img { width: 100%; height: 160px; object-fit: cover; border-radius: 4px; }
.product-info { padding: 8px 0; }
.product-name { font-size: 14px; font-weight: 500; margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price-row { display: flex; align-items: baseline; gap: 8px; margin-bottom: 4px; }
.price { color: #f56c6c; font-size: 18px; font-weight: bold; }
.original-price { color: #999; font-size: 12px; text-decoration: line-through; }
.meta-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.sales { color: #999; font-size: 12px; }
.cart-btn { width: 100%; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
