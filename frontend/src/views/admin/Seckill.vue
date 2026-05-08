<template>
  <div class="page-container">
    <div class="header-bar">
      <h2>秒杀活动</h2>
      <el-button type="primary" @click="openAdd">新建活动</el-button>
    </div>

    <!-- 筛选状态 -->
    <div class="filter-bar">
      <el-radio-group v-model="filterStatus" size="default" @change="loadActivities">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="pending">未开始</el-radio-button>
        <el-radio-button value="running">进行中</el-radio-button>
        <el-radio-button value="paused">已暂停</el-radio-button>
        <el-radio-button value="ended">已结束</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="list" border v-loading="loading" class="mt">
      <el-table-column prop="seckillId" label="ID" width="70" />
      <el-table-column prop="seckillName" label="活动名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="startTime" label="开始时间" width="160">
        <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column prop="endTime" label="结束时间" width="160">
        <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column prop="currentState" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="stateTag(row.currentState)" size="small">{{ stateText(row.currentState) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="seckillStock" label="秒杀库存" width="90" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" @click="openProducts(row)">商品管理</el-button>
          <el-button size="small" type="danger" @click="toggleStatus(row)">
            {{ row.status === 'active' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      class="pagination"
      @current-change="loadActivities"
    />

    <!-- 新建/编辑活动弹窗 -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑活动' : '新建活动'" width="460px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="活动名称" prop="seckillName">
          <el-input v-model="form.seckillName" placeholder="如：端午秒杀专场" maxlength="50" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 商品管理弹窗 -->
    <el-dialog v-model="productVisible" :title="`商品管理 — ${currentActivity?.seckillName}`" width="700px">
      <div class="product-toolbar">
        <el-button type="primary" size="small" @click="openProductAdd">添加商品</el-button>
      </div>

      <el-table :data="productList" border class="mt" max-height="350">
        <el-table-column prop="productName" label="商品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="originalPrice" label="原价" width="90">
          <template #default="{ row }">¥{{ (row.originalPrice || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="seckillPrice" label="秒杀价" width="100">
          <template #default="{ row }">
            <span style="color:#f56c6c;font-weight:bold">¥{{ (row.seckillPrice || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="seckillStock" label="秒杀库存" width="100" />
        <el-table-column prop="remainingStock" label="剩余库存" width="90">
          <template #default="{ row }">{{ row.remainingStock ?? row.seckillStock }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openProductEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeProduct(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="productVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑商品弹窗 -->
    <el-dialog v-model="productFormVisible" :title="isProductEdit ? '编辑商品' : '添加商品'" width="500px">
      <!-- 商品搜索选择 -->
      <el-form label-width="80px">
        <el-form-item label="选择商品" required>
          <el-select
            v-if="!isProductEdit"
            v-model="productForm.productId"
            filterable
            remote
            placeholder="输入商品名称搜索"
            :remote-method="searchProducts"
            :loading="searchLoading"
            style="width:100%"
            @change="onProductSelect"
          >
            <el-option v-for="p in searchResults" :key="p.productId"
              :label="p.productName + '（¥' + (p.price || 0).toFixed(2) + '）'"
              :value="p.productId" />
          </el-select>
          <div v-else class="product-name-display">{{ productForm.productName }}</div>
        </el-form-item>
        <el-form-item label="原价">
          <el-input :model-value="productForm.originalPrice ? '¥' + productForm.originalPrice.toFixed(2) : '—'" disabled />
        </el-form-item>
        <el-form-item label="秒杀价" required>
          <el-input-number v-model="productForm.seckillPrice" :min="0.01" :precision="2" :step="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="秒杀库存" required>
          <el-input-number v-model="productForm.seckillStock" :min="1" :step="1" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitProduct">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { seckillAPI, productAPI, adminAPI } from '@/api'

const loading = ref(false)
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref('')

// 活动表单
const formVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  seckillId: null,
  seckillName: '',
  startTime: '',
  endTime: '',
  status: 'active'
})
const rules = {
  seckillName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (form.startTime && value && new Date(value) <= new Date(form.startTime)) {
          callback(new Error('结束时间必须晚于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 商品管理
const productVisible = ref(false)
const currentActivity = ref(null)
const productList = ref([])

// 商品表单
const productFormVisible = ref(false)
const isProductEdit = ref(false)
const searchLoading = ref(false)
const searchResults = ref([])
const productForm = reactive({
  id: null,
  productId: null,
  productName: '',
  originalPrice: null,
  seckillPrice: null,
  seckillStock: null
})

const loadActivities = async () => {
  loading.value = true
  try {
    const res = await seckillAPI.adminGetList({ pageNum: pageNum.value, pageSize: pageSize.value, state: filterStatus.value || undefined })
    list.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

loadActivities()

const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { seckillId: null, seckillName: '', startTime: '', endTime: '', status: 'active' })
  formRef.value?.clearValidate()
  formVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    seckillId: row.seckillId,
    seckillName: row.seckillName,
    startTime: formatTimeForInput(row.startTime),
    endTime: formatTimeForInput(row.endTime),
    status: row.status
  })
  formRef.value?.clearValidate()
  formVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        seckillName: form.seckillName,
        startTime: form.startTime,
        endTime: form.endTime,
        status: form.status,
        activityType: 'SECKILL'
      }
      if (isEdit.value) {
        await seckillAPI.adminUpdate(form.seckillId, payload)
        ElMessage.success('更新成功')
      } else {
        await seckillAPI.adminCreate(payload)
        ElMessage.success('创建成功')
      }
      formVisible.value = false
      loadActivities()
    } catch (e) {
      // handled by interceptor
    } finally {
      submitting.value = false
    }
  })
}

const toggleStatus = async (row) => {
  const action = row.status === 'active' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}该活动？`, '提示')
    await seckillAPI.adminUpdate(row.seckillId, { status: row.status === 'active' ? 'inactive' : 'active', activityType: 'SECKILL', seckillName: row.seckillName, startTime: row.startTime, endTime: row.endTime })
    ElMessage.success(`${action}成功`)
    loadActivities()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

// 商品管理
const openProducts = async (row) => {
  currentActivity.value = row
  productVisible.value = true
  await loadProducts(row.seckillId)
}

const loadProducts = async (seckillId) => {
  try {
    const res = await seckillAPI.adminGetActivityProducts(seckillId)
    productList.value = res.data || []
  } catch (e) { /* ignore */ }
}

const searchProducts = async (keyword) => {
  if (!keyword) { searchResults.value = []; return }
  searchLoading.value = true
  try {
    const res = await adminAPI.searchProducts(keyword, 1, 20)
    searchResults.value = res.data?.records || res.data || []
  } catch (e) { /* ignore */ } finally {
    searchLoading.value = false
  }
}

const onProductSelect = (productId) => {
  const p = searchResults.value.find(x => x.productId === productId)
  if (p) {
    productForm.productName = p.productName
    productForm.originalPrice = p.price || p.originalPrice || null
  }
}

const openProductAdd = () => {
  isProductEdit.value = false
  Object.assign(productForm, { id: null, productId: null, productName: '', originalPrice: null, seckillPrice: null, seckillStock: null })
  searchResults.value = []
  productFormVisible.value = true
}

const openProductEdit = (row) => {
  isProductEdit.value = true
  Object.assign(productForm, {
    id: row.id,
    productId: row.productId,
    productName: row.productName,
    originalPrice: row.originalPrice,
    seckillPrice: row.seckillPrice,
    seckillStock: row.seckillStock
  })
  productFormVisible.value = true
}

const submitProduct = async () => {
  if (!productForm.productId && !isProductEdit.value) {
    ElMessage.warning('请选择商品')
    return
  }
  if (!productForm.seckillPrice) {
    ElMessage.warning('请填写秒杀价')
    return
  }
  if (!productForm.seckillStock) {
    ElMessage.warning('请填写秒杀库存')
    return
  }
  submitting.value = true
  try {
    const payload = [{
      id: isProductEdit.value ? productForm.id : null,
      productId: productForm.productId,
      seckillPrice: productForm.seckillPrice,
      seckillStock: productForm.seckillStock
    }]
    await seckillAPI.adminUpsertProducts(currentActivity.value.seckillId, payload)
    ElMessage.success('保存成功')
    productFormVisible.value = false
    await loadProducts(currentActivity.value.seckillId)
  } catch (e) {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

const removeProduct = async (row) => {
  try {
    await ElMessageBox.confirm('确认移除该商品？', '提示')
    // 后端未提供单独删除接口，通过重新提交当前商品列表实现删除
    const remaining = productList.value.filter(p => p.productId !== row.productId)
    const payload = remaining.map(p => ({
      id: p.id,
      productId: p.productId,
      seckillPrice: p.seckillPrice,
      seckillStock: p.seckillStock
    }))
    await seckillAPI.adminUpsertProducts(currentActivity.value.seckillId, payload)
    ElMessage.success('已移除')
    await loadProducts(currentActivity.value.seckillId)
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const stateText = (s) => ({ pending: '未开始', running: '进行中', paused: '已暂停', ended: '已结束' })[s] || s || '—'
const stateTag = (s) => ({ pending: 'info', running: 'success', paused: 'warning', ended: 'default' })[s] || 'info'

const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : '—'
const formatTimeForInput = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
</script>

<style scoped>
.page-container { padding: 20px; }
.header-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.header-bar h2 { margin: 0; font-size: 18px; }
.filter-bar { margin-bottom: 14px; }
.mt { margin-top: 14px; }
.pagination { justify-content: center; margin-top: 16px; }
.product-toolbar { margin-bottom: 10px; }
.product-name-display { padding: 0 10px; color: #333; }
</style>
