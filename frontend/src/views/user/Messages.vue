<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>站内消息</h2>
        <p>支持未读筛选、批量已读和分页浏览。</p>
      </div>
      <div class="header-actions">
        <el-select v-model="status" placeholder="全部消息" clearable @change="reload">
          <el-option label="全部消息" value="" />
          <el-option label="未读" value="unread" />
          <el-option label="已读" value="read" />
        </el-select>
        <el-button type="primary" plain @click="markAllRead">全部已读</el-button>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <div class="message-list">
        <div
          v-for="message in messages"
          :key="message.messageId"
          class="message-item"
          :class="{ unread: message.isRead === 0 }"
          @click="markRead(message)"
        >
          <div class="message-main">
            <div class="message-top">
              <span class="message-title">{{ message.title }}</span>
              <el-tag :type="typeTag(message.msgType)" size="small">{{ typeText(message.msgType) }}</el-tag>
            </div>
            <div class="message-content">{{ message.content }}</div>
          </div>
          <div class="message-side">
            <span class="message-time">{{ formatDateTime(message.createTime) }}</span>
            <span v-if="message.isRead === 0" class="unread-dot"></span>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && messages.length === 0" description="暂无消息" />

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadMessages"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { messageAPI } from '@/api'

const messages = ref([])
const status = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const typeText = (value) => {
  const map = {
    SYSTEM: '系统',
    ORDER: '订单',
    COUPON: '优惠券',
    AFTER_SALES: '售后'
  }
  return map[value] || value || '通知'
}

const typeTag = (value) => {
  const map = {
    SYSTEM: 'info',
    ORDER: 'success',
    COUPON: 'warning',
    AFTER_SALES: 'danger'
  }
  return map[value] || ''
}

const formatDateTime = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN')
}

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await messageAPI.getList({
      status: status.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    messages.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const reload = () => {
  pageNum.value = 1
  loadMessages()
}

const markRead = async (message) => {
  if (message.isRead === 1) return
  await messageAPI.markRead(message.messageId)
  message.isRead = 1
}

const markAllRead = async () => {
  await messageAPI.markAllRead()
  ElMessage.success('已全部标记为已读')
  loadMessages()
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 6px;
}

.page-header p {
  margin: 0;
  color: #909399;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.message-item:hover {
  border-color: #cbd5e1;
  background: #fafafa;
}

.message-item.unread {
  border-left: 4px solid #409eff;
  background: #f8fbff;
}

.message-main {
  flex: 1;
  min-width: 0;
}

.message-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.message-title {
  font-weight: 600;
  color: #303133;
}

.message-content {
  color: #606266;
  line-height: 1.6;
}

.message-side {
  min-width: 140px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
}

.message-time {
  color: #909399;
  font-size: 12px;
}

.unread-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #409eff;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
