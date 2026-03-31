package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.SeckillActivityModel;
import com.supermarket.entity.SeckillActivityProductModel;
import com.supermarket.entity.SeckillActivity;
import com.supermarket.entity.SeckillProduct;
import com.supermarket.entity.Product;
import com.supermarket.mapper.SeckillActivityModelMapper;
import com.supermarket.mapper.SeckillActivityProductModelMapper;
import com.supermarket.mapper.SeckillActivityMapper;
import com.supermarket.mapper.SeckillProductMapper;
import com.supermarket.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SeckillService extends ServiceImpl<SeckillActivityModelMapper, SeckillActivityModel> {

    @Autowired
    private SeckillActivityProductModelMapper seckillProductMapper;

    @Autowired
    private SeckillActivityMapper seckillActivityMapper;

    @Autowired
    private SeckillProductMapper seckillProductLegacyMapper;

    @Autowired
    private ProductMapper productMapper;

    public Result<?> listActivities(Integer pageNum, Integer pageSize, String state) {
        // state 可选：pending/running/paused/ended
        Page<SeckillActivityModel> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SeckillActivityModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillActivityModel::getActivityType, "SECKILL");
        wrapper.eq(SeckillActivityModel::getStatus, "active");
        wrapper.orderByDesc(SeckillActivityModel::getSeckillId);
        this.page(page, wrapper);

        Date now = new Date();
        // page.getRecords() 在部分场景下可能是不可变列表，removeIf 会抛 UnsupportedOperationException（getMessage 常为 null）
        List<SeckillActivityModel> records = new ArrayList<>(page.getRecords());
        records.forEach(a -> a.setProducts(null));

        // 过滤到前端需要的 state（如果 state 为空则不过滤）
        if (state != null && !state.isEmpty()) {
            records.removeIf(a -> {
                String computed = computeState(a, now);
                a.setCurrentState(computed);
                return !state.equalsIgnoreCase(computed);
            });
        }
        }
        // 无论是否过滤，都需要写回 records（并填充展示态）
        for (SeckillActivityModel a : records) {
            a.setCurrentState(computeState(a, now));
        }
        page.setRecords(records);

        // 兼容：如果数据库没有 ACTIVITIES(SECKILL) 数据，则尝试读取 SECKILL_ACTIVITIES
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            Page<SeckillActivity> legacyPage = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<SeckillActivity> legacyWrapper = new LambdaQueryWrapper<>();
            if (state != null && !state.isEmpty()) {
                legacyWrapper.eq(SeckillActivity::getStatus, state);
            }
            legacyWrapper.orderByDesc(SeckillActivity::getSeckillId);
            seckillActivityMapper.selectPage(legacyPage, legacyWrapper);

            List<SeckillActivityModel> mapped = new ArrayList<>();
            for (SeckillActivity la : legacyPage.getRecords()) {
                SeckillActivityModel m = new SeckillActivityModel();
                m.setSeckillId(la.getSeckillId());
                m.setSeckillName(la.getSeckillName());
                m.setStartTime(la.getStartTime());
                m.setEndTime(la.getEndTime());
                m.setCurrentState(la.getStatus());
                m.setStatus("active");
                mapped.add(m);
            }

            // 返回一个“同结构”的分页结果（不复用 legacyPage 类型）
            page.setRecords(mapped);
        }

        return Result.success(page);
    }

    public Result<?> getActivityProducts(Integer seckillId) {
        LambdaQueryWrapper<SeckillActivityProductModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillActivityProductModel::getSeckillId, seckillId);
        List<SeckillActivityProductModel> rows = new ArrayList<>(seckillProductMapper.selectList(wrapper));

        // 兼容：如果数据库没有 ACTIVITY_PRODUCTS 数据，则尝试读取 SECKILL_PRODUCTS
        if (rows == null || rows.isEmpty()) {
            LambdaQueryWrapper<SeckillProduct> legacyWrapper = new LambdaQueryWrapper<>();
            legacyWrapper.eq(SeckillProduct::getSeckillId, seckillId);
            List<SeckillProduct> legacyProducts = seckillProductLegacyMapper.selectList(legacyWrapper);

            if (legacyProducts == null || legacyProducts.isEmpty()) {
                return Result.success(Collections.emptyList());
            }

            Set<Integer> productIds = new HashSet<>();
            for (SeckillProduct lp : legacyProducts) {
                if (lp.getProductId() != null) productIds.add(lp.getProductId());
            }
            Map<Integer, Product> productMap = new HashMap<>();
            if (!productIds.isEmpty()) {
                for (Product p : productMapper.selectBatchIds(productIds)) {
                    productMap.put(p.getProductId(), p);
                }
            }

            List<SeckillActivityProductModel> mapped = new ArrayList<>();
            for (SeckillProduct lp : legacyProducts) {
                SeckillActivityProductModel m = new SeckillActivityProductModel();
                m.setSeckillId(seckillId);
                m.setProductId(lp.getProductId());
                m.setSkuId(null);
                m.setSeckillPrice(lp.getSeckillPrice());
                m.setSeckillStock(lp.getSeckillStock());
                // sold_count 旧表可能不存在，直接按 seckill_stock 展示
                int remaining = lp.getSeckillStock() != null ? lp.getSeckillStock() : 0;
                m.setRemainingStock(remaining);

                Product p = productMap.get(lp.getProductId());
                if (p != null) {
                    m.setProductName(p.getProductName());
                    m.setImageUrl(p.getCoverImage());
                    m.setOriginalPrice(p.getOriginalPrice());
                }
                if (m.getRemainingStock() > 0) mapped.add(m);
            }
            return Result.success(mapped);
        }

        Set<Integer> productIds = new HashSet<>();
        for (SeckillActivityProductModel r : rows) {
            if (r.getProductId() != null) productIds.add(r.getProductId());
        }

        Map<Integer, Product> productMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Product> products = productMapper.selectBatchIds(productIds);
            for (Product p : products) productMap.put(p.getProductId(), p);
        }

        for (SeckillActivityProductModel r : rows) {
            r.setRemainingStock(
                    (r.getSeckillStock() != null ? r.getSeckillStock() : 0)
                            - (r.getSoldCount() != null ? r.getSoldCount() : 0)
            );
            Product p = productMap.get(r.getProductId());
            if (p != null) {
                r.setProductName(p.getProductName());
                r.setImageUrl(p.getCoverImage());
                r.setOriginalPrice(p.getOriginalPrice());
            }
        }

        // 只展示还有库存的商品
        rows.removeIf(r -> r.getRemainingStock() == null || r.getRemainingStock() <= 0);
        return Result.success(rows);
    }

    private String computeState(SeckillActivityModel a, Date now) {
        if (a == null || a.getStartTime() == null || a.getEndTime() == null) return "ended";
        if (now.before(a.getStartTime())) return "pending";
        if (now.after(a.getEndTime())) return "ended";
        // DB 只有 active/inactive 状态，不做 paused 区分；这里保持最小可用语义
        if ("inactive".equalsIgnoreCase(a.getStatus())) return "paused";
        return "running";
    }

    // ==================== admin：配置活动与商品（最小 CRUD） ====================

    @Transactional
    public Result<?> adminCreateActivity(SeckillActivityModel activity) {
        if (activity == null) throw new BusinessException("activity不能为空");
        if (activity.getSeckillId() != null) throw new BusinessException("新增活动不需要 seckillId");
        if (activity.getSeckillName() == null || activity.getSeckillName().isEmpty())
            throw new BusinessException("seckillName不能为空");
        if (activity.getStartTime() == null || activity.getEndTime() == null)
            throw new BusinessException("startTime/endTime不能为空");

        activity.setActivityType("SECKILL");
        activity.setSeckillId(this.baseMapper.nextActivityId());
        if (activity.getStatus() == null) activity.setStatus("active");
        this.save(activity);
        return Result.success(activity.getSeckillId());
    }

    @Transactional
    public Result<?> adminUpdateActivity(Integer seckillId, SeckillActivityModel activity) {
        if (activity == null) throw new BusinessException("activity不能为空");
        SeckillActivityModel existing = this.getById(seckillId);
        if (existing == null) throw new BusinessException(404, "秒杀活动不存在");

        activity.setSeckillId(seckillId);
        // 强制 activityType 只能是 SECKILL，避免误写
        activity.setActivityType("SECKILL");
        this.updateById(activity);
        return Result.success();
    }

    @Transactional
    public Result<?> adminUpsertSeckillProducts(Integer seckillId, List<SeckillActivityProductModel> items) {
        if (seckillId == null) throw new BusinessException("seckillId不能为空");
        if (items == null || items.isEmpty()) throw new BusinessException("items不能为空");

        // upsert by unique(activity_id, product_id, sku_id)
        for (SeckillActivityProductModel item : items) {
            if (item.getProductId() == null) throw new BusinessException("productId不能为空");
            if (item.getSeckillPrice() == null) throw new BusinessException("seckillPrice不能为空");
            if (item.getSeckillStock() == null) throw new BusinessException("seckillStock不能为空");

            LambdaQueryWrapper<SeckillActivityProductModel> check = new LambdaQueryWrapper<>();
            check.eq(SeckillActivityProductModel::getSeckillId, seckillId)
                 .eq(SeckillActivityProductModel::getProductId, item.getProductId());
            if (item.getSkuId() != null) {
                check.eq(SeckillActivityProductModel::getSkuId, item.getSkuId());
            } else {
                check.isNull(SeckillActivityProductModel::getSkuId);
            }

            SeckillActivityProductModel exist = seckillProductMapper.selectOne(check);
            if (exist != null) {
                exist.setSeckillPrice(item.getSeckillPrice());
                exist.setSeckillStock(item.getSeckillStock());
                // soldCount 不在配置里强制覆盖（否则会破坏历史）
                seckillProductMapper.updateById(exist);
            } else {
                SeckillActivityProductModel insert = new SeckillActivityProductModel();
                insert.setId(seckillProductMapper.nextProductRowId());
                insert.setSeckillId(seckillId);
                insert.setProductId(item.getProductId());
                insert.setSkuId(item.getSkuId());
                insert.setSeckillPrice(item.getSeckillPrice());
                insert.setSeckillStock(item.getSeckillStock());
                insert.setSoldCount(0);
                seckillProductMapper.insert(insert);
            }
        }

        // 更新活动总秒杀库存 = sum(activity_stock)
        LambdaQueryWrapper<SeckillActivityProductModel> sumWrapper = new LambdaQueryWrapper<>();
        sumWrapper.eq(SeckillActivityProductModel::getSeckillId, seckillId);
        List<SeckillActivityProductModel> all = seckillProductMapper.selectList(sumWrapper);
        int total = 0;
        for (SeckillActivityProductModel r : all) {
            total += (r.getSeckillStock() != null ? r.getSeckillStock() : 0);
        }

        SeckillActivityModel activity = this.getById(seckillId);
        if (activity != null) {
            activity.setSeckillStock(total);
            this.updateById(activity);
        }

        return Result.success();
    }
}

