<template>
  <div class="page-container">
    <h2>财务报表</h2>

    <el-card style="margin-bottom: 20px">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-label">总营收</div>
            <div class="stat-value primary">￥{{ formatNum(data.totalRevenue) }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-label">总订单数</div>
            <div class="stat-value">{{ data.orderCount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-label">客单价</div>
            <div class="stat-value">￥{{ formatNum(data.avgOrderAmount) }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-label">支付方式数</div>
            <div class="stat-value">{{ Object.keys(data.payMethodSummary || {}).length }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header><span>支付方式分布</span></template>
          <el-table :data="payMethodRows" border>
            <el-table-column prop="method" label="支付方式" />
            <el-table-column prop="amount" label="金额">
              <template #default="{ row }">￥{{ formatNum(row.amount) }}</template>
            </el-table-column>
            <el-table-column prop="ratio" label="占比" width="100">
              <template #default="{ row }">
                <el-progress :percentage="row.ratio" :stroke-width="6" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>营收汇总</span></template>
          <div class="summary-item" v-for="item in summaryItems" :key="item.label">
            <span class="s-label">{{ item.label }}</span>
            <span class="s-value" :class="item.cls">{{ item.value }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminAPI } from '@/api'

const data = ref({})

onMounted(loadFinance)

async function loadFinance() {
  try {
    const res = await adminAPI.getFinanceData()
    data.value = res.data || {}
  } catch (e) {
    console.error(e)
  }
}

const payMethodRows = computed(() => {
  const map = data.value.payMethodSummary || {}
  const total = Object.values(map).reduce((s, v) => s + v, 0)
  return Object.entries(map).map(([method, amount]) => ({
    method: methodLabel(method),
    amount,
    ratio: total > 0 ? Math.round(amount / total * 100) : 0
  }))
})

const summaryItems = computed(() => [
  { label: '总营收', value: '￥' + formatNum(data.value.totalRevenue), cls: 'green' },
  { label: '订单总量', value: data.value.orderCount || 0, cls: '' },
  { label: '平均客单价', value: '￥' + formatNum(data.value.avgOrderAmount), cls: '' }
])

function methodLabel(m) {
  return { WECHAT: '微信支付', ALIPAY: '支付宝', CASH: '现金', CARD: '银行卡' }[m] || m
}
function formatNum(n) {
  return (n || 0).toFixed(2)
}
</script>

<style scoped>
.stat-card { text-align: center; padding: 20px 0; }
.stat-label { color: #909399; font-size: 14px; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-value.primary { color: #409eff; }
.summary-item { display: flex; justify-content: space-between; padding: 14px 0; border-bottom: 1px solid #eee; font-size: 15px; }
.summary-item:last-child { border-bottom: none; }
.s-label { color: #606266; }
.s-value { font-weight: bold; }
.s-value.green { color: #67c23a; }
</style>
