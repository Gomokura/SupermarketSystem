<template>
  <div class="page">
    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" class="tabs">
      <el-tab-pane label="待配送" name="pending">
        <div v-if="pendingTasks.length === 0 && !loading" class="empty">
          <el-empty description="暂无待配送任务" />
        </div>
        <div v-else class="card-list">
          <div v-for="task in pendingTasks" :key="task.taskId" class="task-card" @click="toggleExpand(task)">
            <div class="card-header">
              <span class="order-no">订单号：{{ task.orderNo }}</span>
              <el-tag :type="statusTag(task.status)" size="small">{{ statusText(task.status) }}</el-tag>
            </div>
            <div class="card-body">
              <div class="info-row"><el-icon><Location /></el-icon>{{ task.address || '—' }}</div>
            </div>
            <div class="expand-area" v-show="expandedId === task.taskId">
              <el-divider style="margin: 10px 0" />
              <div class="detail-row"><span class="label">收件人</span><span>{{ task.receiverName || '—' }}</span></div>
              <div class="detail-row">
                <span class="label">手机</span>
                <span>{{ maskPhone(task.receiverPhone) }}
                  <el-button link type="primary" size="small" @click.stop="copyPhone(task.receiverPhone)">复制</el-button>
                </span>
              </div>
              <div class="detail-row"><span class="label">详细地址</span><span>{{ task.address || '—' }}</span></div>
              <div class="detail-row"><span class="label">备注</span><span>{{ task.failReason || '无' }}</span></div>
              <div class="action-btns">
                <el-button v-if="task.status === 'ASSIGNED'" type="primary" size="small" @click.stop="pickup(task)">已取件</el-button>
                <el-button v-if="task.status === 'PICKED_UP'" type="success" size="small" @click.stop="complete(task)">已送达</el-button>
                <el-button v-if="task.status === 'PICKED_UP'" type="danger" size="small" @click.stop="showFail(task)">配送失败</el-button>
              </div>
            </div>
            <div class="expand-hint" @click.stop="toggleExpand(task)">
              <el-icon><ArrowDown v-if="expandedId !== task.taskId" /><ArrowUp v-else /></el-icon>
              {{ expandedId === task.taskId ? '收起' : '查看详情' }}
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="历史记录" name="history">
        <div v-if="historyTasks.length === 0 && !loadingHistory" class="empty">
          <el-empty description="暂无历史记录" />
        </div>
        <div v-else class="card-list">
          <div v-for="task in historyTasks" :key="task.taskId" class="task-card">
            <div class="card-header">
              <span class="order-no">订单号：{{ task.orderNo }}</span>
              <el-tag :type="statusTag(task.status)" size="small">{{ statusText(task.status) }}</el-tag>
            </div>
            <div class="card-body">
              <div class="info-row"><el-icon><Location /></el-icon>{{ task.address || '—' }}</div>
              <div class="info-row" v-if="task.failReason"><el-icon><Warning /></el-icon>失败原因：{{ task.failReason }}</div>
              <div class="info-row" v-if="task.deliverTime"><el-icon><Timer /></el-icon>送达时间：{{ formatTime(task.deliverTime) }}</div>
            </div>
          </div>
        </div>
        <el-pagination
          v-if="historyTotal > 0"
          v-model:current-page="historyPage"
          :page-size="10"
          :total="historyTotal"
          layout="prev, pager, next"
          @current-change="loadHistory"
          class="pagination"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 配送失败弹窗 -->
    <el-dialog v-model="failDialogVisible" title="配送失败" width="320px">
      <el-form label-width="80px">
        <el-form-item label="失败原因" required>
          <el-input v-model="failReason" type="textarea" :rows="3" placeholder="请输入配送失败原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="failDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="actionLoading" @click="submitFail">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, ArrowDown, ArrowUp, Warning, Timer } from '@element-plus/icons-vue'
import { courierAPI } from '@/api'

const activeTab = ref('pending')
const pendingTasks = ref([])
const historyTasks = ref([])
const historyPage = ref(1)
const historyTotal = ref(0)
const loading = ref(false)
const loadingHistory = ref(false)
const expandedId = ref(null)
const failDialogVisible = ref(false)
const actionLoading = ref(false)
const currentFailTask = ref(null)
const failReason = ref('')

// 加载待配送任务
const loadPending = async () => {
  loading.value = true
  try {
    const res = await courierAPI.getTasks()
    pendingTasks.value = res.data || []
  } catch (e) {
    // interceptor handles
  } finally {
    loading.value = false
  }
}

// 加载历史记录
const loadHistory = async () => {
  loadingHistory.value = true
  try {
    const res = await courierAPI.getHistory()
    historyTasks.value = res.data || []
    historyTotal.value = historyTasks.value.length
  } catch (e) {
    // interceptor handles
  } finally {
    loadingHistory.value = false
  }
}

onMounted(() => {
  loadPending()
  loadHistory()
})

// 展开/收起
const toggleExpand = (task) => {
  expandedId.value = expandedId.value === task.taskId ? null : task.taskId
}

// 手机号脱敏
const maskPhone = (phone) => {
  if (!phone) return '—'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 复制手机号
const copyPhone = (phone) => {
  navigator.clipboard.writeText(phone).then(() => ElMessage.success('已复制到剪贴板'))
}

// 已取件
const pickup = async (task) => {
  try {
    await ElMessageBox.confirm('确认已取件？', '提示')
    actionLoading.value = true
    await courierAPI.pickup(task.taskId)
    ElMessage.success('已取件')
    loadPending()
    loadHistory()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

// 已送达
const complete = async (task) => {
  try {
    await ElMessageBox.confirm('确认已送达？', '提示')
    actionLoading.value = true
    await courierAPI.complete(task.taskId)
    ElMessage.success('已送达')
    loadPending()
    loadHistory()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

// 配送失败
const showFail = (task) => {
  currentFailTask.value = task
  failReason.value = ''
  failDialogVisible.value = true
}

const submitFail = async () => {
  if (!failReason.value.trim()) {
    ElMessage.warning('请填写失败原因')
    return
  }
  actionLoading.value = true
  try {
    await courierAPI.fail(currentFailTask.value.taskId, failReason.value)
    ElMessage.success('已提交')
    failDialogVisible.value = false
    loadPending()
    loadHistory()
  } catch (e) {
    // interceptor handles
  } finally {
    actionLoading.value = false
  }
}

// 状态标签
const statusText = (s) => {
  const map = { ASSIGNED: '待取件', PICKED_UP: '配送中', DELIVERED: '已送达', FAILED: '配送失败' }
  return map[s] || s
}
const statusTag = (s) => {
  const map = { ASSIGNED: 'warning', PICKED_UP: 'primary', DELIVERED: 'success', FAILED: 'danger' }
  return map[s] || 'info'
}

// 格式化时间
const formatTime = (t) => {
  if (!t) return '—'
  return new Date(t).toLocaleString('zh-CN')
}
</script>

<style scoped>
.page { height: 100%; display: flex; flex-direction: column; }
.tabs { height: 100%; }
.tabs :deep(.el-tabs__content) { height: calc(100vh - 120px); overflow-y: auto; padding: 12px 12px 0; }
.empty { padding: 40px 0; }
.card-list { display: flex; flex-direction: column; gap: 10px; }
.task-card {
  background: #fff; border-radius: 8px; padding: 12px 14px;
  box-shadow: 0 1px 4px rgba(0,0,0,.08); cursor: pointer;
  transition: box-shadow .2s;
}
.task-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,.13); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.order-no { font-size: 13px; color: #666; }
.card-body { display: flex; flex-direction: column; gap: 4px; }
.info-row { display: flex; align-items: center; gap: 4px; font-size: 14px; color: #333; }
.expand-area { margin-top: 10px; }
.detail-row { display: flex; align-items: flex-start; gap: 8px; font-size: 13px; margin-bottom: 6px; }
.label { color: #999; flex-shrink: 0; width: 60px; }
.action-btns { display: flex; gap: 8px; margin-top: 10px; }
.expand-hint { text-align: center; color: #67c23a; font-size: 12px; margin-top: 8px; }
.pagination { justify-content: center; margin-top: 16px; }
</style>
