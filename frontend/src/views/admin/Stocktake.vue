<template>
  <div class="page-container">
    <div class="header-bar">
      <h2>库存盘点</h2>
      <el-button type="primary" @click="openCreate">新建盘点</el-button>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="filterStatus" size="default" @change="loadList">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="pending">待盘点</el-radio-button>
        <el-radio-button value="counting">盘点中</el-radio-button>
        <el-radio-button value="done">已完成</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="list" border v-loading="loading" class="mt">
      <el-table-column prop="taskId" label="盘点单号" width="120" />
      <el-table-column prop="scope" label="盘点范围" width="120">
        <template #default="{ row }">{{ row.scope === 'all' ? '全部商品' : row.categoryName || '分类' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="creatorName" label="创建人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column prop="submitTime" label="提交时间" width="170">
        <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row)">查看详情</el-button>
          <el-button v-if="row.status !== 'done'" size="small" type="primary" @click="openDetail(row)">去盘点</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      :page-size="pageSize"
      :page-sizes="[10, 20, 50]"
      :total="total"
      layout="total, prev, pager, next"
      class="pagination"
      @current-change="loadList"
    />

    <!-- 创建盘点弹窗 -->
    <el-dialog v-model="createVisible" title="新建盘点任务" width="440px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="90px">
        <el-form-item label="盘点范围" prop="scope">
          <el-radio-group v-model="createForm.scope">
            <el-radio value="all">全部商品</el-radio>
            <el-radio value="category">指定分类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="createForm.scope === 'category'" label="选择分类" prop="categoryId">
          <el-select v-model="createForm.categoryId" placeholder="请选择分类" style="width:100%">
            <el-option v-for="c in categories" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">确认创建</el-button>
      </template>
    </el-dialog>

    <!-- 盘点详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="`盘点详情 — #${currentTask?.taskId}`" width="820px" :close-on-click-modal="false">
      <div class="detail-info">
        <span>范围：{{ currentTask?.scope === 'all' ? '全部商品' : currentTask?.categoryName }}</span>
        <span>状态：<el-tag size="small" :type="statusTag(currentTask?.status)">{{ statusText(currentTask?.status) }}</el-tag></span>
        <span>创建：{{ formatTime(currentTask?.createTime) }}</span>
        <span v-if="currentTask?.submitTime">提交：{{ formatTime(currentTask?.submitTime) }}</span>
      </div>

      <!-- 差异汇总 -->
      <div v-if="diffSummary" class="diff-summary">
        <el-alert type="warning" :closable="false" show-icon>
          <template #title>
            盘点完成，共 {{ diffSummary.total }} 件商品，
            盘盈 <span style="color:#67c23a;font-weight:bold">{{ diffSummary.profit }}</span> 件，
            盘亏 <span style="color:#f56c6c;font-weight:bold">{{ diffSummary.loss }}</span> 件
          </template>
        </el-alert>
      </div>

      <!-- 商品列表 -->
      <el-table :data="detailItems" border max-height="420" class="mt">
        <el-table-column prop="productName" label="商品名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="bookStock" label="账面库存" width="100" align="center" />
        <el-table-column label="实际库存" width="140" align="center">
          <template #default="{ row }">
            <template v-if="currentTask?.status !== 'done'">
              <el-input-number
                v-model="row.actualStock"
                :min="0"
                :controls="false"
                size="small"
                style="width:90px"
                @change="onActualChange(row)"
              />
            </template>
            <span v-else :class="row.difference > 0 ? 'profit' : row.difference < 0 ? 'loss' : ''">
              {{ row.actualStock ?? '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="差异" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.difference === undefined || row.difference === null" style="color:#999">—</span>
            <span v-else-if="row.difference > 0" class="profit">+{{ row.difference }}</span>
            <span v-else-if="row.difference < 0" class="loss">{{ row.difference }}</span>
            <span v-else style="color:#67c23a">0</span>
          </template>
        </el-table-column>
        <el-table-column label="差异原因" min-width="160">
          <template #default="{ row }">
            <span v-if="currentTask?.status === 'done'">{{ row.diffReason || '—' }}</span>
            <el-input v-else v-model="row.diffReason" placeholder="选填" size="small" />
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentTask?.status !== 'done'" type="default" :loading="submitting" @click="saveInput">保存录入</el-button>
        <el-button v-if="currentTask?.status !== 'done' && hasInput" type="success" :loading="submitting" @click="submitStocktake">提交盘点</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { stocktakeAPI, adminAPI } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref('')
const categories = ref([])

// 创建盘点
const createVisible = ref(false)
const createFormRef = ref()
const createForm = ref({ scope: 'all', categoryId: null })
const createRules = {
  scope: [{ required: true, message: '请选择盘点范围', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

// 详情
const detailVisible = ref(false)
const currentTask = ref(null)
const detailItems = ref([])

const loadList = async () => {
  loading.value = true
  try {
    const res = await stocktakeAPI.getList({ pageNum: pageNum.value, pageSize: pageSize.value, status: filterStatus.value || undefined })
    list.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) { /* ignore */ } finally {
    loading.value = false
  }
}

loadList()

const openCreate = async () => {
  createForm.value = { scope: 'all', categoryId: null }
  createFormRef.value?.clearValidate()
  if (categories.value.length === 0) {
    try {
      const res = await adminAPI.getProducts({ categoryId, pageNum: 1, pageSize: 500 })
      categories.value = res.data || []
    } catch (e) { /* ignore */ }
  }
  createVisible.value = true
}

const submitCreate = async () => {
  if (createForm.value.scope === 'category' && !createForm.value.categoryId) {
    ElMessage.warning('请选择分类')
    return
  }
  submitting.value = true
  try {
    await stocktakeAPI.create({ scope: createForm.value.scope, categoryId: createForm.value.categoryId || undefined })
    ElMessage.success('盘点任务创建成功')
    createVisible.value = false
    loadList()
  } catch (e) { /* interceptor handles */ } finally {
    submitting.value = false
  }
}

const openDetail = async (row) => {
  currentTask.value = row
  detailVisible.value = true
  await loadDetail(row.taskId)
}

const loadDetail = async (taskId) => {
  try {
    const res = await stocktakeAPI.getDetail(taskId)
    currentTask.value = res.data
    detailItems.value = (res.data?.items || []).map(i => ({ ...i }))
  } catch (e) { /* ignore */ }
}

const onActualChange = (row) => {
  if (row.bookStock != null && row.actualStock != null) {
    row.difference = row.actualStock - row.bookStock
  }
}

const hasInput = computed(() => detailItems.value.some(i => i.actualStock != null))

const saveInput = async () => {
  const items = detailItems.value
    .filter(i => i.actualStock != null)
    .map(i => ({ productId: i.productId, actualQty: i.actualStock, diffReason: i.diffReason }))
  if (items.length === 0) { ElMessage.warning('请至少录入一个商品的实际库存'); return }
  submitting.value = true
  try {
    await stocktakeAPI.inputActual(currentTask.value.taskId, items)
    ElMessage.success('已保存')
    await loadDetail(currentTask.value.taskId)
  } catch (e) { /* interceptor handles */ } finally {
    submitting.value = false
  }
}

const submitStocktake = async () => {
  const items = detailItems.value
    .filter(i => i.actualStock != null)
    .map(i => ({ productId: i.productId, actualQty: i.actualStock, diffReason: i.diffReason }))
  if (items.length === 0) { ElMessage.warning('请先录入实际库存'); return }

  const notAllEntered = detailItems.value.some(i => i.actualStock == null)
  if (notAllEntered) {
    try {
      await ElMessageBox.confirm('尚有商品未录入实际库存，确认提交？未录入商品将以账面库存为准。', '提示', { type: 'warning' })
    } catch { return }
  }

  submitting.value = true
  try {
    await stocktakeAPI.inputActual(currentTask.value.taskId, items)
    await stocktakeAPI.submit(currentTask.value.taskId)
    ElMessage.success('盘点已提交')
    detailVisible.value = false
    loadList()
  } catch (e) { /* interceptor handles */ } finally {
    submitting.value = false
  }
}

const diffSummary = computed(() => {
  if (!detailItems.value.length) return null
  const entered = detailItems.value.filter(i => i.actualStock != null)
  if (!entered.length) return null
  return {
    total: entered.length,
    profit: entered.filter(i => i.difference > 0).length,
    loss: entered.filter(i => i.difference < 0).length
  }
})

const statusText = (s) => ({ pending: '待盘点', counting: '盘点中', done: '已完成' })[s] || s || '—'
const statusTag = (s) => ({ pending: 'info', counting: 'warning', done: 'success' })[s] || 'info'
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : '—'
</script>

<style scoped>
.page-container { padding: 20px; }
.header-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.header-bar h2 { margin: 0; font-size: 18px; }
.filter-bar { margin-bottom: 14px; }
.mt { margin-top: 12px; }
.pagination { justify-content: center; margin-top: 16px; }
.detail-info { display: flex; gap: 24px; font-size: 13px; color: #666; margin-bottom: 10px; }
.diff-summary { margin-bottom: 10px; }
.profit { color: #67c23a; font-weight: bold; }
.loss { color: #f56c6c; font-weight: bold; }
</style>
