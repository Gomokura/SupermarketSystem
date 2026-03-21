<template>
  <div class="page-container">
    <h2>收货地址</h2>
    <el-button type="primary" @click="showDialog = true; isEdit = false; form = {}">添加地址</el-button>
    <el-table :data="addresses" border style="width: 100%; margin-top: 20px">
      <el-table-column prop="receiver" label="收货人" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="detail" label="详细地址" />
      <el-table-column label="默认" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isDefault === 1" type="success">默认</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editAddress(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteAddress(row.addressId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑地址' : '添加地址'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="form.receiver" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" :true-value="1" :false-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addressAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const addresses = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const form = reactive({
  receiver: '',
  phone: '',
  detail: '',
  isDefault: 0
})

const rules = {
  receiver: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const formRef = ref()

onMounted(() => {
  loadAddresses()
})

const loadAddresses = async () => {
  try {
    const res = await addressAPI.getList()
    addresses.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const editAddress = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  showDialog.value = true
}

const saveAddress = async () => {
  await formRef.value.validate()
  try {
    const data = { ...form, userId: userStore.userInfo.userId }
    if (isEdit.value) {
      await addressAPI.update(data)
    } else {
      await addressAPI.add(data)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadAddresses()
  } catch (error) {
    console.error(error)
  }
}

const deleteAddress = async (addressId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', { type: 'warning' })
    await addressAPI.delete(addressId)
    ElMessage.success('删除成功')
    loadAddresses()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}
</script>
