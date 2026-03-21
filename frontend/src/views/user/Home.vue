<template>
  <div class="page-container">
    <h2>首页</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">￥{{ stats.totalAmount || 0 }}</div>
            <div class="stat-label">总消费金额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top: 20px">
      <template #header>
        <span>热门商品推荐</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="6" v-for="product in hotProducts" :key="product.productId">
          <el-card shadow="hover">
            <h4>{{ product.productName }}</h4>
            <p class="price">￥{{ product.price }}</p>
            <el-button type="primary" size="small" @click="addToCart(product)">加入购物车</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { productAPI, cartAPI } from '@/api'

const stats = ref({})
const hotProducts = ref([])

onMounted(() => {
  loadHotProducts()
})

const loadHotProducts = async () => {
  try {
    const res = await productAPI.getList({ pageNum: 1, pageSize: 4 })
    hotProducts.value = res.data.records || res.data || []
  } catch (error) {
    console.error(error)
  }
}

const addToCart = async (product) => {
  try {
    await cartAPI.add(product.productId, 1)
    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.stat-card {
  text-align: center;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}
.stat-label {
  margin-top: 10px;
  color: #666;
}
.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
  margin: 10px 0;
}
</style>
