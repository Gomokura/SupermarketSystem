# 任务文档 —— 成员 B：商城前台（首页 / 商品列表 / 购物车）

> 技术栈：Vue 3 `<script setup>` + Vite + Element Plus + Pinia + Vue Router 4 + Axios
> API 统一从 `src/api/index.js` 引入：
> ```js
> import { productAPI, cartAPI } from '@/api'
> ```

---

## 一、你的负责模块

| 文件路径 | 页面名称 |
|---|---|
| `src/views/user/Home.vue` | 首页 |
| `src/views/user/Products.vue` | 商品列表页（全部商品，带搜索 / 筛选 / 分页） |
| `src/views/user/Cart.vue` | 购物车页面 |

---

## 二、各页面要实现的功能

### 2.1 Home.vue — 首页

#### 功能清单

1. **分类导航栏**
   - 页面加载时调用 `productAPI.getCategories()` 获取所有分类。
   - 用横向滚动或 `el-scrollbar` 展示分类图标 + 分类名，点击某分类跳转到商品列表页并自动筛选该分类（`/products?categoryId=xxx`）。

2. **搜索框**
   - 顶部居中放一个 `el-input`（type="search"，带搜索图标按钮）。
   - 按回车或点击搜索按钮跳转到 `/products?keyword=xxx`。

3. **Banner 轮播图**
   - 使用 `el-carousel` + `el-carousel-item` 实现自动轮播。
   - 轮播图数据可写死（3～5 张本地图片或占位图），无需后端接口。

4. **推荐商品 / 热销商品列表**
   - 调用 `productAPI.getList({ pageNum: 1, pageSize: 8 })` 获取前 8 条商品作为推荐展示。
   - 用商品卡片（`el-card`）网格排列（`el-row` + `el-col`，每行 4 个）展示：商品图片、名称、价格。
   - 点击商品卡片跳转到商品详情页（路由路径参考现有路由配置，一般为 `/product/:id`）。
   - 每张卡片右下角有「加入购物车」按钮，点击调用 `cartAPI.add(productId, 1)`，成功后用 `ElMessage.success('已加入购物车')` 提示。

5. **页面整体布局**
   - 使用项目已有的 Layout 布局（Header + 主内容区），不需要自己写顶部导航。
   - 内容区留好上下内边距（`padding: 16px`）。

#### 数据流参考

```js
// 分类列表
const categories = ref([])
onMounted(async () => {
  const res = await productAPI.getCategories()
  categories.value = res.data  // 根据后端实际返回结构取
})

// 推荐商品
const hotProducts = ref([])
onMounted(async () => {
  const res = await productAPI.getList({ pageNum: 1, pageSize: 8 })
  hotProducts.value = res.data.records
})
```

---

### 2.2 Products.vue — 商品列表页

#### 功能清单

1. **左侧分类菜单 / 顶部分类 Tab**
   - 调用 `productAPI.getCategories()` 获取分类列表。
   - 提供「全部」选项（`categoryId` 为空）。
   - 点击某分类时更新 `categoryId` 并重新请求商品列表（回到第 1 页）。
   - 当前选中分类高亮显示。
   - URL 中的 `?categoryId=` 和 `?keyword=` 参数要能从路由读取并回填到筛选条件（方便从首页跳转过来后自动筛选）。

2. **顶部搜索框**
   - 同首页搜索框，输入关键词按回车或点击搜索按钮，更新 `keyword` 参数并重新请求（回到第 1 页）。
   - 已输入关键词时显示清空按钮（`clearable`）。

3. **商品列表展示（网格卡片）**
   - 每行 4 个商品卡片（响应式可酌情调整）。
   - 每张卡片展示：商品图片（固定高度，`object-fit: cover`）、商品名称（最多显示 2 行，超出省略）、价格（红色加粗）。
   - 点击卡片跳转商品详情页。
   - 卡片底部「加入购物车」按钮，调用 `cartAPI.add(productId, 1)`，成功提示。
   - 若列表为空，用 `el-empty` 展示「暂无商品」。

4. **分页**
   - 列表底部用 `el-pagination` 组件。
   - 支持切换每页条数（`page-sizes: [12, 24, 48]`）和跳转页码。
   - 切换后调用接口并滚动到页面顶部（`window.scrollTo(0, 0)`）。

5. **加载状态**
   - 请求期间用 `v-loading="loading"` 遮罩整个商品列表区域，防止用户重复操作。

#### 核心数据结构参考

```js
const keyword = ref('')
const categoryId = ref(null)
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const products = ref([])
const loading = ref(false)

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await productAPI.getList({
      keyword: keyword.value || undefined,
      categoryId: categoryId.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    products.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

// 从路由参数初始化
onMounted(() => {
  const query = route.query
  keyword.value = query.keyword || ''
  categoryId.value = query.categoryId ? Number(query.categoryId) : null
  fetchProducts()
})

// 监听路由变化（从首页点分类跳过来时触发）
watch(() => route.query, (q) => {
  keyword.value = q.keyword || ''
  categoryId.value = q.categoryId ? Number(q.categoryId) : null
  pageNum.value = 1
  fetchProducts()
})
```

---

### 2.3 Cart.vue — 购物车页面

#### 功能清单

1. **购物车列表**
   - 页面加载时调用 `cartAPI.getList()` 获取购物车条目。
   - 用 `el-table` 展示，列：
     - 勾选框（`el-table-column type="selection"`）
     - 商品图片（宽 80px）
     - 商品名称
     - 单价
     - 数量（`el-input-number`，最小值 1，最大值可不限）
     - 小计（= 单价 × 数量，实时计算）
     - 操作（「删除」按钮）
   - 若购物车为空，用 `el-empty` + 「去购物」按钮（跳转 `/products`）。

2. **修改数量**
   - `el-input-number` 的 `change` 事件触发 `cartAPI.updateQuantity(cartId, newQuantity)`。
   - 接口调用失败时恢复原数量并弹出错误提示。

3. **删除单条**
   - 点击「删除」后，先用 `ElMessageBox.confirm('确认删除？')` 二次确认。
   - 确认后调用 `cartAPI.remove(cartId)`，成功后从本地列表中移除该条，无需重新请求全列表。

4. **全选 / 反选**
   - 利用 `el-table` 自带的 `selection-change` 事件维护 `selectedItems` 数组。
   - 提供「全选」勾选框（`el-table` 默认表头勾选框已支持）。

5. **底部汇总栏（固定在底部或跟随内容）**
   - 已选 N 件商品，合计：**¥ xxx.xx**（红色加粗）。
   - 「清空购物车」按钮：调用 `cartAPI.clear()`，成功后清空本地列表，弹出 `ElMessage.success`。
   - 「去结算」按钮：
     - 若无选中商品，弹出 `ElMessage.warning('请先选择商品')`。
     - 否则将选中的 `cartId` 列表存入 Pinia store（或 sessionStorage），然后路由跳转到 `/checkout`。

6. **价格计算（纯前端）**
   ```js
   const totalPrice = computed(() =>
     selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
   )
   ```

---

## 三、可用的 API 函数

### productAPI

| 函数 | 说明 | 返回值关键字段 |
|---|---|---|
| `productAPI.getList({ keyword, categoryId, pageNum, pageSize })` | 商品分页列表，支持关键词搜索和分类筛选 | `{ records: [], total, pageNum, pageSize }` |
| `productAPI.getById(id)` | 根据 ID 获取商品详情（详情页用，本模块备用） | `{ productId, productName, price, stock, description, imageUrl, categoryId }` |
| `productAPI.getCategories()` | 获取全部分类列表 | `[{ categoryId, categoryName, iconUrl }]` |

### cartAPI

| 函数 | 说明 | 备注 |
|---|---|---|
| `cartAPI.getList()` | 获取当前登录用户的购物车列表 | 返回 `[{ cartId, productId, productName, price, imageUrl, quantity }]` |
| `cartAPI.add(productId, quantity)` | 添加商品到购物车 | 已在购物车中则合并数量（后端处理） |
| `cartAPI.updateQuantity(cartId, quantity)` | 修改购物车某条商品的数量 | quantity 须 ≥ 1 |
| `cartAPI.remove(cartId)` | 删除购物车某条 | — |
| `cartAPI.clear()` | 清空购物车 | — |

---

## 四、后端接口说明

| 方法 | 路径 | 参数说明 |
|---|---|---|
| GET | `/products/list` | Query: `keyword`（可选）、`categoryId`（可选）、`pageNum`（默认1）、`pageSize`（默认10） |
| GET | `/products/{id}` | Path: `id` |
| GET | `/products/categories` | 无参数 |
| GET | `/cart/list` | 无参数，需登录态（Token in Header） |
| POST | `/cart/add` | Query: `productId`、`quantity` |
| PUT | `/cart/update` | Query: `cartId`、`quantity` |
| DELETE | `/cart/{cartId}` | Path: `cartId` |
| DELETE | `/cart/clear` | 无参数 |

> **认证说明**：所有 `/cart/*` 接口需要登录，Axios 拦截器已统一在请求头添加 `Authorization: Bearer {token}`，你无需手动处理，但请确保用户未登录时有跳转登录页的逻辑（一般在路由守卫中已处理）。

---

## 五、注意事项

1. **图片加载失败处理**
   给所有 `<img>` 加上 `@error` 兜底占位图：
   ```html
   <img :src="item.imageUrl" @error="e => e.target.src = '/placeholder.png'" />
   ```

2. **价格精度显示**
   统一用 `.toFixed(2)` 格式化，或封装一个 `formatPrice(price)` 工具函数放在 `src/utils/format.js`。

3. **分页回到顶部**
   `el-pagination` 的 `current-change` 和 `size-change` 事件回调里加 `window.scrollTo({ top: 0, behavior: 'smooth' })`。

4. **防抖搜索**
   搜索框如果做实时搜索（`input` 事件），请加 300ms 防抖（`lodash.debounce` 或手写 `setTimeout`）；如果只在按回车时搜索则不需要。

5. **购物车数量徽章**
   顶部导航栏购物车图标上的数字徽章，建议用 Pinia store 统一管理购物车数量。在 `cartAPI.getList()` 成功后更新 store，`add` / `remove` / `clear` 后也同步更新。

6. **结算前的 cartIds 传递**
   推荐将选中的 `cartId` 数组存到 `sessionStorage`（`JSON.stringify`），Checkout 页面读取时再 `JSON.parse`。避免刷新丢失。

7. **el-input-number 防连续请求**
   数量修改时，若用户快速连点 `+/-`，会触发多次 `change` 事件。建议在 `change` 回调中加 loading 状态锁，请求完成后再解锁。

8. **空状态**
   商品列表为空时不要只显示空白，一定要用 `el-empty` 组件并附带引导性文字，如「暂无相关商品，换个关键词试试？」。

9. **路由跳转**
   - 点商品卡片：`router.push(`/product/${productId}`)`
   - 跳转结算：`router.push('/checkout')`
   - 去购物（购物车为空时）：`router.push('/products')`

10. **统一错误处理**
    Axios 响应拦截器应已统一处理错误弹窗，你不需要在每个 API 调用处都写 `catch` 弹窗，但对于需要恢复状态的操作（如数量修改失败要回滚），还是要写 `try/catch`。
