<template>
  <div class="tasks-page">
    <section class="overview">
      <div class="metric metric-waiting">
        <span class="label">待取件</span>
        <strong>{{ assignedCount }}</strong>
      </div>
      <div class="metric metric-running">
        <span class="label">配送中</span>
        <strong>{{ pickedCount }}</strong>
      </div>
      <div class="metric metric-done">
        <span class="label">今日完成</span>
        <strong>{{ todayDoneCount }}</strong>
      </div>
      <div class="metric metric-risk">
        <span class="label">异常记录</span>
        <strong>{{ failedCount }}</strong>
      </div>
    </section>

    <section class="toolbar">
      <div class="filters">
        <button
          v-for="item in filters"
          :key="item.value"
          class="filter-pill"
          :class="{ active: activeFilter === item.value }"
          @click="activeFilter = item.value"
        >
          {{ item.label }}
        </button>
      </div>
      <el-button :icon="Refresh" :loading="loading || loadingHistory" @click="refreshAll">刷新</el-button>
    </section>

    <section v-if="activeFilter !== 'history'" class="board">
      <div class="column">
        <div class="column-head">
          <span>待取件</span>
          <em>{{ assignedTasks.length }}</em>
        </div>
        <el-empty v-if="visibleAssigned.length === 0 && !loading" description="暂无待取件任务" />
        <task-card
          v-for="task in visibleAssigned"
          :key="task.taskId"
          :task="task"
          @pickup="pickup"
        />
      </div>

      <div class="column">
        <div class="column-head">
          <span>配送中</span>
          <em>{{ pickedTasks.length }}</em>
        </div>
        <el-empty v-if="visiblePicked.length === 0 && !loading" description="暂无配送中任务" />
        <task-card
          v-for="task in visiblePicked"
          :key="task.taskId"
          :task="task"
          @complete="complete"
          @fail="showFail"
        />
      </div>
    </section>

    <section v-else class="history">
      <el-empty v-if="historyTasks.length === 0 && !loadingHistory" description="暂无历史记录" />
      <div v-else class="history-list">
        <div v-for="task in historyTasks" :key="task.taskId" class="history-row">
          <div>
            <div class="order-no">{{ task.orderNo }}</div>
            <div class="address">{{ task.receiverName || '-' }} · {{ task.address || '-' }}</div>
          </div>
          <div class="history-right">
            <el-tag :type="statusTag(task.status)" size="small">{{ statusText(task.status) }}</el-tag>
            <span class="time">{{ formatTime(task.deliverTime || task.pickupTime || task.assignTime) }}</span>
          </div>
        </div>
      </div>
    </section>

    <el-dialog v-model="failDialogVisible" title="标记配送异常" width="360px">
      <el-form label-width="88px">
        <el-form-item label="异常原因" required>
          <el-input v-model="failReason" type="textarea" :rows="3" placeholder="例如：客户电话无人接听，约明日再送" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="failDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="actionLoading" @click="submitFail">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, inject, onMounted, ref } from 'vue'
import { ElButton, ElMessage, ElMessageBox, ElTag } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { courierAPI } from '@/api'

const activeFilter = ref('active')
const filters = [
  { label: '进行中', value: 'active' },
  { label: '只看待取件', value: 'assigned' },
  { label: '只看配送中', value: 'picked' },
  { label: '历史记录', value: 'history' }
]

const activeTasks = ref([])
const historyTasks = ref([])
const loading = ref(false)
const loadingHistory = ref(false)
const failDialogVisible = ref(false)
const actionLoading = ref(false)
const currentFailTask = ref(null)
const failReason = ref('')
const reloadCourierProfile = inject('reloadCourierProfile', null)

const assignedTasks = computed(() => activeTasks.value.filter(task => task.status === 'ASSIGNED'))
const pickedTasks = computed(() => activeTasks.value.filter(task => task.status === 'PICKED_UP'))
const assignedCount = computed(() => assignedTasks.value.length)
const pickedCount = computed(() => pickedTasks.value.length)
const failedCount = computed(() => historyTasks.value.filter(task => task.status === 'FAILED').length)
const todayDoneCount = computed(() => {
  const today = new Date().toDateString()
  return historyTasks.value.filter(task => task.status === 'DELIVERED' && task.deliverTime && new Date(task.deliverTime).toDateString() === today).length
})
const visibleAssigned = computed(() => activeFilter.value === 'picked' ? [] : assignedTasks.value)
const visiblePicked = computed(() => activeFilter.value === 'assigned' ? [] : pickedTasks.value)

const loadActive = async () => {
  loading.value = true
  try {
    const res = await courierAPI.getTasks()
    activeTasks.value = (res.data || []).map(normalizeTask)
  } catch (e) {
    // request interceptor shows the message
  } finally {
    loading.value = false
  }
}

const loadHistory = async () => {
  loadingHistory.value = true
  try {
    const res = await courierAPI.getHistory()
    historyTasks.value = (res.data || []).map(normalizeTask)
  } catch (e) {
    // request interceptor shows the message
  } finally {
    loadingHistory.value = false
  }
}

const refreshAll = async () => {
  await Promise.all([loadActive(), loadHistory()])
  reloadCourierProfile?.()
}

const normalizeTask = (task) => {
  if (task.receiverName && task.receiverPhone && task.address) return task
  const snapshot = task.address || ''
  const parts = snapshot.trim().split(/\s+/)
  return {
    ...task,
    receiverName: task.receiverName || parts[0] || '',
    receiverPhone: task.receiverPhone || parts[1] || '',
    address: task.address && parts.length > 2 ? parts.slice(2).join(' ') : task.address
  }
}

const pickup = async (task) => {
  try {
    await ElMessageBox.confirm(`确认已取到订单 ${task.orderNo}？`, '取件确认')
    actionLoading.value = true
    await courierAPI.pickup(task.taskId)
    ElMessage.success('已进入配送中')
    await refreshAll()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

const complete = async (task) => {
  try {
    await ElMessageBox.confirm(`确认订单 ${task.orderNo} 已送达？`, '送达确认')
    actionLoading.value = true
    await courierAPI.complete(task.taskId)
    ElMessage.success('配送已完成')
    await refreshAll()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

const showFail = (task) => {
  currentFailTask.value = task
  failReason.value = ''
  failDialogVisible.value = true
}

const submitFail = async () => {
  if (!failReason.value.trim()) {
    ElMessage.warning('请填写异常原因')
    return
  }
  actionLoading.value = true
  try {
    await courierAPI.fail(currentFailTask.value.taskId, failReason.value.trim())
    ElMessage.success('异常已记录')
    failDialogVisible.value = false
    await refreshAll()
  } catch (e) {
    // request interceptor shows the message
  } finally {
    actionLoading.value = false
  }
}

const statusText = (status) => {
  const map = { ASSIGNED: '待取件', PICKED_UP: '配送中', DELIVERED: '已送达', FAILED: '异常' }
  return map[status] || status
}

const statusTag = (status) => {
  const map = { ASSIGNED: 'warning', PICKED_UP: 'primary', DELIVERED: 'success', FAILED: 'danger' }
  return map[status] || 'info'
}

const formatTime = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const TaskCard = defineComponent({
  name: 'TaskCard',
  props: { task: { type: Object, required: true } },
  emits: ['pickup', 'complete', 'fail'],
  setup(props, { emit }) {
    const phone = computed(() => props.task.receiverPhone || '-')
    return () => h('article', { class: ['task-card', props.task.status === 'PICKED_UP' ? 'running' : 'waiting'] }, [
      h('div', { class: 'task-head' }, [
        h('span', { class: 'order-no' }, props.task.orderNo || '-'),
        h(ElTag, { type: statusTag(props.task.status), size: 'small' }, () => statusText(props.task.status))
      ]),
      h('div', { class: 'receiver' }, [
        h('strong', props.task.receiverName || '收货人'),
        h('span', phone.value)
      ]),
      h('div', { class: 'task-address' }, props.task.address || '-'),
      h('div', { class: 'task-times' }, [
        h('span', `分配 ${formatTime(props.task.assignTime)}`),
        props.task.pickupTime ? h('span', `取件 ${formatTime(props.task.pickupTime)}`) : null
      ]),
      props.task.failReason ? h('div', { class: 'fail-reason' }, props.task.failReason) : null,
      h('div', { class: 'task-actions' }, [
        props.task.status === 'ASSIGNED'
          ? h(ElButton, { type: 'primary', onClick: () => emit('pickup', props.task) }, () => '确认取件')
          : null,
        props.task.status === 'PICKED_UP'
          ? h(ElButton, { type: 'success', onClick: () => emit('complete', props.task) }, () => '确认送达')
          : null,
        props.task.status === 'PICKED_UP'
          ? h(ElButton, { type: 'danger', plain: true, onClick: () => emit('fail', props.task) }, () => '标记异常')
          : null
      ])
    ])
  }
})

onMounted(refreshAll)
</script>

<style scoped>
.tasks-page {
  max-width: 1220px;
  margin: 0 auto;
}

.overview {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric {
  min-height: 104px;
  padding: 18px;
  border-radius: 8px;
  background: #fff;
  border-left: 5px solid #64748b;
  box-shadow: 0 8px 24px rgba(15, 23, 42, .08);
}

.metric .label {
  display: block;
  color: #64748b;
  font-size: 13px;
}

.metric strong {
  display: block;
  margin-top: 12px;
  color: #0f172a;
  font-size: 34px;
  line-height: 1;
}

.metric-waiting { border-color: #f59e0b; }
.metric-running { border-color: #2563eb; }
.metric-done { border-color: #16a34a; }
.metric-risk { border-color: #dc2626; }

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 18px 0 14px;
}

.filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-pill {
  height: 36px;
  padding: 0 14px;
  border: 1px solid #d4dbe8;
  border-radius: 8px;
  background: #fff;
  color: #475569;
  cursor: pointer;
}

.filter-pill.active {
  background: #111827;
  border-color: #111827;
  color: #fff;
}

.board {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.column {
  min-height: 420px;
  padding: 14px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #f8fafc;
}

.column-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  color: #0f172a;
  font-weight: 800;
}

.column-head em {
  min-width: 28px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: #e2e8f0;
  font-style: normal;
  font-size: 12px;
}

:deep(.task-card) {
  margin-bottom: 12px;
  padding: 14px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 8px 18px rgba(15, 23, 42, .06);
}

:deep(.task-card.running) {
  border-color: #bfdbfe;
}

:deep(.task-head),
.history-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

:deep(.order-no),
.order-no {
  color: #334155;
  font-weight: 800;
  font-size: 13px;
}

:deep(.receiver) {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 14px;
  color: #0f172a;
}

:deep(.receiver span) {
  color: #64748b;
}

:deep(.task-address) {
  margin-top: 10px;
  color: #1f2937;
  line-height: 1.55;
}

:deep(.task-times) {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 10px;
  color: #94a3b8;
  font-size: 12px;
}

:deep(.fail-reason) {
  margin-top: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  background: #fef2f2;
  color: #b91c1c;
  font-size: 13px;
}

:deep(.task-actions) {
  display: flex;
  gap: 8px;
  margin-top: 14px;
}

.history {
  padding: 14px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, .08);
}

.history-list {
  display: grid;
  gap: 10px;
}

.history-row {
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
}

.address {
  margin-top: 5px;
  color: #64748b;
  font-size: 13px;
}

.history-right {
  display: flex;
  align-items: flex-end;
  flex-direction: column;
  gap: 6px;
}

.time {
  color: #94a3b8;
  font-size: 12px;
}

@media (max-width: 900px) {
  .overview,
  .board {
    grid-template-columns: 1fr;
  }
}
</style>
