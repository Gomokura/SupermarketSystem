<template>
  <div class="page-container">
    <!-- Banner 轮播图 -->
    <el-carousel height="280px" class="banner-carousel" :autoplay="banners.length > 0">
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
import { bannerAPI, productAPI, seckillAPI, cartAPI } from '@/api'
import { getProductImage, getBannerImage } from '@/utils/image'

const router = useRouter()
const banners = ref([])
const hotProducts = ref([])
const newProducts = ref([])
const recommendedProducts = ref([])
const seckillActivities = ref([])
const seckillProducts = ref([])

const quickEntries = [
  { icon: '🍎', label: '生鲜食品', path: '/products?categoryId=1', color: '#fff0f0' },
  { icon: '🧴', label: '日用百货', path: '/products?categoryId=2', color: '#f0f8ff' },
  { icon: '🥤', label: '饮料零食', path: '/products?categoryId=3', color: '#f0fff4' },
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
.page-container { padding: 10px; background: #f5f5f5; min-height: 100%; }

.banner-carousel { margin-bottom: 10px; border-radius: 8px; overflow: hidden; }
.banner-placeholder { position: relative; width: 100%; height: 160px; }
.banner-img { width: 100%; height: 160px; object-fit: cover; cursor: pointer; display: block; }
.banner-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.5));
  color: white; padding: 10px 14px;
}
.banner-overlay h2 { margin: 0 0 2px; font-size: 16px; }
.banner-overlay p { margin: 0; font-size: 12px; opacity: 0.85; }

.quick-entry {
  display: flex; gap: 0; margin-bottom: 10px;
  background: #fff; border-radius: 8px; padding: 10px 0;
}
.entry-item {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px; cursor: pointer;
}
.entry-icon {
  width: 40px; height: 40px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; font-size: 20px;
}
.entry-item span { font-size: 11px; color: #555; }

.section-card { margin-bottom: 10px; border-radius: 8px; }
.section-card :deep(.el-card__body) { padding: 10px; }
.section-card :deep(.el-card__header) { padding: 8px 10px; }
.section-header { display: flex; justify-content: space-between; align-items: center; }
.section-title { font-size: 14px; font-weight: bold; }
.product-card { cursor: pointer; }
.product-card :deep(.el-card__body) { padding: 6px; }
.product-img { width: 100%; height: 80px; object-fit: cover; border-radius: 5px; margin-bottom: 5px; display: block; }
.product-name { font-size: 11px; margin-bottom: 3px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 3px; }
.price-tag { color: #ff4d4f; font-size: 13px; font-weight: bold; }
.original-price { color: #ccc; font-size: 10px; text-decoration: line-through; }
.sales-count { color: #bbb; font-size: 10px; }
.seckill-price { display: flex; gap: 5px; align-items: center; justify-content: center; margin-bottom: 4px; }
</style>
