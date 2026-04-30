<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">
        <el-icon><ShoppingCart /></el-icon>
        <span>超市管理后台</span>
      </div>

      <div class="nav-wrapper">
        <el-menu :default-active="$route.path" router mode="horizontal" class="top-menu">
          <template v-for="menu in currentMenus" :key="menu.key">
            <el-menu-item v-if="!menu.children" :index="menu.path" class="menu-item">
              <el-icon size="18"><component :is="getIcon(menu.icon)" /></el-icon>
              <span>{{ menu.label }}</span>
            </el-menu-item>
            <el-dropdown v-else trigger="hover" class="dropdown-item">
              <span class="dropdown-text">
                <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
                <span>{{ menu.label }}</span>
                <el-icon class="arrow-icon"><ChevronDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="child in menu.children"
                    :key="child.key"
                    @click="navigate(child.path)"
                  >
                    {{ child.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-menu>
      </div>

      <div class="user-info">
        <el-button icon="User" text class="user-btn">{{ userStore.userInfo?.realName || '管理员' }}</el-button>
        <el-button icon="LogOut" text @click="handleLogout">退出</el-button>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMenuByRole } from '@/config/menuConfig'
import {
  ShoppingCart, DataAnalysis, Goods, Collection, List, User, Box, Van,
  Discount, Money, Setting, ArrowDown as ElArrowDown, ChevronDown
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const iconMap = {
  DataAnalysis, Goods, Collection, List, User, Box, Van, Discount, Money, Setting
}

const getIcon = (iconName) => iconMap[iconName] || Box

const currentMenus = computed(() => {
  const role = userStore.userInfo?.role || 'admin'
  return getMenuByRole(role)
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const navigate = (path) => {
  router.push(path)
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.header {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: white;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  margin-right: 20px;
}
.nav-wrapper {
  flex: 1;
  overflow: hidden;
}
.top-menu {
  background: transparent;
  border-bottom: none;
  display: flex;
  gap: 4px;
}
.menu-item {
  padding: 0 16px;
  height: 56px;
  line-height: 56px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  transition: all 0.3s ease;
}
.menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}
.menu-item.is-active {
  background: rgba(64, 158, 255, 0.3);
  color: #409eff;
  border-bottom: 2px solid #409eff;
}
.dropdown-item {
  padding: 0 12px;
  height: 56px;
  line-height: 56px;
}
.dropdown-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  cursor: pointer;
}
.dropdown-text:hover {
  color: white;
}
.arrow-icon {
  font-size: 12px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-left: 15px;
  border-left: 1px solid rgba(255, 255, 255, 0.2);
}
.user-btn {
  color: rgba(255, 255, 255, 0.9);
}
.main-content {
  background: #f5f7fa;
  padding: 24px;
  flex: 1;
  overflow-y: auto;
}
</style>
