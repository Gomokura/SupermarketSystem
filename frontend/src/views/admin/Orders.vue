<template>
  <div class="page-container">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>

    <!-- B-16 多条件筛选 -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :model="filter" inline>
        <el-form-item label="订单号">
          <el-input v-model="filter.orderId" clearable placeholder="订单号" style="width:120px" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="filter.phone" clearable placeholder="用户手机号" style="width:140px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" clearable placeholder="全部" style="width:120px">
            <el-option label="待付款" value="PENDING_PAY" />
            <el-option label="待发货" value="PAID" />
            <el-option label="配送中" value="SHIPPING" />
            <el-option label="待收货" value="PENDING_RECEIVED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="filter.source" clearable placeholder="全部" style="width:100px">
            <el-option label="线上" value="ONLINE" />
            <el-option label="收银台" value="CASHIER" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker v-model="filter.dateRange" type="daterange" range-separator="~"
            start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="orders" border v-loading="loading">
        <el-table-column prop="orderId" label="订单号" width="100" />
        <el-table-column prop="username" label="用户" width="110" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="金额" width="110">
          <template #default="{ row }">
            <span class="amount">¥{{ row.payAmount || row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="110" />
        <el-table-column label="来源" width="90">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.source === 'CASHIER' ? '收银台' : '线上' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <!-- B-18 分配配送员 -->
            <el-button v-if="row.status === 'PAID'" size="small" type="primary" @click="openAssign(row)">分配配送员</el-button>
            <!-- B-21 强制取消 -->
            <el-button v-if="['PENDING_PAY','PAID'].includes(row.status)" size="small" type="danger" @click="forceCancel(row)">强制取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" layout="total, prev, pager, next" @current-change="loadOrders" />
      </div>
    </el-card>

    <!-- B-17 订单详情 Dialog -->
    <el-dialog v-model="detailVisible" title="订单详情" width="720px">
      <template v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderId }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(currentOrder.status)">{{ statusText(currentOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户">{{ currentOrder.username }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentOrder.payMethod }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span class="amount">¥{{ currentOrder.payAmount || currentOrder.totalAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.courierName" label="配送员">
            {{ currentOrder.courierName }} {{ currentOrder.courierPhone }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.trackingNo" label="快递单号">{{ currentOrder.trackingNo }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>商品明细</el-divider>
        <el-table :data="currentOrder.items || []" size="small" border>
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="skuSpec" label="规格" width="100" />
          <el-table-column prop="price" label="单价" width="90">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
          </el-table-column>
        </el-table>

        <!-- B-19 快递信息 -->
        <el-divider>快递信息</el-divider>
        <el-form :model="shippingForm" inline label-width="80px">
          <el-form-item label="快递公司">
            <el-input v-model="shippingForm.company" style="width:140px" />
          </el-form-item>
          <el-form-item label="快递单号">
            <el-input v-model="shippingForm.trackingNo" style="width:180px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="small" @click="saveShipping">保存</el-button>
          </el-form-item>
        </el-form>

        <!-- B-20 修改收货地址 -->
        <template v-if="currentOrder.status === 'PAID'">
          <el-divider>修改收货地址</el-divider>
          <el-form :model="addressForm" inline label-width="80px">
            <el-form-item label="收件人">
              <el-input v-model="addressForm.name" style="width:120px" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="addressForm.phone" style="width:140px" />
            </el-form-item>
            <el-form-item label="地址">
              <el-input v-model="addressForm.address" style="width:280px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="saveAddress">保存地址</el-button>
            </el-form-item>
          </el-form>
        </template>
      </template>
    </el-dialog>

    <!-- B-18 分配配送员 Dialog -->
    <el-dialog v-model="assignVisible" title="分配配送员" width="480px">
      <el-select v-model="selectedCourierId" placeholder="请选择配送员" style="width:100%">
        <el-option v-for="c in couriers" :key="c.courierId" :label="`${c.realName}（${c.phone}）`" :value="c.courierId" />
      </el-select>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI, orderAPI, courierAPI } from '@/api'

const loading = ref(false)
const orders = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filter = reactive({
  orderId: '',
  phone: '',
  status: '',
  source: '',
  dateRange: null
})

const detailVisible = ref(false)
const currentOrder = ref(null)
const shippingForm = reactive({ company: '', trackingNo: '' })
const addressForm = reactive({ name: '', phone: '', address: '' })

const assignVisible = ref(false)
const selectedCourierId = ref(null)
const assigningDeliveryId = ref(null)
const couriers = ref([])

onMounted(() => {
  loadOrders()
  loadCouriers()
})

const handleSearch = () => {
  pageNum.value = 1
  loadOrders()
}

const resetFilter = () => {
  Object.assign(filter, { orderId: '', phone: '', status: '', source: '', dateRange: null })
  pageNum.value = 1
  loadOrders()
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filter.orderId) params.orderId = filter.orderId
    if (filter.phone) params.phone = filter.phone
    if (filter.status) params.status = filter.status
    if (filter.source) params.source = filter.source
    if (filter.dateRange?.[0]) params.startDate = filter.dateRange[0]
    if (filter.dateRange?.[1]) params.endDate = filter.dateRange[1]
    const res = await orderAPI.adminGetList(params)
    orders.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadCouriers = async () => {
  try {
    const res = await courierAPI.adminGetList({ pageNum: 1, pageSize: 100 })
    couriers.value = res.data?.records || res.data || []
  } catch {}
}

const statusText = (status) => {
  const map = {
    PENDING_PAY: '待付款', PAID: '待发货', SHIPPING: '配送中',
    PENDING_RECEIVED: '待收货', COMPLETED: '已完成',
    CANCELLED: '已取消', REFUNDED: '已退款'
  }
  return map[status] || status
}

const statusTagType = (status) => {
  const map = { PENDING_PAY: 'warning', PAID: 'info', SHIPPING: 'primary',
    PENDING_RECEIVED: 'warning', COMPLETED: 'success', CANCELLED: 'danger', REFUNDED: 'info' }
  return map[status] || 'info'
}

const formatDate = (d) => d ? new Date(d).toLocaleString('zh-CN') : '-'

const viewDetail = async (row) => {
  try {
    const res = await orderAPI.getDetail(row.orderId)
    currentOrder.value = res.data || row
    shippingForm.company = currentOrder.value.shippingCompany || ''
    shippingForm.trackingNo = currentOrder.value.trackingNo || ''
    addressForm.name = currentOrder.value.receiverName || ''
    addressForm.phone = currentOrder.value.receiverPhone || ''
    addressForm.address = currentOrder.value.address || ''
  } catch {
    currentOrder.value = row
  }
  detailVisible.value = true
}

const saveShipping = async () => {
  try {
    await adminAPI.shipOrder(currentOrder.value.orderId, shippingForm.company, shippingForm.trackingNo)
    ElMessage.success('快递信息已保存')
  } catch {
    ElMessage.error('保存失败')
  }
}

const saveAddress = async () => {
  try {
    await adminAPI.updateOrderAddress(currentOrder.value.orderId, addressForm.name, addressForm.phone, addressForm.address)
    ElMessage.success('地址已更新')
  } catch {
    ElMessage.error('更新失败')
  }
}

const openAssign = (row) => {
  assigningDeliveryId.value = row.deliveryId || row.orderId
  selectedCourierId.value = null
  assignVisible.value = true
}

const submitAssign = async () => {
  if (!selectedCourierId.value) { ElMessage.warning('请选择配送员'); return }
  try {
    await courierAPI.adminAssign(assigningDeliveryId.value, selectedCourierId.value)
    ElMessage.success('配送员已分配')
    assignVisible.value = false
    loadOrders()
  } catch {
    ElMessage.error('分配失败')
  }
}

const forceCancel = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '强制取消订单', {
      inputPlaceholder: '必填，至少5个字',
      inputValidator: (v) => v && v.length >= 5 ? true : '原因不能少于5个字'
    })
    await orderAPI.adminCancel(row.orderId, reason)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.amount { color: #f56c6c; font-weight: bold; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
