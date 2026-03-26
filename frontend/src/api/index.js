import request from '@/utils/request'

// 多端 Token 请求实例
import axios from 'axios'
import { ElMessage } from 'element-plus'

const adminRequest = axios.create({ baseURL: '/api', timeout: 10000 })
adminRequest.interceptors.request.use(config => {
  const token = localStorage.getItem('adminToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
adminRequest.interceptors.response.use(
  res => {
    if (res.data.code === 200) return res.data
    ElMessage.error(res.data.msg || '请求失败')
    return Promise.reject(res.data)
  },
  err => { ElMessage.error(err.response?.data?.msg || '网络错误'); return Promise.reject(err) }
)

const courierRequest = axios.create({ baseURL: '/api', timeout: 10000 })
courierRequest.interceptors.request.use(config => {
  const token = localStorage.getItem('courierToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
courierRequest.interceptors.response.use(
  res => {
    if (res.data.code === 200) return res.data
    ElMessage.error(res.data.msg || '请求失败')
    return Promise.reject(res.data)
  },
  err => { ElMessage.error(err.response?.data?.msg || '网络错误'); return Promise.reject(err) }
)

export const authAPI = {
  login: (data) => request.post('/auth/login', data),
  adminLogin: (data) => adminRequest.post('/auth/admin/login', data),
  courierLogin: (data) => courierRequest.post('/auth/courier/login', data),
  register: (data) => request.post('/auth/register', data),
  getUserInfo: () => request.get('/auth/userinfo'),
  updateUserInfo: (data) => request.put('/auth/userinfo', data),
  changePassword: (data) => request.put('/auth/password', data),
  getAdminInfo: () => adminRequest.get('/auth/admin/info')
}

export const productAPI = {
  getList: (params) => request.get('/products/list', { params }),
  getById: (id) => request.get(`/products/${id}`),
  getSkus: (productId) => request.get(`/products/${productId}/skus`),
  getRecommended: () => request.get('/products/recommended'),
  getCategoryTree: () => request.get('/products/categories/tree'),
  getByBarcode: (barcode) => request.get(`/products/barcode/${barcode}`),
  add: (data) => adminRequest.post('/products', data),
  update: (id, data) => adminRequest.put(`/products/${id}`, data),
  delete: (id) => adminRequest.delete(`/products/${id}`),
  updateStatus: (id, status) => adminRequest.put(`/products/${id}/status`, null, { params: { status } }),
  // 兼容旧调用
  getCategories: () => request.get('/products/categories/tree')
}

export const cartAPI = {
  getList: () => request.get('/cart'),
  add: (productId, quantity = 1, skuId) => request.post('/cart/add', { productId, quantity, skuId }),
  updateQuantity: (cartId, quantity) => request.put(`/cart/${cartId}`, { quantity }),
  remove: (cartId) => request.delete(`/cart/${cartId}`),
  clear: () => request.delete('/cart/clear')
}

export const orderAPI = {
  getList: (params) => request.get('/orders/list', { params }),
  getDetail: (orderId) => request.get(`/orders/${orderId}`),
  create: (data) => request.post('/orders', data),
  cancel: (orderId) => request.put(`/orders/${orderId}/cancel`),
  confirm: (orderId) => request.put(`/orders/${orderId}/confirm`),
  pay: (orderId) => request.post(`/orders/${orderId}/pay`),
  cashierCreate: (data) => adminRequest.post('/orders/cashier', data),
  adminGetList: (params) => adminRequest.get('/admin/orders', { params }),
  adminCancel: (orderId, reason) => adminRequest.put(`/orders/${orderId}/admin-cancel`, { reason })
}

export const addressAPI = {
  getList: () => request.get('/addresses'),
  getDefault: () => request.get('/addresses/default'),
  add: (data) => request.post('/addresses', data),
  update: (addressId, data) => request.put(`/addresses/${addressId}`, data),
  delete: (addressId) => request.delete(`/addresses/${addressId}`)
}

export const bannerAPI = {
  getList: () => request.get('/banners/list'),
  adminGetList: () => adminRequest.get('/admin/banners'),
  create: (data) => adminRequest.post('/admin/banners', data),
  update: (id, data) => adminRequest.put(`/admin/banners/${id}`, data),
  toggle: (id) => adminRequest.put(`/admin/banners/${id}/toggle`),
  delete: (id) => adminRequest.delete(`/admin/banners/${id}`)
}

export const brandAPI = {
  getList: () => request.get('/brands/list'),
  adminGetList: (params) => adminRequest.get('/admin/brands', { params }),
  create: (data) => adminRequest.post('/admin/brands', data),
  update: (id, data) => adminRequest.put(`/admin/brands/${id}`, data),
  delete: (id) => adminRequest.delete(`/admin/brands/${id}`)
}

export const seckillAPI = {
  getActive: () => request.get('/seckill/list'),
  adminGetList: (params) => adminRequest.get('/admin/seckill', { params }),
  create: (data) => adminRequest.post('/admin/seckill', data),
  updateStatus: (id, status) => adminRequest.put(`/admin/seckill/${id}/status`, null, { params: { status } }),
  addProduct: (seckillId, data) => adminRequest.post(`/admin/seckill/${seckillId}/products`, data),
  removeProduct: (seckillId, id) => adminRequest.delete(`/admin/seckill/${seckillId}/products/${id}`)
}

export const couponAPI = {
  getAvailable: (orderAmount) => request.get('/coupons/available', { params: { orderAmount } }),
  getMyCoupons: (status) => request.get('/coupons/my', { params: { status } }),
  claim: (couponId) => request.post(`/coupons/claim/${couponId}`),
  adminGetList: (params) => adminRequest.get('/admin/coupons', { params }),
  adminCreate: (data) => adminRequest.post('/admin/coupons', data),
  adminUpdate: (id, data) => adminRequest.put(`/admin/coupons/${id}`, data),
  adminUpdateStatus: (id, status) => adminRequest.put(`/admin/coupons/${id}/status`, null, { params: { status } }),
  adminDelete: (id) => adminRequest.delete(`/admin/coupons/${id}`)
}

export const reviewAPI = {
  getByProduct: (productId, params) => request.get(`/reviews/product/${productId}`, { params }),
  create: (data) => request.post('/reviews', data),
  adminToggleHidden: (id, hidden) => adminRequest.put(`/reviews/admin/${id}/hidden`, null, { params: { hidden } }),
  adminReply: (id, reply) => adminRequest.put(`/reviews/admin/${id}/reply`, { reply })
}

export const afterSaleAPI = {
  create: (data) => request.post('/after-sales', data),
  getMy: () => request.get('/after-sales/my'),
  adminGetList: (params) => adminRequest.get('/after-sales/admin/list', { params }),
  adminHandle: (id, action, remark) => adminRequest.put(`/after-sales/admin/${id}/handle`, { action, remark })
}

export const pointsAPI = {
  getLogs: (params) => request.get('/users/points-logs', { params }),
  adminAdjust: (userId, amount, remark) => adminRequest.put(`/admin/users/${userId}/points`, { amount, remark })
}

export const messageAPI = {
  getList: (params) => request.get('/messages', { params }),
  getUnreadCount: () => request.get('/messages/unread-count'),
  markRead: (msgId) => request.put(`/messages/${msgId}/read`),
  markAllRead: () => request.put('/messages/read-all'),
  adminSend: (data) => adminRequest.post('/admin/messages', data)
}

export const cashierAPI = {
  openShift: (startCash) => adminRequest.post('/cashier/shift/open', { startCash }),
  getCurrentShift: () => adminRequest.get('/cashier/shift/current'),
  closeShift: (endCash) => adminRequest.post('/cashier/shift/close', { endCash }),
  getHistory: (params) => adminRequest.get('/cashier/shift/history', { params }),
  searchProduct: (keyword) => adminRequest.get('/cashier/products/search', { params: { keyword } })
}

export const courierAPI = {
  getInfo: () => courierRequest.get('/courier/info'),
  changePassword: (data) => courierRequest.put('/courier/password', data),
  getTasks: (status) => courierRequest.get('/courier/tasks', { params: { status } }),
  pickup: (taskId) => courierRequest.put(`/courier/tasks/${taskId}/pickup`),
  complete: (taskId) => courierRequest.put(`/courier/tasks/${taskId}/complete`),
  fail: (taskId, reason) => courierRequest.put(`/courier/tasks/${taskId}/fail`, { reason }),
  getHistory: () => courierRequest.get('/courier/tasks/history'),
  adminGetList: (params) => adminRequest.get('/admin/couriers', { params }),
  adminCreate: (data) => adminRequest.post('/admin/couriers', data),
  adminUpdate: (id, data) => adminRequest.put(`/admin/couriers/${id}`, data),
  adminAssign: (deliveryId, courierId) => adminRequest.put(`/admin/deliveries/${deliveryId}/assign`, null, { params: { courierId } })
}

export const stocktakeAPI = {
  create: (data) => adminRequest.post('/admin/stocktake', data),
  getList: (params) => adminRequest.get('/admin/stocktake', { params }),
  getDetail: (taskId) => adminRequest.get(`/admin/stocktake/${taskId}`),
  updateItems: (taskId, items) => adminRequest.put(`/admin/stocktake/${taskId}/items`, { items }),
  submit: (taskId) => adminRequest.put(`/admin/stocktake/${taskId}/submit`)
}

export const adminAPI = {
  getUsers: (params) => adminRequest.get('/admin/users', { params }),
  updateUserStatus: (userId, status) => adminRequest.put(`/admin/users/${userId}/status`, null, { params: { status } }),
  getAdmins: (params) => adminRequest.get('/admin/admins', { params }),
  createAdmin: (data) => adminRequest.post('/admin/admins', data),
  updateAdmin: (id, data) => adminRequest.put(`/admin/admins/${id}`, data),
  updateAdminStatus: (id, status) => adminRequest.put(`/admin/admins/${id}/status`, null, { params: { status } }),
  resetAdminPassword: (id) => adminRequest.put(`/admin/admins/${id}/reset-password`),
  getOrders: (params) => adminRequest.get('/admin/orders', { params }),
  getInventoryLogs: (params) => adminRequest.get('/admin/inventory/logs', { params }),
  warehousing: (productId, quantity) => adminRequest.post('/admin/inventory/warehousing', null, { params: { productId, quantity } }),
  outbound: (productId, quantity) => adminRequest.post('/admin/inventory/outbound', null, { params: { productId, quantity } }),
  getDeliveries: (params) => adminRequest.get('/admin/deliveries', { params }),
  getSuppliers: (params) => adminRequest.get('/admin/suppliers', { params }),
  createSupplier: (data) => adminRequest.post('/admin/suppliers', data),
  updateSupplier: (id, data) => adminRequest.put(`/admin/suppliers/${id}`, data),
  deleteSupplier: (id) => adminRequest.delete(`/admin/suppliers/${id}`),
  getPurchaseOrders: (params) => adminRequest.get('/admin/purchase-orders', { params }),
  createPurchaseOrder: (data) => adminRequest.post('/admin/purchase-orders', data),
  receivePurchaseOrder: (id, data) => adminRequest.put(`/admin/purchase-orders/${id}/receive`, data),
  cancelPurchaseOrder: (id) => adminRequest.put(`/admin/purchase-orders/${id}/cancel`),
  getDamageRecords: (params) => adminRequest.get('/admin/damage-records', { params }),
  createDamageRecord: (data) => adminRequest.post('/admin/damage-records', data),
  getAuditLogs: (params) => adminRequest.get('/admin/audit-logs', { params }),
  getStatistics: () => adminRequest.get('/admin/statistics'),
  getSalesTrend: (params) => adminRequest.get('/admin/statistics/sales-trend', { params }),
  getProductRank: (params) => adminRequest.get('/admin/statistics/product-rank', { params }),
  getCategorySales: () => adminRequest.get('/admin/statistics/category-sales'),
  getHourlySales: () => adminRequest.get('/admin/statistics/hourly-sales'),
  getUserTrend: (params) => adminRequest.get('/admin/statistics/user-trend', { params }),
  getCouponStats: () => adminRequest.get('/admin/statistics/coupon-stats'),
  getPromotions: (params) => adminRequest.get('/admin/promotions', { params }),
  createPromotion: (data) => adminRequest.post('/admin/promotions', data),
  updatePromotion: (id, data) => adminRequest.put(`/admin/promotions/${id}`, data),
  deletePromotion: (id) => adminRequest.delete(`/admin/promotions/${id}`)
}
