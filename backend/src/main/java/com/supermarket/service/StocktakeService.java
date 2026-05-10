package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.InventoryLog;
import com.supermarket.entity.Product;
import com.supermarket.entity.StocktakeItem;
import com.supermarket.entity.StocktakeTask;
import com.supermarket.mapper.InventoryLogMapper;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.mapper.StocktakeItemMapper;
import com.supermarket.mapper.StocktakeTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StocktakeService extends ServiceImpl<StocktakeTaskMapper, StocktakeTask> {

    @Autowired private StocktakeItemMapper itemMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private InventoryLogMapper inventoryLogMapper;
    @Autowired private StocktakeTaskMapper stocktakeTaskMapper;

    private String toDbScope(String scope) {
        return "category".equalsIgnoreCase(scope) ? "CATEGORY" : "ALL";
    }

    private String toClientScope(String scope) {
        return "CATEGORY".equalsIgnoreCase(scope) ? "category" : "all";
    }

    private String toDbStatus(String status) {
        if ("done".equalsIgnoreCase(status)) return "COMPLETED";
        if ("pending".equalsIgnoreCase(status) || "counting".equalsIgnoreCase(status)) return "IN_PROGRESS";
        return status;
    }

    private String toClientStatus(String status) {
        if ("COMPLETED".equalsIgnoreCase(status)) return "done";
        if ("IN_PROGRESS".equalsIgnoreCase(status) || "PENDING_APPROVE".equalsIgnoreCase(status)) return "counting";
        return status;
    }

    private StocktakeTask toClientTask(StocktakeTask task) {
        if (task == null) return null;
        task.setScope(toClientScope(task.getScope()));
        task.setStatus(toClientStatus(task.getStatus()));
        return task;
    }

    @Transactional
    public Result<?> create(Integer creatorId, String scope, Integer categoryId) {
        StocktakeTask task = new StocktakeTask();
        task.setTaskId(stocktakeTaskMapper.getNextId());
        task.setCheckNo("IC" + System.currentTimeMillis());
        task.setCreatorId(creatorId);
        task.setScope(toDbScope(scope));
        task.setCategoryId("category".equalsIgnoreCase(scope) ? categoryId : null);
        task.setStatus("IN_PROGRESS");
        task.setCreateTime(new Date());
        this.save(task);

        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getStatus, "active");
        productWrapper.eq(Product::getIsDeleted, 0);
        if ("category".equalsIgnoreCase(scope) && categoryId != null) {
            productWrapper.eq(Product::getCategoryId, categoryId);
        }

        List<Product> products = productMapper.selectList(productWrapper);
        for (Product product : products) {
            StocktakeItem item = new StocktakeItem();
            item.setId(itemMapper.getNextId());
            item.setTaskId(task.getTaskId());
            item.setProductId(product.getProductId());
            item.setBookStock(product.getStock() != null ? product.getStock() : 0);
            itemMapper.insert(item);
        }

        return Result.success(toClientTask(task));
    }

    public Result<?> list(String status) {
        LambdaQueryWrapper<StocktakeTask> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(StocktakeTask::getStatus, toDbStatus(status));
        }
        wrapper.orderByDesc(StocktakeTask::getCreateTime);
        List<StocktakeTask> tasks = this.list(wrapper);
        tasks.forEach(this::toClientTask);
        return Result.success(tasks);
    }

    public Result<?> detail(Integer taskId) {
        StocktakeTask task = this.getById(taskId);
        if (task == null) return Result.error("盘点任务不存在");

        LambdaQueryWrapper<StocktakeItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(StocktakeItem::getTaskId, taskId);
        List<StocktakeItem> items = itemMapper.selectList(itemWrapper);
        for (StocktakeItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                item.setProductName(product.getProductName());
                item.setUnit(product.getUnit());
            }
        }
        task.setItems(items);
        return Result.success(toClientTask(task));
    }

    @Transactional
    public Result<?> inputActual(Integer taskId, List<Map<String, Object>> inputs) {
        StocktakeTask task = this.getById(taskId);
        if (task == null) return Result.error("盘点任务不存在");
        if (!"IN_PROGRESS".equals(task.getStatus())) return Result.error("任务状态不允许录入");
        if (inputs == null || inputs.isEmpty()) return Result.success(null);

        for (Map<String, Object> raw : inputs) {
            Integer itemId = raw.get("itemId") != null ? ((Number) raw.get("itemId")).intValue() : null;
            Integer productId = raw.get("productId") != null ? ((Number) raw.get("productId")).intValue() : null;
            Object actualValue = raw.get("actualStock") != null ? raw.get("actualStock") : raw.get("actualQty");
            if (actualValue == null) continue;

            StocktakeItem item = itemId != null ? itemMapper.selectById(itemId) : null;
            if (item == null && productId != null) {
                LambdaQueryWrapper<StocktakeItem> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(StocktakeItem::getTaskId, taskId);
                wrapper.eq(StocktakeItem::getProductId, productId);
                item = itemMapper.selectOne(wrapper);
            }
            if (item == null || !item.getTaskId().equals(taskId)) continue;

            item.setActualStock(((Number) actualValue).intValue());
            if (raw.get("diffReason") != null) item.setDiffReason((String) raw.get("diffReason"));
            itemMapper.updateById(item);
        }
        return Result.success(null);
    }

    @Transactional
    public Result<?> submit(Integer taskId, Integer operatorId) {
        StocktakeTask task = this.getById(taskId);
        if (task == null) return Result.error("盘点任务不存在");
        if (!"IN_PROGRESS".equals(task.getStatus())) return Result.error("任务状态不允许提交");

        LambdaQueryWrapper<StocktakeItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(StocktakeItem::getTaskId, taskId);
        List<StocktakeItem> items = itemMapper.selectList(itemWrapper);
        List<Map<String, Object>> diffList = new ArrayList<>();

        for (StocktakeItem item : items) {
            if (item.getActualStock() == null) continue;
            int bookStock = item.getBookStock() != null ? item.getBookStock() : 0;
            int diff = item.getActualStock() - bookStock;
            if (diff == 0) continue;

            Product product = productMapper.selectById(item.getProductId());
            if (product == null) continue;

            int currentStock = product.getStock() != null ? product.getStock() : 0;
            product.setStock(currentStock + diff);
            productMapper.updateById(product);

            InventoryLog log = new InventoryLog();
            log.setProductId(item.getProductId());
            log.setChangeAmount(diff);
            log.setLogType("CHECK_ADJUST");
            log.setBalanceAfter(product.getStock());
            log.setRemark("盘点调整");
            log.setOperatorId(operatorId);
            log.setCreateTime(new Date());
            log.setLogId(inventoryLogMapper.getNextId());
            inventoryLogMapper.insert(log);

            Map<String, Object> diffItem = new HashMap<>();
            diffItem.put("productId", item.getProductId());
            diffItem.put("productName", product.getProductName());
            diffItem.put("bookStock", bookStock);
            diffItem.put("actualStock", item.getActualStock());
            diffItem.put("diff", diff);
            diffList.add(diffItem);
        }

        task.setStatus("COMPLETED");
        task.setSubmitTime(new Date());
        this.updateById(task);

        Map<String, Object> result = new HashMap<>();
        result.put("task", toClientTask(task));
        result.put("diffList", diffList);
        return Result.success(result);
    }
}
