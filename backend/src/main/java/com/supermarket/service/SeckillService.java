package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.Product;
import com.supermarket.entity.SeckillActivityModel;
import com.supermarket.entity.SeckillActivityProductModel;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.mapper.SeckillActivityModelMapper;
import com.supermarket.mapper.SeckillActivityProductModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SeckillService extends ServiceImpl<SeckillActivityModelMapper, SeckillActivityModel> {

    @Autowired
    private SeckillActivityProductModelMapper seckillProductMapper;

    @Autowired
    private ProductMapper productMapper;

    public Result<?> listActivities(Integer pageNum, Integer pageSize, String state) {
        Page<SeckillActivityModel> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SeckillActivityModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillActivityModel::getActivityType, "SECKILL");
        wrapper.eq(SeckillActivityModel::getStatus, "active");
        wrapper.orderByDesc(SeckillActivityModel::getSeckillId);
        this.page(page, wrapper);

        Date now = new Date();
        List<SeckillActivityModel> records = new ArrayList<>(page.getRecords());
        records.forEach(activity -> activity.setProducts(null));

        if (state != null && !state.isEmpty()) {
            records.removeIf(activity -> {
                String computed = computeState(activity, now);
                activity.setCurrentState(computed);
                return !state.equalsIgnoreCase(computed);
            });
        }
        // 无论是否过滤，都需要写回 records（并填充展示态）
        for (SeckillActivityModel activity : records) {
            activity.setCurrentState(computeState(activity, now));
        }
        page.setRecords(records);

        return Result.success(page);
    }

    public Result<?> getActivityProducts(Integer seckillId) {
        LambdaQueryWrapper<SeckillActivityProductModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillActivityProductModel::getSeckillId, seckillId);
        List<SeckillActivityProductModel> rows = new ArrayList<>(seckillProductMapper.selectList(wrapper));

        if (rows.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        Set<Integer> productIds = new HashSet<>();
        for (SeckillActivityProductModel row : rows) {
            if (row.getProductId() != null) {
                productIds.add(row.getProductId());
            }
        }

        Map<Integer, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Product> products = productMapper.selectBatchIds(productIds);
            for (Product product : products) {
                productMap.put(product.getProductId(), product);
            }
        }

        for (SeckillActivityProductModel row : rows) {
            Product product = productMap.get(row.getProductId());
            if (product != null) {
                row.setProductName(product.getProductName());
                row.setImageUrl(product.getCoverImage());
                row.setOriginalPrice(product.getPrice());
            }
        }

        return Result.success(rows);
    }

    private String computeState(SeckillActivityModel activity, Date now) {
        if (activity == null || activity.getStartTime() == null || activity.getEndTime() == null) {
            return "ended";
        }
        if (now.before(activity.getStartTime())) {
            return "pending";
        }
        if (now.after(activity.getEndTime())) {
            return "ended";
        }
        if ("inactive".equalsIgnoreCase(activity.getStatus())) {
            return "paused";
        }
        return "running";
    }

    @Transactional
    public Result<?> adminCreateActivity(SeckillActivityModel activity) {
        if (activity == null) {
            throw new BusinessException("activity cannot be null");
        }
        if (activity.getSeckillId() != null) {
            throw new BusinessException("new activity should not contain seckillId");
        }
        if (activity.getSeckillName() == null || activity.getSeckillName().isEmpty()) {
            throw new BusinessException("seckillName cannot be empty");
        }
        if (activity.getStartTime() == null || activity.getEndTime() == null) {
            throw new BusinessException("startTime/endTime cannot be empty");
        }

        activity.setActivityType("SECKILL");
        if (activity.getStatus() == null) {
            activity.setStatus("active");
        }
        this.save(activity);
        return Result.success(activity.getSeckillId());
    }

    @Transactional
    public Result<?> adminUpdateActivity(Integer activityId, SeckillActivityModel activity) {
        if (activity == null) {
            throw new BusinessException("activity cannot be null");
        }
        SeckillActivityModel existing = this.getById(activityId);
        if (existing == null) {
            throw new BusinessException(404, "seckill activity not found");
        }

        activity.setSeckillId(activityId);
        activity.setActivityType("SECKILL");
        this.updateById(activity);
        return Result.success();
    }

    @Transactional
    public Result<?> adminUpsertSeckillProducts(Integer activityId, List<SeckillActivityProductModel> items) {
        if (activityId == null) {
            throw new BusinessException("activityId cannot be null");
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessException("items cannot be empty");
        }

        for (SeckillActivityProductModel item : items) {
            if (item.getProductId() == null) {
                throw new BusinessException("productId cannot be null");
            }
            if (item.getSeckillPrice() == null) {
                throw new BusinessException("seckillPrice cannot be null");
            }
            if (item.getSeckillStock() == null) {
                throw new BusinessException("seckillStock cannot be null");
            }

            LambdaQueryWrapper<SeckillActivityProductModel> check = new LambdaQueryWrapper<>();
            check.eq(SeckillActivityProductModel::getSeckillId, activityId)
                    .eq(SeckillActivityProductModel::getProductId, item.getProductId());

            SeckillActivityProductModel existing = seckillProductMapper.selectOne(check);
            if (existing != null) {
                existing.setSeckillPrice(item.getSeckillPrice());
                existing.setSeckillStock(item.getSeckillStock());
                seckillProductMapper.updateById(existing);
            } else {
                SeckillActivityProductModel insert = new SeckillActivityProductModel();
                insert.setSeckillId(activityId);
                insert.setProductId(item.getProductId());
                insert.setSeckillPrice(item.getSeckillPrice());
                insert.setSeckillStock(item.getSeckillStock());
                seckillProductMapper.insert(insert);
            }
        }

        return Result.success();
    }
}
