<template>
  <div class="page-container">
    <h2>确认订单</h2>
    
    <el-card class="address-card">
      <template #header>
        <span>收货地址</span>
        <el-button type="primary" size="small" @click="showAddressDialog = true">添加地址</el-button>
      </template>
      <el-radio-group v-model="selectedAddressId" v-if="addresses.length">
        <el-radio v-for="addr in addresses" :key="addr.addressId" :value="addr.addressId" class="address-item">
          <div class="address-info">
            <span>{{ addr.receiver }}</span>
            <span>{{ addr.phone }}</span>
            <span>{{ addr.detail }}</span>
            <el-tag v-if="addr.isDefault === 1" size="small" type="success">默认</el-tag>
          </div>
        </el-radio>
      </el-radio-group>
      <el-empty v-else description="暂无收货地址" />
    </el-card>

    <el-card class="cart-card">
      <template #header>
        <span>商品清单</span>
      </template>
      <el-table :data="cartItems" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="price" label="单价" width="120">
          <template #default="{ row }">￥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            <span class="subtotal">￥{{ (row.price * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="payment-card">
      <template #header>
        <span>支付方式</span>
      </template>
      <el-radio-group v-model="paymentMethod">
        <el-radio label="微信支付">微信支付</el-radio>
        <el-radio label="支付宝">支付宝</el-radio>
        <el-radio label="银行卡">银行卡</el-radio>
        <el-radio label="货到付款">货到付款</el-radio>
      </el-radio-group>
    </el-card>

    <div class="order-summary">
      <div class="total">
        <span>共 {{ totalCount }} 件商品，合计：</span>
        <span class="total-price">￥{{ totalPrice.toFixed(2) }}</span>
      </div>
      <el-button type="primary" size="large" @click="submitOrder" :loading="submitting">提交订单</el-button>
    </div>

    <el-dialog v-model="showAddressDialog" title="添加收货地址" width="500px">
      <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="80px">
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="addressForm.receiver" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" :true-value="1" :false-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="addAddress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cartAPI, addressAPI, orderAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const cartItems = ref([])
const addresses = ref([])
const selectedAddressId = ref(null)
const paymentMethod = ref('微信支付')
const showAddressDialog = ref(false)
const submitting = ref(false)

const addressForm = reactive({
  receiver: '',
  phone: '',
  detail: '',
  isDefault: 0
})

const addressRules = {
  receiver: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const addressFormRef = ref()

const totalCount = computed(() => cartItems.value.reduce((sum, item) => sum + item.quantity, 0))
const totalPrice = computed(() => cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0))

onMounted(() => {
  loadCart()
  loadAddresses()
})

const loadCart = async () => {
  try {
    const res = await cartAPI.getList()
    cartItems.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const loadAddresses = async () => {
  try {
    const res = await addressAPI.getList()
    addresses.value = res.data || []
    const defaultAddr = addresses.value.find(a => a.isDefault === 1)
    if (defaultAddr) {
      selectedAddressId.value = defaultAddr.addressId
    } else if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].addressId
    }
  } catch (error) {
    console.error(error)
  }
}

const addAddress = async () => {
  await addressFormRef.value.validate()
  try {
    await addressAPI.add({ ...addressForm, userId: userStore.userInfo.userId })
    ElMessage.success('添加成功')
    showAddressDialog.value = false
    loadAddresses()
  } catch (error) {
    console.error(error)
  }
}

const submitOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  
  submitting.value = true
  try {
    const items = cartItems.value.map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }))
    
    await orderAPI.create({
      addressId: selectedAddressId.value,
      paymentMethod: paymentMethod.value,
      items
    })
    
    ElMessage.success('订单提交成功')
    router.push('/orders')
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.address-card, .cart-card, .payment-card {
  margin-bottom: 20px;
}

.address-item {
  display: block;
  margin-bottom: 15px;
}

.address-info {
  display: flex;
  gap: 15px;
  align-items: center;
  padding: 10px;
}

.subtotal {
  color: #f56c6c;
  font-weight: bold;
}

.order-summary {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}

.total {
  font-size: 16px;
}

.total-price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}
</style>
