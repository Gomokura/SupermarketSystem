import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  // 顾客端
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/home',
    children: [
      { path: '/home', name: 'Home', component: () => import('@/views/user/Home.vue') },
      { path: '/products', name: 'Products', component: () => import('@/views/user/Products.vue') },
      { path: '/products/:id', name: 'ProductDetail', component: () => import('@/views/user/ProductDetail.vue') },
      { path: '/cart', name: 'Cart', component: () => import('@/views/user/Cart.vue') },
      { path: '/checkout', name: 'Checkout', component: () => import('@/views/user/Checkout.vue') },
      { path: '/orders', name: 'Orders', component: () => import('@/views/user/Orders.vue') },
      { path: '/orders/:id', name: 'OrderDetail', component: () => import('@/views/user/OrderDetail.vue') },
      { path: '/after-sale', name: 'AfterSale', component: () => import('@/views/user/AfterSale.vue') },
      { path: '/review/:orderId', name: 'Review', component: () => import('@/views/user/Review.vue') },
      { path: '/coupons', name: 'Coupons', component: () => import('@/views/user/Coupons.vue') },
      { path: '/points-logs', name: 'PointsLog', component: () => import('@/views/user/PointsLog.vue') },
      { path: '/messages', name: 'Messages', component: () => import('@/views/user/Messages.vue') },
      { path: '/address', name: 'Address', component: () => import('@/views/user/Address.vue') },
      { path: '/profile', name: 'Profile', component: () => import('@/views/user/Profile.vue') },
      { path: '/seckill', name: 'Seckill', component: () => import('@/views/user/Seckill.vue') },
      { path: '/favorites', name: 'Favorites', component: () => import('@/views/user/Favorites.vue') }
    ]
  },
  // 管理后台（含仓储进货端、数据看板端）
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: '/admin/dashboard', name: 'Dashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: '/admin/products', name: 'AdminProducts', component: () => import('@/views/admin/Products.vue') },
      { path: '/admin/categories', name: 'AdminCategories', component: () => import('@/views/admin/Categories.vue') },
      { path: '/admin/orders', name: 'AdminOrders', component: () => import('@/views/admin/Orders.vue') },
      { path: '/admin/users', name: 'AdminUsers', component: () => import('@/views/admin/Users.vue') },
      { path: '/admin/inventory', name: 'AdminInventory', component: () => import('@/views/admin/Inventory.vue') },
      { path: '/admin/deliveries', name: 'AdminDeliveries', component: () => import('@/views/admin/Deliveries.vue') },
      { path: '/admin/promotions', name: 'AdminPromotions', component: () => import('@/views/admin/Promotions.vue') },
      { path: '/admin/finance', name: 'AdminFinance', component: () => import('@/views/admin/Finance.vue') },
      { path: '/admin/admins', name: 'AdminAdmins', component: () => import('@/views/admin/Admins.vue') },
      { path: '/admin/brands', name: 'AdminBrands', component: () => import('@/views/admin/Brands.vue') },
      { path: '/admin/banners', name: 'AdminBanners', component: () => import('@/views/admin/Banners.vue') },
      { path: '/admin/after-sales', name: 'AdminAfterSales', component: () => import('@/views/admin/AfterSales.vue') },
      { path: '/admin/reviews', name: 'AdminReviews', component: () => import('@/views/admin/Reviews.vue') },
      { path: '/admin/seckill', name: 'AdminSeckill', component: () => import('@/views/admin/Seckill.vue') },
      { path: '/admin/stocktake', name: 'AdminStocktake', component: () => import('@/views/admin/Stocktake.vue') },
      { path: '/admin/damage-records', name: 'AdminDamageRecords', component: () => import('@/views/admin/DamageRecords.vue') },
      { path: '/admin/purchase-orders', name: 'AdminPurchaseOrders', component: () => import('@/views/admin/PurchaseOrders.vue') },
      { path: '/admin/suppliers', name: 'AdminSuppliers', component: () => import('@/views/admin/Suppliers.vue') },
      { path: '/admin/audit-log', name: 'AdminAuditLog', component: () => import('@/views/admin/AuditLog.vue') },
      { path: '/admin/couriers', name: 'AdminCouriers', component: () => import('@/views/admin/Couriers.vue') },
      { path: '/admin/coupons-manage', name: 'AdminCouponsManage', component: () => import('@/views/admin/CouponsManage.vue') }
    ]
  },
  // 收银端
  {
    path: '/cashier',
    component: () => import('@/views/cashier/Layout.vue'),
    children: [
      { path: '', name: 'Cashier', component: () => import('@/views/cashier/Cashier.vue') }
    ]
  },
  // 配送员端
  {
    path: '/courier',
    component: () => import('@/views/courier/Layout.vue'),
    children: [
      { path: '', name: 'CourierTasks', component: () => import('@/views/courier/Tasks.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const path = to.path

  if (path.startsWith('/admin') || path.startsWith('/cashier')) {
    const adminToken = localStorage.getItem('adminToken')
    if (!adminToken && path !== '/login') {
      next('/login')
      return
    }
  } else if (path.startsWith('/courier')) {
    const courierToken = localStorage.getItem('courierToken')
    if (!courierToken) {
      next('/login')
      return
    }
  } else if (path !== '/login' && path !== '/home' && path !== '/products' && !path.startsWith('/products/') && path !== '/seckill') {
    const token = localStorage.getItem('token')
    if (!token) {
      next('/login')
      return
    }
  }

  next()
})

export default router
