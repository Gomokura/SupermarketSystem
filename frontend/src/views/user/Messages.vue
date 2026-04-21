<template>
  <div class="page-container">
    <h2>站内消息</h2>

    <el-card class="header-card">
      <div class="header-actions">
        <el-button type="primary" @click="markAllRead">全部已读</el-button>
        <el-select v-model="filterType" placeholder="消息类型" clearable @change="loadMessages">
          <el-option label="全部" value=""></el-option>
          <el-option label="订单消息" value="ORDER"></el-option>
          <el-option label="促销消息" value="PROMOTION"></el-option>
          <el-option label="系统消息" value="SYSTEM"></el-option>
          <el-option label="退款消息" value="REFUND"></el-option>
        </el-select>
      </div>
    </el-card>

    <div class="message-list">
      <el-card
        v-for="msg in messages"
        :key="msg.messageId"
        class="message-card"
        :class="{ unread: !msg.isRead }"
        @click="handleRead(msg)"
      >
        <div class="message-content">
          <div class="message-icon">
            <el-icon :size="24">
              <component :is="getMessageIcon(msg.type)" />
            </el-icon>
          </div>
          <div class="message-body">
            <div class="message-header">
              <span class="message-title">{{ msg.title }}</span>
              <el-tag size="small" :type="getTypeTag(msg.type)">{{ getTypeText(msg.type) }}</el-tag>
              <span v-if="!msg.isRead" class="unread-dot"></span>
            </div>
            <div class="message-text">{{ msg.content }}</div>
            <div class="message-time">{{ formatDateTime(msg.createTime) }}</div>
          </div>
        </div>
      </el-card>

      <el-empty v-if="messages.length === 0" description="暂无消息"></el-empty>
    </div>

    <el-pagination
      v-if="total > 0"
      class="pagination"
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { messageAPI } from '@/api'

const messages = ref([])
const filterType = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getMessageIcon = (type) => {
  const iconMap = {
    'ORDER': 'Message',
    'PROMOTION': 'Gift',
    'SYSTEM': 'Bell',
    'REFUND': 'Money'
  }
  return iconMap[type] || 'Message'
}

const getTypeTag = (type) => {
  const map = {
    'ORDER': '',
    'PROMOTION': 'warning',
    'SYSTEM': 'info',
    'REFUND': 'success'
  }
  return map[type] || ''
}

const getTypeText = (type) => {
  const map = {
    'ORDER': '订单',
    'PROMOTION': '促销',
    'SYSTEM': '系统',
    'REFUND': '退款'
  }
  return map[type] || '其他'
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const loadMessages = async () => {
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterType.value) {
      params.type = filterType.value
    }
    const res = await messageAPI.getList(params)
    messages.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error(error)
  }
}

const handleRead = async (msg) => {
  if (msg.isRead) return
  try {
    await messageAPI.markRead(msg.messageId)
    msg.isRead = true
  } catch (error) {
    console.error(error)
  }
}

const markAllRead = async () => {
  try {
    await ElMessageBox.confirm('确定将所有消息标记为已读？', '提示', { type: 'info' })
    await messageAPI.markAllRead()
    ElMessage.success('操作成功')
    loadMessages()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
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

.header-card {
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-card {
  cursor: pointer;
  transition: all 0.3s;
}

.message-card:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.message-card.unread {
  background-color: #f0f9ff;
  border-left: 3px solid #409eff;
}

.message-content {
  display: flex;
  gap: 16px;
}

.message-icon {
  width: 40px;
  height: 40px;
  background: #f0f2f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  flex-shrink: 0;
}

.message-body {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.message-title {
  font-weight: 500;
  color: #333;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #409eff;
  border-radius: 50%;
  margin-left: auto;
}

.message-text {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
  line-height: 1.5;
}

.message-time {
  color: #999;
  font-size: 12px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
