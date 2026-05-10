<template>
  <div class="mobile-layout">
    <!-- 顶部导航栏 -->
    <header class="mobile-header">
      <div class="header-left">
        <el-button v-if="showBack" class="back-btn" circle text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="logo">🛒 鲜惠超市</span>
      </div>
      <div class="header-center">
        <div class="search-box" @click="$router.push('/products')">
          <el-icon><Search /></el-icon>
          <span>搜索商品...</span>
        </div>
      </div>
      <div class="header-right">
        <el-badge :value="unreadCount || ''" :hidden="!unreadCount" class="msg-badge">
          <el-icon class="header-icon" @click="$router.push('/messages')"><Bell /></el-icon>
        </el-badge>
        <el-avatar :size="30" :src="userStore.userInfo?.avatarUrl" class="avatar"
          @click="$router.push('/profile')">
          {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0) }}
        </el-avatar>
      </div>
    </header>

    <!-- 页面内容 -->
    <main class="mobile-main">
      <router-view />
    </main>

    <!-- 底部 Tab 导航 -->
    <nav class="mobile-tabbar">
      <div
        v-for="tab in tabs"
        :key="tab.path"
        class="tab-item"
        :class="{ active: isActive(tab) }"
        @click="$router.push(tab.path)"
      >
        <el-icon class="tab-icon"><component :is="tab.icon" /></el-icon>
        <span class="tab-label">{{ tab.label }}</span>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { messageAPI } from '@/api'
import {
  ArrowLeft, Search, Bell, HomeFilled, Grid, ShoppingCart, List, UserFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)
const showBack = computed(() => route.path !== '/')

const tabs = [
  { path: '/', label: '首页', icon: HomeFilled, match: ['/'] },
  { path: '/products', label: '分类', icon: Grid, match: ['/products'] },
  { path: '/cart', label: '购物车', icon: ShoppingCart, match: ['/cart'] },
  { path: '/orders', label: '订单', icon: List, match: ['/orders'] },
  { path: '/profile', label: '我的', icon: UserFilled, match: ['/profile', '/address', '/coupons', '/favorites', '/points-logs', '/messages', '/after-sale', '/review', '/apply-after-sale'] },
]

const isActive = (tab) => {
  const p = route.path
  if (tab.path === '/') return p === '/'
  return tab.match.some(m => p === m || p.startsWith(m + '/') || (m !== '/' && p.startsWith(m)))
}

const goBack = () => {
  if (window.history.length > 1) router.back()
  else router.push('/')
}

const loadUnread = async () => {
  // 只有顾客且已有 token 时才请求，避免登录过程中触发 401
  if (userStore.currentRole !== 'customer') return
  if (!localStorage.getItem('token')) return
  try {
    const res = await messageAPI.getUnreadCount()
    unreadCount.value = res.data || 0
  } catch {}
}

onMounted(loadUnread)
watch(() => route.path, loadUnread)
</script>

<style scoped>
.mobile-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f5f5;
}

/* 顶部导航 */
.mobile-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  height: 52px;
  background: #ff4d4f;
  box-shadow: 0 1px 4px rgba(0,0,0,0.15);
}

.header-left { flex-shrink: 0; display: flex; align-items: center; gap: 4px; }
.back-btn {
  width: 30px;
  height: 30px;
  color: #fff;
}
.back-btn:hover {
  background: rgba(255,255,255,0.14);
  color: #fff;
}
.logo { color: #fff; font-size: 17px; font-weight: bold; white-space: nowrap; }

.header-center { flex: 1; min-width: 0; }
.search-box {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255,255,255,0.92);
  border-radius: 20px;
  padding: 5px 12px;
  cursor: pointer;
  color: #aaa;
  font-size: 13px;
}
.search-box .el-icon { font-size: 14px; }

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}
.header-icon {
  color: #fff;
  font-size: 20px;
  cursor: pointer;
}
.msg-badge :deep(.el-badge__content) { font-size: 10px; padding: 0 4px; }
.avatar { cursor: pointer; border: 2px solid rgba(255,255,255,0.7); background: #faad14; color: #fff; font-size: 13px; }

/* 主内容 */
.mobile-main {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 56px;
}

/* 底部 Tab 栏 */
.mobile-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  height: 56px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.07);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  cursor: pointer;
  color: #999;
  transition: color 0.2s;
  -webkit-tap-highlight-color: transparent;
}
.tab-item.active { color: #ff4d4f; }
.tab-icon { font-size: 22px; }
.tab-label { font-size: 11px; line-height: 1; }
</style>
