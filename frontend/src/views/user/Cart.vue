<template>
  <div class="page-container">
    <h2>购物车</h2>
    <el-table :data="cartItems" border style="width: 100%" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" :selectable="canSelect" />
      <el-table-column label="商品名称" min-width="200">
        <template #default="{ row }">
          <div :class="{ 'disabled-text': !canSelect(row) }">
            {{ row.productName }}
            <el-tag v-if="row.productStatus !== 1" type="info" size="small" class="ml-2">已下架</el-tag>
            <el-tag v-else-if="row.stock <= 0" type="danger" size="small" class="ml-2">缺货</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="单价" width="120">
        <template #default="{ row }">
          <span :class="{ 'disabled-text': !canSelect(row) }">￥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column label="数量" width="180">
        <template #default="{ row }">
          <el-input-number v-model="row.quantity" :min="1" :max="row.stock || 1000" size="small" @change="updateQuantity(row)" :disabled="!canSelect(row)" />
        </template>
      </el-table-column>
      <el-table-column label="小计" width="120">
        <template #default="{ row }">
          <span class="subtotal">￥{{ row.price != null ? (row.price * row.quantity).toFixed(2) : '价格异常' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="removeItem(row.cartId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="cart-summary">
      <div class="total-info">
        <span>共 {{ totalCount }} 件商品</span>
        <span class="total-price">总计: ￥{{ totalPrice.toFixed(2) }}</span>
      </div>
      <div class="actions">
        <el-button @click="clearCart">清空购物车</el-button>
        <el-button type="primary" size="large" @click="goCheckout" :disabled="selectedItems.length === 0">去结算</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cartAPI } from '@/api'

const router = useRouter()
const cartItems = ref([])
const selectedItems = ref([])

const totalCount = computed(() => selectedItems.value.reduce((sum, item) => sum + item.quantity, 0))
const totalPrice = computed(() => selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0))

onMounted(() => {
  loadCart()
})

const loadCart = async () => {
  try {
    const res = await cartAPI.getList()
    cartItems.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const updateQuantity = async (item) => {
  try {
    await cartAPI.updateQuantity(item.cartId, item.quantity)
  } catch (error) {
    console.error(error)
    loadCart()
  }
}

const canSelect = (row) => {
  // 根据业务逻辑判断，若有具体状态字段请替换
  return row.productStatus === 1 && row.stock > 0
}

const handleSelectionChange = (val) => {
  selectedItems.value = val
}

const removeItem = async (cartId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', { type: 'warning' })
    await cartAPI.remove(cartId)
    ElMessage.success('删除成功')
    loadCart()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const clearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', { type: 'warning' })
    await cartAPI.clear()
    ElMessage.success('清空成功')
    loadCart()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const goCheckout = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择商品')
    return
  }
  const ids = selectedItems.value.map(item => item.cartId).join(',')
  router.push(`/checkout?cartIds=${ids}`)
}
</script>

<style scoped>
.cart-summary {
  margin-top: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.total-price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.actions {
  display: flex;
  gap: 10px;
}

.subtotal {
  color: #f56c6c;
  font-weight: bold;
}

.disabled-text {
  color: #c0c4cc;
}

.ml-2 {
  margin-left: 8px;
}
</style>
