<template>
  <div class="page-container">
    <div class="page-header">
      <h2>审计日志</h2>
    </div>

    <el-card class="mt-16">
      <div class="filter-bar">
        <el-select v-model="filterModule" placeholder="全部模块" clearable style="width: 160px" @change="loadLogs">
          <el-option label="全部模块" value="" />
          <el-option label="用户认证" value="auth" />
          <el-option label="商品管理" value="product" />
          <el-option label="订单管理" value="order" />
          <el-option label="库存管理" value="inventory" />
          <el-option label="优惠券" value="coupon" />
          <el-option label="促销管理" value="promotion" />
          <el-option label="供应商" value="supplier" />
          <el-option label="配送管理" value="delivery" />
          <el-option label="系统设置" value="system" />
        </el-select>

        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 240px"
          @change="loadLogs"
        />

        <el-button type="primary" @click="loadLogs">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table :data="logs" border v-loading="loading" class="mt-16" :expand-row-keys="expandedRows" row-key="logId">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-content">
              <div class="expand-section">
                <div class="expand-label">操作人</div>
                <div class="expand-value">{{ row.operator || '-' }}</div>
              </div>
              <div class="expand-section">
                <div class="expand-label">IP地址</div>
                <div class="expand-value">{{ row.ip || '-' }}</div>
              </div>
              <div v-if="row.beforeData" class="expand-section full-width">
                <div class="expand-label">操作前数据</div>
                <pre class="data-pre">{{ JSON.stringify(JSON.parse(row.beforeData), null, 2) }}</pre>
              </div>
              <div v-if="row.afterData" class="expand-section full-width">
                <div class="expand-label">操作后数据</div>
                <pre class="data-pre">{{ JSON.stringify(JSON.parse(row.afterData), null, 2) }}</pre>
              </div>
              <div v-if="!row.beforeData && !row.afterData" class="expand-section full-width">
                <div class="expand-value text-muted">无数据快照</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column label="操作类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="getActionTagType(row.action)">{{ row.action || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="模块" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ getModuleText(row.module) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作时间" width="170">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="toggleExpand(row)">
              {{ expandedRows.includes(row.logId) ? '收起' : '详情' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="mt-16"
        @current-change="loadLogs"
        @size-change="loadLogs"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminAPI } from '@/api'

const logs = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const filterModule = ref('')
const dateRange = ref([])
const expandedRows = ref([])

onMounted(() => {
  loadLogs()
})

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value,
      module: filterModule.value || undefined,
      startDate: dateRange.value?.[0] || undefined,
      endDate: dateRange.value?.[1] || undefined
    }
    const res = await adminAPI.getAuditLogs(params)
    logs.value = res.data?.records || res.data || []
    total.value = res.data?.total || logs.value.length
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filterModule.value = ''
  dateRange.value = []
  page.value = 1
  loadLogs()
}

const toggleExpand = (row) => {
  const idx = expandedRows.value.indexOf(row.logId)
  if (idx > -1) {
    expandedRows.value.splice(idx, 1)
  } else {
    expandedRows.value.push(row.logId)
  }
}

const getModuleText = (module) => {
  const map = {
    'auth': '用户认证',
    'product': '商品管理',
    'order': '订单管理',
    'inventory': '库存管理',
    'coupon': '优惠券',
    'promotion': '促销管理',
    'supplier': '供应商',
    'delivery': '配送管理',
    'system': '系统设置'
  }
  return map[module] || module || '-'
}

const getActionTagType = (action) => {
  const map = {
    'CREATE': 'success',
    'UPDATE': 'warning',
    'DELETE': 'danger',
    'LOGIN': 'info',
    'LOGOUT': 'info'
  }
  return map[action] || 'info'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-header h2 {
  margin: 0;
}
.mt-16 {
  margin-top: 16px;
}
.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
.expand-content {
  padding: 12px 20px;
  background: #f5f7fa;
  border-radius: 4px;
}
.expand-section {
  display: inline-flex;
  align-items: flex-start;
  gap: 12px;
  margin-right: 32px;
  margin-bottom: 8px;
  min-width: 200px;
}
.expand-section.full-width {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: unset;
  width: 100%;
}
.expand-label {
  color: #909399;
  font-size: 13px;
  white-space: nowrap;
  padding-top: 2px;
  min-width: 70px;
}
.expand-value {
  color: #303133;
  font-size: 13px;
  word-break: break-all;
}
.data-pre {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px 14px;
  margin: 4px 0 0 0;
  font-size: 12px;
  color: #606266;
  overflow-x: auto;
  max-height: 200px;
  font-family: 'Courier New', monospace;
}
.text-muted {
  color: #c0c4cc;
}
</style>
