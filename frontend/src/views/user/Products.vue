<template>
  <div class="products-wrap">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索商品..."
        clearable
        @keyup.enter="search"
        @clear="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <!-- 分类快捷滚动 -->
    <div class="cat-scroll">
      <div
        class="cat-chip"
        :class="{ active: !filters.categoryId }"
        @click="setCat('')"
      >全部</div>
      <div
        v-for="cat in categories"
        :key="cat.categoryId"
        class="cat-chip"
        :class="{ active: filters.categoryId == cat.categoryId }"
        @click="setCat(cat.categoryId)"
      >{{ cat.categoryName }}</div>
    </div>

    <!-- 排序栏 -->
    <div class="sort-bar">
      <div
        v-for="s in sortOptions"
        :key="s.value"
        class="sort-item"
        :class="{ active: sortKey === s.value }"
        @click="setSort(s.value)"
      >
        {{ s.label }}
        <span v-if="s.value && sortKey === s.value" class="sort-icon">↓</span>
      </div>
      <div class="sort-item" @click="showFilter = true">
        <el-icon><Filter /></el-icon> 筛选
      </div>
    </div>

    <!-- 商品网格 -->
    <div v-if="loading && products.length === 0" style="text-align:center;padding:60px">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>
    <el-empty v-else-if="!loading && products.length === 0" description="暂无商品" style="padding:60px 0" />

    <div v-else class="product-grid">
      <div
        class="product-card"
        v-for="product in products"
        :key="product.productId"
        @click="$router.push(`/products/${product.productId}`)"
      >
        <div class="card-img-wrap">
          <img
            :src="product.coverImage || imgFallback(product.productId)"
            class="card-img"
            @error="e => e.target.src = 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22300%22 height=%22300%22%3E%3Crect fill=%22%23f5f5f5%22 width=%22300%22 height=%22300%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 text-anchor=%22middle%22 dy=%22.3em%22 fill=%22%23999%22%3E图片加载中...%3C/text%3E%3C/svg%3E'"
          />
          <div class="discount-tag" v-if="product.originalPrice > product.price">
            {{ Math.round(product.price / product.originalPrice * 10) }}折
          </div>
        </div>
        <div class="card-body">
          <div class="card-name">{{ product.productName }}</div>
          <div class="card-sub">已售 {{ product.salesCount || 0 }}</div>
          <div class="card-foot">
            <span class="card-price">￥{{ product.price }}</span>
            <el-button
              type="primary" size="small" circle
              @click.stop="addToCart(product)"
              style="background:#ff4d4f;border:none"
            ><el-icon><Plus /></el-icon></el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hasMore" style="padding:12px;text-align:center">
      <el-button @click="loadMore" :loading="loading" round>加载更多</el-button>
    </div>
    <div v-else-if="products.length > 0" style="padding:12px;text-align:center;font-size:12px;color:#bbb">
      已加载全部商品
    </div>

    <!-- 筛选抽屉 -->
    <el-drawer v-model="showFilter" title="高级筛选" direction="btt" size="auto">
      <div style="padding:0 16px 16px">
        <div class="filter-row">
          <span class="filter-lbl">品牌</span>
          <el-select v-model="filters.brandId" placeholder="不限" clearable style="flex:1">
            <el-option v-for="b in brands" :key="b.brandId" :label="b.brandName" :value="b.brandId" />
          </el-select>
        </div>
        <div class="filter-row">
          <span class="filter-lbl">价格区间</span>
          <div style="display:flex;gap:8px;flex:1;align-items:center">
            <el-input v-model="filters.minPrice" placeholder="最低" type="number" style="flex:1" />
            <span style="color:#ccc">—</span>
            <el-input v-model="filters.maxPrice" placeholder="最高" type="number" style="flex:1" />
          </div>
        </div>
        <div style="display:flex;gap:10px;margin-top:16px">
          <el-button @click="resetFilter" style="flex:1">重置</el-button>
          <el-button type="primary" @click="applyFilter" style="flex:2">确定</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Filter, Loading, Plus } from '@element-plus/icons-vue'
import { productAPI, brandAPI, cartAPI } from '@/api'
import { getProductImage } from '@/utils/image'

const route = useRoute()
const router = useRouter()

const products = ref([])
const categories = ref([])
const brands = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const sortKey = ref('')
const showFilter = ref(false)

const filters = reactive({
  keyword: '', categoryId: '', brandId: '', minPrice: '', maxPrice: ''
})

const sortOptions = [
  { label: '综合', value: '' },
  { label: '销量', value: 'sales' },
  { label: '价格↑', value: 'price_asc' },
  { label: '价格↓', value: 'price_desc' },
]

const sortMap = {
  '': {}, price_asc: { sortBy: 'price', sortOrder: 'asc' },
  price_desc: { sortBy: 'price', sortOrder: 'desc' },
  sales: { sortBy: 'salesCount', sortOrder: 'desc' },
}

const hasMore = ref(false)
const imgFallback = (id) => getProductImage(id)

onMounted(() => {
  // 从URL参数初始化
  if (route.query.categoryId) filters.categoryId = route.query.categoryId
  if (route.query.keyword) filters.keyword = route.query.keyword
  loadCategories()
  loadBrands()
  loadProducts(true)
})

const loadProducts = async (reset = false) => {
  if (reset) { pageNum.value = 1; products.value = [] }
  loading.value = true
  try {
    const sort = sortMap[sortKey.value] || {}
    const res = await productAPI.getList({
      keyword: filters.keyword || undefined,
      categoryId: filters.categoryId || undefined,
      brandId: filters.brandId || undefined,
      minPrice: filters.minPrice || undefined,
      maxPrice: filters.maxPrice || undefined,
      ...sort, pageNum: pageNum.value, pageSize: pageSize.value
    })
    const records = res.data?.records || []
    if (reset) products.value = records
    else products.value = [...products.value, ...records]
    total.value = res.data?.total || 0
    hasMore.value = products.value.length < total.value
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const loadMore = () => {
  pageNum.value++
  loadProducts(false)
}

const loadCategories = async () => {
  try {
    const res = await productAPI.getCategoryTree()
    const flatten = (nodes) => {
      let r = []
      nodes.forEach(n => { r.push(n); if (n.children) r = r.concat(flatten(n.children)) })
      return r
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

const search = () => loadProducts(true)

const setCat = (id) => {
  filters.categoryId = id
  loadProducts(true)
}

const setSort = (v) => {
  sortKey.value = v
  loadProducts(true)
}

const applyFilter = () => {
  showFilter.value = false
  loadProducts(true)
}

const resetFilter = () => {
  filters.brandId = ''
  filters.minPrice = ''
  filters.maxPrice = ''
}

const addToCart = async (product) => {
  const token = localStorage.getItem('token')
  if (!token) { ElMessage.warning('请先登录'); router.push('/login'); return }
  try {
    await cartAPI.add(product.productId, 1)
    ElMessage.success('已加入购物车')
  } catch (e) { console.error(e) }
}
</script>

<style scoped>
.products-wrap { background: #f5f5f5; min-height: 100%; }

.search-bar { padding: 10px 12px; background: #fff; }

.cat-scroll {
  display: flex; overflow-x: auto; gap: 8px;
  padding: 10px 12px; background: #fff;
  border-top: 1px solid #f0f0f0; scrollbar-width: none;
}
.cat-scroll::-webkit-scrollbar { display: none; }
.cat-chip {
  flex-shrink: 0; padding: 4px 12px; border-radius: 16px;
  font-size: 12px; background: #f5f5f5; color: #666; cursor: pointer; white-space: nowrap;
}
.cat-chip.active { background: #ff4d4f; color: #fff; }

.sort-bar {
  display: flex; background: #fff; border-top: 1px solid #f0f0f0;
  margin-bottom: 10px;
}
.sort-item {
  flex: 1; padding: 10px 4px; text-align: center; font-size: 13px;
  color: #666; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 2px;
}
.sort-item.active { color: #ff4d4f; font-weight: bold; }

.product-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 8px; padding: 0 8px 8px;
}

.product-card { background: #fff; border-radius: 10px; overflow: hidden; cursor: pointer; }

.card-img-wrap { position: relative; }
.card-img { width: 100%; aspect-ratio: 1; object-fit: cover; display: block; }
.discount-tag {
  position: absolute; top: 6px; left: 6px;
  background: #ff4d4f; color: #fff; font-size: 11px;
  padding: 1px 5px; border-radius: 4px;
}

.card-body { padding: 8px 10px 10px; }
.card-name { font-size: 13px; color: #333; margin-bottom: 3px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.card-sub { font-size: 11px; color: #bbb; margin-bottom: 6px; }
.card-foot { display: flex; justify-content: space-between; align-items: center; }
.card-price { color: #ff4d4f; font-weight: bold; font-size: 16px; }

.filter-row { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.filter-lbl { font-size: 14px; color: #333; white-space: nowrap; min-width: 56px; }
</style>
