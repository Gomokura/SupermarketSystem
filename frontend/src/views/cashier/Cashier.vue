<template>
  <div class="cashier-container">
    <!-- 顶部状态栏 -->
    <div class="top-bar">
      <div class="shift-info" v-if="currentShift">
        <el-tag type="success" effect="dark">班次进行中</el-tag>
        <span class="shift-text">
          开班时间：{{ currentShift.startTime }} | 备用金：¥{{ currentShift.startCash }}
        </span>
      </div>
      <div class="shift-info" v-else>
        <el-tag type="warning" effect="dark">未开班</el-tag>
        <el-button type="primary" size="small" @click="showOpenShiftDialog = true">开班</el-button>
      </div>
      <div class="top-actions">
        <el-button size="small" @click="showHistoryDialog = true">历史班次</el-button>
        <el-button size="small" @click="showOrderHistoryDialog = true">历史订单</el-button>
        <el-button type="warning" size="small" :disabled="!currentShift" @click="showCloseShiftDialog = true">交班</el-button>
      </div>
    </div>

    <!-- 主体布局 -->
    <div class="main-content">
      <!-- 左侧：商品搜索 + 结果 -->
      <div class="left-panel">
        <!-- 商品搜索 -->
        <el-card class="search-card">
          <div class="search-header">
            <span class="section-title">商品搜索</span>
          </div>
          <el-input
            v-model="searchKeyword"
            placeholder="输入商品名称或条码后按回车搜索"
            clearable
            @keyup.enter="handleSearch"
            :prefix-icon="Search"
          >
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
          <div class="search-hint">输入条码后可自动识别商品</div>

          <!-- 搜索结果 -->
          <div class="search-results" v-if="searchResults.length > 0">
            <div
              v-for="item in searchResults"
              :key="item.id"
              class="product-item"
              @click="addToCart(item)"
            >
              <div class="product-info">
                <span class="product-name">{{ item.name }}</span>
                <span class="product-barcode">{{ item.barcode }}</span>
              </div>
              <div class="product-price">¥{{ item.price }}</div>
            </div>
          </div>
          <div class="empty-results" v-else-if="searchKeyword && searched">
            <el-empty description="未找到商品" :image-size="60" />
          </div>
        </el-card>

        <!-- 会员识别 -->
        <el-card class="member-card">
          <div class="search-header">
            <span class="section-title">会员识别</span>
            <el-button text type="primary" size="small" v-if="member" @click="member = null">清除</el-button>
          </div>
          <el-input
            v-model="memberPhone"
            placeholder="输入手机号查询会员"
            clearable
            @keyup.enter="searchMember"
            :prefix-icon="User"
          >
            <template #append>
              <el-button @click="searchMember">查询</el-button>
            </template>
          </el-input>
          <div class="member-info" v-if="member">
            <el-descriptions :column="2" size="small" border>
              <el-descriptions-item label="会员">{{ member.nickname || member.name }}</el-descriptions-item>
              <el-descriptions-item label="等级">{{ member.memberLevel || member.levelName || member.level || '普通会员' }}</el-descriptions-item>
              <el-descriptions-item label="积分">{{ member.points || 0 }}</el-descriptions-item>
              <el-descriptions-item label="可用优惠券">{{ member.availableCouponCount ?? member.couponCount ?? 0 }} 张</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </div>

      <!-- 右侧：收银清单 -->
      <div class="right-panel">
        <el-card class="cart-card">
          <template #header>
            <div class="cart-header">
              <span class="section-title">收银清单</span>
              <el-button text type="danger" size="small" :disabled="cartItems.length === 0" @click="clearCart">清空清单</el-button>
            </div>
          </template>

          <!-- 清单表格 -->
          <el-table :data="cartItems" size="small" max-height="300">
            <el-table-column prop="name" label="商品" min-width="120" show-overflow-tooltip />
            <el-table-column prop="price" label="单价" width="80" align="right">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column label="数量" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  :max="999"
                  size="small"
                  :disabled="row.isGift"
                  @change="updateCartItem(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="80" align="right">
              <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="60" align="center">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="removeFromCart(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 合计 -->
          <div class="cart-summary">
            <div class="summary-row">
              <span>商品总数</span>
              <span>{{ totalQuantity }} 件</span>
            </div>
            <div class="summary-row total">
              <span>合计金额</span>
              <span class="total-price">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <div class="summary-row" v-if="member">
              <span>可用积分抵扣</span>
              <span>- ¥{{ pointsDiscount.toFixed(2) }}</span>
            </div>
            <div class="summary-row final" v-if="member">
              <span>应付金额</span>
              <span class="final-price">¥{{ finalAmount.toFixed(2) }}</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="cart-actions">
            <el-button type="primary" size="large" :disabled="cartItems.length === 0" @click="showCheckoutDialog = true">
              结账 (¥{{ (member ? finalAmount : totalAmount).toFixed(2) }})
            </el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 开班弹窗 -->
    <el-dialog v-model="showOpenShiftDialog" title="开班" width="400" :close-on-click-modal="false">
      <el-form :model="openShiftForm" label-width="100">
        <el-form-item label="备用金">
          <el-input-number v-model="openShiftForm.startCash" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showOpenShiftDialog = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleOpenShift">确认开班</el-button>
      </template>
    </el-dialog>

    <!-- 结账弹窗 -->
    <el-dialog v-model="showCheckoutDialog" title="结账" width="500" :close-on-click-modal="false">
      <div class="checkout-info">
        <div class="checkout-amount">
          <span class="label">应收金额</span>
          <span class="value">¥{{ (member ? finalAmount : totalAmount).toFixed(2) }}</span>
        </div>
        <div class="checkout-member" v-if="member">
          <span class="label">会员：{{ member.nickname || member.name }}</span>
          <span class="label">积分抵扣：-¥{{ pointsDiscount.toFixed(2) }}</span>
        </div>
      </div>

      <el-divider>选择支付方式</el-divider>

      <div class="pay-methods">
        <div
          class="pay-method"
          :class="{ active: payMethod === 'cash' }"
          @click="payMethod = 'cash'"
        >
          <el-icon :size="32"><Money /></el-icon>
          <span>现金支付</span>
        </div>
        <div
          class="pay-method"
          :class="{ active: payMethod === 'scan' }"
          @click="payMethod = 'scan'"
        >
          <el-icon :size="32"><Postcard /></el-icon>
          <span>模拟支付</span>
        </div>
      </div>

      <!-- 现金支付 -->
      <div class="cash-pay" v-if="payMethod === 'cash'">
        <el-form :model="cashForm" label-width="80" size="large">
          <el-form-item label="实收金额">
            <el-input-number v-model="cashForm.received" :min="0" :precision="2" :step="10" style="width: 100%" />
          </el-form-item>
          <el-form-item label="找零">
            <span class="change-amount" :class="{ warning: changeAmount < 0 }">
              ¥{{ Math.max(0, changeAmount).toFixed(2) }}
            </span>
          </el-form-item>
        </el-form>
        <div class="quick-amounts">
          <el-tag
            v-for="amt in [50, 100, 200, 500]"
            :key="amt"
            class="quick-tag"
            @click="cashForm.received = amt"
          >{{ amt }}</el-tag>
        </div>
      </div>

      <template #footer>
        <el-button @click="showCheckoutDialog = false">取消</el-button>
        <el-button type="success" :loading="loading" @click="handleCheckout">
          确认收款 ¥{{ (member ? finalAmount : totalAmount).toFixed(2) }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 交班弹窗 -->
    <el-dialog v-model="showCloseShiftDialog" title="交班" width="500" :close-on-click-modal="false">
      <el-form :model="closeShiftForm" label-width="100">
        <el-form-item label="实收现金">
          <el-input-number v-model="closeShiftForm.cashReceived" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="closeShiftForm.remark" type="textarea" :rows="2" placeholder="可选填写备注" />
        </el-form-item>
      </el-form>

      <div class="shift-summary" v-if="currentShift">
        <el-descriptions :column="1" size="small" border>
          <el-descriptions-item label="开班时间">{{ currentShift.startTime }}</el-descriptions-item>
          <el-descriptions-item label="备用金">¥{{ currentShift.startCash }}</el-descriptions-item>
          <el-descriptions-item label="预计应交">¥{{ (Number(currentShift.startCash) + Number(currentShift.totalSales || 0)).toFixed(2) }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="showCloseShiftDialog = false">取消</el-button>
        <el-button type="warning" :loading="loading" @click="handleCloseShift">确认交班</el-button>
      </template>
    </el-dialog>

    <!-- 历史班次弹窗 -->
    <el-dialog v-model="showHistoryDialog" title="历史班次" width="700">
      <el-table :data="shiftHistory" size="small" v-loading="historyLoading">
        <el-table-column prop="startTime" label="开班时间" width="160" />
        <el-table-column prop="endTime" label="交班时间" width="160" />
        <el-table-column prop="startCash" label="备用金" width="100" align="right">
          <template #default="{ row }">¥{{ row.startCash }}</template>
        </el-table-column>
        <el-table-column prop="endCash" label="实交现金" width="100" align="right">
          <template #default="{ row }">¥{{ row.endCash || '-' }}</template>
        </el-table-column>
        <el-table-column prop="totalSales" label="销售额" width="100" align="right">
          <template #default="{ row }">¥{{ row.totalSales || '-' }}</template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'closed' ? 'info' : 'success'" size="small">
              {{ row.status === 'closed' ? '已交班' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="historyTotal > 0"
        v-model:current-page="historyPage"
        v-model:page-size="historyPageSize"
        :total="historyTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 12px"
        @size-change="loadHistory"
        @current-change="loadHistory"
      />
    </el-dialog>

    <!-- 历史订单弹窗 -->
    <el-dialog v-model="showOrderHistoryDialog" title="历史订单查询" width="800">
      <el-form :inline="true" :model="orderSearchForm" style="margin-bottom: 16px">
        <el-form-item label="订单号">
          <el-input v-model="orderSearchForm.orderNo" placeholder="请输入订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="orderSearchForm.phone" placeholder="请输入手机号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchOrders">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="orderHistory" size="small" v-loading="orderHistoryLoading">
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column prop="customerName" label="顾客" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="totalAmount" label="金额" width="100" align="right">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.payMethod === 'CASH' ? '现金' : '扫码' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="150" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="refundOrder(row)" 
              :disabled="row.status !== 'COMPLETED'">退款</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="orderHistoryTotal > 0"
        v-model:current-page="orderHistoryPage"
        v-model:page-size="orderHistoryPageSize"
        :total="orderHistoryTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 12px"
        @size-change="searchOrders"
        @current-change="searchOrders"
      />
    </el-dialog>

    <!-- 成功提示 -->
    <el-dialog v-model="showSuccessDialog" title="结账成功" width="400" show-close>
      <div class="success-content">
        <el-icon class="success-icon" :size="64"><CircleCheck /></el-icon>
        <div class="success-info">
          <p>订单号：{{ successOrderNo }}</p>
          <p>实收金额：¥{{ successAmount }}</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showSuccessDialog = false">继续收银</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, User, Money, Postcard, CircleCheck } from '@element-plus/icons-vue'
import { cashierAPI, orderAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

// ========== 状态 ==========
const currentShift = ref(null)
const searchKeyword = ref('')
const searchResults = ref([])
const searched = ref(false)
const cartItems = ref([])
const member = ref(null)
const memberPhone = ref('')
const loading = ref(false)

// 弹窗状态
const showOpenShiftDialog = ref(false)
const showCheckoutDialog = ref(false)
const showCloseShiftDialog = ref(false)
const showHistoryDialog = ref(false)
const showOrderHistoryDialog = ref(false)
const showSuccessDialog = ref(false)

// 表单
const openShiftForm = ref({ startCash: 0 })
const closeShiftForm = ref({ cashReceived: 0, remark: '' })
const payMethod = ref('scan')
const cashForm = ref({ received: 0 })

// 历史班次
const shiftHistory = ref([])
const historyLoading = ref(false)
const historyPage = ref(1)
const historyPageSize = ref(10)
const historyTotal = ref(0)

// 成功信息
const successOrderNo = ref('')
const successAmount = ref('')

// 历史订单
const orderSearchForm = ref({ orderNo: '', phone: '' })
const orderHistory = ref([])
const orderHistoryLoading = ref(false)
const orderHistoryPage = ref(1)
const orderHistoryPageSize = ref(10)
const orderHistoryTotal = ref(0)

// ========== 计算属性 ==========
const totalQuantity = computed(() => cartItems.value.reduce((sum, item) => sum + item.quantity, 0))
const totalAmount = computed(() => cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0))

// 积分抵扣：100积分抵扣1元
const pointsDiscount = computed(() => {
  if (!member.value) return 0
  const maxDiscount = totalAmount.value * 0.2 // 最多抵扣20%
  const discount = Math.min(member.value.points / 100, maxDiscount)
  return Math.floor(discount * 100) / 100
})
const finalAmount = computed(() => Math.max(0, totalAmount.value - pointsDiscount.value))
const changeAmount = computed(() => cashForm.value.received - (member.value ? finalAmount.value : totalAmount.value))

// ========== 方法 ==========
// 加载当前班次
const loadCurrentShift = async () => {
  try {
    const res = await cashierAPI.getCurrentShift()
    currentShift.value = res.data
  } catch {
    currentShift.value = null
  }
}

const normalizeProduct = (p) => ({
  id: p.productId ?? p.id,
  name: p.productName ?? p.name,
  price: p.price,
  barcode: p.barcode
})

// 搜索商品
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) return
  searched.value = true
  searchResults.value = []
  try {
    // 先尝试条码查询
    try {
      const res = await cashierAPI.getByBarcode(searchKeyword.value.trim())
      if (res.data) {
        searchResults.value = [normalizeProduct(res.data)]
        return
      }
    } catch {
      // 不是条码，继续关键字搜索
    }
    // 关键字搜索
    const res = await cashierAPI.searchProduct(searchKeyword.value.trim())
    const raw = res.data?.records || res.data?.list || res.data || []
    searchResults.value = Array.isArray(raw) ? raw.map(normalizeProduct) : []
  } catch {
    searchResults.value = []
  }
}

// 添加到购物车
const addToCart = (product) => {
  const exist = cartItems.value.find(item => item.id === product.id)
  if (exist) {
    exist.quantity++
  } else {
    cartItems.value.push({
      id: product.id,
      name: product.name,
      price: product.price,
      quantity: 1,
      isGift: false
    })
  }
  searchKeyword.value = ''
  searchResults.value = []
  searched.value = false
  ElMessage.success(`已添加：${product.name}`)
}

// 更新购物车项
const updateCartItem = (item) => {
  if (item.quantity < 1) item.quantity = 1
}

// 从购物车移除
const removeFromCart = (item) => {
  const idx = cartItems.value.indexOf(item)
  if (idx > -1) cartItems.value.splice(idx, 1)
}

// 清空购物车
const clearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空收银清单吗？', '提示', { type: 'warning' })
    cartItems.value = []
    member.value = null
    memberPhone.value = ''
  } catch {
    // cancel
  }
}

// 搜索会员
const searchMember = async () => {
  if (!memberPhone.value.trim()) return
  try {
    const res = await cashierAPI.getMemberByPhone(memberPhone.value.trim())
    member.value = res.data
    ElMessage.success('会员识别成功')
  } catch {
    member.value = null
    ElMessage.error('未找到该会员')
  }
}

// 开班
const handleOpenShift = async () => {
  loading.value = true
  try {
    await cashierAPI.openShift(openShiftForm.value.startCash)
    ElMessage.success('开班成功')
    showOpenShiftDialog.value = false
    await loadCurrentShift()
  } catch {
    // error
  } finally {
    loading.value = false
  }
}

// 结账
const handleCheckout = async () => {
  const amount = member.value ? finalAmount.value : totalAmount.value

  if (payMethod.value === 'cash' && cashForm.value.received < amount) {
    ElMessage.error('实收金额不足')
    return
  }

  loading.value = true
  try {
    const checkoutData = {
      memberPhone: memberPhone.value || undefined,
      payMethod: payMethod.value === 'cash' ? 'CASH' : 'MOCK_CARD',
      receivedAmount: payMethod.value === 'cash' ? cashForm.value.received : undefined,
      items: cartItems.value.map(item => ({
        productId: item.id,
        quantity: item.quantity
      }))
    }

    const res = await cashierAPI.checkout(checkoutData)
    successOrderNo.value = res.data?.orderNo || res.data?.id || '-'
    successAmount.value = amount.toFixed(2)

    cartItems.value = []
    member.value = null
    memberPhone.value = ''
    showCheckoutDialog.value = false
    payMethod.value = 'scan'
    cashForm.value.received = 0
    showSuccessDialog.value = true

    await loadCurrentShift()
  } catch {
    // error
  } finally {
    loading.value = false
  }
}

// 交班
const handleCloseShift = async () => {
  loading.value = true
  try {
    await cashierAPI.closeShift(closeShiftForm.value)
    ElMessage.success('交班成功')
    showCloseShiftDialog.value = false
    currentShift.value = null
    cartItems.value = []
    member.value = null
    memberPhone.value = ''
  } catch {
    // error
  } finally {
    loading.value = false
  }
}

// 历史班次
const loadHistory = async () => {
  historyLoading.value = true
  try {
    const res = await cashierAPI.getHistory({
      page: historyPage.value,
      pageSize: historyPageSize.value
    })
    shiftHistory.value = res.data?.list || res.data || []
    historyTotal.value = res.data?.total || 0
  } catch {
    shiftHistory.value = []
  } finally {
    historyLoading.value = false
  }
}

// 历史订单查询
const searchOrders = async () => {
  orderHistoryLoading.value = true
  try {
    const res = await cashierAPI.getOrderHistory({
      pageNum: orderHistoryPage.value,
      pageSize: orderHistoryPageSize.value,
      orderNo: orderSearchForm.value.orderNo,
      phone: orderSearchForm.value.phone
    })
    orderHistory.value = res.data?.records || res.data || []
    orderHistoryTotal.value = res.data?.total || 0
  } catch {
    orderHistory.value = []
  } finally {
    orderHistoryLoading.value = false
  }
}

// 订单退款
const refundOrder = async (order) => {
  try {
    await ElMessageBox.confirm(`确定退款订单 ${order.orderNo} 吗？金额：¥${order.totalAmount}`, '退款确认', {
      type: 'warning'
    })
    await cashierAPI.refund(order.orderNo)
    ElMessage.success('退款成功')
    searchOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('退款失败')
  }
}

// 获取订单状态标签类型
const getStatusTagType = (status) => {
  const map = {
    COMPLETED: 'success',
    REFUNDED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || ''
}

// 获取订单状态文本
const getStatusText = (status) => {
  const map = {
    COMPLETED: '已完成',
    REFUNDED: '已退款',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

// ========== 初始化 ==========
onMounted(() => {
  loadCurrentShift()
})
</script>

<style scoped>
.cashier-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,.08);
  flex-shrink: 0;
}

.shift-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.shift-text {
  font-size: 14px;
  color: #666;
}

.top-actions {
  display: flex;
  gap: 8px;
}

.main-content {
  display: flex;
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.left-panel {
  width: 380px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.right-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.search-card, .member-card {
  flex-shrink: 0;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.search-hint {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}

.search-results {
  margin-top: 10px;
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #eee;
  border-radius: 4px;
}

.product-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background .15s;
}

.product-item:last-child { border-bottom: none; }
.product-item:hover { background: #f5f7fa; }

.product-info { display: flex; flex-direction: column; gap: 2px; }
.product-name { font-size: 14px; color: #333; }
.product-barcode { font-size: 12px; color: #999; }
.product-price { font-size: 14px; font-weight: 600; color: #f56c6c; }

.empty-results { padding: 20px 0; }

.member-info { margin-top: 10px; }

/* 收银清单 */
.cart-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-summary {
  margin-top: 12px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 6px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 14px;
  color: #666;
}

.summary-row.total {
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px dashed #ddd;
  font-weight: 600;
  color: #333;
}

.total-price {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}

.summary-row.final {
  margin-top: 4px;
  color: #67c23a;
}

.final-price {
  font-size: 20px;
  font-weight: 700;
  color: #67c23a;
}

.cart-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.cart-actions .el-button {
  width: 100%;
  font-size: 16px;
  height: 44px;
}

/* 结账 */
.checkout-info {
  background: #f0f9eb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.checkout-amount {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 20px;
  font-weight: 700;
}

.checkout-amount .value { color: #67c23a; }

.checkout-member {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 13px;
  color: #666;
}

.pay-methods {
  display: flex;
  gap: 16px;
  margin: 16px 0;
}

.pay-method {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all .2s;
  color: #666;
}

.pay-method:hover { border-color: #409eff; color: #409eff; }
.pay-method.active { border-color: #409eff; background: #ecf5ff; color: #409eff; }

.cash-pay { margin-top: 16px; }

.change-amount {
  font-size: 20px;
  font-weight: 700;
  color: #67c23a;
}

.change-amount.warning { color: #f56c6c; }

.quick-amounts {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.quick-tag {
  cursor: pointer;
}

/* 交班 */
.shift-summary { margin-top: 12px; }

/* 成功 */
.success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.success-icon { color: #67c23a; }
.success-info { margin-top: 16px; text-align: center; font-size: 16px; }
.success-info p { margin: 6px 0; }
</style>
