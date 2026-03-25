package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StocktakeService {

    @Autowired private StocktakeTaskMapper stocktakeTaskMapper;
    @Autowired private StocktakeItemMapper stocktakeItemMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private InventoryLogMapper inventoryLogMapper;
    @Autowired private CategoryMapper categoryMapper;

    /**
     * 创建盘点任务
     * scope=all：盘点所有商品
     * scope=category：只盘点指定分类
     * 创建时自动快照所有商品的账面库存
     */
    @Transactional
    public Result<?> createStocktakeTask(String scope, Integer categoryId, Integer userId) {
        if (!"all".equals(scope) && !"category".equals(scope)) {
            throw new BusinessException("scope 只能是 all 或 category");
        }
        if ("category".equals(scope) && categoryId == null) {
            throw new BusinessException("按分类盘点必须指定 categoryId");
        }

        // 查询需要盘点的商品
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsDeleted, 0);
        if ("category".equals(scope)) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        List<Product> products = productMapper.selectList(wrapper);

        if (products.isEmpty()) {
            throw new BusinessException("没有需要盘点的商品");
        }

        // 创建盘点任务
        StocktakeTask task = new StocktakeTask();
        task.setScope(scope);
        task.setCategoryId(categoryId);
        task.setStatus("pending");
        task.setCreatorId(userId);
        task.setCreateTime(new Date());
        stocktakeTaskMapper.insert(task);

        // 创建盘点明细（快照账面库存）
        for (Product product : products) {
            StocktakeItem item = new StocktakeItem();
            item.setTaskId(task.getTaskId());
            item.setProductId(product.getProductId());
            item.setBookStock(product.getStock() != null ? product.getStock() : 0);
            item.setActualStock(null);
            item.setDifference(null);
            item.setDiffReason(null);
            stocktakeItemMapper.insert(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("taskId", task.getTaskId());
        data.put("totalProducts", products.size());
        data.put("scope", scope);
        return Result.success(data);
    }

    /**
     * 盘点任务列表
     */
    public Result<?> getStocktakeTasks(String status, Integer pageNum, Integer pageSize) {
        Page<StocktakeTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StocktakeTask> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(StocktakeTask::getStatus, status);
        }
        wrapper.orderByDesc(StocktakeTask::getCreateTime);
        stocktakeTaskMapper.selectPage(page, wrapper);

        for (StocktakeTask task : page.getRecords()) {
            fillTaskInfo(task);
        }
        return Result.success(page);
    }

    /**
     * 盘点任务详情（含盘点项）
     */
    public Result<?> getStocktakeDetail(Integer taskId) {
        StocktakeTask task = stocktakeTaskMapper.selectById(taskId);
        if (task == null) throw new BusinessException(404, "盘点任务不存在");
        fillTaskInfo(task);

        // 填充盘点明细
        LambdaQueryWrapper<StocktakeItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StocktakeItem::getTaskId, taskId);
        List<StocktakeItem> items = stocktakeItemMapper.selectList(wrapper);
        for (StocktakeItem item : items) {
            Product p = productMapper.selectById(item.getProductId());
            if (p != null) {
                item.setProductName(p.getProductName());
                Category c = categoryMapper.selectById(p.getCategoryId());
                if (c != null) item.setCategoryName(c.getCategoryName());
            }
        }
        task.setItems(items);
        return Result.success(task);
    }

    /**
     * 提交盘点结果
     * 逐项计算差异：actualStock - bookStock
     * 有差异则自动调整库存 + 写流水
     * body: { items: [{ itemId, actualStock, diffReason }] }
     */
    @Transactional
    public Result<?> submitStocktake(Integer taskId, Map<String, Object> body, Integer userId) {
        StocktakeTask task = stocktakeTaskMapper.selectById(taskId);
        if (task == null) throw new BusinessException(404, "盘点任务不存在");
        if (!"pending".equals(task.getStatus())) throw new BusinessException("该任务已提交，不能重复提交");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemUpdates = (List<Map<String, Object>>) body.get("items");
        if (itemUpdates == null || itemUpdates.isEmpty()) throw new BusinessException("盘点数据不能为空");

        int processed = 0;
        int profitCount = 0;  // 盘盈
        int lossCount = 0;    // 盘亏

        for (Map<String, Object> update : itemUpdates) {
            Integer itemId = (Integer) update.get("itemId");
            Integer actualStock = ((Number) update.get("actualStock")).intValue();
            String diffReason = (String) update.get("diffReason");

            StocktakeItem item = stocktakeItemMapper.selectById(itemId);
            if (item == null || !item.getTaskId().equals(taskId)) continue;

            int bookStock = item.getBookStock() != null ? item.getBookStock() : 0;
            int difference = actualStock - bookStock;

            item.setActualStock(actualStock);
            item.setDifference(difference);
            item.setDiffReason(diffReason);
            stocktakeItemMapper.updateById(item);

            // 有差异则调整库存
            if (difference != 0) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    int before = product.getStock() != null ? product.getStock() : 0;
                    int newStock = before + difference;
                    product.setStock(newStock);
                    productMapper.updateById(product);

                    writeInventoryLog(item.getProductId(), null, "STOCKTAKE",
                            difference, newStock, taskId,
                            "盘点" + (difference > 0 ? "盘盈" : "盘亏")
                                    + "，" + (diffReason != null ? diffReason : ""), userId);

                    if (difference > 0) profitCount++;
                    else lossCount++;
                }
            }
            processed++;
        }

        // 更新任务状态
        task.setStatus("done");
        task.setSubmitTime(new Date());
        stocktakeTaskMapper.updateById(task);

        Map<String, Object> data = new HashMap<>();
        data.put("processed", processed);
        data.put("profitCount", profitCount);
        data.put("lossCount", lossCount);
        return Result.success(data);
    }

    // ==================== 私有辅助方法 ====================

    private void fillTaskInfo(StocktakeTask task) {
        if (task.getCategoryId() != null) {
            Category c = categoryMapper.selectById(task.getCategoryId());
            if (c != null) task.setCategoryName(c.getCategoryName());
        }
    }

    private void writeInventoryLog(Integer productId, Integer skuId, String logType,
                                   int changeAmount, int balanceAfter,
                                   Integer refId, String remark, Integer operatorId) {
        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setSkuId(skuId);
        log.setLogType(logType);
        log.setChangeAmount(changeAmount);
        log.setBalanceAfter(balanceAfter);
        log.setBeforeStock(balanceAfter - changeAmount);
        log.setAfterStock(balanceAfter);
        log.setRefId(refId);
        log.setRemark(remark);
        log.setOperatorId(operatorId);
        log.setCreateTime(new Date());
        inventoryLogMapper.insert(log);
    }
}
