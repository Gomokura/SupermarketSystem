<template>
  <div class="page-container">
    <h2>数据看板</h2>

    <!-- 统计概览 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon blue"><el-icon><User /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.userCount || 0 }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon green"><el-icon><Goods /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.productCount || 0 }}</div>
            <div class="stat-label">商品总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon orange"><el-icon><Tickets /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayOrderCount || 0 }}</div>
            <div class="stat-label">今日订单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon purple"><el-icon><Money /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.todayRevenue) }}</div>
            <div class="stat-label">今日营收</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon red"><el-icon><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value low-stock-num" @click="jumpLowStock" style="cursor:pointer;color:#f56c6c">
              {{ pendingStats.lowStockCount || 0 }}
            </div>
            <div class="stat-label">低库存商品 <el-icon><ArrowRight /></el-icon></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon cyan"><el-icon><Clock /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingStats.pendingOrders || 0 }}</div>
            <div class="stat-label">待处理订单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon yellow"><el-icon><Box /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingStats.pendingDeliveries || 0 }}</div>
            <div class="stat-label">待配送</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon pink"><el-icon><Service /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingStats.pendingAfterSales || 0 }}</div>
            <div class="stat-label">售后待处理</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <!-- 商品销售排行 -->
      <el-col :span="14">
        <el-card>
          <template #header><div class="card-header">商品销售排行榜</div></template>
          <div ref="rankChartRef" class="chart-container"></div>
          <el-empty v-if="topProducts.length === 0 && !loadingRank" description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
      <!-- 支付方式饼图 -->
      <el-col :span="10">
        <el-card>
          <template #header><div class="card-header">收款占比</div></template>
          <div ref="financeChartRef" class="chart-container"></div>
          <el-empty v-if="financeData.length === 0 && !loadingFinance" description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <!-- 综合看板 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              综合看板
              <el-select v-model="days" size="small" style="width:100px;margin-left:12px" @change="loadDashboard">
                <el-option :value="7" label="近7天" />
                <el-option :value="30" label="近30天" />
                <el-option :value="90" label="近90天" />
              </el-select>
            </div>
          </template>
          <div class="summary-grid" v-if="dashboard">
            <div class="summary-item">
              <span class="sum-label">累计订单</span>
              <span class="sum-value">{{ dashboard.totalOrders || 0 }}</span>
            </div>
            <div class="summary-item">
              <span class="sum-label">累计营收</span>
              <span class="sum-value">¥{{ formatMoney(dashboard.totalRevenue) }}</span>
            </div>
            <div class="summary-item">
              <span class="sum-label">新增用户</span>
              <span class="sum-value">{{ dashboard.newUsers || 0 }}</span>
            </div>
            <div class="summary-item">
              <span class="sum-label">客单价</span>
              <span class="sum-value">¥{{ dashboard.avgOrderAmount ? dashboard.avgOrderAmount.toFixed(2) : '0.00' }}</span>
            </div>
          </div>
          <el-empty v-else-if="loadingDashboard" description="加载中" :image-size="60" />
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
      <!-- 近期库存流水 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              近期库存流水
              <el-button link type="primary" size="small" style="float:right" @click="$router.push('/admin/inventory')">查看全部</el-button>
            </div>
          </template>
          <div class="log-list">
            <div v-for="log in inventoryLogs" :key="log.logId" class="log-item">
              <span class="log-type" :class="log.type">{{ log.type === 'IN' ? '入库' : '出库' }}</span>
              <span class="log-product">{{ log.productName || '商品#' + log.productId }}</span>
              <span class="log-qty">{{ log.type === 'IN' ? '+' : '-' }}{{ log.quantity }}</span>
            </div>
            <el-empty v-if="inventoryLogs.length === 0 && !loadingLogs" description="暂无记录" :image-size="40" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { adminAPI } from '@/api'
import { User, Goods, Tickets, Money, Warning, Clock, Box, Service, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()

const stats = ref({})
const pendingStats = ref({})
const dashboard = ref(null)
const days = ref(30)
const topProducts = ref([])
const financeData = ref([])
const inventoryLogs = ref([])

const rankChartRef = ref()
const financeChartRef = ref()
const loadingRank = ref(false)
const loadingFinance = ref(false)
const loadingDashboard = ref(false)
const loadingLogs = ref(false)

let rankChart = null
let financeChart = null

onMounted(() => {
  loadAll()
})

const loadAll = () => {
  loadStatistics()
  loadDashboard()
  loadTopProducts()
  loadFinance()
  loadInventoryLogs()
}

const loadStatistics = async () => {
  try {
    const res = await adminAPI.getStatistics()
    stats.value = res.data || {}
    pendingStats.value = {
      lowStockCount: res.data.lowStockCount || 0,
      pendingOrders: res.data.pendingOrders || 0,
      pendingDeliveries: res.data.pendingDeliveries || 0,
      pendingAfterSales: res.data.pendingAfterSales || 0
    }
  } catch (e) { /* ignore */ }
}

const loadDashboard = async () => {
  loadingDashboard.value = true
  try {
    const res = await adminAPI.getDashboard({ days: days.value, topN: 10 })
    dashboard.value = res.data || {}
  } catch (e) { /* ignore */ } finally {
    loadingDashboard.value = false
  }
}

const loadTopProducts = async () => {
  loadingRank.value = true
  try {
    const res = await adminAPI.getTopProducts({ limit: 10 })
    topProducts.value = res.data || []
    await nextTick()
    renderRankChart()
  } catch (e) { /* ignore */ } finally {
    loadingRank.value = false
  }
}

const loadFinance = async () => {
  loadingFinance.value = true
  try {
    const res = await adminAPI.getFinance()
    financeData.value = res.data || []
    await nextTick()
    renderFinanceChart()
  } catch (e) { /* ignore */ } finally {
    loadingFinance.value = false
  }
}

const loadInventoryLogs = async () => {
  loadingLogs.value = true
  try {
    const res = await adminAPI.getInventoryLogs({ pageNum: 1, pageSize: 10 })
    const list = res.data?.records || res.data || []
    inventoryLogs.value = Array.isArray(list) ? list : []
  } catch (e) { /* ignore */ } finally {
    loadingLogs.value = false
  }
}

const renderRankChart = () => {
  if (!rankChartRef.value) return
  if (!rankChart) rankChart = echarts.init(rankChartRef.value)
  const data = [...topProducts.value].reverse()
  rankChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 10, right: 20, top: 10, bottom: 10, containLabel: true },
    xAxis: { type: 'value', axisLine: { show: false }, axisTick: { show: false } },
    yAxis: {
      type: 'category',
      data: data.map(d => d.productName || `商品${d.productId}`),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { fontSize: 12 }
    },
    series: [{
      type: 'bar',
      data: data.map(d => d.salesCount || 0),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#79bbff' },
          { offset: 1, color: '#409eff' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      label: { show: true, position: 'right', fontSize: 12, color: '#666' }
    }]
  })
}

const renderFinanceChart = () => {
  if (!financeChartRef.value) return
  if (!financeChart) financeChart = echarts.init(financeChartRef.value)
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
  const total = financeData.value.reduce((s, d) => s + (d.amount || 0), 0)
  financeChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: (p) => `${p.name}<br/>¥${(p.value / 100).toFixed(2)}（${p.percent}%）`
    },
    legend: { bottom: 0, left: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      data: financeData.value.map((d, i) => ({
        name: d.payMethod || d.type || '其他',
        value: d.amount || 0,
        itemStyle: { color: colors[i % colors.length] }
      }))
    }]
  })
}

const formatMoney = (val) => {
  if (!val && val !== 0) return '0.00'
  return (val / 100).toFixed(2)
}

const jumpLowStock = () => {
  router.push('/admin/inventory')
}

// 窗口变化时重绘图表
window.addEventListener('resize', () => {
  rankChart?.resize()
  financeChart?.resize()
})
</script>

<style scoped>
.page-container { padding: 20px; }
h2 { margin: 0 0 20px; font-size: 18px; }
.stat-row { margin-bottom: 16px; }
.chart-row { margin-bottom: 16px; }
.stat-card {
  display: flex; align-items: center; gap: 14px;
  padding: 4px 0;
}
.stat-icon {
  width: 48px; height: 48px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; color: #fff; flex-shrink: 0;
}
.blue { background: #409eff; }
.green { background: #67c23a; }
.orange { background: #e6a23c; }
.purple { background: #9c27b0; }
.red { background: #f56c6c; }
.cyan { background: #36b9c8; }
.yellow { background: #f2c96b; }
.pink { background: #e061ad; }
.stat-info { flex: 1; min-width: 0; }
.stat-value { font-size: 22px; font-weight: bold; color: #333; }
.stat-label { font-size: 13px; color: #999; margin-top: 2px; }
.low-stock-num { text-decoration: underline; }
.card-header { font-size: 14px; font-weight: 600; display: flex; align-items: center; }
.chart-container { height: 280px; }
.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.summary-item { text-align: center; padding: 16px 8px; background: #f5f7fa; border-radius: 8px; }
.sum-label { display: block; font-size: 12px; color: #999; margin-bottom: 6px; }
.sum-value { display: block; font-size: 20px; font-weight: bold; color: #333; }
.log-list { max-height: 280px; overflow-y: auto; }
.log-item { display: flex; align-items: center; gap: 8px; padding: 7px 0; border-bottom: 1px solid #f0f0f0; }
.log-item:last-child { border-bottom: none; }
.log-type { font-size: 11px; padding: 1px 6px; border-radius: 3px; flex-shrink: 0; }
.log-type.IN { background: #e1f3d8; color: #67c23a; }
.log-type.OUT { background: #fef0f0; color: #f56c6c; }
.log-product { flex: 1; font-size: 13px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.log-qty { font-size: 13px; font-weight: 600; color: #333; flex-shrink: 0; }
</style>
