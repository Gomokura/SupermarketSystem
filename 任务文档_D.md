# 任务文档 —— 成员 D：后台管理（商品管理 / 分类管理 / 用户管理）

> 技术栈：Vue 3 `<script setup>` + Vite + Element Plus + Pinia + Vue Router 4 + Axios
> API 统一从 `src/api/index.js` 引入：
> ```js
> import { productAPI, adminAPI } from '@/api'
> ```

---

## 一、你的负责模块

| 文件路径 | 页面名称 |
|---|---|
| `src/views/admin/Products.vue` | 后台商品管理页 |
| `src/views/admin/Categories.vue` | 后台分类管理页 |
| `src/views/admin/Users.vue` | 后台用户管理页 |

> 以上三个页面均嵌套在 `src/views/admin/Layout.vue` 中，你无需修改 Layout，只实现各子页面内容即可。

---

## 二、各页面要实现的功能

### 2.1 admin/Products.vue — 商品管理页

#### 功能清单

1. **顶部工具栏**
   - 左侧：关键词搜索框（`el-input` + 搜索按钮），分类下拉筛选（`el-select`，选项来自 `productAPI.getCategories()`）。
   - 右侧：「新增商品」按钮（`el-button type="primary"`）。

2. **商品数据表格**
   - 使用 `el-table` + `el-table-column` 展示，列：
     - 商品图片（宽 80px，用 `<el-image>` 组件，加 `fit="cover"` 和 `lazy` 懒加载）
     - 商品名称
     - 所属分类（根据 `categoryId` 匹配分类名称展示）
     - 价格（右对齐，`¥ x.xx` 格式）
     - 库存（库存 ≤ 10 时用红色字体警示）
     - 操作列：「编辑」「删除」两个按钮

3. **分页**
   - `el-pagination` 放在表格下方，支持切换每页条数（`[10, 20, 50]`）。

4. **新增商品（el-dialog 弹窗）**
   - 表单字段（均必填）：
     - 商品名称（`el-input`）
     - 所属分类（`el-select`，选项来自分类列表）
     - 价格（`el-input-number`，最小值 0.01，精度 2）
     - 库存（`el-input-number`，最小值 0，步长 1）
     - 商品图片 URL（`el-input`，可先用文本输入图片链接，有余力再做图片上传）
     - 商品描述（`el-input type="textarea"`，最大 500 字）
   - 提交调用 `productAPI.add(data)`，成功后关闭 dialog 并刷新表格。

5. **编辑商品（复用同一个 el-dialog）**
   - 点击「编辑」时将该行数据深拷贝（`{ ...row }`）回填到表单。
   - 提交调用 `productAPI.update(data)`（需要带 `productId`）。

6. **删除商品**
   - `ElMessageBox.confirm('确认删除该商品？删除后不可恢复。', '警告', { type: 'warning' })` 二次确认。
   - 确认后调用 `productAPI.delete(id)`，成功后刷新表格数据。

7. **新增 / 编辑共用逻辑**
   - 用 `isEdit` 布尔值区分，dialog 标题动态显示「新增商品」或「编辑商品」。
   - 每次打开 dialog 前：新增时 `resetFields()`，编辑时先 `resetFields()` 再回填数据。
   - 提交时调用 `formRef.value.validate()` 通过后再发请求。

---

### 2.2 admin/Categories.vue — 分类管理页

#### 功能清单

1. **分类列表**
   - 调用 `productAPI.getCategories()` 获取全部分类。
   - 用 `el-table` 展示，列：
     - 分类图标（`iconUrl`，用 `<el-image>` 展示，宽 50px，若无图标显示占位）
     - 分类名称（`categoryName`）
     - 分类描述（`description`）
     - 操作列：「编辑」「删除」

   > 注意：分类接口无分页（数据量小），直接全量展示即可。

2. **新增分类**
   - 「新增分类」按钮（页面右上角）打开 `el-dialog`。
   - 表单字段：
     - 分类名称（必填，`el-input`，`maxlength="20"`）
     - 描述（可选，`el-input type="textarea"`）
   - 提交调用 `productAPI.addCategory({ categoryName, description })`。

3. **编辑分类**
   - 复用同一 dialog，回填数据后调用 `productAPI.updateCategory({ categoryId, categoryName, description })`。

4. **删除分类**
   - `ElMessageBox.confirm` 二次确认。
   - 调用 `productAPI.deleteCategory(id)`。
   - **重要提示**：若该分类下存在商品，后端可能拒绝删除（返回错误信息），前端需捕获并展示后端的错误原因（从 `error.response.data.message` 取），不能只显示「删除失败」。

5. **操作成功即时更新**
   - 新增 / 编辑 / 删除成功后，重新调用 `productAPI.getCategories()` 刷新列表（也可以乐观更新本地数组，但刷新更稳妥）。

---

### 2.3 admin/Users.vue — 用户管理页

#### 功能清单

1. **顶部工具栏**
   - 关键词搜索框（搜索用户名 / 真实姓名 / 手机号）。
   - 搜索按钮和重置按钮。

2. **用户数据表格**
   - 用 `el-table` 展示，列：
     - 用户 ID
     - 用户名（`username`）
     - 真实姓名（`realName`）
     - 手机号（`phone`）
     - 角色（`role`，用 `el-tag` 区分：管理员 `type="danger"`，普通用户 `type=""`）
     - 状态（用 `el-switch` 展示，绑定 `status === '正常'`，用户可直接拨动开关启用/禁用）
     - 操作列（可选：「查看详情」按钮，若有详情页则跳转）

3. **启用 / 禁用用户**
   - `el-switch` 的 `before-change` 钩子（或 `change` 事件）弹出 `ElMessageBox.confirm`：
     - 禁用时提示「确认禁用该用户？禁用后该用户将无法登录。」
     - 启用时提示「确认启用该用户？」
   - 确认后调用 `adminAPI.updateUserStatus(userId, newStatus)`。
     - `newStatus` 为 `'正常'` 或 `'禁用'`（字符串）。
   - 失败时恢复 switch 原始状态（用 `return false` 阻止 `before-change` 或在 `catch` 中回滚）。

4. **分页**
   - `el-pagination`，`pageSize` 默认 10。
   - 搜索条件变化时重置 `pageNum` 为 1。

5. **禁止操作当前登录账户**
   - 从 Pinia 用户 store 获取当前登录账户的 `userId`，若表格中某行的 `userId` 与当前登录账户相同，则该行 `el-switch` 禁用（`disabled`），避免管理员把自己禁用。

---

## 三、可用的 API 函数

### productAPI（商品管理 + 分类管理）

| 函数签名 | 说明 | 返回值 |
|---|---|---|
| `productAPI.getList({ keyword, categoryId, pageNum, pageSize })` | 商品分页列表，支持关键词和分类筛选 | `{ records: [], total, pageNum, pageSize }` |
| `productAPI.getById(id)` | 商品详情（编辑时可用于获取最新数据） | `{ productId, productName, price, stock, description, imageUrl, categoryId }` |
| `productAPI.getCategories()` | 获取所有分类（下拉框选项来源） | `[{ categoryId, categoryName, iconUrl }]` |
| `productAPI.add(data)` | 新增商品 | — |
| `productAPI.update(data)` | 修改商品（data 需含 `productId`） | — |
| `productAPI.delete(id)` | 删除商品 | — |
| `productAPI.addCategory(data)` | 新增分类，`data: { categoryName, description }` | — |
| `productAPI.updateCategory(data)` | 修改分类，`data` 需含 `categoryId` | — |
| `productAPI.deleteCategory(id)` | 删除分类 | — |

### adminAPI（用户管理）

| 函数签名 | 说明 | 返回值 |
|---|---|---|
| `adminAPI.getUsers({ keyword, pageNum, pageSize })` | 获取用户分页列表，支持关键词搜索 | `{ records: [{ userId, username, realName, phone, role, status }], total }` |
| `adminAPI.updateUserStatus(userId, status)` | 启用或禁用用户，`status` 为 `'正常'` 或 `'禁用'` | — |

---

## 四、后端接口说明

### 商品相关

| 方法 | 路径 | 参数说明 |
|---|---|---|
| GET | `/products/list` | Query: `keyword`、`categoryId`、`pageNum`、`pageSize` |
| GET | `/products/{id}` | Path: `id` |
| POST | `/products` | Body: `{ productName, categoryId, price, stock, description, imageUrl }` |
| PUT | `/products` | Body: `{ productId, productName, categoryId, price, stock, description, imageUrl }` |
| DELETE | `/products/{id}` | Path: `id` |

### 分类相关

| 方法 | 路径 | 参数说明 |
|---|---|---|
| GET | `/products/categories` | 无参数 |
| POST | `/products/categories` | Body: `{ categoryName, description }` |
| PUT | `/products/categories` | Body: `{ categoryId, categoryName, description }` |
| DELETE | `/products/categories/{id}` | Path: `id` |

### 用户管理

| 方法 | 路径 | 参数说明 |
|---|---|---|
| GET | `/admin/users` | Query: `keyword`、`pageNum`、`pageSize` |
| PUT | `/admin/users/{userId}/status` | Path: `userId`；Query: `status`（`'正常'` 或 `'禁用'`） |

> **权限说明**：所有 `/admin/*` 接口需要管理员身份。Axios 拦截器已统一附加 Token，但若非管理员访问这些接口，后端会返回 403，前端应在路由守卫层面阻止普通用户进入 `/admin/*` 路由。

---

## 五、注意事项

1. **分类名在表格中的映射**
   商品表格展示分类名时，需要把 `categoryId` 转成 `categoryName`。建议在 `onMounted` 时同时拉取分类列表，存为一个 `Map`：
   ```js
   const categoryMap = ref({})  // { categoryId: categoryName }
   const fetchCategories = async () => {
     const res = await productAPI.getCategories()
     res.data.forEach(c => { categoryMap.value[c.categoryId] = c.categoryName })
   }
   ```
   表格中使用：
   ```html
   <el-table-column label="分类">
     <template #default="{ row }">
       {{ categoryMap[row.categoryId] || '未知分类' }}
     </template>
   </el-table-column>
   ```

2. **el-input-number 的精度**
   价格字段设置 `:precision="2" :step="0.1"`；库存字段设置 `:precision="0" :step="1" :min="0"`。

3. **表单 `rules` 示例**
   ```js
   const rules = {
     productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
     categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
     price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
     stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
     imageUrl: [{ required: true, message: '请输入图片链接', trigger: 'blur' }]
   }
   ```

4. **库存预警颜色**
   ```html
   <el-table-column label="库存">
     <template #default="{ row }">
       <span :style="{ color: row.stock <= 10 ? '#f56c6c' : '' }">
         {{ row.stock }}
       </span>
     </template>
   </el-table-column>
   ```

5. **`el-switch` 状态回滚**
   `el-switch` 绑定的值是计算属性（`status === '正常'`），不能直接双向绑定到原始字符串。推荐写法：
   ```html
   <el-switch
     :model-value="row.status === '正常'"
     @change="(val) => handleStatusChange(row, val)"
   />
   ```
   `handleStatusChange` 内部调用接口，失败时不更新本地数据（状态自动还原）。

6. **删除分类前提示**
   若后端因「分类下有商品」拒绝删除，会返回错误信息，前端要显示给用户看：
   ```js
   try {
     await productAPI.deleteCategory(id)
     ElMessage.success('删除成功')
     fetchCategories()
   } catch (e) {
     ElMessage.error(e.response?.data?.message || '删除失败，该分类下可能存在商品')
   }
   ```

7. **搜索防抖 / 即时搜索**
   建议搜索框 `@keyup.enter` 触发搜索，不做实时搜索（避免频繁请求）。点「搜索按钮」也触发搜索，点「重置」清空关键词并重新查询全部。

8. **表格 loading 状态**
   每次请求前 `loading.value = true`，`finally` 中 `loading.value = false`，绑定到 `el-table` 的 `v-loading` 指令，提供视觉反馈。

9. **图片 URL 预览**
   编辑商品时，输入图片 URL 后可实时预览：
   ```html
   <el-input v-model="form.imageUrl" placeholder="请输入图片链接" />
   <el-image v-if="form.imageUrl" :src="form.imageUrl" style="width:100px;height:100px;margin-top:8px" fit="cover" />
   ```

10. **统一后台页面风格**
    所有后台页面顶部用 `<div class="page-header">` 包裹页面标题 + 操作按钮，内容区用 `el-card` 包裹表格，保持与其他后台页面风格一致（参考 Dashboard.vue 的布局结构）。
