# 任务文档 —— 成员 C：结算 / 订单 / 地址模块

> 技术栈：Vue 3 `<script setup>` + Vite + Element Plus + Pinia + Vue Router 4 + Axios
> API 统一从 `src/api/index.js` 引入：
> ```js
> import { orderAPI, addressAPI, cartAPI } from '@/api'
> ```

---

## 一、你的负责模块

| 文件路径 | 页面名称 |
|---|---|
| `src/views/user/Checkout.vue` | 结算页（下单确认页） |
| `src/views/user/Orders.vue` | 我的订单列表页 |
| `src/views/user/OrderDetail.vue` | 订单详情页 |
| `src/views/user/Address.vue` | 收货地址管理页 |

---

## 二、各页面要实现的功能

### 2.1 Checkout.vue — 结算页

#### 功能清单

1. **读取待结算商品**
   - 页面初始化时从 `sessionStorage` 读取成员 B 存入的 `cartIds`（`JSON.parse(sessionStorage.getItem('checkoutCartIds'))`）。
   - 若读不到则弹出提示并跳回 `/cart`。
   - 根据 cartId 从购物车接口（`cartAPI.getList()`）中过滤出对应商品，展示商品清单（名称、单价、数量、小计）。

2. **选择收货地址**
   - 调用 `addressAPI.getList()` 获取地址列表。
   - 用 `el-radio-group` 展示所有地址，格式：`{receiverName} {phone} {province}{city}{district}{detail}`。
   - 默认选中 `isDefault === true` 的地址（`isDefault` 可能是 `1` 或 `true`，以后端实际为准）。
   - 提供「新增地址」按钮，点击弹出新增地址的 `el-dialog`（复用 Address.vue 的表单逻辑，或直接内嵌简化版表单）。

3. **备注输入**
   - 一个 `el-input`（`type="textarea"`，`maxlength="200"` 并显示字数统计）供用户填写订单备注。

4. **订单金额汇总**
   - 商品总价：前端根据商品列表计算（`price × quantity` 求和）。
   - 运费：固定写 ¥0.00（免运费），或根据业务写死一个值。
   - 实付款：红色大字显示。

5. **提交订单**
   - 「提交订单」按钮点击时：
     1. 校验：必须选择收货地址，否则 `ElMessage.warning('请选择收货地址')`。
     2. 调用 `orderAPI.create({ addressId, cartIds, remark })`。
     3. 成功后清除 `sessionStorage` 中的 `checkoutCartIds`。
     4. 跳转到订单详情页 `/order/:orderId`（`orderId` 从接口返回值中取）。
     5. 失败时弹出错误信息，不跳转。
   - 按钮在请求期间显示 loading 状态（`:loading="submitting"`）并禁用，防止重复提交。

---

### 2.2 Orders.vue — 我的订单列表页

#### 功能清单

1. **状态 Tab 筛选**
   - 用 `el-tabs` 展示所有订单状态：全部 / 待支付 / 待发货 / 已发货 / 已完成 / 已取消。
   - 切换 Tab 时更新 `status` 参数，重置页码为 1，重新请求列表。

2. **订单列表展示**
   - 每条订单用卡片（`el-card`）展示，内容包括：
     - 订单号（`orderId`）、下单时间、订单状态（用 `el-tag` 不同颜色区分）
     - 商品缩略图列表（`items` 数组，最多展示前 3 张，超出显示「等N件商品」）
     - 订单总金额（红色）
     - 操作按钮区（根据状态决定显示哪些按钮，见下表）

   | 订单状态 | 可操作按钮 |
   |---|---|
   | 待支付 | 「取消订单」「立即支付」（支付按钮可跳转支付页或暂时弹提示，取决于是否有支付模块） |
   | 待发货 | 无（等待商家发货） |
   | 已发货 | 「确认收货」 |
   | 已完成 | 「查看详情」 |
   | 已取消 | 「查看详情」 |

3. **取消订单**
   - 调用 `orderAPI.cancel(orderId)`，调用前用 `ElMessageBox.confirm` 二次确认。
   - 成功后刷新列表（重新调用 `fetchOrders()`）或直接更新本地状态。

4. **确认收货**
   - 调用 `orderAPI.confirm(orderId)`，调用前用 `ElMessageBox.confirm('确认已收到商品？')`。
   - 成功后刷新列表。

5. **分页**
   - `el-pagination`，`pageSize` 默认为 10。

6. **空状态**
   - 当前 Tab 下无订单时展示 `el-empty`，文字如「暂无订单」。

---

### 2.3 OrderDetail.vue — 订单详情页

#### 功能清单

1. **获取订单详情**
   - 从路由参数 `route.params.orderId` 获取 `orderId`。
   - 调用 `orderAPI.getDetail(orderId)` 获取数据。

2. **页面展示内容**
   - **订单状态区**（顶部醒目展示）：当前状态 + 状态描述（如「等待商家发货」）+ 进度步骤条（`el-steps`，步骤：下单 → 待支付 → 待发货 → 已发货 → 已完成）。
   - **收货地址区**：`address.receiverName`、`address.phone`、完整地址拼接。
   - **商品清单区**：`el-table` 展示 `items` 数组，列：商品图片、商品名称、单价、数量、小计。
   - **费用汇总区**：商品总价、运费（¥0.00）、实付金额（加粗红色）。
   - **订单信息区**：订单号、下单时间、备注。

3. **操作按钮**
   - 与订单列表页逻辑相同，根据 `status` 决定显示「取消订单」或「确认收货」。
   - 操作后跳回 `/orders` 或刷新当前页详情。

4. **返回按钮**
   - 页面顶部左侧放「← 返回我的订单」，点击 `router.back()` 或 `router.push('/orders')`。

---

### 2.4 Address.vue — 收货地址管理页

#### 功能清单

1. **地址列表展示**
   - 调用 `addressAPI.getList()` 获取地址列表。
   - 用 `el-card` 列表展示，每条显示：
     - 收货人姓名、手机号
     - 省 / 市 / 区 / 详细地址（拼接展示）
     - 「默认地址」标签（`el-tag type="danger"`）——仅 `isDefault` 为 true 的显示
     - 右侧操作：「编辑」「删除」按钮，非默认地址额外显示「设为默认」按钮

2. **新增地址**
   - 页面右上角「新增地址」按钮，点击打开 `el-dialog`。
   - 表单字段（均为必填）：
     - 收货人姓名（`el-input`）
     - 手机号（`el-input`，校验规则：11 位数字，`/^1[3-9]\d{9}$/`）
     - 省（`el-input` 或省市区级联选择器 `el-cascader`，优先用 `el-cascader`）
     - 市
     - 区
     - 详细地址（`el-input type="textarea"`）
     - 是否设为默认（`el-switch`）
   - 使用 `el-form` + `el-form-item` + `rules` 做表单校验（`:rules="rules"` + `formRef.validate()`）。
   - 提交调用 `addressAPI.add({ receiverName, phone, province, city, district, detail, isDefault })`。

3. **编辑地址**
   - 点击「编辑」打开相同的 `el-dialog`，将选中地址的数据回填到表单（注意深拷贝：`Object.assign({}, selectedAddress)`，避免直接修改列表数据）。
   - 提交调用 `addressAPI.update(data)`（需要带 `addressId`）。

4. **删除地址**
   - `ElMessageBox.confirm` 二次确认后调用 `addressAPI.delete(addressId)`。
   - 成功后从本地列表中移除（避免重新请求全列表）。

5. **新增 / 编辑共用同一个 dialog 和表单**
   - 用一个 `isEdit` 布尔值区分当前 dialog 是新增还是编辑模式，标题和提交逻辑随之切换。
   - 每次打开 dialog 前要重置表单（`formRef.value.resetFields()`），编辑时再回填数据。

---

## 三、可用的 API 函数

### orderAPI

| 函数签名 | 说明 | 返回值关键字段 |
|---|---|---|
| `orderAPI.create({ addressId, cartIds, remark })` | 创建订单，`cartIds` 为数组 | 返回新订单的 `orderId` |
| `orderAPI.getList({ status, pageNum, pageSize })` | 获取我的订单列表，`status` 可不传（查全部） | `{ records: [], total }` |
| `orderAPI.getDetail(orderId)` | 获取订单详情 | `{ orderId, status, totalAmount, items: [], address: {} }` |
| `orderAPI.cancel(orderId)` | 取消订单（仅「待支付」状态可取消） | — |
| `orderAPI.confirm(orderId)` | 确认收货（仅「已发货」状态可确认） | — |

### addressAPI

| 函数签名 | 说明 | 返回值关键字段 |
|---|---|---|
| `addressAPI.getList()` | 获取当前用户所有收货地址 | `[{ addressId, receiverName, phone, province, city, district, detail, isDefault }]` |
| `addressAPI.add({ receiverName, phone, province, city, district, detail, isDefault })` | 新增地址 | — |
| `addressAPI.update(data)` | 修改地址（`data` 中需含 `addressId`） | — |
| `addressAPI.delete(addressId)` | 删除指定地址 | — |

### cartAPI（Checkout.vue 用）

| 函数签名 | 说明 |
|---|---|
| `cartAPI.getList()` | 获取购物车列表，用于在结算页显示商品清单 |

---

## 四、后端接口说明

| 方法 | 路径 | 参数说明 |
|---|---|---|
| POST | `/orders/create` | Body: `{ addressId, cartIds: [1,2,3], remark }` |
| GET | `/orders/list` | Query: `status`（可选）、`pageNum`、`pageSize` |
| GET | `/orders/{orderId}` | Path: `orderId` |
| PUT | `/orders/cancel/{orderId}` | Path: `orderId` |
| PUT | `/orders/confirm/{orderId}` | Path: `orderId` |
| GET | `/addresses/list` | 无参数 |
| POST | `/addresses` | Body: `{ receiverName, phone, province, city, district, detail, isDefault }` |
| PUT | `/addresses` | Body: 同上，但需含 `addressId` |
| DELETE | `/addresses/{addressId}` | Path: `addressId` |

---

## 五、注意事项

1. **cartIds 来源**
   结算页的 `cartIds` 必须从 `sessionStorage` 读取（成员 B 负责写入），读取后第一时间校验是否为有效数组，若无效立刻重定向回购物车页，避免空订单。

2. **提交订单防重复**
   「提交订单」按钮在请求期间必须加 `loading` 状态并 `disabled`，防止网络慢时用户多次点击产生重复订单。

3. **状态判断统一**
   订单状态字符串（`待支付` / `待发货` / `已发货` / `已完成` / `已取消`）在多处使用，建议在 `src/utils/orderStatus.js` 中维护一个映射对象：
   ```js
   export const statusTagType = {
     '待支付': 'warning',
     '待发货': 'primary',
     '已发货': 'success',
     '已完成': '',
     '已取消': 'info'
   }
   ```

4. **`el-steps` 激活步骤计算**
   ```js
   const stepIndex = computed(() => {
     const map = { '待支付': 1, '待发货': 2, '已发货': 3, '已完成': 4, '已取消': 0 }
     return map[order.value.status] ?? 0
   })
   ```

5. **地址表单 Dialog 关闭时重置**
   ```js
   const handleClose = () => {
     formRef.value.resetFields()
     dialogVisible.value = false
   }
   ```
   给 `el-dialog` 的 `@close` 事件绑定 `handleClose`，避免下次打开时残留上次数据。

6. **isDefault 类型一致性**
   后端返回的 `isDefault` 可能是 `0/1` 整数，而 `el-switch` 绑定的是布尔值。提交前注意转换：
   ```js
   // 读取时
   form.isDefault = !!addressData.isDefault
   // 提交时
   addressAPI.add({ ...form, isDefault: form.isDefault ? 1 : 0 })
   ```
   以后端实际接受格式为准，若接受布尔值则无需转换。

7. **操作成功后更新购物车徽章**
   下单成功后，全局购物车数量应清零（或刷新）。若成员 B 已在 Pinia 中维护购物车数量，调用对应的 store action 更新即可。

8. **订单详情页刷新**
   详情页的 `orderId` 来自路由参数，务必用 `watch(() => route.params.orderId, fetchDetail)` 监听参数变化，否则从订单列表同名路由跳转时页面不会刷新。

9. **网络错误提示**
   取消订单 / 确认收货失败时，需明确提示用户原因（如「该订单状态不允许取消」），从 Axios 响应的 `error.response.data.message` 中取，或统一由拦截器处理。

10. **地址为空的兜底**
    结算页如果用户没有任何收货地址，要给出引导提示：「您还没有收货地址，请先添加」，并提供快速新增地址的入口。
