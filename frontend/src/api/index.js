import request from '@/utils/request'

export const authAPI = {
  login(data) {
    return request.post('/auth/login', data)
  },
  adminLogin(data) {
    return request.post('/auth/admin/login', data)
  },
  register(data) {
    return request.post('/auth/register', data)
  },
  getUserInfo() {
    return request.get('/auth/userinfo')
  }
}

export const productAPI = {
  getList(params) {
    return request.get('/products/list', { params })
  },
  getById(id) {
    return request.get(`/products/${id}`)
  },
  add(data) {
    return request.post('/products', data)
  },
  update(data) {
    return request.put('/products', data)
  },
  delete(id) {
    return request.delete(`/products/${id}`)
  },
  getCategories() {
    return request.get('/products/categories')
  },
  addCategory(data) {
    return request.post('/products/categories', data)
  },
  updateCategory(data) {
    return request.put('/products/categories', data)
  },
  deleteCategory(id) {
    return request.delete(`/products/categories/${id}`)
  }
}

export const cartAPI = {
  getList() {
    return request.get('/cart/list')
  },
  add(productId, quantity = 1) {
    return request.post('/cart/add', null, { params: { productId, quantity } })
  },
  updateQuantity(cartId, quantity) {
    return request.put('/cart/update', null, { params: { cartId, quantity } })
  },
  remove(cartId) {
    return request.delete(`/cart/${cartId}`)
  },
  clear() {
    return request.delete('/cart/clear')
  }
}

export const orderAPI = {
  getList(params) {
    return request.get('/orders/list', { params })
  },
  getDetail(orderId) {
    return request.get(`/orders/${orderId}`)
  },
  create(data) {
    return request.post('/orders/create', data)
  },
  cancel(orderId) {
    return request.put(`/orders/cancel/${orderId}`)
  }
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
>>>>>>> Stashed changes
}

export const adminAPI = {
  getUsers(params) {
    return request.get('/admin/users', { params })
  },
  updateUserStatus(userId, status) {
    return request.put(`/admin/users/${userId}/status`, null, { params: { status } })
  },
  getStatistics() {
    return request.get('/admin/statistics')
  },
  warehousing(productId, quantity) {
    return request.post('/admin/inventory/warehousing', null, { params: { productId, quantity } })
  },
  outbound(productId, quantity) {
    return request.post('/admin/inventory/outbound', null, { params: { productId, quantity } })
  },
  getInventoryLogs(params) {
    return request.get('/admin/inventory/logs', { params })
  },
  getDeliveries(params) {
    return request.get('/admin/deliveries', { params })
  },
  assignCourier(deliveryId, courierId) {
    return request.put('/admin/deliveries/assign', null, { params: { deliveryId, courierId } })
  },
  updateDeliveryStatus(deliveryId, status) {
    return request.put('/admin/deliveries/status', null, { params: { deliveryId, status } })
  },
  getPromotions() {
    return request.get('/admin/promotions')
  },
  createPromotion(data) {
    return request.post('/admin/promotions', data)
  },
  updatePromotion(data) {
    return request.put('/admin/promotions', data)
  },
  deletePromotion(promotionId) {
    return request.delete(`/admin/promotions/${promotionId}`)
  },
  getSuppliers() {
    return request.get('/admin/suppliers')
  },
  getPurchaseOrders(params) {
    return request.get('/admin/purchase-orders', { params })
  },
  createPurchaseOrder(data) {
    return request.post('/admin/purchase-orders', data)
  },
  getFinanceData() {
    return request.get('/admin/finance')
  },
  getAuditLogs(params) {
    return request.get('/admin/audit-logs', { params })
  }
}
