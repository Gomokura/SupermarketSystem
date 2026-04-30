<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-left">
        <div class="logo" @click="$router.push('/home')">🛒 鲜惠超市</div>
        <el-button text class="home-btn" @click="$router.push('/home')">
          <el-icon><House /></el-icon> 回到首页
        </el-button>
      </div>
      <div class="nav-center">
        <el-menu
          mode="horizontal"
          :default-active="$route.path"
          router
          background-color="#409eff"
          text-color="rgba(255,255,255,0.85)"
          active-text-color="#ffffff"
          class="top-nav"
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/products">商品</el-menu-item>
          <el-menu-item index="/seckill">秒杀</el-menu-item>
          <el-menu-item index="/cart">购物车</el-menu-item>
          <el-menu-item index="/orders">我的订单</el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <span class="welcome-text">欢迎，{{ userStore.userInfo?.nickname || userStore.userInfo?.realName || userStore.userInfo?.username || '游客' }}</span>
        <el-dropdown @command="handleCommand" trigger="click">
          <el-button round size="small" class="user-btn">
            <el-icon><Avatar /></el-icon>
            个人中心
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon> 个人信息
              </el-dropdown-item>
              <el-dropdown-item command="address">
                <el-icon><Location /></el-icon> 收货地址
              </el-dropdown-item>
              <el-dropdown-item command="coupons">
                <el-icon><Ticket /></el-icon> 我的优惠券
              </el-dropdown-item>
              <el-dropdown-item command="favorites">
                <el-icon><Star /></el-icon> 我的收藏
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                <span style="color:#f56c6c">退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="danger" size="small" round @click="handleLogout">
          <el-icon><SwitchButton /></el-icon> 退出
        </el-button>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  } else {
    router.push('/' + command)
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    userStore.logout()
    router.push('/login')
  } catch {}
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #409eff;
  color: white;
  padding: 0 16px;
  height: 60px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  white-space: nowrap;
  cursor: pointer;
  color: white;
}

.home-btn {
  color: rgba(255,255,255,0.9) !important;
  font-size: 13px;
}

.nav-center {
  flex: 1;
  overflow: hidden;
}

.top-nav {
  border-bottom: none !important;
  height: 60px;
}

.top-nav :deep(.el-menu-item) {
  height: 60px;
  line-height: 60px;
  font-size: 14px;
}

.top-nav :deep(.el-menu-item.is-active) {
  border-bottom: 2px solid #fff !important;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.welcome-text {
  color: rgba(255,255,255,0.9);
  font-size: 13px;
  white-space: nowrap;
}

.user-btn {
  background: rgba(255,255,255,0.15) !important;
  border-color: rgba(255,255,255,0.4) !important;
  color: white !important;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>
