<template>
  <div class="page-container">
    <h2>限时秒杀</h2>

    <!-- 活动状态筛选 -->
    <el-tabs v-model="activeState" @tab-change="loadActivities">
      <el-tab-pane label="进行中" name="running" />
      <el-tab-pane label="即将开始" name="pending" />
      <el-tab-pane label="已结束" name="ended" />
    </el-tabs>

    <div v-loading="loading">
      <el-empty v-if="!loading && activities.length === 0" description="暂无秒杀活动" />

      <div v-for="activity in activities" :key="activity.seckillId" class="activity-block">
        <el-card>
          <template #header>
            <div class="activity-header">
              <span class="activity-name">{{ activity.seckillName || activity.activityName }}</span>
              <el-tag :type="stateTag(activity.currentState || activity.state || 'active')">{{ stateLabel(activity.currentState || activity.state || 'active') }}</el-tag>
              <span class="activity-time">
                {{ formatDate(activity.startTime) }} ~ {{ formatDate(activity.endTime) }}
              </span>
            </div>
          </template>

          <div v-if="activityProducts[activity.seckillId]">
            <el-row :gutter="16">
              <el-col
                :xs="12" :sm="8" :md="6"
                v-for="sp in activityProducts[activity.seckillId]"
                :key="sp.id"
              >
                <el-card
                  shadow="hover"
                  class="product-card"
                  @click="goToProduct(sp.productId)"
                >
                  <img :src="sp.coverImage || sp.imageUrl || getProductImage(sp.productId, 'landscape_4_3')" class="product-img" />
                  <div class="product-name">{{ sp.productName }}</div>
                  <div class="price-block">
                    <span class="seckill-price">￥{{ sp.seckillPrice }}</span>
                    <span class="original-price">￥{{ sp.originalPrice }}</span>
                  </div>
                  <el-progress
                    :percentage="stockPercent(sp)"
                    :stroke-width="6"
                    :show-text="false"
                    status="exception"
                    class="stock-bar"
                  />
                  <div class="stock-text">仅剩 {{ sp.seckillStock }} 件</div>
                  <el-button
                    type="danger"
                    size="small"
                    style="width:100%;margin-top:8px"
                    :disabled="sp.seckillStock === 0 || (activity.currentState !== 'running' && activity.currentState !== 'active')"
                    @click.stop="addToCart(sp)"
                  >
                    {{ sp.seckillStock === 0 ? '已抢光' : '立即抢购' }}
                  </el-button>
                </el-card>
              </el-col>
            </el-row>
          </div>
          <div v-else class="loading-products">
            <el-button link @click="loadActivityProducts(activity.seckillId)">加载商品</el-button>
          </div>
        </el-card>
      </div>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          :total="total"
          :page-size="pageSize"
          layout="prev, pager, next"
          @current-change="loadActivities"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { seckillAPI, cartAPI } from '@/api'
import { getProductImage } from '@/utils/image'

const router = useRouter()
const activities = ref([])
const activityProducts = reactive({})
const loading = ref(false)
const activeState = ref('running')
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)

onMounted(() => loadActivities())

const loadActivities = async () => {
  loading.value = true
  try {
    const res = await seckillAPI.getList({ state: activeState.value, pageNum: pageNum.value, pageSize: pageSize.value })
    activities.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
    // 自动加载每个活动的商品
    for (const a of activities.value) {
      loadActivityProducts(a.seckillId)
    }
  } catch (e) {
    ElMessage.error('加载秒杀活动失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadActivityProducts = async (seckillId) => {
  try {
    const res = await seckillAPI.getActivityProducts(seckillId)
    activityProducts[seckillId] = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const addToCart = async (sp) => {
  try {
    await cartAPI.add(sp.productId, 1, sp.skuId)
    ElMessage.success('已加入购物车，快去结算吧！')
    router.push('/checkout')
  } catch (e) {
    ElMessage.error('加购失败')
    console.error(e)
  }
}

const goToProduct = (id) => router.push(`/products/${id}`)

const stockPercent = (sp) => {
  if (!sp.seckillStock) return 0
  const total = sp.seckillStock + (sp.soldCount || 0)
  if (!total) return 0
  return Math.round((sp.seckillStock / total) * 100)
}

const stateLabel = (state) => ({ running: '进行中', pending: '即将开始', paused: '已暂停', ended: '已结束', active: '进行中' }[state] || state)
const stateTag = (state) => ({ running: 'danger', pending: 'warning', paused: 'info', ended: 'info', active: 'danger' }[state] || '')

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.activity-block { margin-bottom: 20px; }
.activity-header { display: flex; align-items: center; gap: 12px; }
.activity-name { font-size: 16px; font-weight: bold; }
.activity-time { color: #999; font-size: 13px; margin-left: auto; }
.product-card { cursor: pointer; text-align: center; }
.product-img { width: 100%; height: 140px; object-fit: cover; border-radius: 4px; margin-bottom: 8px; }
.product-name { font-size: 13px; margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price-block { display: flex; justify-content: center; align-items: baseline; gap: 8px; margin-bottom: 6px; }
.seckill-price { color: #f56c6c; font-size: 18px; font-weight: bold; }
.original-price { color: #999; font-size: 12px; text-decoration: line-through; }
.stock-bar { margin-bottom: 4px; }
.stock-text { font-size: 12px; color: #e6a23c; }
.loading-products { text-align: center; padding: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
