const productSeeds = [
  'apple', 'milk', 'vegetable', 'chocolate', 'water',
  'bread', 'pizza', 'coffee', 'juice', 'eggs',
  'yogurt', 'tuna', 'strawberry', 'rice', 'snack',
  'tea', 'formula', 'cheese', 'oil', 'meat'
]

const bannerSeeds = ['supermarket', 'fresh', 'grocery', 'sale']

const getProductSeed = (id) => productSeeds[(id - 1) % productSeeds.length]
const getBannerSeed = (id) => bannerSeeds[id % bannerSeeds.length]

export const getProductImage = (id, size = '600/600') => {
  const seed = getProductSeed(id)
  return `https://picsum.photos/seed/${seed}${id}/600/600`
}

export const getBannerImage = (id) => {
  const seed = getBannerSeed(id)
  return `https://picsum.photos/seed/banner${seed}${id}/800/320`
}

export const getAvatarImage = (id) => {
  return `https://picsum.photos/seed/avatar${id}/200/200`
}

export const getCategoryImage = (categoryName) => {
  const map = {
    '生鲜食品': 'fresh',
    '日用百货': 'household',
    '饮料零食': 'snack',
    '粮油调味': 'kitchen',
    '家居用品': 'home',
    '个人护理': 'beauty'
  }
  const seed = map[categoryName] || 'product'
  return `https://picsum.photos/seed/cat${seed}/400/300`
}
