<template>
  <div class="page-container">
    <h2>优惠券中心</h2>
    <el-tabs v-model="activeTab" class="coupon-tabs">
      <el-tab-pane label="领券中心" name="available">
        <div class="coupon-grid">
          <el-card v-for="coupon in availableCoupons" :key="coupon.couponId" class="coupon-card">
            <div class="coupon-content">
              <div class="coupon-left">
                <div class="coupon-value">
                  <span v-if="coupon.couponType === 1" class="amount">¥{{ coupon.value }}</span>
                  <span v-else class="amount">{{ coupon.value }}折</span>
                  <span class="threshold">满{{ coupon.minAmount }}可用</span>
                </div>
              </div>
              <div class="coupon-right">
                <div class="coupon-name">{{ coupon.name }}</div>
                <div class="coupon-desc">{{ coupon.description || '全场通用优惠券' }}</div>
                <div class="coupon-info">
                  <span>剩余 {{ coupon.remainCount || 0 }} 张</span>
                  <span>有效期至 {{ formatDate(coupon.expireTime) }}</span>
                </div>
                <el-button type="danger" size="small" @click="claimCoupon(coupon.couponId)" :disabled="coupon.remainCount === 0">
                  立即领取
                </el-button>
              </div>
            </div>
          </el-card>
          <el-empty v-if="availableCoupons.length === 0" description="暂无优惠券可领"></el-empty>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的优惠券" name="my">
        <el-tabs v-model="myCouponStatus" @tab-change="loadMyCoupons">
          <el-tab-pane label="未使用" name="available"></el-tab-pane>
          <el-tab-pane label="已使用" name="used"></el-tab-pane>
          <el-tab-pane label="已过期" name="expired"></el-tab-pane>
        </el-tabs>
        <div class="coupon-grid">
          <el-card v-for="coupon in myCoupons" :key="coupon.userCouponId" class="coupon-card my-coupon">
            <div class="coupon-content">
              <div class="coupon-left">
                <div class="coupon-value">
                  <span v-if="coupon.couponType === 1" class="amount">¥{{ coupon.value }}</span>
                  <span v-else class="amount">{{ coupon.value }}折</span>
                  <span class="threshold">满{{ coupon.minAmount }}可用</span>
                </div>
              </div>
              <div class="coupon-right">
                <div class="coupon-name">{{ coupon.name }}</div>
                <div class="coupon-status">
                  <el-tag v-if="coupon.status === 'AVAILABLE'" type="success" size="small">未使用</el-tag>
                  <el-tag v-else-if="coupon.status === 'USED'" type="info" size="small">已使用</el-tag>
                  <el-tag v-else type="danger" size="small">已过期</el-tag>
                </div>
                <div class="coupon-info">
                  <span>有效期至 {{ formatDate(coupon.expireTime) }}</span>
                </div>
              </div>
            </div>
          </el-card>
          <el-empty v-if="myCoupons.length === 0" :description="'暂无' + statusText + '优惠券'"></el-empty>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { couponAPI } from '@/api'

const activeTab = ref('available')
const myCouponStatus = ref('available')
const availableCoupons = ref([])
const myCoupons = ref([])

const statusText = computed(() => {
  const map = { available: '未使用', used: '已使用', expired: '已过期' }
  return map[myCouponStatus.value] || ''
})

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const loadAvailableCoupons = async () => {
  try {
    const res = await couponAPI.getAvailable()
    availableCoupons.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const loadMyCoupons = async () => {
  try {
    const res = await couponAPI.getMyCoupons(myCouponStatus.value)
    myCoupons.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const claimCoupon = async (couponId) => {
  try {
    await couponAPI.claim(couponId)
    ElMessage.success('领取成功')
    loadAvailableCoupons()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadAvailableCoupons()
  loadMyCoupons()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.coupon-tabs {
  margin-top: 20px;
}

.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.coupon-card {
  margin-bottom: 0;
}

.coupon-card :deep(.el-card__body) {
  padding: 0;
}

.coupon-content {
  display: flex;
  overflow: hidden;
}

.coupon-left {
  width: 100px;
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 12px;
  flex-shrink: 0;
}

.coupon-value {
  text-align: center;
}

.coupon-value .amount {
  font-size: 24px;
  font-weight: bold;
  display: block;
}

.coupon-value .threshold {
  font-size: 12px;
  opacity: 0.9;
}

.coupon-right {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.coupon-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.coupon-desc {
  font-size: 12px;
  color: #999;
}

.coupon-info {
  font-size: 12px;
  color: #666;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.coupon-status {
  margin-top: 4px;
}

.my-coupon .coupon-left {
  background: linear-gradient(135deg, #909399, #b1b3b8);
}
</style>
