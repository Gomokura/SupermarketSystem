<template>
  <div class="page-container">
    <h2>促销管理</h2>
    <el-button type="primary" @click="openAdd">添加促销</el-button>
    <el-table :data="promotions" border style="margin-top:20px">
      <el-table-column prop="promotionId" label="ID" width="70" />
      <el-table-column prop="promoName" label="活动名称" min-width="140" />
      <el-table-column prop="promoType" label="类型" width="90" />
      <el-table-column label="优惠" width="160">
        <template #default="{ row }">
          <span v-if="row.promoType === '满减'">满{{ row.conditionVal }}减{{ row.discountVal }}</span>
          <span v-else>{{ row.discountVal }}折</span>
        </template>
      </el-table-column>
      <el-table-column label="有效期" min-width="180">
        <template #default="{ row }">
          {{ row.startDate || '—' }} ~ {{ row.endDate || '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '进行中' ? 'success' : 'info'">{{ row.status || '—' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="toggleStatus(row)">
            {{ row.status === '进行中' ? '停用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.promotionId)">删除</el-button>
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
          <el-input v-model="form.promoName" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.promoType" placeholder="选择类型" style="width:100%">
            <el-option label="满减" value="满减" />
            <el-option label="折扣" value="折扣" />
          </el-select>
        </el-form-item>
        <el-form-item label="满减条件" v-if="form.promoType === '满减'">
          <el-input-number v-model="form.conditionVal" :min="0" :precision="2" />
          <span style="margin-left:8px;color:#909399">元起</span>
        </el-form-item>
        <el-form-item label="优惠值" required>
          <el-input-number v-model="form.discountVal" :min="0" :precision="2" />
          <span style="margin-left:8px;color:#909399">{{ form.promoType === '折扣' ? '折（如9.5=九五折）' : '元' }}</span>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="进行中" value="进行中" />
            <el-option label="已暂停" value="已暂停" />
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
  form.value = { status: '进行中' }
  showDialog.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  showDialog.value = true
}

async function savePromotion() {
  if (!form.value.promoName) { ElMessage.warning('请填写活动名称'); return }
  try {
    if (isEdit.value) {
      await adminAPI.updatePromotion(form.value.promotionId, form.value)
    } else {
      await adminAPI.createPromotion(form.value)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadPromotions()
  } catch (e) { console.error(e) }
}

async function toggleStatus(row) {
  const newStatus = row.status === '进行中' ? '已暂停' : '进行中'
  try {
    await adminAPI.updatePromotion(row.promotionId, { ...row, status: newStatus })
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
</script>

<style scoped>
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
