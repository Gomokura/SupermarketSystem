<template>
  <el-container class="admin-layout">
    <el-header class="admin-header">
      <div class="brand-block">
        <div class="brand-mark">SM</div>
        <div>
          <div class="brand-title">超市管理后台</div>
          <div class="brand-subtitle">订单 · 商品 · 仓储 · 运营一体化</div>
        </div>
      </div>

      <div class="header-actions">
        <el-tag type="success" effect="dark">管理员端</el-tag>
        <span class="operator-name">{{ displayName }}</span>
        <el-button text @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-container class="admin-body">
      <el-aside width="250px" class="admin-sidebar">
        <el-scrollbar>
          <el-menu
            :default-active="$route.path"
            router
            class="admin-menu"
            background-color="transparent"
            text-color="#b9c5d9"
            active-text-color="#ffffff"
          >
            <el-menu-item index="/admin/dashboard">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据看板</span>
            </el-menu-item>

            <el-sub-menu index="goods">
              <template #title>
                <el-icon><Goods /></el-icon>
                <span>商品中心</span>
              </template>
              <el-menu-item index="/admin/products">商品管理</el-menu-item>
              <el-menu-item index="/admin/categories">分类管理</el-menu-item>
              <el-menu-item index="/admin/brands">品牌管理</el-menu-item>
              <el-menu-item index="/admin/banners">轮播图管理</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="trade">
              <template #title>
                <el-icon><Tickets /></el-icon>
                <span>交易运营</span>
              </template>
              <el-menu-item index="/admin/orders">订单管理</el-menu-item>
              <el-menu-item index="/admin/promotions">促销管理</el-menu-item>
              <el-menu-item index="/admin/coupons-manage">优惠券管理</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="member">
              <template #title>
                <el-icon><User /></el-icon>
                <span>账号与用户</span>
              </template>
              <el-menu-item index="/admin/users">用户管理</el-menu-item>
              <el-menu-item index="/admin/admins">管理员账号</el-menu-item>
              <el-menu-item index="/admin/couriers">配送员管理</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="warehouse">
              <template #title>
                <el-icon><Box /></el-icon>
                <span>仓储进货</span>
              </template>
              <el-menu-item index="/admin/inventory">库存管理</el-menu-item>
              <el-menu-item index="/admin/suppliers">供应商管理</el-menu-item>
              <el-menu-item index="/admin/purchase-orders">采购管理</el-menu-item>
              <el-menu-item index="/admin/stocktake">库存盘点</el-menu-item>
              <el-menu-item index="/admin/damage-records">报损记录</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="service">
              <template #title>
                <el-icon><Van /></el-icon>
                <span>履约与服务</span>
              </template>
              <el-menu-item index="/admin/deliveries">配送管理</el-menu-item>
              <el-menu-item index="/admin/after-sales">售后管理</el-menu-item>
              <el-menu-item index="/admin/reviews">评价管理</el-menu-item>
              <el-menu-item index="/admin/seckill">秒杀活动</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="system">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统与报表</span>
              </template>
              <el-menu-item index="/admin/finance">财务报表</el-menu-item>
              <el-menu-item index="/admin/audit-log">审计日志</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Box,
  DataAnalysis,
  Goods,
  Setting,
  Tickets,
  User,
  Van
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const displayName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '管理员')

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(58, 123, 213, 0.18), transparent 24%),
    radial-gradient(circle at bottom right, rgba(0, 210, 255, 0.14), transparent 22%),
    #f4f7fb;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  color: #fff;
  background: linear-gradient(135deg, #23324a 0%, #2e4568 55%, #39618a 100%);
  box-shadow: 0 10px 30px rgba(35, 50, 74, 0.28);
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  font-weight: 800;
  letter-spacing: 1px;
  color: #23324a;
  background: linear-gradient(135deg, #dff3ff, #8fd3ff);
}

.brand-title {
  font-size: 20px;
  font-weight: 700;
}

.brand-subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.68);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.operator-name {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.88);
}

.admin-body {
  height: calc(100vh - 60px);
}

.admin-sidebar {
  overflow: hidden;
  border-right: 1px solid rgba(56, 81, 121, 0.1);
  background: linear-gradient(180deg, #24344d 0%, #1d2b40 100%);
}

.admin-menu {
  min-height: 100%;
  border-right: none;
  padding: 12px 10px 18px;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 46px;
  margin: 4px 0;
  border-radius: 12px;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(80, 156, 255, 0.92), rgba(62, 198, 255, 0.82));
  box-shadow: 0 10px 20px rgba(34, 96, 180, 0.24);
}

.admin-main {
  padding: 24px;
  overflow: auto;
}
</style>
