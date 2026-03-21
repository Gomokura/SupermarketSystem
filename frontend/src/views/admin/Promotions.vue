<template>
  <div class="page-container">
    <h2>促销管理</h2>
    <el-button type="primary" @click="showDialog = true; form = {}">添加促销</el-button>
    <el-table :data="promotions" border style="margin-top: 20px">
      <el-table-column prop="promotionId" label="ID" width="80" />
      <el-table-column prop="promoName" label="活动名称" />
      <el-table-column prop="promoType" label="类型" width="100" />
      <el-table-column label="优惠" width="150">
        <template #default="{ row }">
          <span v-if="row.promoType === '满减'">满{{ row.conditionVal }}减{{ row.discountVal }}</span>
          <span v-else>{{ row.discountVal }}折</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '进行中' ? 'success' : 'info'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="deletePromotion(row.promotionId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" title="添加促销" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="活动名称">
          <el-input v-model="form.promoName" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.promoType" placeholder="选择类型">
            <el-option label="满减" value="满减" />
            <el-option label="折扣" value="折扣" />
          </el-select>
        </el-form-item>
        <el-form-item label="满减条件" v-if="form.promoType === '满减'">
          <el-input-number v-model="form.conditionVal" :min="0" />
        </el-form-item>
        <el-form-item label="优惠值">
          <el-input-number v-model="form.discountVal" :min="0" />
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
const showDialog = ref(false)
const form = ref({})

onMounted(() => {
  loadPromotions()
})

const loadPromotions = async () => {
  try {
    const res = await adminAPI.getPromotions()
    promotions.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const savePromotion = async () => {
  try {
    await adminAPI.createPromotion(form.value)
    ElMessage.success('保存成功')
    showDialog.value = false
    loadPromotions()
  } catch (error) {
    console.error(error)
  }
}

const deletePromotion = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个促销吗？', '提示', { type: 'warning' })
    await adminAPI.deletePromotion(id)
    ElMessage.success('删除成功')
    loadPromotions()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}
</script>
