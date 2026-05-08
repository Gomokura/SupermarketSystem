<template>
  <div class="page-container">
    <h2>撰写评价</h2>
    <el-alert title="请为您的订单商品填写评价" type="info" show-icon style="margin-bottom:20px" />
    <el-card v-if="order" shadow="never">
      <el-form ref="formRef" :model="reviewForm" label-width="100px">
        <div v-for="(item, index) in order.items" :key="item.productId" style="margin-bottom: 30px; border-bottom: 1px solid #f0f0f0; padding-bottom: 20px;">
          <div style="display:flex; margin-bottom: 15px;">
              <el-image :src="item.coverImage || item.productImage" style="width: 80px; height: 80px; border-radius: 4px; margin-right: 15px" />
            <div>
              <p style="font-weight:bold">{{ item.productName }}</p>
              <p style="color:#999;font-size:13px">价格: ￥{{ item.unitPrice }} x {{ item.quantity }}</p>
            </div>
          </div>
          <el-form-item label="评分" required>
            <el-rate v-model="reviewsData[index].rating" />
          </el-form-item>
          <el-form-item label="评价内容" required>
            <el-input type="textarea" v-model="reviewsData[index].content" :rows="3" placeholder="商品满足您的期待吗？说说您的真实使用心得..." />
          </el-form-item>
          <el-form-item label="匿名评价">
            <el-switch v-model="reviewsData[index].isAnonymous" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" @click="submitAll">提交全部评价</el-button>
          <el-button @click="$router.push('/orders')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { orderAPI, reviewsAPI } from "@/api"
import { ElMessage } from "element-plus"
const route = useRoute()
const router = useRouter()
const order = ref(null)
const reviewsData = ref([])
onMounted(async () => {
  const orderId = route.params.orderId
  if (!orderId) return router.push("/orders")
  try {
    const res = await orderAPI.getDetail(orderId)
    order.value = res.data
    // Initialize form states
    reviewsData.value = order.value.items.map(item => ({
      orderId: order.value.orderId,
      productId: item.productId,
      rating: 5,
      content: "",
      isAnonymous: 0
    }))
  } catch (e) {
    console.error(e)
  }
})
const submitAll = async () => {
  try {
    // Validate
    const invalid = reviewsData.value.find(r => !r.rating || !r.content.trim())
    if (invalid) {
      return ElMessage.warning("请为所有商品填写评分和评价内容")
    }
    // Submit each sequentially (or via backend batch if available, but API docs only show single submit)
    for (const data of reviewsData.value) {
      await reviewsAPI.submit(data)
    }
    ElMessage.success("评价提交成功")
    router.push("/orders")
  } catch (e) {
    console.error(e)
  }
}
</script>
