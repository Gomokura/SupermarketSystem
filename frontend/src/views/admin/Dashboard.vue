<template>
  <div class="dashboard">
    <!-- 顶部核心指标 -->
    <div class="metrics-grid">
      <div class="metric-card" v-for="m in metrics" :key="m.key">
        <div class="metric-icon" :style="{ background: m.iconBg }">
          <el-icon :size="22" :color="m.iconColor"><component :is="m.icon" /></el-icon>
        </div>
        <div class="metric-body">
          <div class="metric-label">{{ m.label }}</div>
          <div class="metric-value" :style="{ color: m.valueColor || '#1a1a1a' }">
            {{ m.prefix }}{{ formatNum(m.value) }}{{ m.suffix }}
          </div>
          <div class="metric-trend" v-if="m.trend !== undefined">
            <span :class="m.trend >= 0 ? 'up' : 'down'">
              {{ m.trend >= 0 ? '↑' : '↓' }} {{ Math.abs(m.trend).toFixed(1) }}%
            </span>
            <span class="trend-label">较昨日</span>
          </div>
          <div class="metric-sub" v-else-if="m.sub">{{ m.sub }}</div>
        </div>
      </div>
    </div>

    <!-- 待处理事项 -->
    <div class="todo-bar">
      <div
        v-for="todo in todos"
        :key="todo.key"
        class="todo-item"
        @click="$router.push(todo.path)"
      >
        <span class="todo-badge" :style="{ background: todo.color }">{{ todo.value }}</span>
        <span class="todo-label">{{ todo.label }}</span>
        <el-icon class="todo-arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 销售趋势 -->
    <div class="chart-section">
      <div class="chart-card wide">
        <div class="chart-header">
          <span class="chart-title">销售趋势</span>
          <el-radio-group v-model="trendPeriod" size="small" @change="loadData">
            <el-radio-button label="7">7天</el-radio-button>
            <el-radio-button label="30">30天</el-radio-button>
            <el-radio-button label="90">90天</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="trendRef" class="chart-body" style="height:320px" />
      </div>
    </div>

    <!-- 商品排行 + 分类占比 -->
    <div class="chart-section two-col">
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">商品销量 TOP10</span>
        </div>
        <div ref="productRef" class="chart-body" style="height:280px" />
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">分类销售占比</span>
        </div>
        <div ref="categoryRef" class="chart-body" style="height:280px" />
      </div>
    </div>

    <!-- 用户 + 优惠券 -->
    <div class="chart-section two-col">
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">用户新增趋势</span>
        </div>
        <div ref="userRef" class="chart-body" style="height:260px" />
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <span class="chart-title">优惠券核销漏斗</span>
        </div>
        <div ref="couponRef" class="chart-body" style="height:260px" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import * as echarts from 'echarts'
import { adminAPI, orderAPI, afterSaleAPI } from '@/api'
import {
  TrendCharts, ShoppingCart, Money, Warning, List, Tickets, Box, ArrowRight
} from '@element-plus/icons-vue'

const trendPeriod = ref('30')
const trendRef = ref()
const productRef = ref()
const categoryRef = ref()
const userRef = ref()
const couponRef = ref()

let charts = {}

const raw = ref({
  todayRevenue: 0, todayOrderCount: 0, todayGrowthRate: 0,
  monthRevenue: 0, monthOrderCount: 0, monthRefundAmount: 0,
  lowStockCount: 0, pendingOrders: 0, pendingRefunds: 0, pendingPurchase: 0
})

const metrics = computed(() => [
  {
    key: 'revenue', label: '今日销售额', icon: Money,
    value: raw.value.todayRevenue, prefix: '¥',
    trend: raw.value.todayGrowthRate,
    iconBg: '#e6f4ff', iconColor: '#1677ff', valueColor: '#1677ff'
  },
  {
    key: 'orders', label: '今日订单数', icon: ShoppingCart,
    value: raw.value.todayOrderCount,
    sub: `本月: ${raw.value.monthOrderCount} 单`,
    iconBg: '#f6ffed', iconColor: '#52c41a', valueColor: '#52c41a'
  },
  {
    key: 'avg', label: '今日客单价', icon: TrendCharts,
    value: raw.value.todayOrderCount
      ? (raw.value.todayRevenue / raw.value.todayOrderCount).toFixed(2)
      : '0.00',
    prefix: '¥', sub: '日均值',
    iconBg: '#fffbe6', iconColor: '#faad14', valueColor: '#faad14'
  },
  {
    key: 'stock', label: '库存预警', icon: Warning,
    value: raw.value.lowStockCount, suffix: ' 种',
    sub: '低于预警阈值',
    iconBg: '#fff2f0', iconColor: '#ff4d4f', valueColor: '#ff4d4f'
  },
  {
    key: 'mrevenue', label: '本月销售额', icon: Money,
    value: raw.value.monthRevenue, prefix: '¥',
    sub: '当月累计',
    iconBg: '#e6f4ff', iconColor: '#1677ff'
  },
  {
    key: 'refund', label: '本月退款', icon: Tickets,
    value: raw.value.monthRefundAmount, prefix: '¥',
    sub: '当月累计',
    iconBg: '#fff2f0', iconColor: '#ff4d4f', valueColor: '#ff4d4f'
  },
])

const todos = computed(() => [
  { key: 'orders', label: '待发货', value: raw.value.pendingOrders, path: '/admin/orders', color: '#1677ff' },
  { key: 'refunds', label: '待处理退款', value: raw.value.pendingRefunds, path: '/admin/after-sales', color: '#ff4d4f' },
  { key: 'purchase', label: '待审批采购', value: raw.value.pendingPurchase, path: '/admin/purchase-orders', color: '#faad14' },
  { key: 'stock', label: '库存预警商品', value: raw.value.lowStockCount, path: '/admin/inventory', color: '#52c41a' },
])

const formatNum = (n) => {
  const num = Number(n)
  if (isNaN(num)) return n
  return num.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

const initCharts = () => {
  const names = ['trend', 'product', 'category', 'user', 'coupon']
  const refs = [trendRef, productRef, categoryRef, userRef, couponRef]
  names.forEach((name, i) => {
    if (refs[i].value && !charts[name]) {
      charts[name] = echarts.init(refs[i].value)
    }
  })
}

const COLORS = ['#1677ff','#52c41a','#faad14','#ff4d4f','#722ed1','#13c2c2','#eb2f96','#fa8c16']

const renderTrend = (data) => {
  if (!charts.trend || !data?.salesTrend) return
  charts.trend.setOption({
    backgroundColor: '#fff',
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { data: ['销售额', '订单数'], top: 4 },
    grid: { left: 60, right: 60, bottom: 40, top: 44 },
    xAxis: { type: 'category', data: data.salesTrend.series || [], axisLabel: { rotate: 30 } },
    yAxis: [
      { type: 'value', name: '销售额(元)', axisLine: { lineStyle: { color: '#1677ff' } } },
      { type: 'value', name: '订单数', axisLine: { lineStyle: { color: '#52c41a' } } }
    ],
    series: [
      {
        name: '销售额', type: 'line', yAxisIndex: 0,
        data: data.salesTrend.revenue || [], smooth: true,
        areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(22,119,255,0.2)' }, { offset: 1, color: 'rgba(22,119,255,0)' }] } },
        itemStyle: { color: '#1677ff' }, lineStyle: { width: 2 }
      },
      {
        name: '订单数', type: 'bar', yAxisIndex: 1,
        data: data.salesTrend.orderCount || [],
        itemStyle: { color: 'rgba(82,196,26,0.6)', borderRadius: [3,3,0,0] }
      }
    ]
  })
}

const renderProduct = (data) => {
  if (!charts.product || !data?.topProducts) return
  const items = data.topProducts.slice(0, 10).reverse()
  charts.product.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 120, right: 16, top: 8, bottom: 24 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: items.map(p => p.productName || ''), axisLabel: { width: 110, overflow: 'truncate' } },
    series: [{
      type: 'bar', data: items.map(p => p.quantity || p.salesCount || 0),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#1677ff' }, { offset: 1, color: '#69b1ff' }]), borderRadius: [0,4,4,0] }
    }]
  })
}

const renderCategory = (data) => {
  if (!charts.category || !data?.topCategories) return
  charts.category.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { orient: 'vertical', right: 8, top: 'middle', textStyle: { fontSize: 11 } },
    series: [{
      type: 'pie', radius: ['40%', '68%'],
      center: ['40%', '50%'],
      data: data.topCategories.map((c, i) => ({ name: c.categoryName || '其他', value: c.revenue || 0, itemStyle: { color: COLORS[i % COLORS.length] } })),
      label: { show: false }, emphasis: { label: { show: true, fontSize: 13 } }
    }]
  })
}

const renderUser = (data) => {
  if (!charts.user) return
  const analysis = data?.userAnalysis || {}
  charts.user.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 24, bottom: 24 },
    xAxis: { type: 'category', data: ['新用户', '老用户', '总用户'] },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: [
        { value: analysis.newUsers || 0, itemStyle: { color: '#52c41a' } },
        { value: analysis.oldUsers || 0, itemStyle: { color: '#1677ff' } },
        { value: (analysis.newUsers || 0) + (analysis.oldUsers || 0), itemStyle: { color: '#faad14' } }
      ],
      itemStyle: { borderRadius: [4,4,0,0] }
    }]
  })
}

const renderCoupon = (data) => {
  if (!charts.coupon) return
  const a = data?.couponAnalysis || {}
  charts.coupon.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}张' },
    series: [{
      type: 'funnel', left: '15%', width: '70%', gap: 4,
      data: [
        { name: '已发放', value: a.issuedCount || 100, itemStyle: { color: '#1677ff' } },
        { name: '已领取', value: a.claimedCount || 80, itemStyle: { color: '#69b1ff' } },
        { name: '已使用', value: a.usedCount || 50, itemStyle: { color: '#52c41a' } },
      ],
      label: { position: 'inside', formatter: '{b}\n{c}张' }
    }]
  })
}

const loadData = async () => {
  try {
    const days = parseInt(trendPeriod.value)
    const res = await adminAPI.getDashboard({ days, topN: 10 })
    if (res.code === 200) {
      const d = res.data
      raw.value.todayRevenue = d.salesTrend?.currentRevenue || 0
      raw.value.todayOrderCount = d.salesTrend?.currentOrderCount || 0
      raw.value.todayGrowthRate = (d.salesTrend?.momGrowthRate || 0) * 100
      raw.value.monthRevenue = d.monthRevenue || 0
      raw.value.monthOrderCount = d.monthOrderCount || 0
      raw.value.monthRefundAmount = d.monthRefundAmount || 0
      raw.value.lowStockCount = d.lowStockCount || 0
      renderTrend(d)
      renderProduct(d)
      renderCategory(d)
      renderUser(d)
      renderCoupon(d)
    }
  } catch (e) { console.error(e) }

  // 待处理数量
  try {
    const [or, ar, pr] = await Promise.all([
      orderAPI.adminGetList({ status: 'PAID', pageNum: 1, pageSize: 1 }),
      afterSaleAPI.adminGetList({ status: 'PENDING', pageNum: 1, pageSize: 1 }),
      adminAPI.getPurchaseOrders({ status: 'PENDING', pageNum: 1, pageSize: 1 }).catch(() => ({ data: { total: 0 } }))
    ])
    raw.value.pendingOrders = or.data?.total || 0
    raw.value.pendingRefunds = ar.data?.total || 0
    raw.value.pendingPurchase = pr.data?.total || 0
  } catch {}
}

const onResize = () => Object.values(charts).forEach(c => c?.resize())

onMounted(() => {
  initCharts()
  loadData()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  Object.values(charts).forEach(c => c?.dispose())
})
</script>

<style scoped>
.dashboard { padding: 0; display: flex; flex-direction: column; gap: 16px; }

/* 指标网格 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
@media (max-width: 1200px) { .metrics-grid { grid-template-columns: repeat(2, 1fr); } }

.metric-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
  transition: box-shadow 0.2s;
}
.metric-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); }

.metric-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.metric-body { flex: 1; min-width: 0; }
.metric-label { font-size: 12px; color: #999; margin-bottom: 4px; }
.metric-value { font-size: 24px; font-weight: bold; color: #1a1a1a; line-height: 1.2; }
.metric-trend { font-size: 12px; margin-top: 4px; display: flex; align-items: center; gap: 4px; }
.metric-trend .up { color: #52c41a; }
.metric-trend .down { color: #ff4d4f; }
.trend-label { color: #bbb; }
.metric-sub { font-size: 12px; color: #bbb; margin-top: 4px; }

/* 待处理 */
.todo-bar {
  display: flex; gap: 12px;
  background: #fff; border-radius: 10px;
  padding: 14px 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}
.todo-item {
  flex: 1; display: flex; align-items: center; gap: 10px;
  padding: 10px 12px; border-radius: 8px; cursor: pointer;
  background: #fafafa; transition: background 0.2s;
}
.todo-item:hover { background: #f0f4ff; }
.todo-badge {
  min-width: 32px; height: 32px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 15px; font-weight: bold;
}
.todo-label { flex: 1; font-size: 13px; color: #555; }
.todo-arrow { color: #ccc; font-size: 14px; }

/* 图表区域 */
.chart-section { display: flex; gap: 16px; }
.chart-section.two-col .chart-card { flex: 1; }
.chart-card {
  flex: 1; background: #fff; border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
  overflow: hidden;
}
.chart-card.wide { flex: 1; }
.chart-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px 10px;
  border-bottom: 1px solid #f5f5f5;
}
.chart-title { font-size: 14px; font-weight: bold; color: #333; }
.chart-body { padding: 8px; }
</style>
