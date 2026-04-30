export const MENU_CONFIG = {
  admin: [
    { key: 'dashboard', path: '/admin/dashboard', icon: 'DataAnalysis', label: 'Dashboard' },
    { key: 'products', path: '/admin/products', icon: 'Goods', label: '商品' },
    { key: 'categories', path: '/admin/categories', icon: 'Collection', label: '分类' },
    { key: 'orders', path: '/admin/orders', icon: 'List', label: '订单' },
    { key: 'users', path: '/admin/users', icon: 'User', label: '用户' },
    { key: 'inventory', path: '/admin/inventory', icon: 'Box', label: '库存' },
    { key: 'deliveries', path: '/admin/deliveries', icon: 'Van', label: '配送' },
    { key: 'promotions', path: '/admin/promotions', icon: 'Discount', label: '促销' },
    { key: 'finance', path: '/admin/finance', icon: 'Money', label: '财务' },
    {
      key: 'warehouse',
      icon: 'Box',
      label: '仓储进货',
      children: [
        { key: 'suppliers', path: '/admin/suppliers', label: '供应商管理' },
        { key: 'purchase-orders', path: '/admin/purchase-orders', label: '采购管理' },
        { key: 'stocktake', path: '/admin/stocktake', label: '库存盘点' },
        { key: 'damage-records', path: '/admin/damage-records', label: '报损记录' }
      ]
    },
    {
      key: 'system',
      icon: 'Setting',
      label: '系统管理',
      children: [
        { key: 'admins', path: '/admin/admins', label: '管理员账号' },
        { key: 'brands', path: '/admin/brands', label: '品牌管理' },
        { key: 'banners', path: '/admin/banners', label: '轮播图管理' },
        { key: 'coupons-manage', path: '/admin/coupons-manage', label: '优惠券管理' },
        { key: 'audit-log', path: '/admin/audit-log', label: '审计日志' },
        { key: 'couriers', path: '/admin/couriers', label: '配送员管理' }
      ]
    },
    {
      key: 'operation',
      icon: 'Goods',
      label: '运营管理',
      children: [
        { key: 'seckill', path: '/admin/seckill', label: '秒杀活动' },
        { key: 'after-sales', path: '/admin/after-sales', label: '售后管理' },
        { key: 'reviews', path: '/admin/reviews', label: '评价管理' }
      ]
    }
  ],
  store_manager: [
    { key: 'dashboard', path: '/admin/dashboard', icon: 'DataAnalysis', label: 'Dashboard' },
    { key: 'products', path: '/admin/products', icon: 'Goods', label: '商品' },
    { key: 'categories', path: '/admin/categories', icon: 'Collection', label: '分类' },
    { key: 'orders', path: '/admin/orders', icon: 'List', label: '订单' },
    { key: 'inventory', path: '/admin/inventory', icon: 'Box', label: '库存' },
    { key: 'deliveries', path: '/admin/deliveries', icon: 'Van', label: '配送' },
    { key: 'finance', path: '/admin/finance', icon: 'Money', label: '财务' }
  ],
  cashier: [
    { key: 'dashboard', path: '/cashier', icon: 'Money', label: '收银台' }
  ],
  warehouse_staff: [
    { key: 'dashboard', path: '/admin/dashboard', icon: 'DataAnalysis', label: 'Dashboard' },
    { key: 'inventory', path: '/admin/inventory', icon: 'Box', label: '库存' },
    {
      key: 'warehouse',
      icon: 'Box',
      label: '仓储进货',
      children: [
        { key: 'suppliers', path: '/admin/suppliers', label: '供应商管理' },
        { key: 'purchase-orders', path: '/admin/purchase-orders', label: '采购管理' },
        { key: 'stocktake', path: '/admin/stocktake', label: '库存盘点' },
        { key: 'damage-records', path: '/admin/damage-records', label: '报损记录' }
      ]
    }
  ],
  courier: [
    { key: 'tasks', path: '/courier', icon: 'Van', label: '配送任务' }
  ]
}

export const getMenuByRole = (role) => {
  return MENU_CONFIG[role] || MENU_CONFIG.admin
}

export const flattenMenu = (menus) => {
  const result = []
  for (const menu of menus) {
    result.push(menu)
    if (menu.children) {
      result.push(...flattenMenu(menu.children))
    }
  }
  return result
}

export const hasPermission = (role, path) => {
  const menus = getMenuByRole(role)
  const flatMenus = flattenMenu(menus)
  return flatMenus.some(menu => menu.path === path)
}