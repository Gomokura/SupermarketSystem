<template>
  <div class="page-container">
    <h2>库存管理</h2>
    <el-card>
      <template #header>
        <span>入库操作</span>
      </template>
      <el-form :inline="true" :model="warehousingForm">
        <el-form-item label="商品">
          <el-select v-model="warehousingForm.productId" placeholder="选择商品" style="width: 200px">
            <el-option v-for="p in products" :key="p.productId" :label="p.productName" :value="p.productId" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="warehousingForm.quantity" :min="1" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleWarehousing">确认入库</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>库存记录</span>
      </template>
      <el-table :data="logs" border>
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="changeType" label="类型" width="100" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="logTime" label="时间" width="180" />
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadLogs"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAPI, productAPI } from '@/api'

const products = ref([])
const logs = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const warehousingForm = reactive({ productId: null, quantity: 1 })

onMounted(() => {
  loadProducts()
  loadLogs()
})

const loadProducts = async () => {
  try {
    const res = await productAPI.getList({ pageNum: 1, pageSize: 100 })
    products.value = res.data.records || res.data || []
  } catch (error) {
    console.error(error)
  }
}

const loadLogs = async () => {
  try {
    const res = await adminAPI.getInventoryLogs({ pageNum: pageNum.value, pageSize: pageSize.value })
    logs.value = res.data.records || res.data || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const handleWarehousing = async () => {
  if (!warehousingForm.productId) {
    ElMessage.warning('请选择商品')
    return
  }
  try {
    await adminAPI.warehousing(warehousingForm.productId, warehousingForm.quantity)
    ElMessage.success('入库成功')
    loadLogs()
    warehousingForm.quantity = 1
  } catch (error) {
    console.error(error)
  }
}
</script>
