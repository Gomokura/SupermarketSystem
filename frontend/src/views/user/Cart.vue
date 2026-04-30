<template>
  <div class="cart-wrap">
    <div v-if="loading" style="text-align:center;padding:60px">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <el-empty v-else-if="cartItems.length === 0"
      description="购物车空空如也" :image-size="100" style="padding:60px 0">
      <el-button type="primary" @click="$router.push('/products')">去选购</el-button>
    </el-empty>

    <template v-else>
      <!-- 全选栏 -->
      <div class="select-all-bar">
        <el-checkbox v-model="isAllSelected" @change="handleSelectAll">全选</el-checkbox>
        <el-button type="danger" link size="small" @click="batchDeleteSelected" :disabled="selectedItems.length === 0">
          删除选中({{ selectedItems.length }})
        </el-button>
      </div>

      <!-- 商品列表 -->
      <div class="item-list">
        <div
          class="cart-item"
          v-for="item in cartItems"
          :key="item.cartId"
          :class="{ disabled: !canSelect(item) }"
        >
          <el-checkbox
            :model-value="selectedIds.has(item.cartId)"
            @change="(v) => toggleSelect(item, v)"
            :disabled="!canSelect(item)"
          />
          <img
            :src="item.coverImage || `https://picsum.photos/seed/p${item.productId}/120/120`"
            class="item-img"
            @click="$router.push(`/products/${item.productId}`)"
            @error="e => e.target.src = `https://picsum.photos/seed/p${item.productId}/120/120`"
          />
          <div class="item-body">
            <div class="item-name" @click="$router.push(`/products/${item.productId}`)">
              {{ item.productName }}
              <el-tag v-if="item.skuName" type="info" size="small" style="margin-left:4px">{{ item.skuName }}</el-tag>
            </div>
            <div v-if="!canSelect(item)" class="item-warn">
              {{ item.productStatus !== 'active' ? '商品已下架' : '库存不足' }}
            </div>
            <div class="item-foot">
              <span class="item-price">￥{{ item.price }}</span>
              <el-input-number
                v-model="item.quantity"
                :min="1" :max="item.stock || 999"
                size="small"
                :disabled="!canSelect(item)"
                @change="updateQuantity(item)"
                style="width:100px"
              />
            </div>
          </div>
          <el-button link type="danger" @click="removeItem(item.cartId)" class="delete-btn">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </template>

    <!-- 底部结算栏 -->
    <div class="checkout-bar" v-if="cartItems.length > 0">
      <div class="bar-info">
        <span>已选 <strong>{{ totalCount }}</strong> 件</span>
        <span class="bar-total">合计: <strong class="bar-price">￥{{ totalPrice.toFixed(2) }}</strong></span>
      </div>
      <el-button
        type="primary"
        size="large"
        :disabled="selectedItems.length === 0"
        @click="goCheckout"
        style="border-radius:20px;min-width:100px"
      >去结算</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Delete } from '@element-plus/icons-vue'
import { cartAPI } from '@/api'

const router = useRouter()
const cartItems = ref([])
const loading = ref(false)
const selectedIds = ref(new Set())

const selectedItems = computed(() => cartItems.value.filter(i => selectedIds.value.has(i.cartId)))
const isAllSelected = computed({
  get: () => {
    const selectable = cartItems.value.filter(canSelect)
    return selectable.length > 0 && selectable.every(i => selectedIds.value.has(i.cartId))
  },
  set: () => {}
})

const totalCount = computed(() => selectedItems.value.reduce((s, i) => s + i.quantity, 0))
const totalPrice = computed(() => selectedItems.value.reduce((s, i) => s + Math.round(Number(i.price) * 100) * i.quantity, 0) / 100)

onMounted(loadCart)

const loadCart = async () => {
  loading.value = true
  try {
    const res = await cartAPI.getList()
    cartItems.value = res.data || []
    // 默认选中可购买项
    selectedIds.value = new Set(cartItems.value.filter(canSelect).map(i => i.cartId))
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const canSelect = (row) => row.productStatus === 'active' && row.stock > 0

const toggleSelect = (item, v) => {
  const s = new Set(selectedIds.value)
  if (v) s.add(item.cartId)
  else s.delete(item.cartId)
  selectedIds.value = s
}

const handleSelectAll = (v) => {
  if (v) selectedIds.value = new Set(cartItems.value.filter(canSelect).map(i => i.cartId))
  else selectedIds.value = new Set()
}

const updateQuantity = async (item) => {
  try {
    await cartAPI.updateQuantity(item.cartId, item.quantity)
  } catch (e) { console.error(e); loadCart() }
}

const removeItem = async (cartId) => {
  try {
    await ElMessageBox.confirm('确定删除该商品？', { type: 'warning' })
    await cartAPI.remove(cartId)
    ElMessage.success('已删除')
    loadCart()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const batchDeleteSelected = async () => {
  if (selectedItems.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedItems.value.length} 件？`, { type: 'warning' })
    await cartAPI.batchDelete(selectedItems.value.map(i => i.cartId))
    ElMessage.success('删除成功')
    loadCart()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const goCheckout = () => {
  if (selectedItems.value.length === 0) { ElMessage.warning('请选择商品'); return }
  router.push(`/checkout?cartIds=${selectedItems.value.map(i => i.cartId).join(',')}`)
}
</script>

<style scoped>
.cart-wrap { background: #f5f5f5; min-height: 100%; padding-bottom: 72px; }

.select-all-bar {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; padding: 10px 14px; border-bottom: 1px solid #f0f0f0; position: sticky; top: 0; z-index: 1;
}

.item-list { padding: 10px; display: flex; flex-direction: column; gap: 10px; }

.cart-item {
  background: #fff; border-radius: 10px;
  display: flex; align-items: center; gap: 10px;
  padding: 12px; position: relative;
}
.cart-item.disabled { opacity: 0.5; }

.item-img { width: 72px; height: 72px; border-radius: 8px; object-fit: cover; cursor: pointer; flex-shrink: 0; }

.item-body { flex: 1; min-width: 0; }
.item-name { font-size: 13px; color: #333; margin-bottom: 4px; cursor: pointer;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-warn { font-size: 11px; color: #ff4d4f; margin-bottom: 4px; }
.item-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 6px; }
.item-price { color: #ff4d4f; font-weight: bold; font-size: 16px; }

.delete-btn { flex-shrink: 0; padding: 4px; }

.checkout-bar {
  position: fixed; bottom: 56px; left: 0; right: 0;
  background: #fff; border-top: 1px solid #f0f0f0;
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 14px; z-index: 100;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.06);
}
.bar-info { display: flex; flex-direction: column; gap: 2px; font-size: 12px; color: #666; }
.bar-price { color: #ff4d4f; font-size: 18px; }
</style>
