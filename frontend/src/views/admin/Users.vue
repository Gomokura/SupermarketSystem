<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <!-- B-26 多条件筛选 -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :model="filter" inline>
        <el-form-item label="关键词">
          <el-input v-model="filter.keyword" clearable placeholder="姓名/手机号" style="width:200px"
            @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" clearable placeholder="全部" style="width:110px">
            <el-option label="正常" value="active" />
            <el-option label="封禁" value="banned" />
          </el-select>
        </el-form-item>
        <el-form-item label="消费金额">
          <el-input-number v-model="filter.minSpent" :min="0" placeholder="最低" style="width:100px" />
          <span style="margin:0 8px">-</span>
          <el-input-number v-model="filter.maxSpent" :min="0" placeholder="最高" style="width:100px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="users" border v-loading="loading">
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="memberLevel" label="会员等级" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="levelType(row.memberLevel)">{{ row.memberLevel || '普通' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="90" />
        <el-table-column label="消费金额" width="110">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalSpent || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '正常' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <!-- B-27 用户详情 -->
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <!-- B-28 封禁/解封 -->
            <el-button size="small" :type="row.status === 'active' ? 'danger' : 'success'" @click="toggleBan(row)">
              {{ row.status === 'active' ? '封禁' : '解封' }}
            </el-button>
            <!-- B-29 调整积分 -->
            <el-button size="small" type="warning" @click="openAdjustPoints(row)">调积分</el-button>
            <!-- B-30 发消息 -->
            <el-button size="small" type="info" @click="openSendMsg(row)">发消息</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" layout="total, prev, pager, next" @current-change="loadUsers" />
      </div>
    </el-card>

    <!-- B-27 用户详情 Dialog -->
    <el-dialog v-model="detailVisible" title="用户详情" width="560px">
      <template v-if="currentUser">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="会员等级">{{ currentUser.memberLevel || '普通' }}</el-descriptions-item>
          <el-descriptions-item label="积分">{{ currentUser.points || 0 }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDate(currentUser.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentUser.status === 'active' ? 'success' : 'danger'">
              {{ currentUser.status === 'active' ? '正常' : '封禁' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总订单数">{{ currentUser.orderCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="总消费金额">¥{{ currentUser.totalSpent || 0 }}</el-descriptions-item>
          <el-descriptions-item label="最近下单" :span="2">{{ formatDate(currentUser.lastOrderTime) }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- B-29 积分调整 Dialog -->
    <el-dialog v-model="pointsVisible" title="调整积分" width="400px">
      <el-form :model="pointsForm" label-width="80px">
        <el-form-item label="调整方向">
          <el-radio-group v-model="pointsForm.direction">
            <el-radio label="add">增加</el-radio>
            <el-radio label="sub">扣减</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="积分数量">
          <el-input-number v-model="pointsForm.amount" :min="1" :max="99999" style="width:160px" />
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="pointsForm.remark" type="textarea" :rows="2" placeholder="必填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pointsVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdjustPoints">确认</el-button>
      </template>
    </el-dialog>

    <!-- B-30 发站内信 Dialog -->
    <el-dialog v-model="msgVisible" title="发送站内消息" width="440px">
      <el-form :model="msgForm" label-width="70px">
        <el-form-item label="标题">
          <el-input v-model="msgForm.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="msgForm.content" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="msgVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSendMsg">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI, pointsAPI } from '@/api'
import axios from 'axios'

const loading = ref(false)
const users = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filter = reactive({ keyword: '', status: '', minSpent: null, maxSpent: null })

const detailVisible = ref(false)
const currentUser = ref(null)

const pointsVisible = ref(false)
const pointsTargetUser = ref(null)
const pointsForm = reactive({ direction: 'add', amount: 10, remark: '' })

const msgVisible = ref(false)
const msgTargetUser = ref(null)
const msgForm = reactive({ title: '', content: '' })

onMounted(() => loadUsers())

const handleSearch = () => { pageNum.value = 1; loadUsers() }
const resetFilter = () => { Object.assign(filter, { keyword: '', status: '', minSpent: null, maxSpent: null }); pageNum.value = 1; loadUsers() }

const loadUsers = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filter.keyword) params.keyword = filter.keyword
    if (filter.status) params.status = filter.status
    if (filter.minSpent) params.minSpent = filter.minSpent
    if (filter.maxSpent) params.maxSpent = filter.maxSpent
    const res = await adminAPI.getUsers(params)
    users.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (d) => d ? new Date(d).toLocaleString('zh-CN') : '-'

const levelType = (level) => {
  const map = { 银卡: 'info', 金卡: 'warning', 钻石: 'danger' }
  return map[level] || ''
}

const viewDetail = async (row) => {
  try {
    const res = await adminAPI.getUserDetail(row.userId)
    currentUser.value = res.data || row
  } catch { currentUser.value = row }
  detailVisible.value = true
}

const toggleBan = async (row) => {
  const isBan = row.status === 'active'
  try {
    if (isBan) {
      const { value: reason } = await ElMessageBox.prompt('请输入封禁原因', '封禁用户', {
        inputPlaceholder: '请填写原因'
      })
      await adminAPI.updateUserStatus(row.userId, 'banned', reason)
    } else {
      await ElMessageBox.confirm(`确定解封用户 ${row.username}？`, '解封用户', { type: 'warning' })
      await adminAPI.updateUserStatus(row.userId, 'active')
    }
    ElMessage.success(isBan ? '用户已封禁' : '用户已解封')
    loadUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const openAdjustPoints = (row) => {
  pointsTargetUser.value = row
  Object.assign(pointsForm, { direction: 'add', amount: 10, remark: '' })
  pointsVisible.value = true
}

const submitAdjustPoints = async () => {
  if (!pointsForm.remark) { ElMessage.warning('请填写调整原因'); return }
  const delta = pointsForm.direction === 'add' ? pointsForm.amount : -pointsForm.amount
  try {
    await pointsAPI.adminAdjust({ userId: pointsTargetUser.value.userId, points: delta, remark: pointsForm.remark })
    ElMessage.success('积分已调整')
    pointsVisible.value = false
    loadUsers()
  } catch {
    ElMessage.error('调整失败')
  }
}

const openSendMsg = (row) => {
  msgTargetUser.value = row
  Object.assign(msgForm, { title: '', content: '' })
  msgVisible.value = true
}

const submitSendMsg = async () => {
  if (!msgForm.title || !msgForm.content) { ElMessage.warning('标题和内容不能为空'); return }
  try {
    await adminAPI.sendMessage(msgTargetUser.value.userId, msgForm.title, msgForm.content)
    ElMessage.success('消息已发送')
    msgVisible.value = false
  } catch {
    ElMessage.error('发送失败')
  }
}
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.amount { color: #f56c6c; font-weight: bold; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
