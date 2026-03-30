<template>
  <div class="page-container">
    <div class="page-header">
      <h2>站内消息</h2>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
        <el-button type="primary" plain @click="markAllRead">全部已读</el-button>
      </el-badge>
    </div>

    <el-card>
      <el-tabs v-model="activeTab" @tab-change="loadMessages">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="订单" name="order" />
        <el-tab-pane label="促销" name="promotion" />
        <el-tab-pane label="系统" name="system" />
        <el-tab-pane label="退款" name="refund" />
      </el-tabs>

      <div v-loading="loading">
        <div v-if="messages.length > 0">
          <div
            v-for="msg in messages"
            :key="msg.messageId"
            class="message-item"
            :class="{ 'message-unread': msg.isRead === 0 }"
            @click="handleRead(msg)"
          >
            <div class="message-icon">
              <el-icon size="20"><Bell /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-title">
                <span v-if="msg.isRead === 0" class="unread-dot" />
                {{ msg.title }}
              </div>
              <div class="message-body">{{ msg.content }}</div>
              <div class="message-time">{{ formatDate(msg.createTime) }}</div>
            </div>
            <div class="message-type">
              <el-tag size="small" :type="getTagType(msg.type)">{{ getTypeText(msg.type) }}</el-tag>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无消息" />

        <el-pagination
          v-if="total > 0"
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          class="mt-16"
          @current-change="loadMessages"
          @size-change="loadMessages"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { messageAPI } from '@/api'

const activeTab = ref('all')
const messages = ref([])
const unreadCount = ref(0)
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadUnreadCount()
  loadMessages()
})

const loadUnreadCount = async () => {
  try {
    const res = await messageAPI.getUnreadCount()
    unreadCount.value = res.data ?? 0
  } catch (error) {
    console.error(error)
  }
}

const loadMessages = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value,
      type: activeTab.value !== 'all' ? activeTab.value : undefined
    }
    const res = await messageAPI.getList(params)
    messages.value = res.data?.records || res.data || []
    total.value = res.data?.total || messages.value.length
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleRead = async (msg) => {
  if (msg.isRead === 0) {
    try {
      await messageAPI.markRead(msg.messageId)
      msg.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error(error)
    }
  }
}

const markAllRead = async () => {
  try {
    await messageAPI.markAllRead()
    ElMessage.success('已全部标记为已读')
    messages.value.forEach(m => m.isRead = 1)
    unreadCount.value = 0
  } catch (error) {
    console.error(error)
  }
}

const getTagType = (type) => {
  const map = { 'order': 'primary', 'promotion': 'danger', 'system': 'info', 'refund': 'success' }
  return map[type] || 'info'
}

const getTypeText = (type) => {
  const map = { 'order': '订单', 'promotion': '促销', 'system': '系统', 'refund': '退款' }
  return map[type] || '其他'
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
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
}
.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}
.message-item:last-child {
  border-bottom: none;
}
.message-item:hover {
  background: #f5f7fa;
  padding-left: 8px;
  padding-right: 8px;
  margin: 0 -8px;
}
.message-unread {
  background: #ecf5ff;
  border-radius: 8px;
  padding: 16px 12px;
  margin: 4px 0;
}
.message-unread:hover {
  background: #d9ecff;
}
.message-icon {
  color: #409eff;
  padding-top: 2px;
  flex-shrink: 0;
}
.message-unread .message-icon {
  color: #337ecc;
}
.message-content {
  flex: 1;
  min-width: 0;
}
.message-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}
.message-body {
  font-size: 13px;
  color: #606266;
  margin-top: 4px;
  line-height: 1.5;
}
.message-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 6px;
}
.unread-dot {
  width: 8px;
  height: 8px;
  background: #409eff;
  border-radius: 50%;
  flex-shrink: 0;
}
.message-type {
  flex-shrink: 0;
}
.mt-16 {
  margin-top: 16px;
}
</style>
