<template>
  <div class="page-container">
    <h2>审计日志</h2>

    <el-card>
      <el-form :inline="true" class="filter-form">
        <el-form-item label="模块">
          <el-select v-model="filterModule" placeholder="全部模块" clearable @change="loadLogs">
            <el-option label="用户管理" value="USER"></el-option>
            <el-option label="订单管理" value="ORDER"></el-option>
            <el-option label="商品管理" value="PRODUCT"></el-option>
            <el-option label="库存管理" value="INVENTORY"></el-option>
            <el-option label="优惠券" value="COUPON"></el-option>
            <el-option label="促销活动" value="PROMOTION"></el-option>
            <el-option label="售后管理" value="AFTER_SALE"></el-option>
            <el-option label="系统设置" value="SYSTEM"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="loadLogs"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadLogs">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logs" border v-loading="loading" @row-click="handleRowExpand" :expand-row-keys="expandedRows" row-key="logId">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="log-detail">
              <h4>操作详情</h4>
              <div class="detail-content">
                <div class="detail-row">
                  <span class="detail-label">操作人：</span>
                  <span>{{ row.operator }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">操作类型：</span>
                  <span>{{ row.action }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">操作对象：</span>
                  <span>{{ row.target }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">IP地址：</span>
                  <span>{{ row.ipAddress }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">操作时间：</span>
                  <span>{{ formatDateTime(row.createTime) }}</span>
                </div>
                <div v-if="row.beforeData || row.afterData" class="detail-row">
                  <span class="detail-label">操作前数据：</span>
                  <pre class="data-block">{{ formatJson(row.beforeData) }}</pre>
                </div>
                <div v-if="row.afterData" class="detail-row">
                  <span class="detail-label">操作后数据：</span>
                  <pre class="data-block">{{ formatJson(row.afterData) }}</pre>
                </div>
                <div v-if="row.description" class="detail-row">
                  <span class="detail-label">操作描述：</span>
                  <span>{{ row.description }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="action" label="操作类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="getActionTag(row.action)">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="操作对象" min-width="150" show-overflow-tooltip />
        <el-table-column prop="module" label="模块" width="100">
          <template #default="{ row }">
            {{ getModuleText(row.module) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
      </el-table>

      <el-pagination
        v-if="total > 0"
        class="pagination"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminAPI } from '@/api'

const logs = ref([])
const loading = ref(false)
const filterModule = ref('')
const dateRange = ref([])
const currentPage = ref(1)
const pageSize = ref(15)
const total = ref(0)
const expandedRows = ref([])

const getActionTag = (action) => {
  if (action.includes('CREATE') || action.includes('ADD')) return 'success'
  if (action.includes('UPDATE') || action.includes('EDIT')) return 'warning'
  if (action.includes('DELETE') || action.includes('REMOVE')) return 'danger'
  return 'info'
}

const getModuleText = (module) => {
  const map = {
    'USER': '用户',
    'ORDER': '订单',
    'PRODUCT': '商品',
    'INVENTORY': '库存',
    'COUPON': '优惠券',
    'PROMOTION': '促销',
    'AFTER_SALE': '售后',
    'SYSTEM': '系统'
  }
  return map[module] || module || '-'
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const formatJson = (data) => {
  if (!data) return '-'
  if (typeof data === 'string') {
    try {
      return JSON.stringify(JSON.parse(data), null, 2)
    } catch {
      return data
    }
  }
  return JSON.stringify(data, null, 2)
}

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterModule.value) {
      params.module = filterModule.value
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await adminAPI.getAuditLogs(params)
    logs.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  filterModule.value = ''
  dateRange.value = []
  loadLogs()
}

const handleRowExpand = (row) => {
  const index = expandedRows.value.indexOf(row.logId)
  if (index > -1) {
    expandedRows.value.splice(index, 1)
  } else {
    expandedRows.value.push(row.logId)
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  expandedRows.value = []
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

.filter-form {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.log-detail {
  padding: 16px;
  background: #f5f7fa;
  margin: 8px 16px;
  border-radius: 4px;
}

.log-detail h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #303133;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  font-size: 13px;
  line-height: 1.6;
}

.detail-label {
  color: #909399;
  min-width: 90px;
  flex-shrink: 0;
}

.data-block {
  background: #fff;
  padding: 8px 12px;
  border-radius: 4px;
  margin: 4px 0;
  font-size: 12px;
  max-width: 600px;
  overflow-x: auto;
}
</style>
