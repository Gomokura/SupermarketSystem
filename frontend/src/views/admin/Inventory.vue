<template>
  <div class="page-container">
    <h2>库存管理</h2>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="12">
        <el-card>
          <template #header><span>入库操作</span></template>
          <el-form :inline="true" :model="warehousingForm">
            <el-form-item label="商品">
              <el-select v-model="warehousingForm.productId" placeholder="选择商品" filterable style="width:200px">
                <el-option v-for="p in products" :key="p.productId" :label="p.productName" :value="p.productId" />
              </el-select>
            </el-form-item>
            <el-form-item label="数量">
              <el-input-number v-model="warehousingForm.quantity" :min="1" style="width:120px" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="warehousingForm.remark" placeholder="备注说明" style="width:160px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleWarehousing">确认入库</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>出库操作</span></template>
          <el-form :inline="true" :model="outboundForm">
            <el-form-item label="商品">
              <el-select v-model="outboundForm.productId" placeholder="选择商品" filterable style="width:200px">
                <el-option v-for="p in products" :key="p.productId" :label="p.productName" :value="p.productId" />
              </el-select>
            </el-form-item>
            <el-form-item label="数量">
              <el-input-number v-model="outboundForm.quantity" :min="1" style="width:120px" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="outboundForm.remark" placeholder="备注说明" style="width:160px" />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="handleOutbound">确认出库</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>库存记录</span>
          <div>
            <el-select v-model="logTypeFilter" placeholder="操作类型" clearable style="width:140px;margin-right:10px" @change="loadLogs">
              <el-option label="入库" value="purchase_in" />
              <el-option label="出库" value="ORDER_OUT" />
              <el-option label="报损" value="DAMAGE" />
              <el-option label="人工调整" value="MANUAL" />
            </el-select>
            <el-button @click="loadLogs">刷新</el-button>
          </div>
        </div>
      </template>
      <el-table :data="logs" border>
        <el-table-column prop="productName" label="商品" min-width="140" />
        <el-table-column prop="logType" label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="logTypeTag(row.logType)" size="small">
              {{ logTypeText(row.logType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeAmount" label="变动数量" width="100">
          <template #default="{ row }">
            <span :class="row.changeAmount >= 0 ? 'num-up' : 'num-down'">
              {{ row.changeAmount >= 0 ? '+' : '' }}{{ row.changeAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="变动后库存" width="110" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="createTime" label="时间" width="170">
          <template #default="{ row }">
            {{ row.createTime ? new Date(row.createTime).toLocaleString('zh-CN') : '-' }}
          </template>
        </el-table-column>
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
import { adminAPI } from '@/api'

const products = ref([])
const logs = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const logTypeFilter = ref('')
const warehousingForm = reactive({ productId: null, quantity: 1, remark: '' })
const outboundForm = reactive({ productId: null, quantity: 1, remark: '' })

onMounted(() => {
  loadProducts()
  loadLogs()
})

async function loadProducts() {
  try {
    // 用管理端商品列表接口（含所有状态商品）
    const res = await adminAPI.getProducts({ pageNum: 1, pageSize: 500 })
    products.value = res.data?.records || res.data || []
  } catch (e) { console.error(e) }
}

async function loadLogs() {
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (logTypeFilter.value) params.logType = logTypeFilter.value
    const res = await adminAPI.getInventoryLogs(params)
    logs.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) { console.error(e) }
}

const logTypeText = (t) => ({
  purchase_in: '入库', ORDER_OUT: '出库', DAMAGE: '报损', MANUAL: '人工调整'
}[t] || t || '-')

const logTypeTag = (t) => ({
  purchase_in: 'success', ORDER_OUT: 'danger', DAMAGE: 'warning', MANUAL: 'info'
}[t] || 'info')

async function handleWarehousing() {
  if (!warehousingForm.productId) { ElMessage.warning('请选择商品'); return }
  try {
    await adminAPI.warehousing(warehousingForm.productId, warehousingForm.quantity, warehousingForm.remark)
    ElMessage.success('入库成功')
    Object.assign(warehousingForm, { productId: null, quantity: 1, remark: '' })
    loadLogs()
  } catch (e) { console.error(e) }
}

async function handleOutbound() {
  if (!outboundForm.productId) { ElMessage.warning('请选择商品'); return }
  try {
    await adminAPI.outbound(outboundForm.productId, outboundForm.quantity, outboundForm.remark)
    ElMessage.success('出库成功')
    Object.assign(outboundForm, { productId: null, quantity: 1, remark: '' })
    loadLogs()
  } catch (e) { console.error(e) }
}
</script>

<style scoped>
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
