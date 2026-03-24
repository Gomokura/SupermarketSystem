import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login') {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
