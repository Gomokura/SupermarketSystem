<template>
  <div class="page-container">
    <h2>商品列表</h2>
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索商品" style="width: 200px" clearable @change="loadProducts" />
      <el-select v-model="categoryId" placeholder="选择分类" clearable style="width: 150px" @change="loadProducts">
        <el-option v-for="cat in categories" :key="cat.categoryId" :label="cat.categoryName" :value="cat.categoryId" />
      </el-select>
      <el-button @click="loadProducts">搜索</el-button>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in products" :key="product.productId">
        <el-card class="product-card" shadow="hover">
          <div class="product-info">
            <h3>{{ product.productName }}</h3>
            <p class="price">￥{{ product.price }}</p>
            <p class="stock">库存: {{ product.stock }} {{ product.unit }}</p>
          </div>
          <div class="product-actions">
            <el-input-number v-model="quantity[product.productId]" :min="1" :max="product.stock" size="small" />
            <el-button type="primary" size="small" @click="addToCart(product)">加入购物车</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { productAPI, cartAPI } from '@/api'

const products = ref([])
const categories = ref([])
const keyword = ref('')
const categoryId = ref('')
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const quantity = reactive({})

onMounted(() => {
  loadProducts()
  loadCategories()
})

const loadProducts = async () => {
  try {
    const res = await productAPI.getList({
      keyword: keyword.value,
      categoryId: categoryId.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    products.value = res.data.records || res.data
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const loadCategories = async () => {
  try {
    const res = await productAPI.getCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const addToCart = async (product) => {
  const qty = quantity[product.productId] || 1
  try {
    await cartAPI.add(product.productId, qty)
    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.product-card {
  margin-bottom: 20px;
}

.product-info h3 {
  font-size: 16px;
  margin-bottom: 10px;
}

.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.stock {
  color: #909399;
  font-size: 14px;
  margin: 5px 0;
}

.product-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
</style>
