<template>
  <div class="page-container">
    <h2>申请售后</h2>
    
    <el-card class="form-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="售后类型" prop="asType">
          <el-radio-group v-model="form.asType">
            <el-radio label="REFUND">仅退款</el-radio>
            <el-radio label="RETURN_REFUND">退货退款</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="申请原因" prop="reason">
          <el-select v-model="form.reason" placeholder="请选择申请原因">
            <el-option label="商品质量问题" value="QUALITY_ISSUE" />
            <el-option label="商品与描述不符" value="DESCRIPTION_MISMATCH" />
            <el-option label="商品破损/漏发" value="DAMAGED_OR_MISSING" />
            <el-option label="拍错/多拍" value="WRONG_ITEM" />
            <el-option label="七天无理由退换" value="WITHOUT_REASON" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="退款金额" prop="refundAmount">
          <el-input v-model="form.refundAmount" type="number" placeholder="请输入退款金额" />
          <span class="input-tip">最大可退 ￥{{ maxRefundAmount }}</span>
        </el-form-item>
        
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="4" placeholder="请详细描述问题情况" />
        </el-form-item>
        
        <el-form-item label="凭证图片">
          <el-upload
            class="upload-demo"
            action="/api/upload/image"
            :limit="5"
            :on-success="handleUploadSuccess"
            :file-list="uploadFiles"
            list-type="picture-card"
          >
            <el-icon size="20"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交申请</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { afterSalesAPI, orderAPI } from '@/api'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const uploadFiles = ref([])

const orderId = route.params.orderId
const order = ref(null)

const form = reactive({
  asType: 'REFUND',
  reason: '',
  refundAmount: '',
  description: '',
  images: []
})

const maxRefundAmount = computed(() => order.value?.payAmount || 0)

const rules = {
  asType: [{ required: true, message: '请选择售后类型', trigger: 'change' }],
  reason: [{ required: true, message: '请选择申请原因', trigger: 'change' }],
  refundAmount: [
    { required: true, message: '请输入退款金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '退款金额必须大于0', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value > maxRefundAmount.value) {
        callback(new Error(`退款金额不能超过 ${maxRefundAmount.value}`))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ],
  description: [{ required: true, message: '请描述问题情况', trigger: 'blur' }]
}

onMounted(() => {
  loadOrderInfo()
})

const loadOrderInfo = async () => {
  try {
    const res = await orderAPI.getDetail(orderId)
    order.value = res.data
    form.refundAmount = order.value?.payAmount?.toString() || ''
  } catch (e) {
    console.error(e)
  }
}

const handleUploadSuccess = (response, file) => {
  if (response.code === 200) {
    form.images.push(response.data)
    ElMessage.success('图片上传成功')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    await ElMessageBox.confirm('确认提交售后申请？', '提示', { type: 'info' })
    await afterSalesAPI.apply({
      orderId,
      asType: form.asType,
      reason: form.reason,
      refundAmount: Number(form.refundAmount),
      description: form.description || form.reason,
      images: form.images.join(',')
    })
    ElMessage.success('申请提交成功')
    router.push('/orders')
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('提交失败')
    }
  }
}

const goBack = () => router.push('/orders')
</script>

<style scoped>
.page-container { padding: 20px; }
.form-card { max-width: 600px; margin: 0 auto; }
.input-tip { margin-left: 10px; color: #999; font-size: 12px; }
.upload-demo { margin-top: 10px; }
</style>