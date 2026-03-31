<template>
  <div class="page-container">
    <div class="page-header">
      <h2>优惠券中心</h2>
    </div>

    <el-tabs v-model="activeTab" class="coupon-tabs">
      <!-- 领券中心 -->
      <el-tab-pane label="领券中心" name="claim">
        <div v-loading="claimLoading">
          <el-row :gutter="20" v-if="claimList.length > 0">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="coupon in claimList" :key="coupon.couponId">
              <div class="coupon-card" :class="{ 'coupon-disabled': coupon.remainingCount <= 0 }">
                <div class="coupon-left">
                  <div class="coupon-value">
                    <span class="coupon-symbol">¥</span>
                    <span class="coupon-amount">{{ coupon.type === 'cash' ? coupon.value : coupon.value }}</span>
                    <span class="coupon-unit">{{ coupon.type === 'discount' ? '折' : '元' }}</span>
                  </div>
                  <div class="coupon-threshold">
                    满{{ coupon.minAmount }}元可用
                  </div>
                </div>
                <div class="coupon-right">
                  <div class="coupon-name">{{ coupon.name }}</div>
                  <div class="coupon-desc">{{ coupon.description || '全场通用' }}</div>
                  <div class="coupon-date">有效期至 {{ formatDate(coupon.endTime) }}</div>
                  <div class="coupon-count">
                    剩余 {{ coupon.remainingCount }} 张
                    <el-tag v-if="coupon.remainingCount <= 0" type="danger" size="small">已领完</el-tag>
                  </div>
                  <el-button
                    type="danger"
                    size="small"
                    class="coupon-btn"
                    :disabled="coupon.remainingCount <= 0"
                    @click="claimCoupon(coupon)"
                  >
                    立即领取
                  </el-button>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-empty v-else description="暂无优惠券可领取" />
        </div>
      </el-tab-pane>

      <!-- 我的优惠券 -->
      <el-tab-pane label="我的优惠券" name="mine">
        <el-tabs v-model="myCouponStatus" @tab-change="loadMyCoupons">
          <el-tab-pane label="未使用" name="available" />
          <el-tab-pane label="已使用" name="used" />
          <el-tab-pane label="已过期" name="expired" />
        </el-tabs>

        <div v-loading="myLoading">
          <el-row :gutter="20" v-if="myCouponList.length > 0">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="coupon in myCouponList" :key="coupon.userCouponId">
              <div class="coupon-card" :class="getCouponStatusClass(coupon)">
                <div class="coupon-left">
                  <div class="coupon-value">
                    <span class="coupon-symbol">¥</span>
                    <span class="coupon-amount">{{ coupon.value }}</span>
                    <span class="coupon-unit">{{ coupon.type === 'discount' ? '折' : '元' }}</span>
                  </div>
                  <div class="coupon-threshold">
                    满{{ coupon.minAmount }}元可用
                  </div>
                </div>
                <div class="coupon-right">
                  <div class="coupon-name">{{ coupon.name }}</div>
                  <div class="coupon-desc">{{ coupon.description || '全场通用' }}</div>
                  <div class="coupon-date">有效期至 {{ formatDate(coupon.endTime) }}</div>
                  <div class="coupon-status">
                    <el-tag :type="getCouponTagType(coupon)" size="small">
                      {{ getCouponStatusText(coupon) }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-empty v-else :description="`暂无${getStatusLabel()}的优惠券`" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { couponAPI } from '@/api'

const activeTab = ref('claim')
const myCouponStatus = ref('available')

const claimList = ref([])
const myCouponList = ref([])
const claimLoading = ref(false)
const myLoading = ref(false)

onMounted(() => {
  loadClaimList()
  loadMyCoupons()
})

const loadClaimList = async () => {
  claimLoading.value = true
  try {
    const res = await couponAPI.getAvailable()
    claimList.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    claimLoading.value = false
  }
}

const loadMyCoupons = async () => {
  myLoading.value = true
  try {
    const res = await couponAPI.getMyCoupons(myCouponStatus.value)
    myCouponList.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    myLoading.value = false
  }
}

const claimCoupon = async (coupon) => {
  try {
    await couponAPI.claim(coupon.couponId)
    ElMessage.success(`成功领取「${coupon.name}」`)
    loadClaimList()
    loadMyCoupons()
  } catch (error) {
    console.error(error)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.split(' ')[0]
}

const getStatusLabel = () => {
  const map = { available: '未使用', used: '已使用', expired: '已过期' }
  return map[myCouponStatus.value] || ''
}

const getCouponStatusClass = (coupon) => {
  if (myCouponStatus.value === 'used') return 'coupon-used'
  if (myCouponStatus.value === 'expired') return 'coupon-expired'
  return ''
}

const getCouponTagType = (coupon) => {
  if (myCouponStatus.value === 'used') return 'info'
  if (myCouponStatus.value === 'expired') return 'warning'
  if (coupon.status === 'used') return 'info'
  if (coupon.status === 'expired') return 'warning'
  return 'success'
}

const getCouponStatusText = (coupon) => {
  if (myCouponStatus.value === 'used') return '已使用'
  if (myCouponStatus.value === 'expired') return '已过期'
  if (coupon.status === 'used') return '已使用'
  if (coupon.status === 'expired') return '已过期'
  return '未使用'
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
}
.coupon-tabs {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}
.coupon-card {
  display: flex;
  border: 1px solid #ed6a0c;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
  height: 140px;
  background: #fff;
}
.coupon-card.coupon-disabled {
  border-color: #dcdfe6;
  opacity: 0.6;
}
.coupon-card.coupon-used {
  border-color: #dcdfe6;
}
.coupon-card.coupon-expired {
  border-color: #dcdfe6;
  opacity: 0.6;
}
.coupon-left {
  width: 100px;
  background: linear-gradient(135deg, #ed6a0c, #ff8c3a);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  padding: 10px;
  flex-shrink: 0;
}
.coupon-disabled .coupon-left {
  background: linear-gradient(135deg, #909399, #b1b3b8);
}
.coupon-used .coupon-left,
.coupon-expired .coupon-left {
  background: linear-gradient(135deg, #909399, #b1b3b8);
}
.coupon-value {
  font-size: 22px;
  font-weight: bold;
  line-height: 1;
}
.coupon-symbol {
  font-size: 14px;
}
.coupon-unit {
  font-size: 12px;
}
.coupon-threshold {
  font-size: 11px;
  margin-top: 6px;
  opacity: 0.9;
}
.coupon-right {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.coupon-name {
  font-weight: bold;
  font-size: 14px;
  color: #303133;
}
.coupon-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.coupon-date {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}
.coupon-count {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.coupon-btn {
  margin-top: 6px;
  width: 100%;
}
.coupon-status {
  margin-top: 6px;
}
</style>
