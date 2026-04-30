<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed }">
      <div class="sidebar-header">
        <div class="sidebar-logo">
          <el-icon class="logo-icon"><ShoppingCart /></el-icon>
          <span class="logo-text" v-show="!collapsed">超市管理后台</span>
        </div>
        <el-icon class="collapse-btn" @click="collapsed = !collapsed">
          <component :is="collapsed ? Expand : Fold" />
        </el-icon>
      </div>

      <el-scrollbar class="sidebar-scroll">
        <el-menu
          :default-active="$route.path"
          router
          :collapse="collapsed"
          :collapse-transition="false"
          background-color="#001529"
          text-color="rgba(255,255,255,0.65)"
          active-text-color="#fff"
          class="sidebar-menu"
        >
          <template v-for="menu in currentMenus" :key="menu.key">
            <!-- 有子菜单 -->
            <el-sub-menu v-if="menu.children" :index="menu.key">
              <template #title>
                <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
                <span>{{ menu.label }}</span>
              </template>
              <el-menu-item
                v-for="child in menu.children"
                :key="child.key"
                :index="child.path"
              >{{ child.label }}</el-menu-item>
            </el-sub-menu>
            <!-- 无子菜单 -->
            <el-menu-item v-else :index="menu.path">
              <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
              <template #title>{{ menu.label }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </aside>

    <!-- 右侧主体 -->
    <div class="main-wrapper">
      <!-- 顶部栏 -->
      <header class="topbar">
        <div class="breadcrumb-area">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentPageTitle">{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="topbar-right">
          <el-tooltip content="数据看板" placement="bottom">
            <el-icon class="topbar-icon" @click="$router.push('/admin')"><DataAnalysis /></el-icon>
          </el-tooltip>
          <el-tooltip content="收银台" placement="bottom">
            <el-icon class="topbar-icon" @click="$router.push('/pos')"><Money /></el-icon>
          </el-tooltip>
          <el-divider direction="vertical" />
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-info">
              <el-avatar :size="30" style="background:#1677ff;font-size:13px">
                {{ (userStore.userInfo?.realName || userStore.userInfo?.username || 'A').charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="pos">
                  <el-icon><Money /></el-icon> 前往收银台
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span style="color:#ff4d4f">退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="page-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMenuByRole, flattenMenu } from '@/config/menuConfig'
import {
  ShoppingCart, DataAnalysis, Goods, Collection, List, User, Box, Van,
  Discount, Money, Setting, Fold, Expand, ArrowDown, SwitchButton
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const collapsed = ref(false)

const iconMap = {
  DataAnalysis, Goods, Collection, List, User, Box, Van, Discount, Money, Setting
}
const getIcon = (name) => iconMap[name] || Box

const currentMenus = computed(() => {
  const role = userStore.userInfo?.role || 'admin'
  return getMenuByRole(role)
})

const currentPageTitle = computed(() => {
  const allMenus = flattenMenu(currentMenus.value)
  const found = allMenus.find(m => m.path === route.path)
  return found?.label || ''
})

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (cmd === 'pos') {
    router.push('/pos')
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #f0f2f5;
}

/* 侧边栏 */
.sidebar {
  width: 220px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #001529;
  transition: width 0.25s ease;
  overflow: hidden;
}
.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255,255,255,0.07);
  flex-shrink: 0;
}
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}
.logo-icon {
  color: #1677ff;
  font-size: 22px;
  flex-shrink: 0;
}
.logo-text {
  color: #fff;
  font-size: 15px;
  font-weight: bold;
  white-space: nowrap;
}
.collapse-btn {
  color: rgba(255,255,255,0.45);
  font-size: 16px;
  cursor: pointer;
  flex-shrink: 0;
  transition: color 0.2s;
}
.collapse-btn:hover { color: #fff; }

.sidebar-scroll { flex: 1; }
.sidebar-menu { border-right: none; }
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #1677ff !important;
  color: #fff !important;
}
.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255,255,255,0.08) !important;
  color: #fff !important;
}
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255,255,255,0.08) !important;
  color: #fff !important;
}
.sidebar-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #1677ff !important;
}

/* 右侧主体 */
.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶部栏 */
.topbar {
  height: 56px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  flex-shrink: 0;
}
.breadcrumb-area { flex: 1; }
.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.topbar-icon {
  font-size: 18px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}
.topbar-icon:hover { color: #1677ff; }

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}
.user-info:hover { background: #f5f5f5; }
.username { font-size: 14px; color: #333; }

/* 页面内容 */
.page-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #f0f2f5;
}
</style>
