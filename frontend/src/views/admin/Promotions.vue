<template>
  <div class="page-container">
    <h2>促销管理</h2>
    <el-button type="primary" @click="openAdd">添加促销</el-button>
    <el-table :data="promotions" border style="margin-top:20px">
      <el-table-column prop="activityId" label="ID" width="70" />
      <el-table-column prop="title" label="活动名称" min-width="180" />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">
          {{ promoTypeLabel(row.promoType) }}
        </template>
      </el-table-column>
      <el-table-column label="范围" width="110">
        <template #default="{ row }">
          {{ row.scopeType === 'CATEGORY' ? '指定分类' : '全场' }}
        </template>
      </el-table-column>
      <el-table-column label="有效期" min-width="260">
        <template #default="{ row }">
          {{ formatDate(row.startTime) }} ~ {{ formatDate(row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'info'">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="toggleStatus(row)">
            {{ row.status === 'active' ? '停用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.activityId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="loadPromotions" />
    </div>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑促销' : '添加促销'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.promoType" placeholder="选择类型" style="width:100%">
            <el-option label="满减" value="FULL_REDUCE" />
            <el-option label="折扣" value="DISCOUNT" />
            <el-option label="秒杀" value="SECKILL" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动范围">
          <el-select v-model="form.scopeType" style="width:100%">
            <el-option label="全场" value="ALL" />
            <el-option label="指定分类" value="CATEGORY" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类ID" v-if="form.scopeType === 'CATEGORY'">
          <el-input-number v-model="form.scopeCategoryId" :min="1" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="启用" value="active" />
            <el-option label="停用" value="inactive" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="savePromotion">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api'

const promotions = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showDialog = ref(false)
const isEdit = ref(false)
const form = ref({})

onMounted(loadPromotions)

async function loadPromotions() {
  try {
    const res = await adminAPI.getPromotions({ pageNum: pageNum.value, pageSize: pageSize.value })
    promotions.value = res.data?.records || res.data || []
    total.value = res.data?.total || 0
  } catch (e) { console.error(e) }
}

function openAdd() {
  isEdit.value = false
  form.value = { title: '', promoType: 'FULL_REDUCE', scopeType: 'ALL', status: 'active', startTime: '', endTime: '' }
  showDialog.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  showDialog.value = true
}

async function savePromotion() {
  if (!form.value.title) { ElMessage.warning('请填写活动名称'); return }
  if (!form.value.startTime || !form.value.endTime) { ElMessage.warning('请选择活动时间'); return }
  try {
    if (isEdit.value) {
      await adminAPI.updatePromotion(form.value.activityId, form.value)
    } else {
      await adminAPI.createPromotion(form.value)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadPromotions()
  } catch (e) { console.error(e) }
}

async function toggleStatus(row) {
  const newStatus = row.status === 'active' ? 'inactive' : 'active'
  try {
    await adminAPI.updatePromotion(row.activityId, { ...row, status: newStatus })
    ElMessage.success('状态已更新')
    loadPromotions()
  } catch (e) { console.error(e) }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个促销吗？', '提示', { type: 'warning' })
    await adminAPI.deletePromotion(id)
    ElMessage.success('删除成功')
    loadPromotions()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

function promoTypeLabel(type) {
  return { FULL_REDUCE: '满减', DISCOUNT: '折扣', SECKILL: '秒杀' }[type] || type || '—'
}

function statusLabel(status) {
  return { active: '启用', inactive: '停用' }[status] || status || '—'
}

function formatDate(value) {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(0, 19)
}
</script>

<style scoped>
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
