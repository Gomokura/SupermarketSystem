<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>审计日志</h2>
        <p>按模块和日期筛选后台操作记录，可展开查看快照内容。</p>
      </div>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="moduleValue" placeholder="全部模块" clearable @change="reload">
          <el-option label="用户" value="USER" />
          <el-option label="订单" value="ORDER" />
          <el-option label="商品" value="PRODUCT" />
          <el-option label="库存" value="INVENTORY" />
          <el-option label="优惠券" value="COUPON" />
          <el-option label="促销" value="PROMOTION" />
          <el-option label="售后" value="AFTER_SALE" />
          <el-option label="系统" value="SYSTEM" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="reload"
        />
      </div>

      <el-table :data="logs" border v-loading="loading" row-key="logId">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="detail-grid">
              <div><strong>操作人：</strong>{{ row.operatorName || row.operatorId || '-' }}</div>
              <div><strong>模块：</strong>{{ moduleText(row.module) }}</div>
              <div><strong>动作：</strong>{{ row.action || '-' }}</div>
              <div><strong>目标 ID：</strong>{{ row.targetId || '-' }}</div>
              <div><strong>IP：</strong>{{ row.ipAddress || '-' }}</div>
              <div><strong>时间：</strong>{{ formatDateTime(row.createTime) }}</div>
              <div class="snapshot">
                <strong>变更前</strong>
                <pre>{{ prettyJson(row.beforeData) }}</pre>
              </div>
              <div class="snapshot">
                <strong>变更后</strong>
                <pre>{{ prettyJson(row.afterData) }}</pre>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="logId" label="ID" width="80" />
        <el-table-column label="操作人" width="120">
          <template #default="{ row }">
            {{ row.operatorName || row.operatorId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="模块" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ moduleText(row.module) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="动作" width="120" />
        <el-table-column prop="targetId" label="目标 ID" width="100" />
        <el-table-column prop="ipAddress" label="IP" width="150" />
        <el-table-column label="时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadLogs"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminAPI } from '@/api'

const logs = ref([])
const loading = ref(false)
const moduleValue = ref('')
const dateRange = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const moduleText = (value) => {
  const map = {
    USER: '用户',
    ORDER: '订单',
    PRODUCT: '商品',
    INVENTORY: '库存',
    COUPON: '优惠券',
    PROMOTION: '促销',
    AFTER_SALE: '售后',
    SYSTEM: '系统'
  }
  return map[value] || value || '-'
}

const formatDateTime = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN')
}

const prettyJson = (value) => {
  if (!value) return '-'
  if (typeof value === 'string') {
    try {
      return JSON.stringify(JSON.parse(value), null, 2)
    } catch {
      return value
    }
  }
  return JSON.stringify(value, null, 2)
}

const loadLogs = async () => {
  loading.value = true
  try {
    const res = await adminAPI.getAuditLogs({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      module: moduleValue.value || undefined,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
    })
    logs.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const reload = () => {
  pageNum.value = 1
  loadLogs()
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
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
  gap: 12px;
  flex-wrap: wrap;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 20px;
  padding: 8px 4px;
}

.snapshot {
  grid-column: 1 / -1;
}

.snapshot pre {
  margin: 8px 0 0;
  background: #0f172a;
  color: #e2e8f0;
  padding: 12px;
  border-radius: 6px;
  overflow: auto;
  font-size: 12px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
