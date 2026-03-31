<template>
  <div class="page-container">
    <h2>我的收藏</h2>
    <el-row :gutter="16" v-loading="loading">
      <el-col v-for="item in list" :key="item.productId || item.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card shadow="hover" class="card">
          <div class="name">{{ item.productName || item.name }}</div>
          <div class="price">¥{{ item.price }}</div>
          <div class="actions">
            <el-button type="primary" link @click="$router.push('/products/' + (item.productId || item.id))">查看</el-button>
            <el-button type="danger" link @click="remove(item)">取消收藏</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && !list.length" description="暂无收藏" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { favoriteAPI } from '@/api'

const list = ref([])
const loading = ref(false)

onMounted(() => load())

const load = async () => {
  loading.value = true
  try {
    const res = await favoriteAPI.getMyFavorites()
    list.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const remove = async (item) => {
  const id = item.productId || item.id
  try {
    await ElMessageBox.confirm('确定取消收藏？', '提示', { type: 'warning' })
    await favoriteAPI.remove(id)
    ElMessage.success('已取消')
    load()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.card {
  margin-bottom: 16px;
}
.name {
  font-weight: bold;
  margin-bottom: 8px;
}
.price {
  color: #f56c6c;
  margin-bottom: 8px;
}
.actions {
  display: flex;
  gap: 8px;
}
</style>
