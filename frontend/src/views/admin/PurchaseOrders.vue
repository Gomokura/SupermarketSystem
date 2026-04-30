<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>采购管理</h2>
        <p>支持创建采购单、查看明细、审批、收货和取消。</p>
      </div>
      <el-button type="primary" @click="openCreate">新建采购单</el-button>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="status" placeholder="全部状态" clearable @change="reload">
          <el-option label="草稿" value="draft" />
          <el-option label="已审批" value="approved" />
          <el-option label="已收货" value="received" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
      </div>

      <el-table :data="records" border v-loading="loading">
        <el-table-column prop="poId" label="ID" width="80" />
        <el-table-column prop="poNo" label="采购单号" min-width="180" />
        <el-table-column prop="supplierName" label="供应商" min-width="160" />
        <el-table-column prop="status" label="状态" width="110" />
        <el-table-column prop="totalAmount" label="金额" width="120" align="right">
          <template #default="{ row }">
            ￥{{ Number(row.totalAmount || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'draft'" size="small" type="success" @click="approve(row)">审批</el-button>
            <el-button v-if="row.status === 'approved'" size="small" type="warning" @click="openReceive(row)">收货</el-button>
            <el-button v-if="row.status === 'draft'" size="small" type="danger" @click="cancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadOrders"
        />
      </div>
    </el-card>

    <el-dialog v-model="createVisible" title="新建采购单" width="860px">
      <el-form label-width="100px">
        <el-form-item label="供应商">
          <el-select v-model="createForm.order.supplierId" style="width: 280px">
            <el-option v-for="item in suppliers" :key="item.supplierId" :label="item.supplierName" :value="item.supplierId" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计到货">
          <el-date-picker v-model="createForm.order.expectedDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.order.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <div class="items-toolbar">
        <el-select v-model="draftItem.productId" filterable placeholder="选择商品" style="width: 280px">
          <el-option v-for="item in products" :key="item.productId" :label="item.productName" :value="item.productId" />
        </el-select>
        <el-input-number v-model="draftItem.quantity" :min="1" />
        <el-input-number v-model="draftItem.unitPrice" :min="0" :precision="2" />
        <el-button type="primary" @click="addItem">添加商品</el-button>
      </div>

      <el-table :data="createForm.items" border>
        <el-table-column prop="productName" label="商品" min-width="180" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="unitPrice" label="单价" width="120">
          <template #default="{ row }">￥{{ Number(row.unitPrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">￥{{ Number(row.quantity * row.unitPrice).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ $index }">
            <el-button size="small" type="danger" @click="createForm.items.splice($index, 1)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">提交采购单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="采购单详情" width="760px">
      <div v-if="detail">
        <div class="detail-meta">
          <span>单号：{{ detail.poNo }}</span>
          <span>供应商：{{ detail.supplierName }}</span>
          <span>状态：{{ detail.status }}</span>
        </div>
        <el-table :data="detail.items || []" border>
          <el-table-column prop="productName" label="商品" min-width="180" />
          <el-table-column prop="orderQuantity" label="采购数量" width="110" />
          <el-table-column prop="arrivedQuantity" label="已收货" width="110" />
          <el-table-column prop="unitPrice" label="单价" width="120">
            <template #default="{ row }">￥{{ Number(row.unitPrice || 0).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog v-model="receiveVisible" title="确认收货" width="760px">
      <el-table :data="receiveItems" border>
        <el-table-column prop="productName" label="商品" min-width="180" />
        <el-table-column prop="orderQuantity" label="采购数量" width="100" />
        <el-table-column label="实收数量" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.arrivedQuantity" :min="0" :max="row.orderQuantity || 9999" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="receiveVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingReceive" @click="submitReceive">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI, productAPI } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const submittingReceive = ref(false)
const createVisible = ref(false)
const detailVisible = ref(false)
const receiveVisible = ref(false)
const records = ref([])
const suppliers = ref([])
const products = ref([])
const detail = ref(null)
const receiveTargetId = ref(null)
const receiveItems = ref([])
const status = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const createForm = reactive({
  order: {
    supplierId: null,
    expectedDate: '',
    remark: ''
  },
  items: []
})

const draftItem = reactive({
  productId: null,
  quantity: 1,
  unitPrice: 1
})

const formatDateTime = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN')
}

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getPurchaseOrders({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: status.value || undefined
    })
    records.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadBaseData = async () => {
  const [supplierRes, productRes] = await Promise.all([
    adminAPI.getSuppliers(),
    productAPI.getList({ pageNum: 1, pageSize: 100 })
  ])
  suppliers.value = supplierRes.data || []
  products.value = productRes.data?.records || productRes.data || []
}

const reload = () => {
  pageNum.value = 1
  loadOrders()
}

const resetCreateForm = () => {
  createForm.order.supplierId = null
  createForm.order.expectedDate = ''
  createForm.order.remark = ''
  createForm.items = []
  draftItem.productId = null
  draftItem.quantity = 1
  draftItem.unitPrice = 1
}

const openCreate = () => {
  resetCreateForm()
  createVisible.value = true
}

const addItem = () => {
  const product = products.value.find(item => item.productId === draftItem.productId)
  if (!product) {
    ElMessage.warning('请先选择商品')
    return
  }
  createForm.items.push({
    productId: product.productId,
    productName: product.productName,
    quantity: draftItem.quantity,
    unitPrice: draftItem.unitPrice
  })
}

const submitCreate = async () => {
  if (!createForm.order.supplierId) {
    ElMessage.warning('请选择供应商')
    return
  }
  if (!createForm.items.length) {
    ElMessage.warning('请至少添加一个采购商品')
    return
  }
  submitting.value = true
  try {
    await adminAPI.createPurchaseOrder({
      order: createForm.order,
      items: createForm.items
    })
    ElMessage.success('采购单已创建')
    createVisible.value = false
    await loadOrders()
  } finally {
    submitting.value = false
  }
}

const viewDetail = async (row) => {
  const res = await adminAPI.getPurchaseDetail(row.poId)
  detail.value = res.data
  detailVisible.value = true
}

const approve = async (row) => {
  await adminAPI.approvePurchaseOrder(row.poId)
  ElMessage.success('采购单已审批')
  await loadOrders()
}

const cancel = async (row) => {
  await ElMessageBox.confirm(`确认取消采购单 ${row.poNo} 吗？`, '提示', { type: 'warning' })
  await adminAPI.cancelPurchaseOrder(row.poId)
  ElMessage.success('采购单已取消')
  await loadOrders()
}

const openReceive = async (row) => {
  const res = await adminAPI.getPurchaseDetail(row.poId)
  receiveTargetId.value = row.poId
  receiveItems.value = (res.data?.items || []).map(item => ({
    itemId: item.itemId,
    productName: item.productName,
    orderQuantity: item.orderQuantity,
    arrivedQuantity: item.arrivedQuantity || item.orderQuantity || 0
  }))
  receiveVisible.value = true
}

const submitReceive = async () => {
  submittingReceive.value = true
  try {
    await adminAPI.receivePurchaseOrder(receiveTargetId.value, receiveItems.value)
    ElMessage.success('采购单已收货入库')
    receiveVisible.value = false
    await loadOrders()
  } finally {
    submittingReceive.value = false
  }
}

onMounted(() => {
  loadOrders()
  loadBaseData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 6px;
}

.page-header p {
  margin: 0;
  color: #909399;
}

.toolbar {
  margin-bottom: 16px;
}

.items-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.detail-meta {
  display: flex;
  gap: 18px;
  margin-bottom: 14px;
  color: #606266;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
