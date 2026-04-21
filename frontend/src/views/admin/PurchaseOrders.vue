<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <div class="page-title">采购管理</div>
        <div class="page-desc">支持创建采购单、审批、入库和取消，适合仓储进货端使用。</div>
      </div>
      <div class="toolbar-actions">
        <el-select v-model="statusFilter" clearable placeholder="筛选状态" style="width: 150px" @change="load">
          <el-option label="草稿" value="draft" />
          <el-option label="已审批" value="approved" />
          <el-option label="已收货" value="received" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
        <el-button @click="load">刷新</el-button>
        <el-button type="primary" @click="openCreate">新建采购单</el-button>
      </div>
    </div>

    <el-card shadow="never" class="panel-card">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="poId" label="采购单ID" width="100" />
        <el-table-column prop="poNo" label="采购单号" min-width="160" />
        <el-table-column prop="supplierName" label="供应商" min-width="150" />
        <el-table-column prop="totalAmount" label="总金额" width="110">
          <template #default="{ row }">¥ {{ money(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }"><el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="expectedDate" label="预计到货" min-width="140" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">详情</el-button>
            <el-button size="small" type="success" :disabled="row.status !== 'draft'" @click="approve(row)">审批</el-button>
            <el-button size="small" type="primary" :disabled="row.status !== 'approved'" @click="showReceive(row)">入库</el-button>
            <el-button size="small" type="danger" :disabled="row.status !== 'draft'" @click="cancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" background layout="total, prev, pager, next" :total="total" @current-change="load" />
      </div>
    </el-card>

    <el-dialog v-model="createVisible" title="新建采购单" width="880px">
      <el-form label-width="100px">
        <el-form-item label="供应商">
          <el-select v-model="createForm.supplierId" placeholder="请选择供应商" style="width: 100%">
            <el-option v-for="item in suppliers" :key="item.supplierId" :label="item.supplierName" :value="item.supplierId" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计到货">
          <el-date-picker v-model="createForm.expectedDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <div class="dialog-subtitle">采购明细</div>
      <el-table :data="createForm.items" border>
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <el-select v-model="row.productId" placeholder="选择商品" style="width: 100%">
              <el-option v-for="item in products" :key="item.productId" :label="item.productName" :value="item.productId" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="120">
          <template #default="{ row }"><el-input-number v-model="row.orderQuantity" :min="1" /></template>
        </el-table-column>
        <el-table-column label="单价" width="150">
          <template #default="{ row }"><el-input-number v-model="row.unitPrice" :min="0" :precision="2" /></template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ $index }"><el-button type="danger" size="small" @click="removeCreateItem($index)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <div class="table-toolbar"><el-button @click="addCreateItem">新增一行</el-button></div>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">提交采购单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="采购单详情" width="820px">
      <div v-if="currentOrder" class="detail-grid">
        <div class="detail-panel">
          <div class="detail-row"><span>采购单号</span><strong>{{ currentOrder.poNo }}</strong></div>
          <div class="detail-row"><span>供应商</span><strong>{{ currentOrder.supplierName || '-' }}</strong></div>
          <div class="detail-row"><span>状态</span><strong>{{ statusText(currentOrder.status) }}</strong></div>
          <div class="detail-row"><span>备注</span><strong>{{ currentOrder.remark || '-' }}</strong></div>
        </div>
        <div class="detail-panel">
          <div class="detail-row"><span>总金额</span><strong>¥ {{ money(currentOrder.totalAmount) }}</strong></div>
          <div class="detail-row"><span>创建时间</span><strong>{{ currentOrder.createTime || '-' }}</strong></div>
          <div class="detail-row"><span>预计到货</span><strong>{{ currentOrder.expectedDate || '-' }}</strong></div>
          <div class="detail-row"><span>完成时间</span><strong>{{ currentOrder.completeTime || '-' }}</strong></div>
        </div>
      </div>
      <el-divider />
      <el-table :data="currentOrder?.items || []" border stripe>
        <el-table-column prop="productName" label="商品" min-width="180" />
        <el-table-column prop="orderQuantity" label="采购数量" width="100" />
        <el-table-column prop="arrivedQuantity" label="已到货" width="100" />
        <el-table-column prop="unitPrice" label="单价" width="100">
          <template #default="{ row }">¥ {{ money(row.unitPrice) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="receiveVisible" title="确认到货入库" width="760px">
      <el-table :data="receiveItems" border>
        <el-table-column prop="productName" label="商品" min-width="180" />
        <el-table-column prop="orderQuantity" label="采购数量" width="100" />
        <el-table-column label="实际到货" width="140">
          <template #default="{ row }"><el-input-number v-model="row.arrivedQuantity" :min="0" :max="row.orderQuantity || 9999" /></template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="receiveVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReceive">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI, productAPI, purchaseAPI } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const createVisible = ref(false)
const detailVisible = ref(false)
const receiveVisible = ref(false)
const list = ref([])
const suppliers = ref([])
const products = ref([])
const currentOrder = ref(null)
const receiveItems = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')

const createForm = reactive({
  supplierId: null,
  expectedDate: '',
  remark: '',
  items: []
})

const money = (v) => Number(v || 0).toFixed(2)
const statusText = (s) => ({ draft: '草稿', approved: '已审批', received: '已收货', cancelled: '已取消' }[s] || s || '-')
const statusTag = (s) => ({ draft: 'info', approved: 'warning', received: 'success', cancelled: 'danger' }[s] || 'info')

const resetCreateForm = () => {
  createForm.supplierId = null
  createForm.expectedDate = ''
  createForm.remark = ''
  createForm.items = [{ productId: null, orderQuantity: 1, unitPrice: 0 }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await purchaseAPI.list({ pageNum: pageNum.value, pageSize: pageSize.value, status: statusFilter.value || undefined })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadBaseData = async () => {
  const [supplierRes, productRes] = await Promise.all([
    adminAPI.getSuppliers({}),
    productAPI.getList({ pageNum: 1, pageSize: 200 })
  ])
  suppliers.value = supplierRes.data || []
  products.value = productRes.data?.records || productRes.data || []
}

const openCreate = async () => {
  resetCreateForm()
  await loadBaseData()
  createVisible.value = true
}

const addCreateItem = () => createForm.items.push({ productId: null, orderQuantity: 1, unitPrice: 0 })
const removeCreateItem = (index) => createForm.items.splice(index, 1)

const submitCreate = async () => {
  if (!createForm.supplierId) return ElMessage.warning('请选择供应商')
  if (!createForm.items.length || createForm.items.some(item => !item.productId || !item.orderQuantity)) {
    return ElMessage.warning('请完整填写采购明细')
  }
  submitting.value = true
  try {
    const payload = {
      order: {
        supplierId: createForm.supplierId,
        expectedDate: createForm.expectedDate,
        remark: createForm.remark
      },
      items: createForm.items.map(item => ({
        productId: item.productId,
        orderQuantity: item.orderQuantity,
        unitPrice: item.unitPrice
      }))
    }
    await purchaseAPI.create(payload)
    ElMessage.success('采购单已创建')
    createVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

const showDetail = (row) => {
  currentOrder.value = row
  detailVisible.value = true
}

const approve = async (row) => {
  await purchaseAPI.approve(row.poId)
  ElMessage.success('采购单已审批')
  load()
}

const showReceive = (row) => {
  currentOrder.value = row
  receiveItems.value = (row.items || []).map(item => ({ ...item, arrivedQuantity: item.arrivedQuantity ?? item.orderQuantity ?? 0 }))
  receiveVisible.value = true
}

const submitReceive = async () => {
  submitting.value = true
  try {
    await purchaseAPI.receive(currentOrder.value.poId, receiveItems.value.map(item => ({ itemId: item.itemId, arrivedQuantity: item.arrivedQuantity })))
    ElMessage.success('采购入库完成')
    receiveVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

const cancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消该采购单吗？', '提示', { type: 'warning' })
    await purchaseAPI.cancel(row.poId)
    ElMessage.success('采购单已取消')
    load()
  } catch (e) {
    if (e !== 'cancel') throw e
  }
}

onMounted(load)
</script>

<style scoped>
.page-shell { display: grid; gap: 18px; }
.page-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.page-title { font-size: 28px; font-weight: 800; color: #22324d; }
.page-desc { margin-top: 4px; color: #6f7f97; }
.toolbar-actions { display: flex; gap: 12px; align-items: center; }
.panel-card { border: none; border-radius: 18px; box-shadow: 0 14px 36px rgba(42, 68, 110, 0.08); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 18px; }
.table-toolbar { margin-top: 12px; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.detail-panel { padding: 16px; border-radius: 16px; background: #f7faff; }
.detail-row { display: flex; justify-content: space-between; gap: 12px; margin-bottom: 10px; color: #6d7c92; }
.detail-row strong { color: #24344c; }
.dialog-subtitle { margin-bottom: 12px; font-weight: 700; color: #24344c; }
</style>
