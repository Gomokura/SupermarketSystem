<template>
  <div class="page-wrap">
    <div class="page-header">
      <h2 class="page-title">我的售后</h2>
      <el-button @click="$router.push('/orders')" link type="primary" size="small">
        <el-icon><ArrowLeft /></el-icon> 返回订单
      </el-button>
    </div>

    <div v-if="loading" style="text-align:center;padding:40px">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <el-empty v-else-if="asRecords.length === 0"
      description="暂无售后申请" :image-size="100" style="padding:40px 0" />

    <div v-else class="record-list">
      <el-card v-for="item in asRecords" :key="item.asId" class="record-card" shadow="hover">
        <div class="card-top">
          <span class="order-no">订单号：{{ item.orderId }}</span>
          <el-tag :type="getStatusType(item.status)" size="small" effect="light">
            {{ getStatusName(item.status) }}
          </el-tag>
        </div>

        <el-divider style="margin:10px 0" />

        <div class="card-body">
          <div class="info-item">
            <span class="lbl">类型</span>
            <el-tag size="small" :type="item.asType === 'REFUND' ? 'warning' : 'info'" effect="plain">
              {{ item.asType === 'REFUND' ? '仅退款' : '退货退款' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="lbl">申请原因</span>
            <span class="val">{{ item.reason || '-' }}</span>
          </div>
          <div class="info-item" v-if="item.refundAmount">
            <span class="lbl">退款金额</span>
            <span class="val price">￥{{ item.refundAmount }}</span>
          </div>
          <div class="info-item" v-if="item.adminRemark">
            <span class="lbl">处理备注</span>
            <span class="val">{{ item.adminRemark }}</span>
          </div>
          <div class="info-item" v-if="item.createTime">
            <span class="lbl">申请时间</span>
            <span class="val gray">{{ item.createTime }}</span>
          </div>
        </div>

        <!-- 状态时间线 -->
        <el-timeline style="margin-top:10px;padding-left:4px">
          <el-timeline-item color="#52c41a" size="small">已提交申请</el-timeline-item>
          <el-timeline-item
            v-if="['APPROVED','COMPLETED'].includes(item.status)"
            color="#1677ff" size="small">申请已通过</el-timeline-item>
          <el-timeline-item
            v-if="item.status === 'REJECTED'"
            color="#ff4d4f" size="small">申请被拒绝</el-timeline-item>
          <el-timeline-item
            v-if="item.status === 'COMPLETED'"
            color="#52c41a" size="small">退款已到账</el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { afterSalesAPI } from '@/api'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'

const asRecords = ref([])
const loading = ref(false)

const getStatusName = (st) => ({ PENDING: '待处理', APPROVED: '已同意', REJECTED: '已拒绝', COMPLETED: '已完成' })[st] || st
const getStatusType = (st) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', COMPLETED: 'info' })[st] || ''

onMounted(async () => {
  loading.value = true
  try {
    const res = await afterSalesAPI.getMyList()
    asRecords.value = res.data?.records || res.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})
</script>

<style scoped>
.page-wrap { padding: 16px 12px; }
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;
}
.page-title { font-size: 17px; font-weight: bold; color: #222; margin: 0; }
.record-list { display: flex; flex-direction: column; gap: 12px; }
.record-card { border-radius: 10px; }
.card-top { display: flex; justify-content: space-between; align-items: center; }
.order-no { font-size: 12px; color: #888; }
.card-body { display: flex; flex-direction: column; gap: 7px; }
.info-item { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.lbl { color: #aaa; white-space: nowrap; min-width: 52px; }
.val { color: #333; }
.price { color: #ff4d4f; font-weight: bold; }
.gray { color: #bbb; font-size: 12px; }
</style>
