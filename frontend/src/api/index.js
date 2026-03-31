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
    ElMessage.error(res.data.message || '请求失败')
    return Promise.reject(res.data)
  },
  err => { ElMessage.error(err.response?.data?.message || '网络错误'); return Promise.reject(err) }
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
    ElMessage.error(res.data.message || '请求失败')
    return Promise.reject(res.data)
  },
  err => { ElMessage.error(err.response?.data?.message || '网络错误'); return Promise.reject(err) }
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
  getList: () => request.get('/cart/list'),
  add: (productId, quantity = 1, skuId) => request.post(`/cart/add?productId=${productId}&quantity=${quantity}${skuId != null ? '&skuId=' + skuId : ''}`),
  updateQuantity: (cartId, quantity) => request.put('/cart/update', { cartId, quantity }),
  remove: (cartId) => request.delete(`/cart/${cartId}`),
  clear: () => request.delete('/cart/clear')
}

export const orderAPI = {
  getList: (params) => request.get('/orders/list', { params }),
  getDetail: (orderId) => request.get(`/orders/${orderId}`),
  create: (data) => request.post('/orders/create', data),
  cancel: (orderId) => request.put(`/orders/${orderId}/cancel`),
  confirm: (orderId) => request.put(`/orders/${orderId}/confirm`),
  pay: (orderId, payMethod) => request.post(`/orders/${orderId}/pay`, { payMethod }),
  reorder: (orderId) => request.post(`/orders/${orderId}/reorder`),
  cashierCreate: (data) => adminRequest.post('/orders/cashier', data),
  adminGetList: (params) => adminRequest.get('/orders/admin/list', { params }),
  adminCancel: (orderId, reason) => adminRequest.put(`/orders/${orderId}/admin-cancel`, { reason })
}

export const addressAPI = {
  getList: () => request.get('/addresses/list'),
  add: (data) => request.post('/addresses', data),
  update: (data) => request.put('/addresses', data),
  delete: (addressId) => request.delete(`/addresses/${addressId}`)
}

export const favoriteAPI = {
  getMyFavorites: () => request.get('/favorites/my'),
  add: (productId) => request.post(`/favorites/${productId}`),
  remove: (productId) => request.delete(`/favorites/${productId}`)
}

export const bannerAPI = {
  getList: () => request.get('/banners/list'),
  adminGetList: () => adminRequest.get('/banners/admin/list'),
  create: (data) => adminRequest.post('/banners/admin', data),
  update: (id, data) => adminRequest.put(`/banners/admin/${id}`, data),
  toggle: (id, isActive) => adminRequest.put(`/banners/admin/${id}/toggle`, null, { params: { isActive } }),
  delete: (id) => adminRequest.delete(`/banners/admin/${id}`)
}

export const brandAPI = {
  getList: (params) => request.get('/brands/list', { params }),
  create: (data) => adminRequest.post('/brands/create', data),
  update: (id, data) => adminRequest.put(`/brands/${id}`, data),
  delete: (id) => adminRequest.delete(`/brands/${id}`)
}

export const seckillAPI = {
  getList: (params) => request.get('/seckill/activities', { params }),
  getActivityProducts: (seckillId) => request.get(`/seckill/activities/${seckillId}/products`),
  adminGetList: (params) => adminRequest.get('/seckill/admin/activities', { params }),
  adminCreate: (data) => adminRequest.post('/seckill/admin/activities', data),
  adminUpdate: (id, data) => adminRequest.put(`/seckill/admin/activities/${id}`, data),
  adminUpsertProducts: (seckillId, items) => adminRequest.post(`/seckill/admin/activities/${seckillId}/products`, items)
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
  adminGetList: (params) => adminRequest.get('/reviews/admin/list', { params }),
  adminDelete: (id) => adminRequest.delete(`/reviews/admin/${id}`),
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
  getMyPoints: () => request.get('/points/my'),
  getLogs: (params) => request.get('/points/logs', { params }),
  adminAdjust: (data) => adminRequest.post('/points/admin/adjust', data),
  adminGetLogs: (userId, params) => adminRequest.get('/points/admin/logs', { params: { userId, ...params } })
}

export const messageAPI = {
  getList: (params) => request.get('/messages/my', { params }),
  getUnreadCount: () => request.get('/messages/unread-count'),
  markRead: (msgId) => request.put(`/messages/${msgId}/read`),
  markAllRead: () => request.put('/messages/read-all')
}

export const cashierAPI = {
  openShift: (startCash) => adminRequest.post('/cashier/shift/open', { startCash }),
  getCurrentShift: () => adminRequest.get('/cashier/shift/current'),
  closeShift: (endCash) => adminRequest.post('/cashier/shift/close', { endCash }),
  getHistory: (params) => adminRequest.get('/cashier/shift/history', { params }),
  searchProduct: (keyword) => adminRequest.get('/cashier/products/search', { params: { keyword } })
}

export const courierAPI = {
  getProfile: () => courierRequest.get('/courier/profile'),
  changePassword: (data) => courierRequest.put('/courier/password', data),
  updateStatus: (status) => courierRequest.put('/courier/status', null, { params: { status } }),
  getTasks: (status) => courierRequest.get('/courier/tasks', { params: { status } }),
  pickup: (taskId) => courierRequest.put(`/courier/tasks/${taskId}/pickup`),
  complete: (taskId) => courierRequest.put(`/courier/tasks/${taskId}/complete`),
  fail: (taskId, failReason) => courierRequest.put(`/courier/tasks/${taskId}/fail`, { failReason }),
  getHistory: () => courierRequest.get('/courier/tasks/history'),
  adminGetList: (params) => adminRequest.get('/admin/couriers', { params }),
  adminCreate: (data) => adminRequest.post('/admin/couriers', data),
  adminUpdateStatus: (id, isDisabled) => adminRequest.put(`/admin/couriers/${id}/status`, null, { params: { isDisabled } }),
  adminAssign: (deliveryId, courierId) => adminRequest.put(`/admin/deliveries/${deliveryId}/assign`, null, { params: { courierId } }),
  getMemberByPhone: (phone) => adminRequest.get('/auth/cashier/member', { params: { phone } })
}

export const stocktakeAPI = {
  create: (data) => adminRequest.post('/stocktake/create', data),
  getList: (params) => adminRequest.get('/stocktake/list', { params }),
  getDetail: (taskId) => adminRequest.get(`/stocktake/${taskId}`),
  inputActual: (taskId, items) => adminRequest.put(`/stocktake/${taskId}/input`, { items }),
  submit: (taskId) => adminRequest.put(`/stocktake/${taskId}/submit`)
}

export const adminAPI = {
  getUsers: (params) => adminRequest.get('/admin/users', { params }),
  getUserDetail: (userId) => adminRequest.get(`/admin/users/${userId}`),
  updateUserStatus: (userId, status) => adminRequest.put(`/admin/users/${userId}/status`, null, { params: { status } }),
  getAdmins: (params) => adminRequest.get('/admin/admins', { params }),
  createAdmin: (data) => adminRequest.post('/admin/admins', data),
  updateAdmin: (id, data) => adminRequest.put(`/admin/admins/${id}`, data),
  updateAdminStatus: (id, status) => adminRequest.put(`/admin/admins/${id}/status`, null, { params: { status } }),
  resetAdminPassword: (id) => adminRequest.put(`/admin/admins/${id}/reset-password`),
  getOrders: (params) => adminRequest.get('/orders/admin/list', { params }),
  shipOrder: (id, company, trackingNo) => adminRequest.put(`/admin/orders/${id}/shipping`, { company, trackingNo }),
  updateOrderAddress: (id, name, phone, address) => adminRequest.put(`/admin/orders/${id}/address`, { name, phone, address }),
  getInventoryLogs: (params) => adminRequest.get('/admin/inventory/logs', { params }),
  warehousing: (productId, quantity, remark) => adminRequest.post('/admin/inventory/warehousing', null, { params: { productId, quantity, remark } }),
  outbound: (productId, quantity, remark) => adminRequest.post('/admin/inventory/outbound', null, { params: { productId, quantity, remark } }),
  getDeliveries: (params) => adminRequest.get('/admin/deliveries', { params }),
  getSuppliers: (params) => adminRequest.get('/admin/suppliers', { params }),
  createSupplier: (data) => adminRequest.post('/admin/suppliers', data),
  updateSupplier: (id, data) => adminRequest.put(`/admin/suppliers/${id}`, data),
  deleteSupplier: (id) => adminRequest.delete(`/admin/suppliers/${id}`),
  getPurchaseOrders: (params) => adminRequest.get('/admin/purchase-orders', { params }),
  createPurchaseOrder: (data) => adminRequest.post('/admin/purchase-orders', data),
  getPurchaseDetail: (poId) => adminRequest.get(`/admin/purchase-orders/${poId}`),
  approvePurchaseOrder: (id) => adminRequest.put(`/admin/purchase-orders/${id}/approve`),
  receivePurchaseOrder: (id, data) => adminRequest.put(`/admin/purchase-orders/${id}/receive`, data),
  cancelPurchaseOrder: (id) => adminRequest.put(`/admin/purchase-orders/${id}/cancel`),
  getDamageRecords: (params) => adminRequest.get('/warehouse/damage/list', { params }),
  createDamageRecord: (data) => adminRequest.post('/warehouse/damage', data),
  getInventoryOverview: (params) => adminRequest.get('/warehouse/inventory', { params }),
  getLowStock: () => adminRequest.get('/warehouse/inventory/low-stock'),
  getWarehouseLogs: (params) => adminRequest.get('/warehouse/inventory/logs', { params }),
  getAuditLogs: (params) => adminRequest.get('/admin/audit-logs', { params }),
  getStatistics: () => adminRequest.get('/admin/statistics'),
  getDashboard: (params) => adminRequest.get('/admin/dashboard', { params }),
  getTopProducts: (params) => adminRequest.get('/admin/dashboard/top-products', { params }),
  getFinance: () => adminRequest.get('/admin/finance'),
  getPromotions: (params) => adminRequest.get('/admin/promotions', { params }),
  createPromotion: (data) => adminRequest.post('/admin/promotions', data),
  updatePromotion: (id, data) => adminRequest.put(`/admin/promotions/${id}`, data),
  updatePromotionStatus: (id, status) => adminRequest.put(`/promotions/${id}/status`, { status }),
  getCoupons: (params) => adminRequest.get('/coupons/admin/list', { params }),
  createCoupon: (data) => adminRequest.post('/coupons/admin', data),
  updateCoupon: (id, data) => adminRequest.put(`/coupons/admin/${id}`, data),
  toggleCoupon: (id, status) => adminRequest.put(`/coupons/admin/${id}/status`, null, { params: { status } }),
  deleteCoupon: (id) => adminRequest.delete(`/coupons/admin/${id}`),
  batchIssueCoupons: (couponId, userIds) => adminRequest.post('/coupons/admin/batch-issue', { couponId, userIds })
}
