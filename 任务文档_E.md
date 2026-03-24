# 任务文档 —— 成员 E：后台管理（库存管理 / 配送管理 / 促销管理 / 财务报表）

> 技术栈：Vue 3 `<script setup>` + Vite + Element Plus + Pinia + Vue Router 4 + Axios
> API 统一从 `src/api/index.js` 引入：
> ```js
> import { adminAPI } from '@/api'
> ```

---

## 一、你的负责模块

| 文件路径 | 页面名称 |
|---|---|
| `src/views/admin/Inventory.vue` | 库存管理页 |
| `src/views/admin/Deliveries.vue` | 配送管理页 |
| `src/views/admin/Promotions.vue` | 促销活动管理页 |
| `src/views/admin/Finance.vue` | 财务报表页 |

> 以上四个页面均嵌套在 `src/views/admin/Layout.vue` 中，只实现各子页面内容即可。

---

## 二、各页面要实现的功能

### 2.1 admin/Inventory.vue — 库存管理页

#### 功能清单

1. **顶部操作区**
   - 「入库」按钮（`el-button type="success"`）和「出库」按钮（`el-button type="warning"`），点击弹出对应操作 dialog。
   - 筛选条件：商品名称关键词（`el-input`）、操作类型（`el-select`：全部 / 入库 / 出库）。

2. **库存操作日志表格**
   - 调用 `adminAPI.getInventoryLogs({ productId, type, pageNum, pageSize })` 获取数据。
   - 用 `el-table` 展示，列：
     - 日志 ID
     - 商品名称（`productName`）
     - 操作类型（`type`：入库 / 出库，用 `el-tag`：入库 `type="success"`，出库 `type="warning"`）
     - 操作数量（`quantity`）
     - 操作人（`operator`）
     - 操作时间（`createTime`，格式化显示：`YYYY-MM-DD HH:mm:ss`）
   - 按操作时间倒序排列（后端通常已处理，前端无需排序）。

3. **入库操作（el-dialog）**
   - 表单字段：
     - 商品（`el-select`，通过 `productAPI.getList` 或复用已有数据，选项显示商品名+当前库存）
     - 入库数量（`el-input-number`，最小值 1，整数）
   - 提交调用 `adminAPI.warehousing(productId, quantity)`。
   - 成功后弹出 `ElMessage.success('入库成功')`，关闭 dialog 并刷新日志列表。

4. **出库操作（el-dialog）**
   - 同入库，提交调用 `adminAPI.outbound(productId, quantity)`。
   - 出库时需注意：若出库数量超过当前库存，后端会返回错误，前端要展示错误信息（不能只显示「出库失败」）。

5. **分页**
   - `el-pagination`，`pageSize` 默认 10。

---

### 2.2 admin/Deliveries.vue — 配送管理页

#### 功能清单

1. **顶部筛选栏**
   - 配送状态下拉选择（`el-select`）：全部 / 待配送 / 配送中 / 已送达。
   - 搜索按钮和重置按钮。

2. **配送列表表格**
   - 调用 `adminAPI.getDeliveries({ status, pageNum, pageSize })` 获取数据。
   - 用 `el-table` 展示，列：
     - 配送单 ID（`deliveryId`）
     - 关联订单号（`orderId`）
     - 收货人（`receiverName`）
     - 收货人电话（`phone`）
     - 配送地址（`address`）
     - 配送状态（`status`，用 `el-tag` 区分颜色：待配送 `type="warning"`，配送中 `type="primary"`，已送达 `type="success"`）
     - 骑手（`courierName`，若未分配显示「未分配」并标红）
     - 操作列（根据状态显示不同按钮）

3. **分配骑手（el-dialog）**
   - 「分配骑手」按钮（状态为「待配送」时显示）。
   - 弹出 dialog，提供骑手 ID 输入框（`el-input`，暂无骑手列表接口，手动输入 `courierId`）。
   - 提交调用 `adminAPI.assignCourier(deliveryId, courierId)`，成功后刷新列表。

4. **更新配送状态**
   - 「开始配送」按钮（状态为「待配送」且已分配骑手时显示）：调用 `adminAPI.updateDeliveryStatus(deliveryId, '配送中')`。
   - 「标记送达」按钮（状态为「配送中」时显示）：调用 `adminAPI.updateDeliveryStatus(deliveryId, '已送达')`。
   - 操作前均使用 `ElMessageBox.confirm` 二次确认。
   - 成功后刷新当前页列表。

5. **分页**
   - `el-pagination`，`pageSize` 默认 10。

---

### 2.3 admin/Promotions.vue — 促销活动管理页

#### 功能清单

1. **顶部工具栏**
   - 「新建促销」按钮（右上角）。

2. **促销活动列表**
   - 调用 `adminAPI.getPromotions()` 获取全量数据（无分页）。
   - 用 `el-table` 展示，列：
     - 活动名称（`name`）
     - 促销类型（`type`，如：折扣 / 满减，用 `el-tag` 区分）
     - 折扣力度（`discount`，如 `0.9` 显示为「九折」或「-10%」，根据 `type` 决定展示方式）
     - 开始时间（`startTime`，格式化）
     - 结束时间（`endTime`，格式化）
     - 状态（`status`，用 `el-tag`：进行中 `type="success"`，未开始 `type="info"`，已结束 `type="danger"`）
     - 操作列：「编辑」「删除」

3. **新建促销（el-dialog）**
   - 表单字段：
     - 活动名称（`el-input`，必填）
     - 促销类型（`el-select`：折扣 / 满减，必填）
     - 折扣力度（`el-input-number`，必填；折扣类型时为 0~1 的小数如 `0.8`；满减类型时为满减金额如 `50`，`label` 动态变化）
     - 活动时间（`el-date-picker type="datetimerange"`，选择开始和结束时间，必填）
     - 参与商品（`el-select multiple`，选项来自商品列表；可先从 `productAPI.getList` 获取商品供选择；存储 `productIds` 数组）
   - 提交调用 `adminAPI.createPromotion({ name, type, discount, startTime, endTime, productIds })`。
   - `startTime` / `endTime` 为日期字符串，注意格式（通常后端接受 ISO 8601 或 `YYYY-MM-DD HH:mm:ss`，以实际为准）。

4. **编辑促销（复用同一 dialog）**
   - 点击「编辑」回填数据后调用 `adminAPI.updatePromotion(data)`（data 需含 `promotionId`）。
   - 日期范围回填：`dateRange.value = [promotion.startTime, promotion.endTime]`。

5. **删除促销**
   - `ElMessageBox.confirm` 后调用 `adminAPI.deletePromotion(promotionId)`，成功后刷新列表。

6. **自动计算状态**（可在前端根据时间判断，若后端已提供 `status` 字段则直接用）：
   ```js
   const getStatus = (p) => {
     const now = Date.now()
     const start = new Date(p.startTime).getTime()
     const end = new Date(p.endTime).getTime()
     if (now < start) return '未开始'
     if (now > end) return '已结束'
     return '进行中'
   }
   ```

---

### 2.4 admin/Finance.vue — 财务报表页

> 本页面以**数据展示**为主，无增删改操作，重点在于图表渲染和数据格式化。
> 推荐使用 **ECharts**（`npm install echarts`，按需引入）或项目中已集成的图表库。

#### 功能清单

1. **顶部数据概览卡片**
   - 调用 `adminAPI.getFinanceData()` 获取数据。
   - 用 `el-row` + `el-col` 展示 4 个统计卡片（`el-card`）：
     - 总营收（`totalRevenue`，`¥ xxx,xxx.xx`）
     - 本月营收（`monthRevenue`）
     - 总订单数（从 `orderStats` 中取，如 `orderStats.total`）
     - 本月订单数（`orderStats.thisMonth`）
   - 每张卡片展示图标 + 数值 + 标签，颜色区分（参考 Element Plus 的 `el-statistic` 组件，Element Plus ≥ 2.2.28 已内置）。

2. **月度营收折线图**
   - 数据来源：`revenueByMonth: [{ month: '2026-01', revenue: 12345.67 }, ...]`
   - 用 ECharts 渲染折线图，X 轴为月份，Y 轴为营收金额（单位：元）。
   - 图表容器用一个带固定高度的 `div`：`<div ref="chartRef" style="height:360px"></div>`。
   - ECharts 初始化：
     ```js
     import * as echarts from 'echarts'
     const chartRef = ref(null)
     let chartInstance = null
     onMounted(async () => {
       const res = await adminAPI.getFinanceData()
       const data = res.data
       // 渲染图表
       chartInstance = echarts.init(chartRef.value)
       chartInstance.setOption({
         xAxis: { type: 'category', data: data.revenueByMonth.map(i => i.month) },
         yAxis: { type: 'value' },
         tooltip: { trigger: 'axis' },
         series: [{
           data: data.revenueByMonth.map(i => i.revenue),
           type: 'line',
           smooth: true,
           areaStyle: {}
         }]
       })
     })
     // 响应式：窗口大小变化时重绘
     window.addEventListener('resize', () => chartInstance?.resize())
     onUnmounted(() => window.removeEventListener('resize', () => chartInstance?.resize()))
     ```

3. **订单状态分布饼图（可选，有余力再做）**
   - 数据来源：`orderStats`（字段以后端实际为准，可能含各状态订单数量）。
   - 用 ECharts 饼图展示各状态订单占比。

4. **页面布局**
   - 顶部：4 个统计卡片（一行四列）。
   - 中间：月度营收折线图（宽度 100% 或 `el-col :span="16"`），右侧可放饼图（`el-col :span="8"`）。
   - 整体用 `el-card` 包裹各个区块，留好间距。

---

## 三、可用的 API 函数

### adminAPI

| 函数签名 | 说明 | 返回值关键字段 |
|---|---|---|
| `adminAPI.getInventoryLogs({ productId, type, pageNum, pageSize })` | 获取库存操作日志，支持商品筛选和类型筛选 | `[{ logId, productName, type, quantity, operator, createTime }]` |
| `adminAPI.warehousing(productId, quantity)` | 商品入库，增加库存 | — |
| `adminAPI.outbound(productId, quantity)` | 商品出库，减少库存（超出库存时后端报错） | — |
| `adminAPI.getDeliveries({ status, pageNum, pageSize })` | 获取配送单列表，可按状态筛选 | `[{ deliveryId, orderId, receiverName, phone, address, status, courierId, courierName }]` |
| `adminAPI.assignCourier(deliveryId, courierId)` | 为配送单分配骑手 | — |
| `adminAPI.updateDeliveryStatus(deliveryId, status)` | 更新配送状态（`'配送中'` 或 `'已送达'`） | — |
| `adminAPI.getPromotions()` | 获取所有促销活动（全量） | `[{ promotionId, name, type, discount, startTime, endTime, status }]` |
| `adminAPI.createPromotion({ name, type, discount, startTime, endTime, productIds })` | 新建促销活动 | — |
| `adminAPI.updatePromotion(data)` | 修改促销（data 需含 `promotionId`） | — |
| `adminAPI.deletePromotion(promotionId)` | 删除促销活动 | — |
| `adminAPI.getFinanceData()` | 获取财务报表数据 | `{ totalRevenue, monthRevenue, orderStats, revenueByMonth: [] }` |

---

## 四、后端接口说明

### 库存管理

| 方法 | 路径 | 参数说明 |
|---|---|---|
| GET | `/admin/inventory/logs` | Query: `productId`（可选）、`type`（可选：`入库`/`出库`）、`pageNum`、`pageSize` |
| POST | `/admin/inventory/warehousing` | Query: `productId`、`quantity` |
| POST | `/admin/inventory/outbound` | Query: `productId`、`quantity` |

### 配送管理

| 方法 | 路径 | 参数说明 |
|---|---|---|
| GET | `/admin/deliveries` | Query: `status`（可选）、`pageNum`、`pageSize` |
| PUT | `/admin/deliveries/assign` | Query: `deliveryId`、`courierId` |
| PUT | `/admin/deliveries/status` | Query: `deliveryId`、`status` |

### 促销管理

| 方法 | 路径 | 参数说明 |
|---|---|---|
| GET | `/admin/promotions` | 无参数 |
| POST | `/admin/promotions` | Body: `{ name, type, discount, startTime, endTime, productIds }` |
| PUT | `/admin/promotions` | Body: 同上，加 `promotionId` |
| DELETE | `/admin/promotions/{promotionId}` | Path: `promotionId` |

### 财务报表

| 方法 | 路径 | 参数说明 |
|---|---|---|
| GET | `/admin/finance` | 无参数 |

> **权限说明**：所有接口需要管理员 Token。Axios 拦截器已统一处理，无需手动加 Header。

---

## 五、注意事项

1. **ECharts 按需引入（减少打包体积）**
   若项目对打包体积有要求，用按需引入方式：
   ```js
   import { init, use } from 'echarts/core'
   import { LineChart } from 'echarts/charts'
   import { GridComponent, TooltipComponent } from 'echarts/components'
   import { CanvasRenderer } from 'echarts/renderers'
   use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])
   ```
   若嫌麻烦，直接 `import * as echarts from 'echarts'` 也可以（只是体积稍大）。

2. **图表 resize 监听必须在 onUnmounted 时移除**
   用具名函数便于移除：
   ```js
   const handleResize = () => chartInstance?.resize()
   onMounted(() => window.addEventListener('resize', handleResize))
   onUnmounted(() => window.removeEventListener('resize', handleResize))
   ```

3. **日期格式化工具**
   推荐封装到 `src/utils/format.js`：
   ```js
   export const formatDate = (dateStr) => {
     if (!dateStr) return '—'
     return new Date(dateStr).toLocaleString('zh-CN', {
       year: 'numeric', month: '2-digit', day: '2-digit',
       hour: '2-digit', minute: '2-digit', second: '2-digit'
     })
   }
   ```

4. **促销时间范围选择器回填**
   `el-date-picker type="datetimerange"` 绑定数组，编辑时回填：
   ```js
   form.dateRange = [promotion.startTime, promotion.endTime]
   // 提交时拆分
   const [startTime, endTime] = form.dateRange
   ```

5. **货币格式化**
   财务页金额数字要用千分位格式化：
   ```js
   export const formatMoney = (num) =>
     `¥ ${Number(num).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
   ```

6. **库存操作中商品下拉选择**
   入库 / 出库 dialog 中的「选择商品」下拉框，调用 `productAPI.getList({ pageSize: 999 })` 获取商品列表（若商品不超过几百条）。若商品很多，可改为搜索输入框（`el-select` + `filterable` + `remote`）。

7. **配送状态按钮逻辑**
   ```
   状态 = 待配送 + 已分配骑手 → 显示「开始配送」按钮
   状态 = 待配送 + 未分配骑手 → 显示「分配骑手」按钮
   状态 = 配送中 → 显示「标记送达」按钮
   状态 = 已送达 → 无按钮（或显示「查看详情」）
   ```
   在模板中用多个 `v-if` 控制，不要用嵌套三目运算符，可读性更好。

8. **促销折扣展示逻辑**
   ```js
   const formatDiscount = (type, discount) => {
     if (type === '折扣') return `${discount * 10} 折`  // 0.9 → 9折
     if (type === '满减') return `满减 ¥${discount}`
     return discount
   }
   ```

9. **统计卡片数字动画（加分项）**
   Element Plus 的 `el-statistic` 组件支持 `value` 数字动画。也可用第三方库 `vue-count-to` 实现数字滚动效果，提升视觉体验。

10. **`getInventoryLogs` 的 `productId` 筛选**
    日志页面筛选条件是「商品名称关键词」，但接口参数是 `productId`。有两种处理方式：
    - 方式 A（推荐）：让用户从商品下拉框选择商品，选中后得到 `productId` 传入接口。
    - 方式 B：若后端支持，将关键词改为商品名直接传（需后端配合，以实际接口为准）。
    若后端 `productId` 参数不传（`undefined`）则查全部，这是正常的。

11. **`admin/Finance.vue` 无数据时的处理**
    若 `revenueByMonth` 为空数组，图表要展示「暂无数据」提示，而不是渲染一个空坐标轴：
    ```js
    if (!data.revenueByMonth.length) {
      // 不初始化图表，显示 el-empty 组件
      return
    }
    ```
