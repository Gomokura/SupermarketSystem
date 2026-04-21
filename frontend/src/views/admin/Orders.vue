<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <div class="page-title">订单管理</div>
        <div class="page-desc">订单筛选、详情查看、快递发货、修改收货地址、分配配送员与强制取消。</div>
      </div>
    </div>

    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="订单号">
          <el-input v-model="filters.orderNo" clearable />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="filters.userId" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable style="width: 160px">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="filters.startDate"
            type="date"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="filters.endDate"
            type="date"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="orders" border stripe v-loading="loading">
        <el-table-column prop="orderId" label="订单ID" width="90" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="username" label="用户" min-width="120" />
        <el-table-column label="收货信息" min-width="180">
          <template #default="{ row }">
            {{ row.receiverSnapshot || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="实付金额" width="110">
          <template #default="{ row }">
            ¥ {{ money(row.payAmount || row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" min-width="170" />
        <el-table-column label="操作" width="470" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">详情</el-button>
            <el-button
              size="small"
              type="primary"
              :disabled="row.status !== 'PENDING_SHIP'"
              @click="showShip(row)"
            >
              填写快递发货
            </el-button>
            <el-button
              size="small"
              :disabled="row.status !== 'PENDING_SHIP'"
              @click="showAddress(row)"
            >
              修改收货地址
            </el-button>
            <el-button
              size="small"
              :disabled="row.status !== 'PENDING_SHIP'"
              @click="showAssign(row)"
            >
              分配配送员
            </el-button>
            <el-button
              size="small"
              type="danger"
              :disabled="!canCancel(row.status)"
              @click="showCancel(row)"
            >
              强制取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          background
          layout="total, prev, pager, next"
          :total="total"
          @current-change="loadOrders"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="960px">
      <div v-if="currentOrder" class="detail-grid">
        <div class="detail-panel">
          <div class="panel-title">基础信息</div>
          <div class="detail-row">
            <span>订单号</span>
            <strong>{{ currentOrder.orderNo }}</strong>
          </div>
          <div class="detail-row">
            <span>订单状态</span>
            <strong>{{ statusText(currentOrder.status) }}</strong>
          </div>
          <div class="detail-row">
            <span>支付方式</span>
            <strong>{{ currentOrder.payMethod || '-' }}</strong>
          </div>
          <div class="detail-row">
            <span>配送时段</span>
            <strong>{{ currentOrder.deliveryTimeSlot || '-' }}</strong>
          </div>
          <div class="detail-row">
            <span>备注</span>
            <strong>{{ currentOrder.remark || '-' }}</strong>
          </div>
        </div>

        <div class="detail-panel">
          <div class="panel-title">收货地址</div>
          <div class="detail-row">
            <span>收货人</span>
            <strong>{{ detailAddress.name || '-' }}</strong>
          </div>
          <div class="detail-row">
            <span>联系电话</span>
            <strong>{{ detailAddress.phone || '-' }}</strong>
          </div>
          <div class="detail-address">
            {{ detailAddress.address || currentOrder.receiverSnapshot || '-' }}
          </div>
        </div>
      </div>

      <el-divider />
      <div class="panel-title with-margin">商品明细</div>
      <el-table :data="currentOrder?.items || []" border stripe>
        <el-table-column prop="productName" label="商品" min-width="180" />
        <el-table-column prop="skuName" label="规格" min-width="120" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">
            ¥ {{ money(row.price) }}
          </template>
        </el-table-column>
        <el-table-column label="小计" width="110">
          <template #default="{ row }">
            ¥ {{ money((row.price || 0) * (row.quantity || 0)) }}
          </template>
        </el-table-column>
      </el-table>

      <el-divider />
      <div class="panel-title with-margin">时间线</div>
      <el-timeline v-if="timeline.length" class="timeline-wrap">
        <el-timeline-item
          v-for="(item, index) in timeline"
          :key="index"
          :timestamp="item.createTime || item.logTime || '-'"
        >
          {{ item.statusDesc || item.remark || item.status || '-' }}
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无时间线数据" />
    </el-dialog>

    <el-dialog v-model="shipVisible" title="填写快递发货" width="420px">
      <el-form ref="shipRef" :model="shipForm" :rules="shipRules" label-width="90px">
        <el-form-item label="快递公司" prop="company">
          <el-input v-model="shipForm.company" />
        </el-form-item>
        <el-form-item label="运单号" prop="trackingNo">
          <el-input v-model="shipForm.trackingNo" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitShip">
          确认发货
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addressVisible" title="修改收货地址" width="460px">
      <el-form ref="addressRef" :model="addressForm" :rules="addressRules" label-width="90px">
        <el-form-item label="收货人" prop="name">
          <el-input v-model="addressForm.name" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="addressForm.address" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAddress">
          保存地址
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="分配配送员" width="420px">
      <el-select v-model="assignCourierId" placeholder="请选择配送员" style="width: 100%">
        <el-option
          v-for="item in courierOptions"
          :key="item.courierId"
          :label="`${item.courierName || '-'}（${item.phone || '-'}）`"
          :value="item.courierId"
        />
      </el-select>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAssign">
          确认分配
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="cancelVisible" title="强制取消订单" width="420px">
      <el-input
        v-model="cancelReason"
        type="textarea"
        :rows="4"
        placeholder="请输入取消原因"
      />
      <template #footer>
        <el-button @click="cancelVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitCancel">
          确认取消
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAPI, courierAPI, orderAPI } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const orders = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const currentOrder = ref(null)
const timeline = ref([])

const detailVisible = ref(false)
const shipVisible = ref(false)
const addressVisible = ref(false)
const assignVisible = ref(false)
const cancelVisible = ref(false)

const courierOptions = ref([])
const assignCourierId = ref(null)
const cancelReason = ref('')

const shipRef = ref()
const addressRef = ref()

const filters = reactive({
  orderNo: '',
  userId: '',
  status: '',
  startDate: '',
  endDate: ''
})

const shipForm = reactive({
  company: '',
  trackingNo: ''
})

const addressForm = reactive({
  name: '',
  phone: '',
  address: ''
})

const shipRules = {
  company: [{ required: true, message: '请输入快递公司', trigger: 'blur' }],
  trackingNo: [{ required: true, message: '请输入运单号', trigger: 'blur' }]
}

const addressRules = {
  name: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const statusOptions = [
  { label: '待付款', value: 'PENDING_PAY' },
  { label: '已支付', value: 'PAID' },
  { label: '待发货', value: 'PENDING_SHIP' },
  { label: '配送中', value: 'SHIPPING' },
  { label: '待收货', value: 'PENDING_RECEIVED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

const money = (v) => Number(v || 0).toFixed(2)

const statusText = (s) =>
  ({
    PENDING_PAY: '待付款',
    PAID: '已支付',
    PENDING_SHIP: '待发货',
    SHIPPING: '配送中',
    PENDING_RECEIVED: '待收货',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }[s] || s || '-')

const tagType = (s) =>
  ({
    PENDING_PAY: 'warning',
    PAID: 'success',
    PENDING_SHIP: 'warning',
    SHIPPING: 'primary',
    PENDING_RECEIVED: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }[s] || 'info')

const canCancel = (s) => ['PENDING_PAY', 'PAID', 'PENDING_SHIP'].includes(s)

const detailAddress = computed(() => {
  const snapshot = currentOrder.value?.receiverSnapshot || ''
  if (!snapshot) return { name: '', phone: '', address: '' }

  const parts = snapshot
    .split(/[，,]/)
    .map(item => item.trim())
    .filter(Boolean)

  return {
    name: parts[0] || '',
    phone: parts[1] || '',
    address: parts.slice(2).join(' ') || snapshot
  }
})

const params = () => ({
  pageNum: pageNum.value,
  pageSize: pageSize.value,
  orderNo: filters.orderNo || undefined,
  userId: filters.userId ? Number(filters.userId) : undefined,
  status: filters.status || undefined,
  startDate: filters.startDate || undefined,
  endDate: filters.endDate || undefined
})

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await orderAPI.adminGetList(params())
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadTimeline = async (orderId) => {
  try {
    const res = await orderAPI.adminGetTimeline(orderId)
    timeline.value = res.data || []
  } catch {
    timeline.value = []
  }
}

const search = () => {
  pageNum.value = 1
  loadOrders()
}

const resetFilters = () => {
  Object.assign(filters, {
    orderNo: '',
    userId: '',
    status: '',
    startDate: '',
    endDate: ''
  })
  search()
}

const showDetail = async (row) => {
  currentOrder.value = row
  detailVisible.value = true
  await loadTimeline(row.orderId)
}

const showShip = (row) => {
  currentOrder.value = row
  shipForm.company = ''
  shipForm.trackingNo = ''
  shipVisible.value = true
}

const submitShip = async () => {
  await shipRef.value.validate()
  submitting.value = true
  try {
    await adminAPI.shipOrder(currentOrder.value.orderId, shipForm.company, shipForm.trackingNo)
    ElMessage.success('订单已发货')
    shipVisible.value = false
    loadOrders()
  } finally {
    submitting.value = false
  }
}

const showAddress = (row) => {
  currentOrder.value = row
  addressForm.name = detailAddress.value.name
  addressForm.phone = detailAddress.value.phone
  addressForm.address = detailAddress.value.address
  addressVisible.value = true
}

const submitAddress = async () => {
  await addressRef.value.validate()
  submitting.value = true
  try {
    await adminAPI.updateOrderAddress(
      currentOrder.value.orderId,
      addressForm.name,
      addressForm.phone,
      addressForm.address
    )
    ElMessage.success('收货地址已更新')
    addressVisible.value = false
    loadOrders()
  } finally {
    submitting.value = false
  }
}

const showAssign = async (row) => {
  currentOrder.value = row
  assignCourierId.value = null
  const res = await courierAPI.adminGetList({ pageNum: 1, pageSize: 100 })
  courierOptions.value = res.data?.records || res.data || []
  assignVisible.value = true
}

const submitAssign = async () => {
  if (!assignCourierId.value) {
    ElMessage.warning('请选择配送员')
    return
  }

  const deliveryId =
    currentOrder.value.deliveryId ||
    currentOrder.value.taskId ||
    currentOrder.value.orderId

  submitting.value = true
  try {
    await courierAPI.adminAssign(deliveryId, assignCourierId.value)
    ElMessage.success('配送员分配成功')
    assignVisible.value = false
    loadOrders()
  } finally {
    submitting.value = false
  }
}

const showCancel = (row) => {
  currentOrder.value = row
  cancelReason.value = ''
  cancelVisible.value = true
}

const submitCancel = async () => {
  if (!cancelReason.value.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }

  submitting.value = true
  try {
    await orderAPI.adminCancel(currentOrder.value.orderId, cancelReason.value)
    ElMessage.success('订单已取消')
    cancelVisible.value = false
    loadOrders()
  } finally {
    submitting.value = false
  }
}

onMounted(loadOrders)
</script>

<style scoped>
.page-shell {
  display: grid;
  gap: 18px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  color: #22324d;
}

.page-desc {
  margin-top: 4px;
  color: #6f7f97;
}

.panel-card {
  border: none;
  border-radius: 18px;
  box-shadow: 0 14px 36px rgba(42, 68, 110, 0.08);
}

.filter-form {
  row-gap: 10px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.detail-panel {
  padding: 16px;
  border-radius: 16px;
  background: #f7faff;
}

.panel-title {
  margin-bottom: 12px;
  font-weight: 700;
  color: #24344c;
}

.with-margin {
  margin-bottom: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  color: #6d7c92;
}

.detail-row strong {
  color: #24344c;
}

.detail-address {
  color: #24344c;
  line-height: 1.7;
}

.timeline-wrap {
  padding-left: 8px;
}
</style>