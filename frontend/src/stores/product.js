import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 模拟的初始商品数据
const mockProducts = [
  {
    productId: '1',
    name: '新鲜苹果',
    price: 9.9,
    originalPrice: 12.9,
    categoryId: '1',
    categoryName: '水果',
    stock: 100,
    sales: 50,
    status: 'active',
    image: '',
    description: '新鲜的红富士苹果'
  },
  {
    productId: '2',
    name: '牛奶',
    price: 5.5,
    originalPrice: 6.5,
    categoryId: '2',
    categoryName: '饮品',
    stock: 200,
    sales: 80,
    status: 'active',
    image: '',
    description: '新鲜牛奶'
  },
  {
    productId: '3',
    name: '面包',
    price: 3.5,
    originalPrice: 4.5,
    categoryId: '3',
    categoryName: '食品',
    stock: 150,
    sales: 60,
    status: 'active',
    image: '',
    description: '松软面包'
  }
]

export const useProductStore = defineStore('product', () => {
  const products = ref([])
  const categories = ref([
    { categoryId: '1', categoryName: '水果' },
    { categoryId: '2', categoryName: '饮品' },
    { categoryId: '3', categoryName: '食品' }
  ])

  // 初始化商品数据
  const initProducts = () => {
    if (products.value.length === 0) {
      products.value = [...mockProducts]
    }
  }

  // 添加商品
  const addProduct = (product) => {
    products.value.push({
      ...product,
      productId: Date.now().toString(),
      sales: 0,
      status: 'active'
    })
  }

  // 更新商品
  const updateProduct = (productId, data) => {
    const index = products.value.findIndex(p => p.productId === productId)
    if (index !== -1) {
      products.value[index] = { ...products.value[index], ...data }
    }
  }

  // 删除商品
  const deleteProduct = (productId) => {
    const index = products.value.findIndex(p => p.productId === productId)
    if (index !== -1) {
      products.value[index].status = 'inactive'
    }
  }

  // 真正删除（物理删除）
  const realDeleteProduct = (productId) => {
    products.value = products.value.filter(p => p.productId !== productId)
  }

  // 上下架商品
  const toggleStatus = (productId) => {
    const product = products.value.find(p => p.productId === productId)
    if (product) {
      product.status = product.status === 'active' ? 'inactive' : 'active'
    }
  }

  // 获取商品列表
  const getProductList = (params = {}) => {
    let list = [...products.value]

    if (params.keyword) {
      list = list.filter(p =>
        p.name.includes(params.keyword) ||
        p.productId === params.keyword
      )
    }

    if (params.categoryId) {
      list = list.filter(p => p.categoryId === params.categoryId)
    }

    if (params.status) {
      list = list.filter(p => p.status === params.status)
    }

    return list
  }

  // 根据ID获取商品详情
  const getProductById = (productId) => {
    return products.value.find(p => p.productId === productId)
  }

  // 更新库存
  const updateStock = (productId, quantity) => {
    const product = products.value.find(p => p.productId === productId)
    if (product) {
      product.stock = Math.max(0, product.stock - quantity)
    }
  }

  // 增加销量
  const addSales = (productId, quantity) => {
    const product = products.value.find(p => p.productId === productId)
    if (product) {
      product.sales += quantity
    }
  }

  return {
    products,
    categories,
    initProducts,
    addProduct,
    updateProduct,
    deleteProduct,
    realDeleteProduct,
    toggleStatus,
    getProductList,
    getProductById,
    updateStock,
    addSales
  }
}, {
  persist: {
    storage: localStorage,
    key: 'supermarket-products',
    paths: ['products', 'categories']
  }
})
