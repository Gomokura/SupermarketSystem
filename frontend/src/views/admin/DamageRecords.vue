<template>
  <div class="page-container">
    <h2>仓储管理</h2>

    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- 报损记录 -->
      <el-tab-pane label="报损记录" name="damage">
        <div class="sub-toolbar">
          <el-input v-model="damageKeyword" placeholder="搜索商品名称" clearable style="width:200px" @clear="loadDamage" @keyup.enter="loadDamage" />
          <el-button type="primary" @click="openDamage">新增报损</el-button>
        </div>
        <el-table :data="damageList" border v-loading="damageLoading" class="mt">
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column prop="productName" label="商品名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="quantity" label="报损数量" width="100" align="center" />
          <el-table-column prop="reason" label="报损原因" width="120">
            <template #default="{ row }">{{ reasonText(row.reason) }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
          <el-table-column prop="operatorName" label="操作人" width="100" />
          <el-table-column prop="createTime" label="登记时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="damagePage" :page-size="damageSize" :total="damageTotal"
          layout="total, prev, pager, next" class="pagination" @current-change="loadDamage"
        />
      </el-tab-pane>

      <!-- 库存总览 -->
      <el-tab-pane label="库存总览" name="inventory">
        <div class="sub-toolbar">
          <el-input v-model="invKeyword" placeholder="搜索商品" clearable style="width:200px" @keyup.enter="loadInventory" />
          <el-button @click="loadInventory">搜索</el-button>
        </div>
        <el-table :data="invList" border v-loading="invLoading" class="mt" max-height="500">
          <el-table-column prop="productName" label="商品名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="分类" width="120" />
          <el-table-column prop="stock" label="当前库存" width="100" align="center">
            <template #default="{ row }">
              <span :class="row.stock <= (row.lowStockThreshold || 10) ? 'low-stock' : ''">{{ row.stock ?? 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="单价" width="100" align="center">
            <template #default="{ row }">¥{{ (row.price || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="库存价值" width="120" align="center">
            <template #default="{ row }">¥{{ ((row.stock || 0) * (row.price || 0)).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="lowStockThreshold" label="低库存阈值" width="110" align="center" />
        </el-table>
        <el-pagination
          v-model:current-page="invPage" :page-size="invSize" :total="invTotal"
          layout="total, prev, pager, next" class="pagination" @current-change="loadInventory"
        />
      </el-tab-pane>

      <!-- 低库存预警 -->
      <el-tab-pane label="低库存预警" name="lowstock">
        <div class="sub-toolbar">
          <el-alert type="warning" :closable="false" show-icon style="flex:1">
            共 {{ lowStockList.length }} 件商品低于库存阈值
          </el-alert>
          <el-button @click="loadLowStock">刷新</el-button>
        </div>
        <el-table :data="lowStockList" border v-loading="lowStockLoading" class="mt" max-height="500">
          <el-table-column prop="productName" label="商品名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="分类" width="120" />
          <el-table-column prop="stock" label="当前库存" width="100" align="center">
            <template #default="{ row }"><span class="low-stock">{{ row.stock ?? 0 }}</span></template>
          </el-table-column>
          <el-table-column prop="lowStockThreshold" label="低库存阈值" width="110" align="center" />
          <el-table-column label="缺口" width="100" align="center">
            <template #default="{ row }">
              <span class="loss">{{ (row.lowStockThreshold || 0) - (row.stock || 0) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="建议补货" width="100" align="center">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="quickPurchase(row)">进货</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 库存流水 -->
      <el-tab-pane label="库存流水" name="logs">
        <div class="sub-toolbar">
          <el-select v-model="logType" placeholder="类型" clearable style="width:120px">
            <el-option value="IN" label="入库" />
            <el-option value="OUT" label="出库" />
          </el-select>
          <el-input v-model="logKeyword" placeholder="商品名称" clearable style="width:160px" />
          <el-date-picker v-model="logDateRange" type="daterange" range-separator="至" start-placeholder="开始日期"
            end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:240px" />
          <el-button @click="loadLogs">查询</el-button>
          <el-button @click="resetLogs">重置</el-button>
        </div>
        <el-table :data="logList" border v-loading="logLoading" class="mt" max-height="500">
          <el-table-column prop="logId" label="编号" width="80" />
          <el-table-column prop="productName" label="商品名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="type" label="类型" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.type === 'IN' ? 'success' : 'danger'" size="small">{{ row.type === 'IN' ? '入库' : '出库' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" align="center">
            <template #default="{ row }">
              <span :class="row.type === 'IN' ? 'profit' : 'loss'">{{ row.type === 'IN' ? '+' : '-' }}{{ row.quantity }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="stockBefore" label="变动前" width="90" align="center" />
          <el-table-column prop="stockAfter" label="变动后" width="90" align="center" />
          <el-table-column prop="reason" label="原因" min-width="120" show-overflow-tooltip />
          <el-table-column prop="operatorName" label="操作人" width="100" />
          <el-table-column prop="createTime" label="时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="logPage" :page-size="logSize" :total="logTotal"
          layout="total, prev, pager, next" class="pagination" @current-change="loadLogs"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 新增报损弹窗 -->
    <el-dialog v-model="damageVisible" title="新增报损" width="500px">
      <el-form :model="damageForm" :rules="damageRules" ref="damageFormRef" label-width="90px">
        <el-form-item label="选择商品" prop="productId">
          <el-select
            v-model="damageForm.productId"
            filterable
            remote
            placeholder="输入商品名称搜索"
            :remote-method="searchProducts"
            :loading="searchLoading"
            style="width:100%"
            @change="onProductSelect"
          >
            <el-option v-for="p in searchResults" :key="p.productId"
              :label="p.productName + '（库存：' + p.stock + '）'"
              :value="p.productId" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input :model-value="damageForm.currentStock" disabled />
        </el-form-item>
        <el-form-item label="报损数量" prop="quantity">
          <el-input-number v-model="damageForm.quantity" :min="1" :max="damageForm.currentStock || 9999" style="width:100%" />
        </el-form-item>
        <el-form-item label="报损原因" prop="reason">
          <el-select v-model="damageForm.reason" placeholder="请选择" style="width:100%">
            <el-option value="破损" label="破损" />
            <el-option value="过期" label="过期" />
            <el-option value="其他" label="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="damageForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="damageVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitDamage">提交</el-button>
      </template>
    </el-dialog>

    <!-- 快捷进货弹窗 -->
    <el-dialog v-model="purchaseVisible" title="快捷进货" width="420px">
      <el-form label-width="90px">
        <el-form-item label="商品">{{ purchaseItem?.productName }}</el-form-item>
        <el-form-item label="当前库存"><span class="low-stock">{{ purchaseItem?.stock }}</span></el-form-item>
        <el-form-item label="进货数量" required>
          <el-input-number v-model="purchaseQty" :min="1" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="purchaseVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPurchase">提交进货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAPI, productAPI } from '@/api'

const activeTab = ref('damage')

// ===== 报损记录 =====
const damageLoading = ref(false)
const damageList = ref([])
const damagePage = ref(1)
const damageSize = ref(10)
const damageTotal = ref(0)
const damageKeyword = ref('')
const damageVisible = ref(false)
const submitting = ref(false)
const damageFormRef = ref()
const searchLoading = ref(false)
const searchResults = ref([])
const damageForm = ref({ productId: null, productName: '', currentStock: 0, quantity: 1, reason: '', remark: '' })
const damageRules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [{ required: true, message: '请填写数量', trigger: 'blur' }],
  reason: [{ required: true, message: '请选择原因', trigger: 'change' }]
}

const loadDamage = async () => {
  damageLoading.value = true
  try {
    const res = await adminAPI.getDamageRecords({ pageNum: damagePage.value, pageSize: damageSize.value, keyword: damageKeyword.value || undefined })
    damageList.value = res.data?.records || res.data || []
    damageTotal.value = res.data?.total || 0
  } catch (e) { /* ignore */ } finally {
    damageLoading.value = false
  }
}

loadDamage()

const openDamage = () => {
  damageForm.value = { productId: null, productName: '', currentStock: 0, quantity: 1, reason: '', remark: '' }
  searchResults.value = []
  damageFormRef.value?.clearValidate()
  damageVisible.value = true
}

const searchProducts = async (keyword) => {
  if (!keyword) { searchResults.value = []; return }
  searchLoading.value = true
  try {
    const res = await productAPI.getList({ keyword, pageNum: 1, pageSize: 20 })
    const list = res.data?.records || res.data || []
    // 合并库存信息
    try {
      const inv = await adminAPI.getInventoryOverview({ keyword, pageNum: 1, pageSize: 20 })
      const invMap = {}
      ;(inv.data?.records || inv.data || []).forEach(i => { invMap[i.productId] = i.stock })
      list.forEach(p => { p.stock = invMap[p.productId] ?? 0 })
    } catch {}
    searchResults.value = list
  } catch (e) { /* ignore */ } finally {
    searchLoading.value = false
  }
}

const onProductSelect = async (productId) => {
  const p = searchResults.value.find(x => x.productId === productId)
  damageForm.value.productName = p?.productName || ''
  damageForm.value.currentStock = p?.stock || 0
}

const submitDamage = async () => {
  if (!damageFormRef.value) return
  await damageFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await adminAPI.createDamageRecord({
        productId: damageForm.value.productId,
        quantity: damageForm.value.quantity,
        reason: damageForm.value.reason,
        remark: damageForm.value.remark || undefined
      })
      ElMessage.success('报损登记成功')
      damageVisible.value = false
      loadDamage()
    } catch (e) { /* interceptor handles */ } finally {
      submitting.value = false
    }
  })
}

// ===== 库存总览 =====
const invLoading = ref(false)
const invList = ref([])
const invPage = ref(1)
const invSize = ref(10)
const invTotal = ref(0)
const invKeyword = ref('')

const loadInventory = async () => {
  invLoading.value = true
  try {
    const res = await adminAPI.getInventoryOverview({ pageNum: invPage.value, pageSize: invSize.value, keyword: invKeyword.value || undefined })
    invList.value = res.data?.records || res.data || []
    invTotal.value = res.data?.total || 0
  } catch (e) { /* ignore */ } finally {
    invLoading.value = false
  }
}

// ===== 低库存预警 =====
const lowStockLoading = ref(false)
const lowStockList = ref([])

const loadLowStock = async () => {
  lowStockLoading.value = true
  try {
    const res = await adminAPI.getLowStock()
    lowStockList.value = res.data || []
  } catch (e) { /* ignore */ } finally {
    lowStockLoading.value = false
  }
}

// ===== 库存流水 =====
const logLoading = ref(false)
const logList = ref([])
const logPage = ref(1)
const logSize = ref(10)
const logTotal = ref(0)
const logType = ref('')
const logKeyword = ref('')
const logDateRange = ref(null)

const loadLogs = async () => {
  logLoading.value = true
  try {
    const params = {
      pageNum: logPage.value,
      pageSize: logSize.value,
      type: logType.value || undefined,
      keyword: logKeyword.value || undefined,
      startDate: logDateRange.value?.[0] || undefined,
      endDate: logDateRange.value?.[1] || undefined
    }
    const res = await adminAPI.getWarehouseLogs(params)
    logList.value = res.data?.records || res.data || []
    logTotal.value = res.data?.total || 0
  } catch (e) { /* ignore */ } finally {
    logLoading.value = false
  }
}

const resetLogs = () => {
  logType.value = ''
  logKeyword.value = ''
  logDateRange.value = null
  loadLogs()
}

// ===== 快捷进货 =====
const purchaseVisible = ref(false)
const purchaseItem = ref(null)
const purchaseQty = ref(1)

const quickPurchase = (row) => {
  purchaseItem.value = row
  purchaseQty.value = Math.max(1, (row.lowStockThreshold || 10) - (row.stock || 0))
  purchaseVisible.value = true
}

const submitPurchase = async () => {
  if (!purchaseQty.value || purchaseQty.value < 1) { ElMessage.warning('请填写进货数量'); return }
  submitting.value = true
  try {
    await adminAPI.warehousing(purchaseItem.value.productId, purchaseQty.value, '低库存补货')
    ElMessage.success('进货成功')
    purchaseVisible.value = false
    loadLowStock()
    loadInventory()
  } catch (e) { /* interceptor handles */ } finally {
    submitting.value = false
  }
}

// ===== 工具 =====
const reasonText = (r) => ({ 破损: '破损', 过期: '过期', 其他: '其他' })[r] || r || '—'
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : '—'
</script>

<style scoped>
.page-container { padding: 20px; }
h2 { margin: 0 0 16px; font-size: 18px; }
.main-tabs :deep(.el-tabs__content) { overflow: visible; }
.sub-toolbar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.mt { margin-top: 12px; }
.pagination { justify-content: center; margin-top: 16px; }
.low-stock { color: #f56c6c; font-weight: bold; }
.profit { color: #67c23a; font-weight: bold; }
.loss { color: #f56c6c; font-weight: bold; }
</style>
