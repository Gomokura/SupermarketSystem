<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">超市管理系统</div>
      <div class="user-info">
        <span>欢迎，{{ userStore.userInfo.realName || userStore.userInfo.username }}</span>
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link">
            <el-icon><Avatar /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="address">收货地址</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" class="sidebar">
        <el-menu :default-active="$route.path" router>
          <el-menu-item index="/home">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/products">
            <el-icon><Goods /></el-icon>
            <span>商品浏览</span>
          </el-menu-item>
          <el-menu-item index="/cart">
            <el-icon><ShoppingCart /></el-icon>
            <span>购物车</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><List /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="/address">
            <el-icon><Location /></el-icon>
            <span>收货地址</span>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else {
    router.push('/' + command)
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #409eff;
  color: white;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.sidebar {
  background: #f5f5f5;
}

.main-content {
  background: #fafafa;
  padding: 20px;
}
</style>
