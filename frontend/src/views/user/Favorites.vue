<template>
  <div class="page-wrap">
    <div class="page-header">
      <h2 class="page-title">我的收藏</h2>
      <span class="count" v-if="favorites.length">共 {{ favorites.length }} 件</span>
    </div>

    <div v-if="loading" style="text-align:center;padding:40px">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <el-empty v-else-if="favorites.length === 0"
      description="还没有收藏任何商品" :image-size="100" style="padding:40px 0">
      <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
    </el-empty>

    <div v-else class="product-grid">
      <div class="product-item" v-for="item in favorites" :key="item.productId">
        <div class="product-img-wrap" @click="$router.push(`/products/${item.productId}`)">
          <img
            :src="item.coverImage || item.imageUrl || placeholder(item.productId)"
            class="product-img"
            @error="e => e.target.src = placeholder(item.productId)"
          />
          <el-tag v-if="item.status === 0" size="small" type="info" class="status-tag">已下架</el-tag>
        </div>
        <div class="product-info" @click="$router.push(`/products/${item.productId}`)">
          <div class="product-name">{{ item.productName }}</div>
          <div class="price-row">
            <span class="price">￥{{ item.price }}</span>
            <span class="original" v-if="item.originalPrice && item.originalPrice > item.price">
              ￥{{ item.originalPrice }}
            </span>
          </div>
        </div>
        <div class="product-actions">
          <el-button
            type="primary" size="small" style="flex:1"
            :disabled="item.status === 0 || item.stock === 0"
            @click="addToCart(item)"
          >
            {{ item.stock === 0 ? '已售罄' : '加购' }}
          </el-button>
          <el-button size="small" type="danger" plain @click="unfavorite(item.productId)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { favoritesAPI, cartAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const favorites = ref([])
const loading = ref(false)

const placeholder = (id) => `https://picsum.photos/seed/p${id}/300/300`

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await favoritesAPI.getList()
    favorites.value = res.data?.records || res.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const unfavorite = async (id) => {
  try {
    await ElMessageBox.confirm('确定取消收藏吗？', { type: 'warning', confirmButtonText: '取消收藏', cancelButtonText: '保留' })
    await favoritesAPI.remove(id)
    ElMessage.success('已取消收藏')
    favorites.value = favorites.value.filter(f => f.productId !== id)
  } catch {}
}

const addToCart = async (item) => {
  try {
    await cartAPI.add(item.productId, 1)
    ElMessage.success('已加入购物车')
  } catch (e) { console.error(e) }
}

onMounted(loadFavorites)
</script>

<style scoped>
.page-wrap { padding: 14px 12px; }
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;
}
.page-title { font-size: 17px; font-weight: bold; color: #222; margin: 0; }
.count { font-size: 13px; color: #aaa; }

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.product-item {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(0,0,0,0.07);
}

.product-img-wrap {
  position: relative;
  cursor: pointer;
}
.product-img {
  width: 100%; aspect-ratio: 1; object-fit: cover; display: block;
}
.status-tag {
  position: absolute; top: 6px; left: 6px;
}

.product-info {
  padding: 8px 10px 4px;
  cursor: pointer;
}
.product-name {
  font-size: 13px; color: #333;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin-bottom: 4px;
}
.price-row { display: flex; align-items: center; gap: 6px; }
.price { color: #ff4d4f; font-size: 15px; font-weight: bold; }
.original { color: #ccc; font-size: 11px; text-decoration: line-through; }

.product-actions {
  display: flex; gap: 6px; padding: 8px 10px;
}
</style>
