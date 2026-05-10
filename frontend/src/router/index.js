import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { layout: 'none' }
  },
  // ==========================================
  // 顾客端（Mobile Layout）
  // ==========================================
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/user/Home.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/products',
    name: 'Products',
    component: () => import('@/views/user/Products.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: () => import('@/views/user/ProductDetail.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/user/Cart.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/user/Checkout.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/user/Orders.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: () => import('@/views/user/OrderDetail.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/after-sale',
    name: 'AfterSale',
    component: () => import('@/views/user/AfterSale.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/apply-after-sale/:orderId',
    name: 'ApplyAfterSale',
    component: () => import('@/views/user/ApplyAfterSale.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/review/:orderId',
    name: 'Review',
    component: () => import('@/views/user/Review.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/coupons',
    name: 'Coupons',
    component: () => import('@/views/user/Coupons.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/points-logs',
    name: 'PointsLog',
    component: () => import('@/views/user/PointsLog.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/user/Messages.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/address',
    name: 'Address',
    component: () => import('@/views/user/Address.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/user/Profile.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/seckill',
    name: 'Seckill',
    component: () => import('@/views/user/Seckill.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/user/Favorites.vue'),
    meta: { layout: 'mobile', role: 'customer' }
  },
  // ==========================================
  // 配送员端（Mobile Layout）
  // ==========================================
  {
    path: '/courier',
    component: () => import('@/views/courier/Layout.vue'),
    meta: { layout: 'none', role: 'courier' },
    children: [
      {
        path: '',
        name: 'CourierTasks',
        component: () => import('@/views/courier/Tasks.vue'),
        meta: { role: 'courier' }
      }
    ]
  },
  // ==========================================
  // 管理后台（Admin Layout）
  // ==========================================
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/products',
    name: 'AdminProducts',
    component: () => import('@/views/admin/Products.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/categories',
    name: 'AdminCategories',
    component: () => import('@/views/admin/Categories.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/orders',
    name: 'AdminOrders',
    component: () => import('@/views/admin/Orders.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/Users.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/inventory',
    name: 'AdminInventory',
    component: () => import('@/views/admin/Inventory.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/deliveries',
    name: 'AdminDeliveries',
    component: () => import('@/views/admin/Deliveries.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/promotions',
    name: 'AdminPromotions',
    component: () => import('@/views/admin/Promotions.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/finance',
    name: 'AdminFinance',
    component: () => import('@/views/admin/Finance.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/admins',
    name: 'AdminAdmins',
    component: () => import('@/views/admin/Admins.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/brands',
    name: 'AdminBrands',
    component: () => import('@/views/admin/Brands.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/banners',
    name: 'AdminBanners',
    component: () => import('@/views/admin/Banners.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/after-sales',
    name: 'AdminAfterSales',
    component: () => import('@/views/admin/AfterSales.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/reviews',
    name: 'AdminReviews',
    component: () => import('@/views/admin/Reviews.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/seckill',
    name: 'AdminSeckill',
    component: () => import('@/views/admin/Seckill.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  // ==========================================
  // 仓储进货端（Admin Layout）
  // ==========================================
  {
    path: '/admin/stocktake',
    name: 'AdminStocktake',
    component: () => import('@/views/admin/Stocktake.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/damage-records',
    name: 'AdminDamageRecords',
    component: () => import('@/views/admin/DamageRecords.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/purchase-orders',
    name: 'AdminPurchaseOrders',
    component: () => import('@/views/admin/PurchaseOrders.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/suppliers',
    name: 'AdminSuppliers',
    component: () => import('@/views/admin/Suppliers.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/audit-log',
    name: 'AdminAuditLog',
    component: () => import('@/views/admin/AuditLog.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/couriers',
    name: 'AdminCouriers',
    component: () => import('@/views/admin/Couriers.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  {
    path: '/admin/coupons-manage',
    name: 'AdminCouponsManage',
    component: () => import('@/views/admin/CouponsManage.vue'),
    meta: { layout: 'admin', role: 'admin' }
  },
  // ==========================================
  // 收银端（FullScreen Layout）
  // ==========================================
  {
    path: '/pos',
    name: 'Cashier',
    component: () => import('@/views/cashier/Cashier.vue'),
    meta: { layout: 'fullscreen', role: 'cashier' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const path = to.path
  const currentRole = userStore.currentRole || userStore.userInfo?.role
  const defaultPath = {
    customer: '/',
    admin: '/admin',
    cashier: '/pos',
    courier: '/courier',
    warehouse: '/admin',
    dashboard: '/admin'
  }
  const allowedByRouteRole = {
    customer: ['customer'],
    courier: ['courier'],
    cashier: ['cashier', 'admin'],
    admin: ['admin', 'warehouse', 'dashboard']
  }

  if (path === '/login') {
    next()
    return
  }

  // 简单的登录拦截
  if (!currentRole && path !== '/login') {
    next('/login')
    return
  }

  const routeRole = to.meta.role
  if (routeRole) {
    const allowedRoles = allowedByRouteRole[routeRole] || [routeRole]
    if (!allowedRoles.includes(currentRole)) {
      next(defaultPath[currentRole] || '/login')
      return
    }
  }

  next()
})

export default router
