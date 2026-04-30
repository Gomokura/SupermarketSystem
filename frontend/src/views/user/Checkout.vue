<template>
  <div class="page-container">
    <h2>确认订单</h2>

    <!-- 收货地址 -->
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span>收货地址</span>
          <el-button type="primary" size="small" @click="showAddressDialog = true">添加地址</el-button>
        </div>
      </template>
      <el-radio-group v-model="selectedAddressId" v-if="addresses.length">
        <el-radio v-for="addr in addresses" :key="addr.addressId" :value="addr.addressId" class="address-item">
          <div class="address-info">
            <span class="addr-name">{{ addr.receiverName }}</span>
            <span class="addr-phone">{{ addr.phone }}</span>
            <span class="addr-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail }}</span>
            <el-tag v-if="addr.isDefault === 1" size="small" type="success">默认</el-tag>
          </div>
        </el-radio>
      </el-radio-group>
      <el-empty v-else description="暂无收货地址，请先添加" />
    </el-card>

    <!-- 商品清单 -->
    <el-card class="section-card">
      <template #header><span>商品清单</span></template>
      <el-table :data="cartItems" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column label="规格" width="120">
          <template #default="{ row }">{{ row.skuName || '-' }}</template>
        </el-table-column>
        <el-table-column label="单价" width="100">
          <template #default="{ row }">￥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="110">
          <template #default="{ row }">
            <span class="subtotal">￥{{ toYuan(toCents(row.price) * row.quantity) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 优惠与配送 -->
    <el-card class="section-card">
      <template #header><span>优惠与配送</span></template>
      <el-form label-width="100px">
        <!-- 优惠券 -->
        <el-form-item label="优惠券">
          <el-select v-model="selectedCouponId" placeholder="选择优惠券" clearable style="width: 300px" @change="calcPrice">
            <el-option :value="null" label="不使用优惠券" />
            <el-option
              v-for="c in availableCoupons"
              :key="c.userCouponId"
              :value="c.userCouponId"
              :label="couponLabel(c)"
            />
          </el-select>
        </el-form-item>

        <!-- 积分抵扣 -->
        <el-form-item label="积分抵扣">
          <el-checkbox v-model="usePoints" @change="calcPrice">
            使用积分（当前 {{ userPoints }} 分，最多抵扣 ￥{{ maxPointsDeduction.toFixed(2) }}）
          </el-checkbox>
        </el-form-item>

        <!-- 期望配送时间 -->
        <el-form-item label="配送时间">
          <el-select v-model="deliveryDate" style="width: 120px; margin-right: 8px">
            <el-option label="今日" value="today" />
            <el-option label="明日" value="tomorrow" />
          </el-select>
          <el-select v-model="deliveryTime" style="width: 120px">
            <el-option label="上午 9-12点" value="morning" />
            <el-option label="下午 14-18点" value="afternoon" />
            <el-option label="晚上 19-21点" value="evening" />
          </el-select>
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注">
          <el-input v-model="remark" type="textarea" :rows="2" placeholder="选填，对本次订单的备注" style="width: 400px" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 支付方式 -->
    <el-card class="section-card">
      <template #header><span>支付方式</span></template>
      <el-radio-group v-model="paymentMethod">
        <el-radio value="wechat">微信支付</el-radio>
        <el-radio value="alipay">支付宝</el-radio>
        <el-radio value="bank">银行卡</el-radio>
        <el-radio value="cod">货到付款</el-radio>
      </el-radio-group>
    </el-card>

    <!-- 价格明细 + 提交 -->
    <div class="order-summary">
      <div class="price-detail">
        <div class="price-row">
          <span>商品总价：</span>
          <span>￥{{ productTotal.toFixed(2) }}</span>
        </div>
        <div class="price-row" v-if="couponDiscount > 0">
          <span>优惠券减免：</span>
          <span class="discount">-￥{{ couponDiscount.toFixed(2) }}</span>
        </div>
        <div class="price-row" v-if="pointsDeduction > 0">
          <span>积分抵扣：</span>
          <span class="discount">-￥{{ pointsDeduction.toFixed(2) }}</span>
        </div>
        <div class="price-row total-row">
          <span>实付金额：</span>
          <span class="total-price">￥{{ actualAmount.toFixed(2) }}</span>
        </div>
      </div>
      <el-button type="primary" size="large" @click="submitOrder" :loading="submitting">提交订单</el-button>
    </div>

    <!-- 添加地址弹窗 -->
    <el-dialog v-model="showAddressDialog" title="添加收货地址" width="560px">
      <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="90px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区" required>
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="addressForm.province" placeholder="省" @change="onProvinceChange" style="width: 100%">
                <el-option v-for="p in provinceList" :key="p" :label="p" :value="p" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="addressForm.city" placeholder="市" @change="onCityChange" :disabled="!addressForm.province" style="width: 100%">
                <el-option v-for="c in cityList" :key="c" :label="c" :value="c" />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="addressForm.district" placeholder="区/县" :disabled="!addressForm.city" style="width: 100%">
                <el-option v-for="d in districtList" :key="d" :label="d" :value="d" />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" type="textarea" :rows="2" placeholder="请输入详细地址（街道、门牌号等）" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
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
import { cartAPI, addressAPI, orderAPI, couponAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const cartItems = ref([])
const addresses = ref([])
const availableCoupons = ref([])
const selectedAddressId = ref(null)
const selectedCouponId = ref(null)
const usePoints = ref(false)
const paymentMethod = ref('wechat')
const remark = ref('')
const deliveryDate = ref('today')
const deliveryTime = ref('morning')
const showAddressDialog = ref(false)
const submitting = ref(false)
const addressFormRef = ref()

const addressForm = reactive({ receiverName: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: 0 })
const addressRules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区/县', trigger: 'change' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

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
  addressForm.city = ''
  addressForm.district = ''
  districtList.value = []
  if (addressForm.province) {
    cityList.value = Object.keys(regionData[addressForm.province] || {}).sort()
  } else {
    cityList.value = []
  }
}

const onCityChange = () => {
  addressForm.district = ''
  if (addressForm.province && addressForm.city) {
    districtList.value = regionData[addressForm.province]?.[addressForm.city] || []
  } else {
    districtList.value = []
  }
}

const toCents = (price) => Math.round(Number(price) * 100)
const toYuan = (cents) => (Number(cents) / 100).toFixed(2)

const userPoints = computed(() => userStore.userInfo?.points || 0)
const productTotal = computed(() => {
  const totalCents = cartItems.value.reduce((s, i) => s + toCents(i.price) * i.quantity, 0)
  return totalCents / 100
})
const maxPointsDeduction = computed(() => {
  const maxByPoints = userPoints.value
  const maxByTotal = Math.round(productTotal.value * 0.3 * 100)
  return Math.min(maxByPoints, maxByTotal) / 100
})
const pointsDeduction = computed(() => usePoints.value ? maxPointsDeduction.value : 0)

const couponDiscount = computed(() => {
  if (!selectedCouponId.value) return 0
  const c = availableCoupons.value.find(x => x.userCouponId === selectedCouponId.value)
  if (!c) return 0
  if (c.couponType === 'full_reduction') return c.discountValue
  if (c.couponType === 'discount') {
    const discountCents = Math.round(productTotal.value * (1 - c.discountValue / 10) * 100)
    return discountCents / 100
  }
  return 0
})

const actualAmount = computed(() => {
  const totalCents = toCents(productTotal.value) - toCents(couponDiscount.value) - toCents(pointsDeduction.value)
  return Math.max(0, totalCents / 100)
})

onMounted(() => {
  loadCart()
  loadAddresses()
})

const loadCart = async () => {
  try {
    const res = await cartAPI.getList()
    cartItems.value = (res.data || []).filter(i => i.selected !== false)
    await loadCoupons()
  } catch (e) { console.error(e) }
}

const loadAddresses = async () => {
  try {
    const res = await addressAPI.getList()
    addresses.value = res.data || []
    const def = addresses.value.find(a => a.isDefault === 1)
    selectedAddressId.value = def?.addressId || addresses.value[0]?.addressId || null
  } catch (e) { console.error(e) }
}

const loadCoupons = async () => {
  if (productTotal.value <= 0) return
  try {
    const res = await couponAPI.getAvailable(productTotal.value)
    availableCoupons.value = res.data || []
    selectBestCoupon()
  } catch (e) { console.error(e) }
}

const selectBestCoupon = () => {
  if (availableCoupons.value.length === 0) {
    selectedCouponId.value = null
    return
  }
  
  let bestCoupon = null
  let maxDiscount = 0
  
  for (const c of availableCoupons.value) {
    let discount = 0
    if (c.couponType === 'full_reduction') {
      discount = c.discountValue
    } else if (c.couponType === 'discount') {
      discount = productTotal.value * (1 - c.discountValue / 10)
    }
    
    if (discount > maxDiscount) {
      maxDiscount = discount
      bestCoupon = c
    }
  }
  
  if (bestCoupon && maxDiscount > 0) {
    selectedCouponId.value = bestCoupon.userCouponId
  }
}

const calcPrice = async () => {
  if (productTotal.value <= 0) return
  try {
    const res = await couponAPI.getAvailable(productTotal.value)
    availableCoupons.value = res.data || []
    selectBestCoupon()
  } catch (e) { console.error(e) }
}

const couponLabel = (c) => {
  if (c.couponType === 'full_reduction') return `满${c.minOrderAmount}减${c.discountValue}元`
  if (c.couponType === 'discount') return `${c.discountValue}折优惠券`
  return c.couponName
}

const addAddress = async () => {
  await addressFormRef.value.validate()
  try {
    await addressAPI.add(addressForm)
    ElMessage.success('添加成功')
    showAddressDialog.value = false
    loadAddresses()
  } catch (e) { console.error(e) }
}

const submitOrder = async () => {
    if (!selectedAddressId.value) { ElMessage.warning('请选择收货地址'); return }
    if (cartItems.value.length === 0) { ElMessage.warning('购物车为空'); return }

    submitting.value = true
    try {
      const deliveryTimeStr = `${deliveryDate.value === 'today' ? '今日' : '明日'} ${
        { morning: '上午9-12点', afternoon: '下午14-18点', evening: '晚上19-21点' }[deliveryTime.value]
      }`
      const orderData = {
        addressId: selectedAddressId.value,
        paymentMethod: paymentMethod.value,
        couponId: selectedCouponId.value || undefined,
        remark: remark.value || undefined,
        deliveryTimeSlot: deliveryTimeStr,
        items: cartItems.value.map(i => ({ productId: i.productId, skuId: i.skuId, quantity: i.quantity }))
      }
      if (usePoints.value) {
        orderData.pointsUsed = Math.floor(maxPointsDeduction.value * 100)
      }
      await orderAPI.create(orderData)
      
      await cartAPI.clear()
      
      ElMessage.success('订单提交成功')
      router.push('/orders')
    } catch (e) {
      console.error(e)
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped>
.section-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.address-item { display: block; margin-bottom: 12px; }
.address-info { display: flex; gap: 12px; align-items: center; }
.addr-name { font-weight: 500; }
.addr-phone { color: #666; }
.addr-detail { color: #333; }
.subtotal { color: #f56c6c; font-weight: bold; }
.order-summary { display: flex; justify-content: flex-end; align-items: flex-end; gap: 30px; padding: 20px; background: #fafafa; border-radius: 8px; }
.price-detail { text-align: right; }
.price-row { display: flex; justify-content: space-between; gap: 40px; margin-bottom: 6px; font-size: 14px; }
.discount { color: #67c23a; }
.total-row { font-size: 16px; font-weight: bold; border-top: 1px solid #eee; padding-top: 8px; margin-top: 4px; }
.total-price { color: #f56c6c; font-size: 22px; }
</style>
