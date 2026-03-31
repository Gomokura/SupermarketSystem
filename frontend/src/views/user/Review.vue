<template>
  <div class="page-container">
    <h2>评价订单</h2>
    <p v-if="orderId" class="tip">订单号：{{ orderId }}</p>
    <el-form :model="form" label-width="100px" style="max-width: 520px">
      <el-form-item label="商品ID">
        <el-input-number v-model="form.productId" :min="1" />
      </el-form-item>
      <el-form-item label="评分">
        <el-rate v-model="form.rating" />
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="form.content" type="textarea" rows="4" />
      </el-form-item>
      <el-form-item label="匿名">
        <el-switch v-model="form.isAnonymous" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit">提交评价</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { reviewAPI } from '@/api'

const route = useRoute()
const router = useRouter()
const orderId = computed(() => route.params.orderId)

const form = reactive({
  orderId: Number(route.params.orderId) || 1,
  productId: 1001,
  rating: 5,
  content: '',
  images: '',
  isAnonymous: 0
})

const submit = async () => {
  try {
    await reviewAPI.create({
      orderId: Number(orderId.value) || form.orderId,
      productId: form.productId,
      rating: form.rating,
      content: form.content,
      images: form.images,
      isAnonymous: form.isAnonymous
    })
    ElMessage.success('评价成功')
    router.push('/orders')
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.tip {
  color: #909399;
  margin-bottom: 16px;
}
</style>
