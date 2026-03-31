<template>
  <div class="page-container">
    <h2>售后</h2>
    <el-tabs v-model="tab">
      <el-tab-pane label="申请售后" name="apply">
        <el-form :model="form" label-width="100px" style="max-width: 520px">
          <el-form-item label="订单ID">
            <el-input-number v-model="form.orderId" :min="1" />
          </el-form-item>
          <el-form-item label="类型">
            <el-radio-group v-model="form.asType">
              <el-radio label="refund_only">仅退款</el-radio>
              <el-radio label="return_refund">退货退款</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="原因">
            <el-input v-model="form.reason" type="textarea" rows="3" />
          </el-form-item>
          <el-form-item label="退款金额">
            <el-input-number v-model="form.refundAmount" :min="0" :precision="2" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submit">提交申请</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="我的售后" name="list">
        <el-table :data="list" border v-loading="loading">
          <el-table-column prop="afterSaleId" label="ID" width="80" />
          <el-table-column prop="orderId" label="订单" width="90" />
          <el-table-column prop="asType" label="类型" width="100" />
          <el-table-column prop="reason" label="原因" min-width="160" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { afterSaleAPI } from '@/api'

const route = useRoute()
const tab = ref('apply')
const loading = ref(false)
const list = ref([])
const form = reactive({
  orderId: 1,
  asType: 'refund_only',
  reason: '',
  refundAmount: 0
})

watch(() => route.query.orderId, (v) => {
  if (v) form.orderId = Number(v) || 1
}, { immediate: true })

onMounted(() => {
  loadList()
})

const loadList = async () => {
  loading.value = true
  try {
    const res = await afterSaleAPI.getMy()
    list.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  try {
    await afterSaleAPI.create({ ...form })
    ElMessage.success('已提交')
    tab.value = 'list'
    loadList()
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
</style>
