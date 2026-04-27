<template>
  <div class="page-container">
    <el-row :gutter="24">
      <!-- 左侧：商品信息 -->
      <el-col :span="16">
        <el-card>
          <el-row :gutter="20">
            <el-col :span="10">
              <img :src="product.coverImage" :alt="product.productName || '商品封面'" class="main-img" />
            </el-col>
            <el-col :span="14">
              <h2 class="product-name">{{ product.productName }}</h2>
              <div class="brand-tag" v-if="product.brandName">品牌：{{ product.brandName }}</div>

              <div class="price-block">
                <span class="price">￥{{ currentPrice }}</span>
                <span v-if="product.originalPrice > product.price" class="original-price">￥{{ product.originalPrice }}</span>
              </div>

              <div class="stock-block">
                <span v-if="isLowStock" class="low-stock-tip">仅剩 {{ currentStock }} 件</span>
                <span v-else class="stock-normal">库存：{{ currentStock }}</span>
              </div>

              <!-- SKU 规格选择 -->
              <div v-if="skus.length" class="sku-block">
                <div class="sku-label">规格：</div>
                <el-radio-group v-model="selectedSkuId">
                  <el-radio-button
                    v-for="sku in skus"
                    :key="sku.skuId"
                    :value="sku.skuId"
                    :disabled="sku.stock === 0"
                  >
                    {{ sku.skuName }}
                  </el-radio-button>
                </el-radio-group>
              </div>

              <div class="qty-block">
                <span>数量：</span>
                <el-input-number v-model="quantity" :min="1" :max="currentStock" />
              </div>

              <div class="action-block">
                <el-button type="primary" size="large" @click="addToCart" :disabled="currentStock === 0">
                  加入购物车
                </el-button>
                <el-button size="large" @click="buyNow" :disabled="currentStock === 0">立即购买</el-button>
                <el-button size="large" :type="isFavorite ? 'danger' : 'info'" :plain="!isFavorite" @click="toggleFavorite">
                  <el-icon style="margin-right: 4px"><Star v-if="!isFavorite" /><StarFilled v-else /></el-icon>
                  {{ isFavorite ? '已收藏' : '收藏' }}
                </el-button>
              </div>
            </el-col>
          </el-row>

          <!-- 商品描述 -->
          <el-divider />
          <div class="description">
            <h3>商品详情</h3>
            <p>{{ product.description || '暂无详情' }}</p>
          </div>
        </el-card>

        <!-- 商品评价 -->
        <el-card class="reviews-card">
          <template #header>
            <span>商品评价（{{ reviews.length }}条）</span>
          </template>
          <div v-if="reviews.length === 0" class="no-reviews">暂无评价</div>
          <div v-for="review in reviews" :key="review.reviewId" class="review-item">
            <div class="review-header">
              <span class="reviewer">{{ review.isAnonymous ? '匿名用户' : review.username }}</span>
              <el-rate :model-value="review.rating" disabled size="small" />
              <span class="review-time">{{ formatDate(review.createTime) }}</span>
            </div>
            <div class="review-tags" v-if="review.tags">
              <el-tag v-for="tag in review.tags.split(',')" :key="tag" size="small" style="margin-right: 4px">{{ tag }}</el-tag>
            </div>
            <p class="review-content">{{ review.content }}</p>
            <div v-if="review.adminReply" class="admin-reply">
              <span>商家回复：</span>{{ review.adminReply }}
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：相关推荐 -->
      <el-col :span="8">
        <el-card>
          <template #header><span>相关推荐</span></template>
          <div
            v-for="p in relatedProducts"
            :key="p.productId"
            class="related-item"
            @click="$router.push(`/products/${p.productId}`)"
          >
            <img :src="p.coverImage" :alt="p.productName || '相关商品图片'" class="related-img" />
            <div class="related-info">
              <div class="related-name">{{ p.productName }}</div>
              <div class="related-price">￥{{ p.price }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
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

const selectedSku = computed(() => skus.value.find(s => s.skuId === selectedSkuId.value))
const currentPrice = computed(() => selectedSku.value?.price ?? product.value.price ?? 0)
const currentStock = computed(() => selectedSku.value?.stock ?? product.value.stock ?? 0)
const isLowStock = computed(() => currentStock.value > 0 && currentStock.value <= (product.value.warningStock || 10))

onMounted(() => {
  loadProduct()
})

watch(() => route.params.id, () => {
  loadProduct()
})

const loadProduct = async () => {
  const id = route.params.id
  try {
    const [prodRes, skuRes, reviewRes, favRes] = await Promise.all([
      productAPI.getById(id),
      productAPI.getSkus(id),
      reviewAPI.getByProduct(id, { pageNum: 1, pageSize: 10 }),
      favoriteAPI.getMyFavorites()
    ])
    product.value = prodRes.data || {}
    skus.value = skuRes.data || []
    reviews.value = reviewRes.data?.records || reviewRes.data || []

    const favList = favRes.data || []
    isFavorite.value = favList.some(f => (f.productId || f.id) === Number(id))

    if (skus.value.length > 0) {
      selectedSkuId.value = skus.value[0].skuId
    }

    // 加载同分类相关商品
    if (product.value.categoryId) {
      const relRes = await productAPI.getList({
        categoryId: product.value.categoryId,
        pageNum: 1,
        pageSize: 6
      })
      relatedProducts.value = (relRes.data?.records || []).filter(p => p.productId !== Number(id))
    }
  } catch (e) {
    console.error(e)
  }
}

const toggleFavorite = async () => {
  try {
    if (isFavorite.value) {
      await favoriteAPI.remove(product.value.productId)
      ElMessage.success('已取消收藏')
      isFavorite.value = false
    } else {
      await favoriteAPI.add(product.value.productId)
      ElMessage.success('已收藏')
      isFavorite.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

const addToCart = async () => {
  try {
    await cartAPI.add(product.value.productId, quantity.value, selectedSkuId.value)
    ElMessage.success('已加入购物车')
  } catch (e) { console.error(e) }
}

const buyNow = async () => {
  await addToCart()
  router.push('/checkout')
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.main-img { width: 100%; border-radius: 8px; }
.product-name { font-size: 20px; margin-bottom: 8px; }
.brand-tag { color: #666; font-size: 13px; margin-bottom: 12px; }
.price-block { margin-bottom: 12px; }
.price { color: #f56c6c; font-size: 28px; font-weight: bold; }
.original-price { color: #999; font-size: 14px; text-decoration: line-through; margin-left: 10px; }
.stock-block { margin-bottom: 12px; }
.low-stock-tip { color: #e6a23c; font-size: 13px; }
.stock-normal { color: #666; font-size: 13px; }
.sku-block { margin-bottom: 16px; }
.sku-label { font-size: 14px; margin-bottom: 8px; }
.qty-block { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.action-block { display: flex; gap: 12px; }
.description { padding: 10px 0; }
.reviews-card { margin-top: 20px; }
.no-reviews { color: #999; text-align: center; padding: 20px; }
.review-item { border-bottom: 1px solid #f0f0f0; padding: 12px 0; }
.review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.reviewer { font-weight: 500; }
.review-time { color: #999; font-size: 12px; margin-left: auto; }
.review-tags { margin-bottom: 6px; }
.review-content { color: #333; font-size: 14px; }
.admin-reply { background: #f5f5f5; padding: 8px; border-radius: 4px; font-size: 13px; color: #666; margin-top: 6px; }
.related-item { display: flex; gap: 10px; padding: 10px 0; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.related-item:hover { background: #fafafa; }
.related-img { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; flex-shrink: 0; }
.related-info { flex: 1; }
.related-name { font-size: 13px; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.related-price { color: #f56c6c; font-weight: bold; }
</style>
