<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">超市管理后台</div>
      <div class="user-info">
        <span>管理员</span>
        <el-button text @click="handleLogout">退出</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" class="sidebar">
        <el-menu :default-active="$route.path" router>
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>Dashboard</span>
          </el-menu-item>
          <el-menu-item index="/admin/products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Collection /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/inventory">
            <el-icon><Box /></el-icon>
            <span>库存管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/deliveries">
            <el-icon><Van /></el-icon>
            <span>配送管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/promotions">
            <el-icon><Discount /></el-icon>
            <span>促销管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/finance">
            <el-icon><Money /></el-icon>
            <span>财务报表</span>
          </el-menu-item>

          <!-- 仓储进货端 -->
          <el-sub-menu index="warehouse">
            <template #title>
              <el-icon><Box /></el-icon>
              <span>仓储进货</span>
            </template>
            <el-menu-item index="/admin/suppliers">供应商管理</el-menu-item>
            <el-menu-item index="/admin/purchase-orders">采购管理</el-menu-item>
            <el-menu-item index="/admin/stocktake">库存盘点</el-menu-item>
            <el-menu-item index="/admin/damage-records">报损记录</el-menu-item>
          </el-sub-menu>

          <!-- 系统管理 -->
          <el-sub-menu index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/admin/admins">管理员账号</el-menu-item>
            <el-menu-item index="/admin/brands">品牌管理</el-menu-item>
            <el-menu-item index="/admin/banners">轮播图管理</el-menu-item>
            <el-menu-item index="/admin/coupons-manage">优惠券管理</el-menu-item>
            <el-menu-item index="/admin/audit-log">审计日志</el-menu-item>
            <el-menu-item index="/admin/couriers">配送员管理</el-menu-item>
          </el-sub-menu>

          <!-- 运营管理 -->
          <el-sub-menu index="operation">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span>运营管理</span>
            </template>
            <el-menu-item index="/admin/seckill">秒杀活动</el-menu-item>
            <el-menu-item index="/admin/after-sales">售后管理</el-menu-item>
            <el-menu-item index="/admin/reviews">评价管理</el-menu-item>
          </el-sub-menu>
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
import { Setting } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
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
  background: #304156;
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
  background: #304156;
}
.main-content {
  background: #f0f2f5;
  padding: 20px;
}
</style>
