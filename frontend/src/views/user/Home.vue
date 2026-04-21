<template>
  <div class="page-container">
    <!-- Banner 轮播图 -->
    <el-carousel height="300px" class="banner-carousel">
      <el-carousel-item v-for="banner in banners" :key="banner.bannerId">
        <img :src="banner.imageUrl" :alt="banner.title" class="banner-img" @click="handleBannerClick(banner)" />
      </el-carousel-item>
    </el-carousel>

    <!-- 秒杀活动入口 -->
    <el-card v-if="seckillActivities.length" class="section-card">
      <template #header>
        <div class="section-header">
          <span class="section-title">🔥 限时秒杀</span>
          <el-button link type="primary" @click="$router.push('/seckill')">查看全部</el-button>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="sp in seckillProducts" :key="sp.seckillProductId">
          <el-card shadow="hover" class="product-card" @click="goToProduct(sp.productId)">
            <img :src="sp.coverImage || sp.imageUrl" class="product-img" />
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
          <span class="section-title">热销商品</span>
          <el-button link type="primary" @click="$router.push('/products')">查看更多</el-button>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="product in hotProducts" :key="product.productId">
          <el-card shadow="hover" class="product-card" @click="goToProduct(product.productId)">
            <img :src="product.coverImage" class="product-img" />
            <div class="product-name">{{ product.productName }}</div>
            <div class="price-row">
              <span class="price-tag">￥{{ product.price }}</span>
              <span class="sales-count">已售 {{ product.salesCount }}</span>
            </div>
            <el-button type="primary" size="small" @click.stop="addToCart(product)">加入购物车</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 新品上市 -->
    <el-card class="section-card">
      <template #header>
        <div class="section-header">
          <span class="section-title">新品上市</span>
          <el-button link type="primary" @click="$router.push('/products?sort=new')">查看更多</el-button>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="product in newProducts" :key="product.productId">
          <el-card shadow="hover" class="product-card" @click="goToProduct(product.productId)">
            <img :src="product.coverImage" class="product-img" />
            <div class="product-name">{{ product.productName }}</div>
            <div class="price-row">
              <span class="price-tag">￥{{ product.price }}</span>
              <el-tag size="small" type="success">新品</el-tag>
            </div>
            <el-button type="primary" size="small" @click.stop="addToCart(product)">加入购物车</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 推荐商品 -->
    <el-card class="section-card">
      <template #header>
        <span class="section-title">猜你喜欢</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="product in recommendedProducts" :key="product.productId">
          <el-card shadow="hover" class="product-card" @click="goToProduct(product.productId)">
            <img :src="product.coverImage" class="product-img" />
            <div class="product-name">{{ product.productName }}</div>
            <div class="price-row">
              <span class="price-tag">￥{{ product.price }}</span>
            </div>
            <el-button type="primary" size="small" @click.stop="addToCart(product)">加入购物车</el-button>
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

const router = useRouter()
const banners = ref([])
const hotProducts = ref([])
const newProducts = ref([])
const recommendedProducts = ref([])
const seckillActivities = ref([])
const seckillProducts = ref([])

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
    const res = await productAPI.getList({ sortBy: 'salesCount', sortOrder: 'desc', pageSize: 8, pageNum: 1 })
    hotProducts.value = res.data?.records || []
  } catch (e) { console.error(e) }
}

const loadNewProducts = async () => {
  try {
    const res = await productAPI.getList({ sortBy: 'createTime', sortOrder: 'desc', pageSize: 8, pageNum: 1 })
    newProducts.value = res.data?.records || []
  } catch (e) { console.error(e) }
}

const loadRecommended = async () => {
  try {
    const res = await productAPI.getRecommended()
    recommendedProducts.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadSeckill = async () => {
  try {
    const res = await seckillAPI.getList({ state: 'running', pageNum: 1, pageSize: 1 })
    const activities = res.data?.records || res.data || []
    seckillActivities.value = activities
    // 取第一个进行中活动的商品
    if (activities.length > 0) {
      const prodRes = await seckillAPI.getActivityProducts(activities[0].seckillId)
      seckillProducts.value = (prodRes.data || []).slice(0, 4)
    }
  } catch (e) { console.error(e) }
}

const goToProduct = (id) => router.push(`/products/${id}`)

const handleBannerClick = (banner) => {
  if (banner.linkType === 'PRODUCT' && banner.linkTarget) {
    router.push(`/products/${banner.linkTarget}`)
  } else if (banner.linkType === 'CATEGORY' && banner.linkTarget) {
    router.push(`/products?categoryId=${banner.linkTarget}`)
  } else if (banner.linkType === 'ACTIVITY') {
    router.push('/seckill')
  }
}

const addToCart = async (product) => {
  try {
    await cartAPI.add(product.productId, 1)
    ElMessage.success('已加入购物车')
  } catch (e) { console.error(e) }
}
</script>

<style scoped>
.banner-carousel { margin-bottom: 20px; border-radius: 8px; overflow: hidden; }
.banner-img { width: 100%; height: 300px; object-fit: cover; cursor: pointer; }
.section-card { margin-bottom: 20px; }
.section-header { display: flex; justify-content: space-between; align-items: center; }
.section-title { font-size: 18px; font-weight: bold; }
.product-card { cursor: pointer; text-align: center; padding: 10px; }
.product-img { width: 100%; height: 140px; object-fit: cover; border-radius: 4px; margin-bottom: 8px; }
.product-name { font-size: 14px; margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.price-tag { color: #f56c6c; font-size: 16px; font-weight: bold; }
.original-price { color: #999; font-size: 12px; text-decoration: line-through; }
.sales-count { color: #999; font-size: 12px; }
.seckill-price { display: flex; gap: 8px; align-items: center; justify-content: center; margin-bottom: 8px; }
</style>
