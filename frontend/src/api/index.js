import request from '@/utils/request'

export const authAPI = {
  login(data) {
    return request.post('/auth/login', data)
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
  },
  confirm(orderId) {
    return request.put(`/orders/confirm/${orderId}`)
  }
}

export const addressAPI = {
  getList() {
    return request.get('/addresses/list')
  },
  add(data) {
    return request.post('/addresses', data)
  },
  update(data) {
    return request.put('/addresses', data)
  },
  delete(addressId) {
    return request.delete(`/addresses/${addressId}`)
  }
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
  shipOrder(orderId) {
    return request.put(`/admin/orders/${orderId}/ship`)
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
