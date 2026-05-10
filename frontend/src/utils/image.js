const productSeeds = [
  'apple', 'milk', 'vegetable', 'chocolate', 'water',
  'bread', 'pizza', 'coffee', 'juice', 'eggs',
  'yogurt', 'tuna', 'strawberry', 'rice', 'snack',
  'tea', 'formula', 'cheese', 'oil', 'meat'
]

const bannerSeeds = ['supermarket', 'fresh', 'grocery', 'sale']

const getProductSeed = (id) => productSeeds[(id - 1) % productSeeds.length]
const getBannerSeed = (id) => bannerSeeds[id % bannerSeeds.length]

// 生成产品图片 - 使用 picsum.photos
export const getProductImage = (id, size = '300/300') => {
  return `https://picsum.photos/300/300?random=${id}`
}

// 生成轮播图 - 使用 picsum.photos
export const getBannerImage = (id) => {
  return `https://picsum.photos/800/320?random=banner${id}`
}

// 头像
export const getAvatarImage = (id) => {
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=user${id}`
}

// 分类图片 - 使用 picsum.photos
export const getCategoryImage = (categoryName) => {
  const map = {
    '生鲜食品': 'fresh1',
    '日用百货': 'daily1',
    '饮料零食': 'snack1',
    '粮油调味': 'oil1',
    '家居用品': 'home1',
    '个人护理': 'care1'
  }
  const seed = map[categoryName] || 'category'
  return `https://picsum.photos/300/300?random=${seed}`
}
