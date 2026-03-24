<template>
  <div class="dashboard">
    <h2 class="page-title">数据概览</h2>

    <!-- 数字卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #ecf5ff">
            <el-icon size="28" color="#409eff"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.userCount ?? '-' }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #f0f9eb">
            <el-icon size="28" color="#67c23a"><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.productCount ?? '-' }}</div>
            <div class="stat-label">商品总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #fdf6ec">
            <el-icon size="28" color="#e6a23c"><List /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.orderCount ?? '-' }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #fef0f0">
            <el-icon size="28" color="#f56c6c"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥ {{ stats.revenue ?? '0' }}</div>
            <div class="stat-label">累计营收</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 折线图：近7天订单趋势 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">近7天订单趋势</span>
          </template>
          <div ref="lineChartRef" class="chart-box"></div>
        </el-card>
      </el-col>

      <!-- 饼图：订单状态分布 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">订单状态分布</span>
          </template>
          <div ref="pieChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 柱状图：商品销量 Top5 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">商品销量 Top5</span>
          </template>
          <div ref="barChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { adminAPI } from '@/api'

const stats       = ref({})
const lineChartRef = ref(null)
const pieChartRef  = ref(null)
const barChartRef  = ref(null)

let lineChart = null
let pieChart  = null
let barChart  = null

// ── 加载统计数字 ──────────────────────────────────────────
const loadStatistics = async () => {
  try {
    const res = await adminAPI.getStatistics()
    stats.value = res.data || {}
  } catch (e) {
    console.error('统计接口失败', e)
  }
}

// ── 折线图：近7天订单趋势 ─────────────────────────────────
const initLineChart = () => {
  lineChart = echarts.init(lineChartRef.value)

  // 生成近7天日期标签
  const days = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    days.push(`${d.getMonth() + 1}/${d.getDate()}`)
  }

  // 模拟数据（对接真实接口时替换 data 数组）
  const mockData = [12, 19, 8, 25, 17, 30, 22]

  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: days,
      axisLine: { lineStyle: { color: '#ddd' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [{
      name: '订单数',
      type: 'line',
      data: mockData,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#409eff', width: 2 },
      itemStyle: { color: '#409eff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.3)' },
          { offset: 1, color: 'rgba(64,158,255,0.02)' }
        ])
      }
    }]
  })
}

// ── 饼图：订单状态分布 ────────────────────────────────────
const initPieChart = () => {
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, itemWidth: 10, itemHeight: 10 },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      data: [
        { name: '待支付', value: 18,  itemStyle: { color: '#faad14' } },
        { name: '待发货', value: 35,  itemStyle: { color: '#1890ff' } },
        { name: '已发货', value: 27,  itemStyle: { color: '#13c2c2' } },
        { name: '已完成', value: 110, itemStyle: { color: '#52c41a' } },
        { name: '已取消', value: 9,   itemStyle: { color: '#ff4d4f' } }
      ],
      label: { show: false },
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' }
      }
    }]
  })
}

// ── 柱状图：商品销量 Top5 ─────────────────────────────────
const initBarChart = () => {
  barChart = echarts.init(barChartRef.value)
  barChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 120, right: 40, top: 20, bottom: 30 },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
    yAxis: {
      type: 'category',
      data: ['矿泉水 500ml', '牛奶 250ml', '面包', '鸡蛋（10枚）', '苹果 1kg'],
      axisLine: { show: false },
      axisTick: { show: false }
    },
    series: [{
      name: '销量',
      type: 'bar',
      data: [320, 278, 245, 198, 167],
      barMaxWidth: 28,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#409eff' },
          { offset: 1, color: '#79bbff' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      label: { show: true, position: 'right', color: '#666' }
    }]
  })
}

// ── 窗口 resize 自适应 ────────────────────────────────────
const handleResize = () => {
  lineChart?.resize()
  pieChart?.resize()
  barChart?.resize()
}

onMounted(async () => {
  await loadStatistics()
  await nextTick()
  initLineChart()
  initPieChart()
  initBarChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 4px 0;
}

.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}

/* 数字卡片 */
.stat-row {
  margin-bottom: 20px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

/* 图表 */
.chart-row {
  margin-bottom: 20px;
}

.chart-box {
  height: 280px;
}

.card-title {
  font-weight: 600;
  color: #303133;
}
</style>
