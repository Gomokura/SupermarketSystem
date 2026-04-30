<template>
  <div class="page-container">
    <div class="page-header">
      <h2>收货地址</h2>
      <el-button type="primary" @click="openDialog(false)" :disabled="addresses.length >= 10">
        {{ addresses.length >= 10 ? '已达上限(10条)' : '添加地址' }}
      </el-button>
    </div>

    <el-table :data="addresses" border style="width: 100%; margin-top: 20px" v-loading="loading">
      <el-table-column prop="receiverName" label="收货人" width="120" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column label="所在地区" min-width="200">
        <template #default="{ row }">
          {{ row.province || '' }}{{ row.city || '' }}{{ row.district || '' }}
        </template>
      </el-table-column>
      <el-table-column prop="detail" label="详细地址" min-width="250" />
      <el-table-column label="默认" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isDefault === 1" type="success">默认</el-tag>
          <el-link v-else type="primary" :underline="false" @click="setDefault(row)">设为默认</el-link>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(true, row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteAddress(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑地址' : '添加地址'" width="560px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="请输入收货人" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区" required>
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="form.province" placeholder="省" @change="onProvinceChange" style="width: 100%">
                <el-option v-for="p in provinceList" :key="p" :label="p" :value="p" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="form.city" placeholder="市" @change="onCityChange" :disabled="!form.province" style="width: 100%">
                <el-option v-for="c in cityList" :key="c" :label="c" :value="c" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="form.district" placeholder="区/县" :disabled="!form.city" style="width: 100%">
                <el-option v-for="d in districtList" :key="d" :label="d" :value="d" />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="请输入详细地址（街道、门牌号等）" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" :true-value="1" :false-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveAddress">确定</el-button>
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
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)

const formRef = ref()

const form = reactive({
  addressId: null,
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0
})

const rules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

// 省市区静态数据（吉林省）
const regionData = {
  '吉林省': {
    '长春市': ['南关区', '宽城区', '朝阳区', '二道区', '绿园区', '双阳区', '九台区', '农安县', '德惠市', '榆树市'],
    '吉林市': ['昌邑区', '龙潭区', '船营区', '丰满区', '永吉县', '蛟河市', '桦甸市', '舒兰市', '磐石市']
  }
}

const provinceList = Object.keys(regionData).sort()
const cityList = ref([])
const districtList = ref([])

const onProvinceChange = () => {
  form.city = ''
  form.district = ''
  districtList.value = []
  if (form.province) {
    const cities = regionData[form.province] || {}
    cityList.value = Object.keys(cities).sort()
  } else {
    cityList.value = []
  }
}

const onCityChange = () => {
  form.district = ''
  if (form.province && form.city) {
    const cities = regionData[form.province] || {}
    districtList.value = cities[form.city] || []
  } else {
    districtList.value = []
  }
}

const resetForm = () => {
  Object.assign(form, { addressId: null, receiverName: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: 0 })
  cityList.value = []
  districtList.value = []
}

const openDialog = (edit, row = null) => {
  if (edit && row) {
    isEdit.value = true
    Object.assign(form, row)
    if (form.province) {
      const savedCity = form.city
      const savedDistrict = form.district
      onProvinceChange()
      form.city = savedCity
      if (form.city) {
        onCityChange()
        form.district = savedDistrict
      }
    }
  } else {
    isEdit.value = false
    resetForm()
  }
  dialogVisible.value = true
}

onMounted(() => {
  loadAddresses()
})

const loadAddresses = async () => {
  loading.value = true
  try {
    const res = await addressAPI.getList()
    addresses.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const setDefault = async (row) => {
  try {
    await addressAPI.update({ ...row, isDefault: 1 })
    ElMessage.success('已设为默认地址')
    loadAddresses()
  } catch (error) {
    console.error(error)
  }
}

const saveAddress = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const data = { ...form, userId: userStore.userInfo.userId }
    if (isEdit.value) {
      await addressAPI.update(data)
    } else {
      await addressAPI.add(data)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadAddresses()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const deleteAddress = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', { type: 'warning' })
    await addressAPI.delete(row.addressId)
    ElMessage.success('删除成功')
    loadAddresses()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-header h2 {
  margin: 0;
}
</style>
