<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>优惠券中心</h2>
        <p>领券、查看状态和有效期都放在这里。</p>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="领券中心" name="center">
        <div class="toolbar">
          <el-tag type="info">可领取 {{ centerCoupons.length }} 张</el-tag>
        </div>
        <div class="coupon-grid" v-loading="loadingCenter">
          <el-card
            v-for="coupon in centerCoupons"
            :key="coupon.couponId"
            class="coupon-card"
            shadow="hover"
          >
            <div class="coupon-left">
              <div class="face-value">{{ formatValue(coupon) }}</div>
              <div class="condition">{{ formatMinAmount(coupon.minAmount) }}</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ coupon.couponName }}</div>
              <div class="coupon-desc">{{ coupon.description || '通用优惠券' }}</div>
              <div class="coupon-meta">
                <span>剩余 {{ coupon.remainCount < 0 ? '不限' : coupon.remainCount }}</span>
                <span>每人限领 {{ coupon.perLimit < 0 ? '不限' : coupon.perLimit }}</span>
              </div>
              <div class="coupon-meta">
                <span>{{ formatDate(coupon.startTime) }} - {{ formatDate(coupon.endTime) }}</span>
              </div>
              <div class="coupon-actions">
                <el-tag v-if="!coupon.canClaim" type="warning" effect="plain">已达上限</el-tag>
                <el-button
                  type="danger"
                  size="small"
                  :disabled="!coupon.canClaim"
                  @click="claimCoupon(coupon)"
                >
                  立即领取
                </el-button>
              </div>
            </div>
          </el-card>
          <el-empty v-if="!loadingCenter && centerCoupons.length === 0" description="暂无可领取优惠券" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的优惠券" name="mine">
        <div class="toolbar">
          <el-radio-group v-model="couponStatus" size="small" @change="loadMyCoupons">
            <el-radio-button label="unused">未使用</el-radio-button>
            <el-radio-button label="used">已使用</el-radio-button>
            <el-radio-button label="expired">已过期</el-radio-button>
          </el-radio-group>
        </div>
        <div class="coupon-grid" v-loading="loadingMine">
          <el-card
            v-for="coupon in myCoupons"
            :key="coupon.userCouponId"
            class="coupon-card mine"
            shadow="never"
          >
            <div class="coupon-left">
              <div class="face-value">{{ formatValue(coupon) }}</div>
              <div class="condition">{{ formatMinAmount(coupon.minAmount) }}</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ coupon.couponName }}</div>
              <div class="coupon-meta">
                <el-tag :type="statusTag(coupon.status)" size="small">{{ statusText(coupon.status) }}</el-tag>
              </div>
              <div class="coupon-meta">
                <span>有效期至 {{ formatDate(coupon.endTime) }}</span>
              </div>
            </div>
          </el-card>
          <el-empty v-if="!loadingMine && myCoupons.length === 0" description="当前没有对应状态的优惠券" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { couponAPI } from '@/api'

const activeTab = ref('center')
const couponStatus = ref('unused')
const centerCoupons = ref([])
const myCoupons = ref([])
const loadingCenter = ref(false)
const loadingMine = ref(false)

const formatDate = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleDateString('zh-CN')
}

const formatValue = (coupon) => {
  if (coupon.couponType === 'discount') {
    return `${coupon.faceValue} 折`
  }
  return `￥${Number(coupon.faceValue || coupon.discount || 0).toFixed(0)}`
}

const formatMinAmount = (amount) => {
  if (!amount) return '无门槛'
  return `满 ￥${Number(amount).toFixed(0)} 可用`
}

const statusText = (status) => {
  const map = {
    unused: '未使用',
    used: '已使用',
    expired: '已过期'
  }
  return map[status] || status
}

const statusTag = (status) => {
  const map = {
    unused: 'success',
    used: 'info',
    expired: 'warning'
  }
  return map[status] || ''
}

const loadCenterCoupons = async () => {
  loadingCenter.value = true
  try {
    const res = await couponAPI.getCenter()
    centerCoupons.value = res.data || []
  } finally {
    loadingCenter.value = false
  }
}

const loadMyCoupons = async () => {
  loadingMine.value = true
  try {
    const res = await couponAPI.getMyCoupons(couponStatus.value)
    myCoupons.value = res.data || []
  } finally {
    loadingMine.value = false
  }
}

const claimCoupon = async (coupon) => {
  await couponAPI.claim(coupon.couponId)
  ElMessage.success(`已领取 ${coupon.couponName}`)
  await Promise.all([loadCenterCoupons(), loadMyCoupons()])
}

onMounted(() => {
  loadCenterCoupons()
  loadMyCoupons()
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
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}

.coupon-card :deep(.el-card__body) {
  display: flex;
  padding: 0;
  min-height: 140px;
}

.coupon-left {
  width: 108px;
  background: linear-gradient(180deg, #ef4444, #fb7185);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 10px;
}

.mine .coupon-left {
  background: linear-gradient(180deg, #64748b, #94a3b8);
}

.face-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.condition {
  margin-top: 8px;
  font-size: 12px;
  text-align: center;
  opacity: 0.92;
}

.coupon-right {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.coupon-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.coupon-desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}

.coupon-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  color: #909399;
  font-size: 12px;
}

.coupon-actions {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
