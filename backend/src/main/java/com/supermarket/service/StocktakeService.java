package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StocktakeService extends ServiceImpl<StocktakeTaskMapper, StocktakeTask> {

    @Autowired private StocktakeItemMapper itemMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private InventoryLogMapper inventoryLogMapper;
    @Autowired private StocktakeTaskMapper stocktakeTaskMapper;

    /** 创建盘点任务 */
    @Transactional
    public Result<?> create(Integer creatorId, String scope, Integer categoryId) {
        StocktakeTask task = new StocktakeTask();
        task.setCreatorId(creatorId);
        task.setScope(scope);
        task.setCategoryId("category".equals(scope) ? categoryId : null);
        task.setStatus("pending");
        task.setCreateTime(new Date());
        task.setTaskId(stocktakeTaskMapper.getNextId());
        this.save(task);

        // 拉取商品账面库存生成盘点明细
        LambdaQueryWrapper<Product> pw = new LambdaQueryWrapper<>();
        pw.eq(Product::getStatus, "on");
        if ("category".equals(scope) && categoryId != null) pw.eq(Product::getCategoryId, categoryId);
        List<Product> products = productMapper.selectList(pw);
        for (Product p : products) {
            StocktakeItem item = new StocktakeItem();
            item.setTaskId(task.getTaskId());
            item.setProductId(p.getProductId());
            item.setBookStock(p.getStock());
            item.setActualStock(null);
            item.setId(itemMapper.getNextId());
            itemMapper.insert(item);
        }
        task.setStatus("counting");
        this.updateById(task);
        return Result.success(task);
    }

    /** 盘点任务列表 */
    public Result<?> list(String status) {
        LambdaQueryWrapper<StocktakeTask> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) wrapper.eq(StocktakeTask::getStatus, status);
        wrapper.orderByDesc(StocktakeTask::getCreateTime);
        return Result.success(this.list(wrapper));
    }

    /** 盘点任务详情（含明细） */
    public Result<?> detail(Integer taskId) {
        StocktakeTask task = this.getById(taskId);
        if (task == null) return Result.error("盘点任务不存在");
        LambdaQueryWrapper<StocktakeItem> iw = new LambdaQueryWrapper<>();
        iw.eq(StocktakeItem::getTaskId, taskId);
        List<StocktakeItem> items = itemMapper.selectList(iw);
        for (StocktakeItem item : items) {
            Product p = productMapper.selectById(item.getProductId());
            if (p != null) item.setProductName(p.getProductName());
        }
        task.setItems(items);
        return Result.success(task);
    }

    /** 录入实际盘点数量 */
    @Transactional
    public Result<?> inputActual(Integer taskId, List<Map<String, Object>> inputs) {
        StocktakeTask task = this.getById(taskId);
        if (task == null) return Result.error("盘点任务不存在");
        if (!"counting".equals(task.getStatus())) return Result.error("任务状态不允许录入");
        for (Map<String, Object> raw : inputs) {
            Integer itemId = (Integer) raw.get("itemId");
            int actualQty = ((Number) raw.get("actualStock")).intValue();
            StocktakeItem item = itemMapper.selectById(itemId);
            if (item == null || !item.getTaskId().equals(taskId)) continue;
            item.setActualStock(actualQty);
            itemMapper.updateById(item);
        }
        return Result.success(null);
    }

    /** 提交盘点结果，调整库存 */
    @Transactional
    public Result<?> submit(Integer taskId, Integer operatorId) {
        StocktakeTask task = this.getById(taskId);
        if (task == null) return Result.error("盘点任务不存在");
        if (!"counting".equals(task.getStatus())) return Result.error("任务状态不允许提交");

        LambdaQueryWrapper<StocktakeItem> iw = new LambdaQueryWrapper<>();
        iw.eq(StocktakeItem::getTaskId, taskId);
        List<StocktakeItem> items = itemMapper.selectList(iw);
        List<Map<String, Object>> diffList = new ArrayList<>();
        for (StocktakeItem item : items) {
            if (item.getActualStock() == null) continue;
            int diff = item.getActualStock() - item.getBookStock();
            if (diff != 0) {
                Product p = productMapper.selectById(item.getProductId());
                if (p != null) {
                    p.setStock(p.getStock() + diff);
                    productMapper.updateById(p);
                    InventoryLog log = new InventoryLog();
                    log.setProductId(item.getProductId());
                    log.setChangeAmount(diff);
                    log.setLogType(diff > 0 ? "stocktake_up" : "stocktake_down");
                    log.setRemark("盘点调整");
                    log.setOperatorId(operatorId);
                    log.setCreateTime(new Date());
                    log.setLogId(inventoryLogMapper.getNextId());
                    inventoryLogMapper.insert(log);
                    Map<String, Object> d = new HashMap<>();
                    d.put("productId", item.getProductId());
                    d.put("productName", p.getProductName());
                    d.put("bookStock", item.getBookStock());
                    d.put("actualStock", item.getActualStock());
                    d.put("diff", diff);
                    diffList.add(d);
                }
            }
        }
        task.setStatus("done");
        task.setSubmitTime(new Date());
        this.updateById(task);
        Map<String, Object> result = new HashMap<>();
        result.put("task", task);
        result.put("diffList", diffList);
        return Result.success(result);
    }
}
