import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/home',
    meta: { requiresAuth: true },
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/user/Home.vue')
      },
      {
        path: '/products',
        name: 'Products',
        component: () => import('@/views/user/Products.vue')
      },
      {
        path: '/cart',
        name: 'Cart',
        component: () => import('@/views/user/Cart.vue')
      },
      {
        path: '/orders',
        name: 'Orders',
        component: () => import('@/views/user/Orders.vue')
      },
      {
        path: '/orders/:id',
        name: 'OrderDetail',
        component: () => import('@/views/user/OrderDetail.vue')
      },
      {
        path: '/address',
        name: 'Address',
        component: () => import('@/views/user/Address.vue')
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue')
      },
      {
        path: '/checkout',
        name: 'Checkout',
        component: () => import('@/views/user/Checkout.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: '/admin/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: '/admin/products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/Products.vue')
      },
      {
        path: '/admin/categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/Categories.vue')
      },
      {
        path: '/admin/orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/Orders.vue')
      },
      {
        path: '/admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue')
      },
      {
        path: '/admin/inventory',
        name: 'AdminInventory',
        component: () => import('@/views/admin/Inventory.vue')
      },
      {
        path: '/admin/deliveries',
        name: 'AdminDeliveries',
        component: () => import('@/views/admin/Deliveries.vue')
      },
      {
        path: '/admin/promotions',
        name: 'AdminPromotions',
        component: () => import('@/views/admin/Promotions.vue')
      },
      {
        path: '/admin/finance',
        name: 'AdminFinance',
        component: () => import('@/views/admin/Finance.vue')
      }
    ]
  },
  // 兜底：未匹配的路由跳首页
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user  = JSON.parse(localStorage.getItem('user') || '{}')
  const isLoggedIn = !!token
  const isAdmin    = user.role === 'admin'

  // 已登录 → 还想进登录页 → 跳对应首页
  if (isLoggedIn && to.path === '/login') {
    return next(isAdmin ? '/admin/dashboard' : '/home')
  }

  // 需要登录但未登录 → 跳登录页
  if (to.meta.requiresAuth && !isLoggedIn) {
    return next('/login')
  }

  // 需要管理员但不是管理员 → 跳用户首页
  if (to.meta.requiresAdmin && !isAdmin) {
    return next('/home')
  }

  next()
})

export default router
