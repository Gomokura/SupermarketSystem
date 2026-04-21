<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <div class="page-title">促销管理</div>
        <div class="page-desc">统一维护活动名称、类型、时间和状态，便于运营集中配置。</div>
      </div>
      <div class="toolbar-actions">
        <el-button @click="loadPromotions">刷新</el-button>
        <el-button type="primary" @click="openCreate">添加促销</el-button>
      </div>
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" @tab-change="loadPromotions">
      <el-tab-pane label="全部活动" name="all" />
      <el-tab-pane label="满减活动" name="FULL_REDUCE" />
      <el-tab-pane label="秒杀活动" name="SECKILL" />
    </el-tabs>

    <!-- 进行中活动提示条 -->
    <el-alert
      v-if="activePromotions.length"
      :title="`当前 C 端进行中活动：${activePromotions.map(a => a.promoName).join('、')}`"
      type="success"
      show-icon
      :closable="false"
    />

    <el-card shadow="never" class="panel-card">
      <el-table :data="promotions" border stripe v-loading="loading">
        <el-table-column prop="promotionId" label="ID" width="70" />
        <el-table-column prop="promoName" label="活动名称" min-width="160" />
        <el-table-column prop="promoType" label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTagType(row.promoType)">{{ typeText(row.promoType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="范围" width="120">
          <template #default="{ row }">
            {{ row.categoryId ? '指定分类' : '全场' }}
          </template>
        </el-table-column>
        <!-- 满减阶梯摘要列，只在满减tab显示 -->
        <el-table-column v-if="activeTab === 'FULL_REDUCE'" label="阶梯规则" min-width="200">
          <template #default="{ row }">
            <span v-if="row.tiers && row.tiers.length">
              <el-tag
                v-for="(tier, i) in row.tiers"
                :key="i"
                size="small"
                style="margin-right:4px"
              >满{{ tier.threshold }}减{{ tier.discount }}</el-tag>
            </span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" min-width="160" />
        <el-table-column prop="endTime" label="结束时间" min-width="160" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="toggleStatus(row)">
              {{ row.status === 'active' ? '暂停' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="deletePromotion(row.promotionId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="form.promotionId ? '编辑促销' : '新增促销'" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="活动名称" prop="promoName">
          <el-input v-model="form.promoName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动类型" prop="promoType">
          <el-select v-model="form.promoType" style="width: 100%" @change="onTypeChange">
            <el-option label="满减" value="FULL_REDUCE" />
            <el-option label="秒杀" value="SECKILL" />
          </el-select>
        </el-form-item>

        <!-- 适用范围 -->
        <el-form-item label="适用范围">
          <el-radio-group v-model="form.scopeType">
            <el-radio label="ALL">全场通用</el-radio>
            <el-radio label="CATEGORY">指定分类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.scopeType === 'CATEGORY'" label="选择分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.categoryId"
              :label="cat.categoryName"
              :value="cat.categoryId"
            />
          </el-select>
        </el-form-item>

        <!-- 满减阶梯规则，仅满减类型显示 -->
        <template v-if="form.promoType === 'FULL_REDUCE'">
          <el-form-item label="阶梯规则">
            <div style="width:100%">
              <div
                v-for="(tier, index) in form.tiers"
                :key="index"
                style="display:flex; gap:8px; align-items:center; margin-bottom:8px"
              >
                <span style="white-space:nowrap">满</span>
                <el-input-number
                  v-model="tier.threshold"
                  :min="1"
                  :precision="2"
                  style="width:130px"
                  placeholder="消费金额"
                />
                <span style="white-space:nowrap">减</span>
                <el-input-number
                  v-model="tier.discount"
                  :min="0.01"
                  :precision="2"
                  style="width:130px"
                  placeholder="减免金额"
                />
                <el-button
                  type="danger"
                  :icon="Delete"
                  circle
                  size="small"
                  @click="removeTier(index)"
                />
              </div>
              <el-button size="small" @click="addTier">+ 添加阶梯</el-button>
            </div>
          </el-form-item>
        </template>

        <!-- 折扣率，仅折扣类型显示 -->
        <el-form-item v-if="form.promoType === 'DISCOUNT'" label="折扣率" prop="discountRate">
          <el-input-number
            v-model="form.discountRate"
            :min="0.01"
            :max="0.99"
            :precision="2"
            :step="0.05"
            style="width:160px"
          />
          <span style="margin-left:8px;color:#888">（0.01 ~ 0.99，如 0.85 表示 85 折）</span>
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="savePromotion">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { adminAPI, productAPI } from '@/api'

const promotions = ref([])
const activePromotions = ref([])
const categories = ref([])
const showDialog = ref(false)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const activeTab = ref('all')

const form = reactive({
  promotionId: null,
  promoName: '',
  promoType: 'FULL_REDUCE',
  scopeType: 'ALL',
  categoryId: null,
  tiers: [{ threshold: null, discount: null }],
  discountRate: null,
  startTime: '',
  endTime: '',
  status: 'active'
})

const rules = {
  promoName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  promoType: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const statusText = (s) => ({ active: '启用', inactive: '停用' }[s] || s || '-')
const typeText = (t) => ({ FULL_REDUCE: '满减', SECKILL: '秒杀' }[t] || t || '-')
const typeTagType = (t) => ({ FULL_REDUCE: 'danger', SECKILL: '' }[t] || 'info')

const resetForm = () => {
  Object.assign(form, {
    promotionId: null,
    promoName: '',
    promoType: 'FULL_REDUCE',
    scopeType: 'ALL',
    categoryId: null,
    tiers: [{ threshold: null, discount: null }],
    discountRate: null,
    startTime: '',
    endTime: '',
    status: 'active'
  })
}

const onTypeChange = () => {
  form.tiers = [{ threshold: null, discount: null }]
  form.discountRate = null
}

const addTier = () => form.tiers.push({ threshold: null, discount: null })
const removeTier = (i) => {
  if (form.tiers.length === 1) return ElMessage.warning('至少保留一条阶梯规则')
  form.tiers.splice(i, 1)
}

const loadPromotions = async () => {
  loading.value = true
  try {
    const params = { pageNum: 1, pageSize: 100 }
    if (activeTab.value !== 'all') params.promoType = activeTab.value
    const res = await adminAPI.getPromotions(params)
    promotions.value = res.data?.records || res.data?.list || []
  } finally {
    loading.value = false
  }
}

const loadActivePromotions = async () => {
  try {
    const res = await adminAPI.getActivePromotions()
    activePromotions.value = res.data || []
  } catch {
    activePromotions.value = []
  }
}

const loadCategories = async () => {
  try {
    const res = await productAPI.getCategoryTree()
    // 拍平一级分类列表
    const flatten = (list) => list.flatMap(c => [c, ...(c.children ? flatten(c.children) : [])])
    categories.value = flatten(res.data || [])
  } catch {
    categories.value = []
  }
}

const openCreate = () => {
  resetForm()
  showDialog.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    promotionId: row.promotionId,
    promoName: row.promoName,
    promoType: row.promoType,
    scopeType: row.categoryId ? 'CATEGORY' : 'ALL',
    categoryId: row.categoryId || null,
    tiers: row.tiers?.length ? row.tiers.map(t => ({ ...t })) : [{ threshold: null, discount: null }],
    discountRate: row.discountRate || null,
    startTime: row.startTime,
    endTime: row.endTime,
    status: row.status || 'active'
  })
  showDialog.value = true
}

const savePromotion = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      promoName: form.promoName,
      promoType: form.promoType,
      categoryId: form.scopeType === 'CATEGORY' ? form.categoryId : null,
      startTime: form.startTime,
      endTime: form.endTime,
      status: form.status
    }
    if (form.promoType === 'FULL_REDUCE') {
      payload.tiers = form.tiers.filter(t => t.threshold && t.discount)
    } else if (form.promoType === 'DISCOUNT') {
      payload.discountRate = form.discountRate
    }

    if (form.promotionId) {
      await adminAPI.updatePromotion(form.promotionId, payload)
      ElMessage.success('促销活动已更新')
    } else {
      await adminAPI.createPromotion(payload)
      ElMessage.success('促销活动已创建')
    }
    showDialog.value = false
    loadPromotions()
    loadActivePromotions()
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 'active' ? 'inactive' : 'active'
  await adminAPI.updatePromotionStatus(row.promotionId, newStatus)
  ElMessage.success('活动状态已更新')
  loadPromotions()
  loadActivePromotions()
}

const deletePromotion = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这个促销活动吗？', '提示', { type: 'warning' })
    await adminAPI.deletePromotion(id)
    ElMessage.success('删除成功')
    loadPromotions()
    loadActivePromotions()
  } catch (error) {
    if (error !== 'cancel') throw error
  }
}

onMounted(() => {
  loadPromotions()
  loadActivePromotions()
  loadCategories()
})
</script>

<style scoped>
.page-shell { display: grid; gap: 18px; }
.page-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.page-title { font-size: 28px; font-weight: 800; color: #22324d; }
.page-desc { margin-top: 4px; color: #6f7f97; }
.toolbar-actions { display: flex; gap: 12px; }
.panel-card { border: none; border-radius: 18px; box-shadow: 0 14px 36px rgba(42, 68, 110, 0.08); }
.text-muted { color: #aaa; }
</style>
