<template>
  <div class="page-container">
    <h2>我的订单</h2>

    <el-tabs v-model="activeStatus" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name=""></el-tab-pane>
      <el-tab-pane label="待付款" name="pending"></el-tab-pane>
      <el-tab-pane label="待发货" name="paid"></el-tab-pane>
      <el-tab-pane label="待收货" name="shipped"></el-tab-pane>
      <el-tab-pane label="已完成" name="completed"></el-tab-pane>
      <el-tab-pane label="已取消" name="cancelled"></el-tab-pane>
    </el-tabs>

    <el-table :data="orders" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column label="商品信息" min-width="250">
        <template #default="{ row }">
          <div v-for="item in row.items" :key="item.id" class="order-item">
            <el-image
              :src="item.productImage"
              style="width: 40px; height: 40px; margin-right: 10px; border-radius: 4px;"
              fit="cover"
            >
              <template #error>
                <div class="image-slot">暂无图片</div>
              </template>
            </el-image>
            <div class="item-info">
              <span class="product-name">{{ item.productName }}</span>
              <span class="product-sku" v-if="item.skuName">{{ item.skuName }}</span>
              <span class="product-quantity">x{{ item.quantity }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="总金额" width="120">
        <template #default="{ row }">
          <span class="amount">￥{{ row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="goDetail(row.orderId)">查看详情</el-button>
          <el-button v-if="canCancel(row.status)" type="danger" link size="small" @click="cancelOrder(row.orderId)">取消订单</el-button>
          <el-button v-if="row.status === 'completed' && !row.isReviewed" type="success" link size="small" @click="router.push(`/review/${row.orderId}`)">去评价</el-button>
          <el-button v-if="row.status === 'completed'" type="warning" link size="small" @click="router.push(`/after-sale?orderId=${row.orderId}`)">申请售后</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderAPI } from '@/api'

const router = useRouter()
const orders = ref([])
const activeStatus = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadOrders()
})

const loadOrders = async () => {
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (activeStatus.value) {
      params.status = activeStatus.value
    }
    const res = await orderAPI.getList(params)
    orders.value = res.data?.list || res.data || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error(error)
  }
}

const handleTabChange = () => {
  pageNum.value = 1
  loadOrders()
}

const getStatusType = (status) => {
  const map = {
    pending: 'warning',
    paid: 'info',
    shipped: 'primary',
    completed: 'success',
    cancelled: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    pending: '待付款',
    paid: '待发货',
    shipped: '待收货',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

const goDetail = (orderId) => {
  router.push(`/orders/${orderId}`)
}

const canCancel = (status) => {
  return ['pending', 'paid'].includes(status)
}

const cancelOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', { type: 'warning' })
    await orderAPI.cancel(orderId)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.order-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.order-item:last-child {
  margin-bottom: 0;
}
.item-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.product-name {
  font-size: 13px;
  line-height: 1.2;
}
.product-sku {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
.product-quantity {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 10px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
