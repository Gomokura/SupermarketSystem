<template>
  <div class="orders-wrap">
    <!-- Tab筛选 -->
    <div class="tab-bar">
      <div
        v-for="tab in tabs" :key="tab.value"
        class="tab-item"
        :class="{ active: activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >{{ tab.label }}</div>
    </div>

    <div v-if="loading" style="text-align:center;padding:40px">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <el-empty v-else-if="orders.length === 0"
      description="暂无订单" :image-size="100" style="padding:40px 0">
      <el-button type="primary" @click="$router.push('/products')">去购物</el-button>
    </el-empty>

    <div v-else class="order-list">
      <div class="order-card" v-for="order in orders" :key="order.orderId">
        <!-- 卡头 -->
        <div class="card-header" @click="viewDetail(order.orderId)">
          <span class="order-no">订单 #{{ order.orderId }}</span>
          <el-tag :type="getStatusType(order.status)" size="small" effect="plain">
            {{ statusText(order.status) }}
          </el-tag>
        </div>

        <!-- 商品列表 -->
        <div class="items-wrap" @click="viewDetail(order.orderId)">
          <div class="order-item" v-for="item in (order.items || []).slice(0, 2)" :key="item.orderItemId">
            <img
              :src="item.coverImage || `https://picsum.photos/seed/p${item.productId}/80/80`"
              class="item-img"
              @error="e => e.target.src = `https://picsum.photos/seed/p${item.productId}/80/80`"
            />
            <div class="item-info">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-price">￥{{ item.price }} × {{ item.quantity }}</div>
            </div>
          </div>
          <div v-if="(order.items || []).length > 2" class="more-hint">
            还有 {{ order.items.length - 2 }} 件商品 ›
          </div>
        </div>

        <!-- 卡底 -->
        <div class="card-footer">
          <div class="order-time">{{ formatDate(order.createTime) }}</div>
          <div class="order-total">
            共 {{ getTotalCount(order) }} 件，实付
            <span class="amount">￥{{ order.payAmount || order.totalAmount }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="card-actions">
          <el-button v-if="order.status === 'PENDING_PAY'" type="primary" size="small" @click="payOrder(order.orderId)">立即支付</el-button>
          <el-button v-if="canCancel(order.status)" size="small" @click="cancelOrder(order.orderId)">取消订单</el-button>
          <el-button v-if="order.status === 'PENDING_RECEIVED'" type="warning" size="small" @click="confirmReceive(order.orderId)">确认收货</el-button>
          <el-button v-if="order.status === 'COMPLETED' && !order.isReviewed" type="info" size="small" @click="$router.push(`/review/${order.orderId}`)">去评价</el-button>
          <el-button v-if="order.status === 'COMPLETED'" size="small" @click="reorder(order.orderId)">再次购买</el-button>
          <el-button size="small" plain @click="viewDetail(order.orderId)">查看详情</el-button>
        </div>
      </div>
    </div>

    <div v-if="total > pageSize" style="padding:12px;text-align:center">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        small
        @current-change="loadOrders"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { orderAPI } from '@/api'

const router = useRouter()
const activeTab = ref('all')
const orders = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待付款', value: 'PENDING_PAY' },
  { label: '待发货', value: 'PAID' },
  { label: '配送中', value: 'SHIPPING' },
  { label: '待收货', value: 'PENDING_RECEIVED' },
  { label: '已完成', value: 'COMPLETED' },
]

onMounted(loadOrders)

const switchTab = (val) => {
  activeTab.value = val
  pageNum.value = 1
  loadOrders()
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (activeTab.value !== 'all') params.status = activeTab.value
    const res = await orderAPI.getList(params)
    orders.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) { ElMessage.error('加载订单失败') }
  finally { loading.value = false }
}

const formatDate = (date) => date ? new Date(date).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : ''

const getTotalCount = (order) => (order.items || []).reduce((s, i) => s + (i.quantity || 0), 0)

const statusText = (s) => ({ PENDING_PAY: '待付款', PAID: '待发货', SHIPPING: '配送中', PENDING_RECEIVED: '待收货', COMPLETED: '已完成', CANCELLED: '已取消' })[s] || s
const getStatusType = (s) => ({ PENDING_PAY: 'warning', PAID: 'info', SHIPPING: 'primary', PENDING_RECEIVED: 'warning', COMPLETED: 'success', CANCELLED: 'danger' })[s] || ''
const canCancel = (s) => ['PENDING_PAY', 'PAID'].includes(s)

const viewDetail = (id) => router.push(`/orders/${id}`)

const payOrder = async (id) => {
  try {
    await orderAPI.pay(id, 'ONLINE')
    ElMessage.success('支付成功')
    loadOrders()
  } catch { ElMessage.error('支付失败') }
}

const cancelOrder = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', { type: 'warning' })
    await orderAPI.cancel(id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (e) { if (e !== 'cancel') ElMessage.error('取消失败') }
}

const confirmReceive = async (id) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'success' })
    await orderAPI.confirm(id)
    ElMessage.success('已确认收货，感谢购物！')
    loadOrders()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const reorder = async (id) => {
  try {
    await orderAPI.reorder(id)
    ElMessage.success('已加入购物车')
    router.push('/cart')
  } catch { ElMessage.error('操作失败') }
}
</script>

<style scoped>
.orders-wrap { background: #f5f5f5; min-height: 100%; }

.tab-bar {
  display: flex; overflow-x: auto; background: #fff;
  border-bottom: 1px solid #f0f0f0; padding: 0 4px;
  scrollbar-width: none;
}
.tab-bar::-webkit-scrollbar { display: none; }
.tab-item {
  flex-shrink: 0; padding: 12px 14px; font-size: 13px;
  color: #666; white-space: nowrap; cursor: pointer;
  border-bottom: 2px solid transparent; transition: all 0.2s;
}
.tab-item.active { color: #ff4d4f; border-bottom-color: #ff4d4f; font-weight: bold; }

.order-list { padding: 10px; display: flex; flex-direction: column; gap: 10px; }

.order-card { background: #fff; border-radius: 10px; overflow: hidden; }

.card-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 14px; border-bottom: 1px solid #f5f5f5; cursor: pointer;
}
.order-no { font-size: 12px; color: #888; }

.items-wrap { padding: 10px 14px; cursor: pointer; }
.order-item { display: flex; gap: 10px; margin-bottom: 8px; }
.order-item:last-child { margin-bottom: 0; }
.item-img { width: 56px; height: 56px; border-radius: 6px; object-fit: cover; flex-shrink: 0; }
.item-info { flex: 1; min-width: 0; }
.item-name { font-size: 13px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 4px; }
.item-price { font-size: 12px; color: #999; }
.more-hint { font-size: 12px; color: #bbb; text-align: right; }

.card-footer {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 14px; border-top: 1px solid #f5f5f5; border-bottom: 1px solid #f5f5f5;
}
.order-time { font-size: 11px; color: #bbb; }
.order-total { font-size: 12px; color: #666; }
.amount { color: #ff4d4f; font-weight: bold; font-size: 15px; }

.card-actions {
  display: flex; gap: 8px; padding: 10px 14px; justify-content: flex-end; flex-wrap: wrap;
}
</style>
