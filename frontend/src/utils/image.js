const API_URL = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image'

const productPrompts = [
  'fresh red apples on white background, studio lighting, product photography',
  'organic milk carton, clean white background, studio shot',
  'fresh vegetables assortment, carrots tomatoes lettuce, white background',
  'premium chocolate bar, elegant packaging, studio lighting',
  'bottled mineral water, blue bottle, clean background',
  'fresh bread loaf, golden crust, bakery product photography',
  'frozen pizza box, premium packaging, white background',
  'coffee beans in burlap sack, rustic style, product photo',
  'orange juice bottle, bright orange color, studio shot',
  'eggs in carton, white background, product photography',
  'yogurt cup, creamy texture, clean packaging',
  'canned tuna, premium can design, white background',
  'fresh strawberries, red ripe berries, studio lighting',
  'rice bag, Asian cuisine, clean packaging',
  'frozen vegetables, green peas carrots, white background',
  'snack chips bag, colorful packaging, product photography',
  'tea box, elegant design, studio shot',
  'baby formula can, premium packaging, white background',
  'cheese block, dairy product, professional photography',
  'cooking oil bottle, golden color, clean background'
]

const bannerPrompts = [
  'supermarket promotion banner, fresh groceries, colorful products, sale discount',
  'summer sale at supermarket, fresh fruits vegetables, vibrant colors',
  'grocery store banner, weekly specials, fresh produce, shopping cart',
  'supermarket grand opening, discount promotion, bright cheerful colors'
]

const getRandomProductPrompt = (id) => {
  return productPrompts[id % productPrompts.length]
}

const getRandomBannerPrompt = (id) => {
  return bannerPrompts[id % bannerPrompts.length]
}

const encodePrompt = (prompt) => {
  return encodeURIComponent(prompt)
}

export const getProductImage = (id, size = 'landscape_4_3') => {
  const prompt = getRandomProductPrompt(id)
  return `${API_URL}?prompt=${encodePrompt(prompt)}&image_size=${size}`
}

export const getBannerImage = (id) => {
  const prompt = getRandomBannerPrompt(id)
  return `${API_URL}?prompt=${encodePrompt(prompt)}&image_size=landscape_16_9`
}

export const getAvatarImage = (id) => {
  const prompts = [
    'friendly young woman portrait, casual smile, natural lighting',
    'professional man portrait, business casual, studio lighting',
    'cheerful young man portrait, casual wear, natural background',
    'elegant woman portrait, warm smile, soft lighting'
  ]
  const prompt = prompts[id % prompts.length]
  return `${API_URL}?prompt=${encodePrompt(prompt)}&image_size=square`
}

export const getCategoryImage = (categoryName) => {
  const categoryMap = {
    '生鲜食品': 'fresh fruits and vegetables, colorful produce, market display',
    '日用百货': 'household cleaning products, various items, white background',
    '饮料零食': 'snacks and drinks assortment, colorful packaging, studio shot',
    '粮油调味': 'cooking oil and spices, kitchen products, clean background',
    '家居用品': 'home decor items, modern design, lifestyle photography',
    '个人护理': 'personal care products, cosmetics, beauty items'
  }
  const prompt = categoryMap[categoryName] || 'supermarket products assortment, colorful display'
  return `${API_URL}?prompt=${encodePrompt(prompt)}&image_size=landscape_4_3`
}