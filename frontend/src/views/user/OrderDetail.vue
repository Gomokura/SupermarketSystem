<template>
  <div class="page-container">
    <h2>订单详情</h2>
    <el-card v-if="order" class="order-card">
      <template #header>
        <div class="card-header">
          <span>订单号: {{ order.orderId }}</span>
          <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="下单时间">{{ formatDate(order.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ order.payMethod === 'WECHAT' ? '微信支付' : order.payMethod === 'ALIPAY' ? '支付宝' : order.payMethod === 'BANK' ? '银行卡' : order.payMethod === 'COD' ? '货到付款' : '其他' }}</el-descriptions-item>
        <el-descriptions-item label="商品总额">￥{{ order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="优惠券抵扣">-￥{{ (order.couponDiscount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="积分抵扣">-￥{{ (order.pointsDeductAmount || 0).toFixed(2) }} ({{ order.pointsUsed || 0 }}积分)</el-descriptions-item>
        <el-descriptions-item label="实付金额" class="pay-amount">￥{{ order.payAmount }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ order.receiverAddress }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="order && (order.deliveryTaskId || order.pickupTime || order.deliverTime)" class="delivery-card">
      <template #header>
        <span>配送信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="配送状态">{{ getDeliveryStatusText(order) }}</el-descriptions-item>
        <el-descriptions-item label="配送员" v-if="order.courierName">
          {{ order.courierName }} {{ order.courierPhone || '' }}
        </el-descriptions-item>
        <el-descriptions-item label="取件时间" v-if="order.pickupTime">{{ formatDate(order.pickupTime) }}</el-descriptions-item>
        <el-descriptions-item label="送达时间" v-if="order.deliverTime">{{ formatDate(order.deliverTime) }}</el-descriptions-item>
        <el-descriptions-item label="失败原因" :span="2" v-if="order.deliveryFailReason">
          {{ order.deliveryFailReason }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="items-card" v-if="items.length">
      <template #header>
        <span>商品明细</span>
      </template>
      <el-table :data="items" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="skuName" label="规格" width="100" />
        <el-table-column prop="unitPrice" label="单价" width="120">
          <template #default="{ row }">￥{{ row.unitPrice }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            <span class="subtotal">￥{{ ((row.unitPrice || 0) * (row.quantity || 0)).toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="actions">
      <el-button @click="goBack">返回列表</el-button>
      <template v-if="order">
        <el-button v-if="order.status === 'PENDING_PAY'" type="primary" @click="handlePay">去支付</el-button>
        <el-button v-if="['PENDING_PAY', 'PAID'].includes(order.status)" type="danger" @click="handleCancel">取消订单</el-button>
        <el-button v-if="['SHIPPING', 'PENDING_RECEIVED'].includes(order.status)" type="success" @click="handleConfirm">确认收货</el-button>
        <template v-if="order.status === 'COMPLETED'">
          <el-button type="primary" plain @click="handleReorder">再次购买</el-button>
          <el-button type="warning" plain @click="goAfterSale">申请售后</el-button>
          <el-button type="success" plain @click="goReview" v-if="!order.isReviewed">去评价</el-button>
        </template>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderAPI } from '@/api'

const router = useRouter()
const route = useRoute()
const order = ref(null)
const items = ref([])

onMounted(() => {
  loadOrderDetail()
})

const loadOrderDetail = async () => {
  try {
    const res = await orderAPI.getDetail(route.params.id)
    if (res.data) {
      order.value = res.data
      items.value = res.data.items || []
    }
  } catch (error) {
    console.error(error)
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', { hour12: false })
}

const getStatusType = (status) => {
  const map = {
    PENDING_PAY: 'warning',
    PAID: 'info',
    PENDING_SHIP: 'info',
    SHIPPING: 'primary',
    PENDING_RECEIVE: 'warning',
    PENDING_RECEIVED: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info',
    REFUNDED: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    PENDING_PAY: '待付款',
    PAID: '待发货',
    PENDING_SHIP: '待发货',
    SHIPPING: '配送中',
    PENDING_RECEIVE: '待收货',
    PENDING_RECEIVED: '待收货',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REFUNDED: '已退款'
  }
  return map[status] || status
}

const getDeliveryStatusText = (order) => {
  const deliveryMap = {
    ASSIGNED: '待取件',
    PICKED_UP: '配送中',
    DELIVERED: '已送达',
    FAILED: '配送失败'
  }
  if (order.deliveryStatus) return deliveryMap[order.deliveryStatus] || order.deliveryStatus
  const map = {
    PENDING_PAY: '待付款',
    PENDING_SHIP: '待发货',
    SHIPPING: '配送中',
    PENDING_RECEIVED: '待收货',
    COMPLETED: '已完成',
  }
  return map[order.status] || order.status || '-'
}

const handlePay = async () => {
  try {
    // 简化支付流程，直接弹窗模拟支付
    await ElMessageBox.confirm('确认支付该订单？', '提示', { type: 'info', confirmButtonText: '确认支付' })
    await orderAPI.pay(order.value.orderId, 'ALIPAY')
    ElMessage.success('支付成功')
    loadOrderDetail()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', { type: 'warning' })
    await orderAPI.cancel(order.value.orderId)
    ElMessage.success('订单已取消')
    loadOrderDetail()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认收到商品了吗？', '提示', { type: 'success' })
    await orderAPI.confirm(order.value.orderId)
    ElMessage.success('已确认收货')
    loadOrderDetail()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleReorder = async () => {
  try {
    await orderAPI.reorder(order.value.orderId)
    ElMessage.success('已加入购物车')
    router.push('/cart')
  } catch (error) {
    console.error(error)
  }
}

const goAfterSale = () => {
  router.push(`/apply-after-sale/${order.value.orderId}`)
}

const goReview = () => {
  router.push(`/review/${order.value.orderId}`)
}

const goBack = () => {
  router.push('/orders')
}
</script>

<style scoped>
.order-card, .items-card, .delivery-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subtotal {
  color: #f56c6c;
  font-weight: bold;
}

.pay-amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.actions {
  margin-top: 20px;
}
</style>
