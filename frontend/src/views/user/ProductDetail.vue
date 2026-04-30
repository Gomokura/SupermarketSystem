<template>
  <div class="detail-wrap" v-loading="loading">
    <!-- 返回按钮 -->
    <div class="nav-bar">
      <el-button link @click="$router.back()"><el-icon><ArrowLeft /></el-icon></el-button>
      <span class="nav-title">商品详情</span>
      <el-button link @click="$router.push('/cart')">
        <el-icon><ShoppingCart /></el-icon>
      </el-button>
    </div>

    <template v-if="product.productId">
      <!-- 商品主图 -->
      <div class="img-wrap">
        <img
          :src="product.coverImage || imgFallback(product.productId)"
          class="main-img"
          @error="e => e.target.src = imgFallback(product.productId)"
        />
      </div>

      <!-- 价格信息 -->
      <div class="price-section">
        <div class="price-row">
          <span class="price">￥{{ currentPrice }}</span>
          <span v-if="product.originalPrice > product.price" class="original-price">￥{{ product.originalPrice }}</span>
          <el-tag v-if="isLowStock" type="danger" size="small" effect="plain" style="margin-left:8px">
            仅剩 {{ currentStock }} 件
          </el-tag>
        </div>
        <div class="product-name">{{ product.productName }}</div>
        <div class="product-meta">
          <span v-if="product.brandName" class="meta-tag">{{ product.brandName }}</span>
          <span class="meta-tag">已售 {{ product.salesCount || 0 }}</span>
          <el-rate :model-value="product.rating || 0" disabled size="small" />
        </div>
      </div>

      <!-- SKU规格 -->
      <div class="section" v-if="skus.length">
        <div class="section-label">选择规格</div>
        <div class="sku-tags">
          <div
            v-for="sku in skus"
            :key="sku.skuId"
            class="sku-tag"
            :class="{ selected: selectedSkuId === sku.skuId, disabled: sku.stock === 0 }"
            @click="sku.stock > 0 && (selectedSkuId = sku.skuId)"
          >{{ sku.skuName }}</div>
        </div>
      </div>

      <!-- 数量选择 -->
      <div class="section qty-section">
        <div class="section-label">购买数量</div>
        <el-input-number v-model="quantity" :min="1" :max="currentStock || 999" size="small" />
      </div>

      <!-- 商品详情 -->
      <div class="section">
        <div class="section-label">商品详情</div>
        <p class="description">{{ product.description || '暂无详情说明' }}</p>
      </div>

      <!-- 评价 -->
      <div class="section" v-if="reviews.length">
        <div class="section-label-row">
          <span class="section-label">商品评价</span>
          <span class="review-count">{{ reviews.length }} 条</span>
        </div>
        <div class="review-item" v-for="r in reviews.slice(0, 3)" :key="r.reviewId">
          <div class="review-head">
            <span class="reviewer">{{ r.isAnonymous ? '匿名用户' : r.username }}</span>
            <el-rate :model-value="r.rating" disabled size="small" />
            <span class="review-time">{{ formatDate(r.createTime) }}</span>
          </div>
          <div class="review-tags" v-if="r.tags">
            <el-tag v-for="tag in r.tags.split(',')" :key="tag" size="small" style="margin:2px">{{ tag }}</el-tag>
          </div>
          <p class="review-content">{{ r.content }}</p>
          <div v-if="r.adminReply" class="admin-reply">商家回复：{{ r.adminReply }}</div>
        </div>
      </div>

      <!-- 相关推荐 -->
      <div class="section" v-if="relatedProducts.length">
        <div class="section-label">相关推荐</div>
        <div class="related-list">
          <div
            v-for="p in relatedProducts"
            :key="p.productId"
            class="related-item"
            @click="$router.push(`/products/${p.productId}`)"
          >
            <img :src="p.coverImage || imgFallback(p.productId)" class="related-img"
              @error="e => e.target.src = imgFallback(p.productId)" />
            <div class="related-name">{{ p.productName }}</div>
            <div class="related-price">￥{{ p.price }}</div>
          </div>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="action-bar">
        <div class="action-icons">
          <div class="action-icon" @click="$router.push('/')">
            <el-icon :size="20"><HomeFilled /></el-icon>
            <span>首页</span>
          </div>
          <div class="action-icon" @click="$router.push('/cart')">
            <el-icon :size="20"><ShoppingCart /></el-icon>
            <span>购物车</span>
          </div>
          <div class="action-icon" @click="toggleFavorite" :style="{ color: isFavorite ? '#ff4d4f' : '#666' }">
            <el-icon :size="20"><StarFilled v-if="isFavorite" /><Star v-else /></el-icon>
            <span>{{ isFavorite ? '已收藏' : '收藏' }}</span>
          </div>
        </div>
        <div class="action-btns">
          <el-button @click="addToCart" :disabled="currentStock === 0"
            style="border-radius:0;background:#ff7875;border-color:#ff7875;color:#fff">加入购物车</el-button>
          <el-button type="primary" @click="buyNow" :disabled="currentStock === 0"
            style="border-radius:0 20px 20px 0;background:#ff4d4f;border-color:#ff4d4f">立即购买</el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ShoppingCart, Star, StarFilled, HomeFilled } from '@element-plus/icons-vue'
import { productAPI, cartAPI, reviewAPI, favoriteAPI } from '@/api'

const route = useRoute()
const router = useRouter()

const product = ref({})
const skus = ref([])
const reviews = ref([])
const relatedProducts = ref([])
const selectedSkuId = ref(null)
const quantity = ref(1)
const isFavorite = ref(false)
const loading = ref(false)

const imgFallback = (id) => `https://picsum.photos/seed/p${id}/600/600`

const selectedSku = computed(() => skus.value.find(s => s.skuId === selectedSkuId.value))
const currentPrice = computed(() => selectedSku.value?.price ?? product.value.price ?? 0)
const currentStock = computed(() => selectedSku.value?.stock ?? product.value.stock ?? 0)
const isLowStock = computed(() => currentStock.value > 0 && currentStock.value <= (product.value.warningStock || 10))

onMounted(loadProduct)
watch(() => route.params.id, loadProduct)

const loadProduct = async () => {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const [prodRes, skuRes, reviewRes, favRes] = await Promise.all([
      productAPI.getById(id),
      productAPI.getSkus(id),
      reviewAPI.getByProduct(id, { pageNum: 1, pageSize: 10 }),
      favoriteAPI.getMyFavorites().catch(() => ({ data: [] }))
    ])
    product.value = prodRes.data || {}
    skus.value = skuRes.data || []
    reviews.value = reviewRes.data?.records || reviewRes.data || []

    const favList = favRes.data || []
    isFavorite.value = favList.some(f => (f.productId || f.id) === Number(id))
    if (skus.value.length > 0) selectedSkuId.value = skus.value[0].skuId

    if (product.value.categoryId) {
      const relRes = await productAPI.getList({ categoryId: product.value.categoryId, pageNum: 1, pageSize: 6 })
      relatedProducts.value = (relRes.data?.records || []).filter(p => p.productId !== Number(id)).slice(0, 5)
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const toggleFavorite = async () => {
  const token = localStorage.getItem('token')
  if (!token) { ElMessage.warning('请先登录'); router.push('/login'); return }
  try {
    if (isFavorite.value) {
      await favoriteAPI.remove(product.value.productId)
      ElMessage.success('已取消收藏')
    } else {
      await favoriteAPI.add(product.value.productId)
      ElMessage.success('已收藏')
    }
    isFavorite.value = !isFavorite.value
  } catch (e) { console.error(e) }
}

const addToCart = async () => {
  const token = localStorage.getItem('token')
  if (!token) { ElMessage.warning('请先登录'); router.push('/login'); return }
  try {
    await cartAPI.add(product.value.productId, quantity.value, selectedSkuId.value)
    ElMessage.success('已加入购物车')
  } catch (e) { console.error(e) }
}

const buyNow = async () => {
  await addToCart()
  router.push('/cart')
}

const formatDate = (d) => d ? new Date(d).toLocaleDateString('zh-CN') : ''
</script>

<style scoped>
.detail-wrap { background: #f5f5f5; min-height: 100%; padding-bottom: 70px; }

.nav-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 12px; background: #fff; position: sticky; top: 0; z-index: 10;
}
.nav-title { font-size: 16px; font-weight: bold; }

.img-wrap { background: #fff; }
.main-img { width: 100%; aspect-ratio: 1; object-fit: cover; display: block; }

.price-section { background: #fff; padding: 12px 14px; margin-bottom: 8px; }
.price-row { display: flex; align-items: center; margin-bottom: 8px; flex-wrap: wrap; gap: 6px; }
.price { color: #ff4d4f; font-size: 26px; font-weight: bold; }
.original-price { color: #bbb; font-size: 13px; text-decoration: line-through; }
.product-name { font-size: 16px; color: #222; font-weight: 500; margin-bottom: 8px; line-height: 1.4; }
.product-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.meta-tag { font-size: 12px; color: #888; background: #f5f5f5; padding: 2px 8px; border-radius: 10px; }

.section { background: #fff; padding: 14px; margin-bottom: 8px; }
.section-label { font-size: 14px; font-weight: bold; color: #333; margin-bottom: 10px; }
.section-label-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.review-count { font-size: 12px; color: #999; }

.sku-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.sku-tag {
  padding: 6px 14px; border-radius: 16px; font-size: 13px;
  border: 1px solid #ddd; cursor: pointer; background: #fff; color: #333;
}
.sku-tag.selected { border-color: #ff4d4f; color: #ff4d4f; background: #fff0f0; }
.sku-tag.disabled { opacity: 0.4; cursor: not-allowed; }

.qty-section { display: flex; align-items: center; justify-content: space-between; }

.description { font-size: 14px; color: #555; line-height: 1.6; }

.review-item { border-bottom: 1px solid #f5f5f5; padding-bottom: 12px; margin-bottom: 12px; }
.review-item:last-child { border-bottom: none; margin-bottom: 0; }
.review-head { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; flex-wrap: wrap; }
.reviewer { font-size: 13px; font-weight: 500; }
.review-time { font-size: 11px; color: #bbb; margin-left: auto; }
.review-tags { margin-bottom: 6px; }
.review-content { font-size: 13px; color: #444; }
.admin-reply { background: #f8f8f8; padding: 8px; border-radius: 6px; font-size: 12px; color: #888; margin-top: 6px; }

.related-list { display: flex; gap: 10px; overflow-x: auto; scrollbar-width: none; }
.related-list::-webkit-scrollbar { display: none; }
.related-item { flex-shrink: 0; width: 100px; cursor: pointer; }
.related-img { width: 100px; height: 100px; border-radius: 8px; object-fit: cover; display: block; }
.related-name { font-size: 11px; color: #444; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.related-price { font-size: 13px; color: #ff4d4f; font-weight: bold; }

.action-bar {
  position: fixed; bottom: 56px; left: 0; right: 0;
  display: flex; background: #fff; border-top: 1px solid #f0f0f0;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.06); z-index: 100; height: 54px;
}
.action-icons { display: flex; flex: 0 0 auto; }
.action-icon {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 2px; padding: 0 14px; cursor: pointer; color: #666; font-size: 10px;
}
.action-btns { display: flex; flex: 1; }
.action-btns .el-button { flex: 1; height: 100%; border-radius: 0; margin: 0; }
</style>
