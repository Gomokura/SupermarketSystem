<template>
  <div class="page-container">
    <!-- Banner 轮播图 -->
    <el-carousel height="180px" class="banner-carousel" :autoplay="banners.length > 0">
      <el-carousel-item v-if="banners.length === 0">
        <div class="banner-placeholder">
          <img :src="getBannerImage(0)" class="banner-img" alt="超市促销" />
          <div class="banner-overlay">
            <h2>欢迎光临鲜惠超市</h2>
            <p>新鲜食材 · 优惠每天</p>
          </div>
        </div>
      </el-carousel-item>
      <el-carousel-item v-for="banner in banners" :key="banner.bannerId">
        <div class="banner-placeholder">
          <img
            :src="banner.imageUrl || getBannerImage(banner.bannerId)"
            :alt="banner.title"
            class="banner-img"
            @click="handleBannerClick(banner)"
            @error="e => e.target.src = getBannerImage(banner.bannerId)"
          />
          <div class="banner-overlay" v-if="banner.title">
            <h2>{{ banner.title }}</h2>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 快捷入口 -->
    <div class="quick-entry">
      <div class="entry-item" v-for="item in quickEntries" :key="item.label" @click="$router.push(item.path)">
        <div class="entry-icon" :style="{ background: item.color }">{{ item.icon }}</div>
        <span>{{ item.label }}</span>
      </div>
    </div>

    <!-- 秒杀活动入口 -->
    <el-card v-if="seckillActivities.length" class="section-card">
      <template #header>
        <div class="section-header">
          <span class="section-title">🔥 限时秒杀</span>
          <el-button link type="primary" @click="$router.push('/seckill')">查看全部 →</el-button>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="sp in seckillProducts" :key="sp.seckillProductId">
          <el-card shadow="hover" class="product-card" @click="goToProduct(sp.productId)">
            <img :src="imgSrc(sp.productId, sp.coverImage || sp.imageUrl)" class="product-img"
              @error="e => e.target.src = imgPlaceholder(sp.productId)" />
            <div class="product-name">{{ sp.productName }}</div>
            <div class="seckill-price">
              <span class="price-tag">￥{{ sp.seckillPrice }}</span>
              <span class="original-price">￥{{ sp.originalPrice }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 促销活动 -->
    <el-card v-if="promotions.length" class="section-card promotion-section">
      <template #header>
        <div class="section-header">
          <span class="section-title">🎁 今日促销</span>
          <el-button link type="primary" @click="$router.push('/products')">去选购 →</el-button>
        </div>
      </template>
      <div class="promotion-list">
        <div class="promotion-item" v-for="activity in promotions" :key="activity.activityId" @click="$router.push('/products')">
          <div class="promotion-main">
            <span class="promotion-name">{{ activity.title }}</span>
            <el-tag size="small" :type="activity.promoType === 'FULL_REDUCE' ? 'danger' : 'success'">
              {{ promoTypeLabel(activity.promoType) }}
            </el-tag>
          </div>
          <div class="promotion-meta">
            {{ activity.scopeType === 'CATEGORY' ? '指定分类可用' : '全场可用' }} · {{ formatDate(activity.endTime) }} 截止
          </div>
        </div>
      </div>
    </el-card>

    <!-- 热销商品 -->
    <el-card class="section-card">
      <template #header>
        <div class="section-header">
          <span class="section-title">🔥 热销商品</span>
          <el-button link type="primary" @click="$router.push('/products')">查看更多 →</el-button>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="product in hotProducts" :key="product.productId">
          <el-card shadow="hover" class="product-card" @click="goToProduct(product.productId)">
            <img :src="imgSrc(product.productId, product.coverImage)" class="product-img"
              @error="e => e.target.src = imgPlaceholder(product.productId)" />
            <div class="product-name">{{ product.productName }}</div>
            <div class="price-row">
              <span class="price-tag">￥{{ product.price }}</span>
              <span class="sales-count">已售 {{ product.salesCount }}</span>
            </div>
            <el-button type="primary" size="small" style="width:100%;margin-top:6px" @click.stop="addToCart(product)">加入购物车</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 新品上市 -->
    <el-card class="section-card">
      <template #header>
        <div class="section-header">
          <span class="section-title">✨ 新品上市</span>
          <el-button link type="primary" @click="$router.push('/products?sort=new')">查看更多 →</el-button>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="product in newProducts" :key="product.productId">
          <el-card shadow="hover" class="product-card" @click="goToProduct(product.productId)">
            <img :src="imgSrc(product.productId, product.coverImage)" class="product-img"
              @error="e => e.target.src = imgPlaceholder(product.productId)" />
            <div class="product-name">{{ product.productName }}</div>
            <div class="price-row">
              <span class="price-tag">￥{{ product.price }}</span>
              <el-tag size="small" type="success">新品</el-tag>
            </div>
            <el-button type="primary" size="small" style="width:100%;margin-top:6px" @click.stop="addToCart(product)">加入购物车</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 猜你喜欢 -->
    <el-card class="section-card">
      <template #header>
        <span class="section-title">💡 猜你喜欢</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="product in recommendedProducts" :key="product.productId">
          <el-card shadow="hover" class="product-card" @click="goToProduct(product.productId)">
            <img :src="imgSrc(product.productId, product.coverImage)" class="product-img"
              @error="e => e.target.src = imgPlaceholder(product.productId)" />
            <div class="product-name">{{ product.productName }}</div>
            <div class="price-row">
              <span class="price-tag">￥{{ product.price }}</span>
            </div>
            <el-button type="primary" size="small" style="width:100%;margin-top:6px" @click.stop="addToCart(product)">加入购物车</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { bannerAPI, productAPI, seckillAPI, promotionAPI, cartAPI } from '@/api'
import { getProductImage, getBannerImage } from '@/utils/image'

const router = useRouter()
const banners = ref([])
const hotProducts = ref([])
const newProducts = ref([])
const recommendedProducts = ref([])
const seckillActivities = ref([])
const seckillProducts = ref([])
const promotions = ref([])

const quickEntries = [
  { icon: '🥤', label: '饮料', path: '/products?categoryId=1', color: '#f0fff4' },
  { icon: '🍪', label: '食品', path: '/products?categoryId=2', color: '#fff8f0' },
  { icon: '🧴', label: '日用品', path: '/products?categoryId=3', color: '#f0f8ff' },
  { icon: '⚡', label: '限时秒杀', path: '/seckill', color: '#fff8f0' },
  { icon: '🛒', label: '购物车', path: '/cart', color: '#f5f0ff' },
  { icon: '📦', label: '我的订单', path: '/orders', color: '#f0f0ff' },
]

const imgPlaceholder = (id) => getProductImage(id, 'landscape_4_3')
const imgSrc = (id, url) => (url && url.trim()) ? url : imgPlaceholder(id)

onMounted(() => {
  loadBanners()
  loadHotProducts()
  loadNewProducts()
  loadRecommended()
  loadSeckill()
  loadPromotions()
})

const loadBanners = async () => {
  try {
    const res = await bannerAPI.getList()
    banners.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadHotProducts = async () => {
  try {
    const res = await productAPI.getTopSales(8)
    hotProducts.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadNewProducts = async () => {
  try {
    const res = await productAPI.getNew(8)
    newProducts.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadRecommended = async () => {
  try {
    const res = await productAPI.getRecommended()
    const list = res.data || []
    if (list.length === 0) {
      // 兜底：取热销商品作为推荐
      const hotRes = await productAPI.getTopSales(8)
      recommendedProducts.value = hotRes.data || []
    } else {
      recommendedProducts.value = list
    }
  } catch {
    // 异常兜底：取热销商品
    try {
      const hotRes = await productAPI.getTopSales(8)
      recommendedProducts.value = hotRes.data || []
    } catch {}
  }
}

const loadSeckill = async () => {
  try {
    const res = await seckillAPI.getList({ state: 'running', pageNum: 1, pageSize: 1 })
    const activities = res.data?.records || res.data || []
    seckillActivities.value = activities
    if (activities.length > 0) {
      const prodRes = await seckillAPI.getActivityProducts(activities[0].seckillId)
      seckillProducts.value = (prodRes.data || []).slice(0, 4)
    }
  } catch (e) { console.error(e) }
}

const loadPromotions = async () => {
  try {
    const res = await promotionAPI.getActive()
    promotions.value = (res.data || [])
      .filter(item => item.promoType !== 'SECKILL')
      .slice(0, 4)
  } catch (e) { console.error(e) }
}

const promoTypeLabel = (type) => ({
  FULL_REDUCE: '满减',
  DISCOUNT: '折扣',
  SECKILL: '秒杀'
})[type] || '活动'

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const goToProduct = (id) => router.push(`/products/${id}`)

const handleBannerClick = (banner) => {
  if (banner.linkType === 'product' && banner.linkTarget) {
    router.push(`/products/${banner.linkTarget}`)
  } else if (banner.linkType === 'category' && banner.linkTarget) {
    router.push(`/products?categoryId=${banner.linkTarget}`)
  } else if (banner.linkType === 'activity') {
    router.push('/seckill')
  }
}

const addToCart = async (product) => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await cartAPI.add(product.productId, 1)
    ElMessage.success('已加入购物车')
  } catch (e) { console.error(e) }
}
</script>

<style scoped>
.page-container { padding: 8px; background: #f5f5f5; min-height: 100%; }

/* 轮播图 - 改为更合理的高度 */
.banner-carousel { margin-bottom: 8px; border-radius: 8px; overflow: hidden; height: 180px !important; }
.banner-placeholder { position: relative; width: 100%; height: 180px; }
.banner-img { width: 100%; height: 180px; object-fit: cover; cursor: pointer; display: block; }
.banner-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.5));
  color: white; padding: 10px 14px;
}
.banner-overlay h2 { margin: 0 0 2px; font-size: 14px; font-weight: 600; }
.banner-overlay p { margin: 0; font-size: 11px; opacity: 0.85; }

/* 快捷入口 - 优化大小 */
.quick-entry {
  display: flex; gap: 0; margin-bottom: 8px;
  background: #fff; border-radius: 8px; padding: 8px 0;
}
.entry-item {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 3px; cursor: pointer;
  padding: 6px 0;
  transition: background 0.3s;
}
.entry-item:active { background: #f0f0f0; }
.entry-icon {
  width: 36px; height: 36px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; font-size: 18px;
}
.entry-item span { font-size: 10px; color: #666; text-align: center; line-height: 1; }

/* 分类卡片 */
.section-card { margin-bottom: 8px; border-radius: 8px; }
.section-card :deep(.el-card__body) { padding: 8px; }
.section-card :deep(.el-card__header) { padding: 6px 8px; }
.section-header { display: flex; justify-content: space-between; align-items: center; }
.section-title { font-size: 13px; font-weight: 600; }

/* 商品卡片 - 减小尺寸 */
.product-card { cursor: pointer; }
.product-card :deep(.el-card__body) { padding: 5px; }

/* 商品图片 - 固定高度，防止溢出 */
.product-img { 
  width: 100%; 
  height: 70px;          /* 减小高度 */
  object-fit: cover; 
  border-radius: 4px; 
  margin-bottom: 4px; 
  display: block;
  background: #f0f0f0;
}

.product-name { 
  font-size: 10px; 
  margin-bottom: 3px; 
  overflow: hidden; 
  text-overflow: ellipsis; 
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.2;
  height: 24px;
  color: #333;
}

.price-row { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: 3px; 
  font-size: 11px;
}

.price-tag { 
  color: #ff4d4f; 
  font-size: 12px; 
  font-weight: bold; 
}

.original-price { 
  color: #ccc; 
  font-size: 9px; 
  text-decoration: line-through; 
}

.sales-count { 
  color: #bbb; 
  font-size: 9px; 
}

.seckill-price { 
  display: flex; 
  gap: 4px; 
  align-items: center; 
  justify-content: center; 
  margin-bottom: 3px; 
  font-size: 10px;
}

.promotion-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.promotion-item {
  border: 1px solid #f2d7bf;
  border-radius: 8px;
  padding: 8px;
  background: #fffaf4;
  cursor: pointer;
}

.promotion-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.promotion-name {
  font-size: 12px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.promotion-meta {
  margin-top: 5px;
  font-size: 10px;
  color: #8a6d3b;
}

/* 按钮尺寸 */
.section-card :deep(.el-button) {
  padding: 4px 8px;
  font-size: 11px;
  height: 24px;
  line-height: 22px;
}
</style>
