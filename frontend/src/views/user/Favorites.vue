<template>
  <div class="page-container">
    <h2>我的收藏</h2>
    <div v-if="favorites.length === 0" style="text-align:center; padding: 40px; color:#999">
      暂无收藏记录
    </div>
    <el-row v-else :gutter="20">
      <el-col :span="6" v-for="item in favorites" :key="item.productId" style="margin-bottom:20px;">
        <el-card shadow="hover">
          <el-image :src="item.coverImage || ''" style="width: 100%; height: 200px" fit="cover" />
          <h3 style="margin: 10px 0; font-size: 16px">{{ item.productName }}</h3>
          <p style="color: red; font-size: 18px">￥{{ item.price }}</p>
          <div style="margin-top: 10px; display:flex; justify-content:space-between">
            <el-button type="primary" size="small" @click="$router.push(`/products/${item.productId}`)">查看</el-button>
            <el-button type="danger" size="small" @click="unfavorite(item.productId)">取消收藏</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, onMounted } from "vue"
import { favoritesAPI } from "@/api"
import { ElMessage } from "element-plus"
const favorites = ref([])
const loadFavorites = async () => {
  try {
    const res = await favoritesAPI.getList()
    favorites.value = res.data?.records || res.data || []
  } catch (e) {}
}
const unfavorite = async (id) => {
  try {
    await favoritesAPI.remove(id)
    ElMessage.success("已取消收藏")
    loadFavorites()
  } catch (e) {}
}
onMounted(() => {
  loadFavorites()
})
</script>
