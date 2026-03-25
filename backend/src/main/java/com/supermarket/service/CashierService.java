package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class CashierService extends ServiceImpl<CashierShiftMapper, CashierShift> {

    @Autowired private CashierShiftMapper cashierShiftMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private ProductSkuMapper productSkuMapper;

    // ==================== 班次管理 ====================

    /**
     * 开班（创建新班次）
     * 只有在没有未关闭班次时才能开班
     */
    @Transactional
    public Result<?> openShift(Integer cashierId, Double startCash) {
        // 检查是否存在未关闭班次
        LambdaQueryWrapper<CashierShift> openWrapper = new LambdaQueryWrapper<>();
        openWrapper.eq(CashierShift::getCashierId, cashierId)
                   .eq(CashierShift::getStatus, "open");
        if (this.count(openWrapper) > 0) {
            throw new BusinessException("当前有未关闭的班次，请先交班");
        }

        CashierShift shift = new CashierShift();
        shift.setCashierId(cashierId);
        shift.setStartCash(startCash != null ? startCash : 0.0);
        shift.setTotalOrders(0);
        shift.setCashTotal(0.0);
        shift.setSimPayTotal(0.0);
        shift.setStartTime(new Date());
        shift.setStatus("open");
        this.save(shift);

        return Result.success(shift);
    }

    /**
     * 交班（关闭班次）
     * 自动汇总本班现金/模拟支付订单数、金额
     */
    @Transactional
    public Result<?> closeShift(Integer cashierId, Double endCash) {
        CashierShift shift = getCurrentOpenShift(cashierId);
        if (shift == null) throw new BusinessException(404, "没有未关闭的班次");

        // 汇总本班所有收银订单
        List<Order> shiftOrders = getShiftOrders(shift.getShiftId(), cashierId);

        int totalOrders = 0;
        double cashTotal = 0.0;
        double simPayTotal = 0.0;

        for (Order order : shiftOrders) {
            totalOrders++;
            if ("CASH".equals(order.getPayMethod())) {
                cashTotal += order.getPayAmount() != null ? order.getPayAmount() : 0;
            } else {
                simPayTotal += order.getPayAmount() != null ? order.getPayAmount() : 0;
            }
        }

        shift.setTotalOrders(totalOrders);
        shift.setCashTotal(cashTotal);
        shift.setSimPayTotal(simPayTotal);
        shift.setEndCash(endCash != null ? endCash : 0.0);
        shift.setEndTime(new Date());
        shift.setStatus("closed");
        this.updateById(shift);

        return Result.success(shift);
    }

    /**
     * 当前班次状态查询
     */
    public Result<?> getCurrentShift(Integer cashierId) {
        CashierShift shift = getCurrentOpenShift(cashierId);
        if (shift == null) {
            return Result.success(null); // 无未关闭班次
        }

        // 实时汇总当前班次数据
        List<Order> shiftOrders = getShiftOrders(shift.getShiftId(), cashierId);
        Map<String, Object> data = buildShiftData(shift, shiftOrders);
        return Result.success(data);
    }

    /**
     * 班次历史记录
     */
    public Result<?> getShiftHistory(Integer cashierId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<CashierShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashierShift::getCashierId, cashierId)
               .orderByDesc(CashierShift::getStartTime);
        Page<CashierShift> page = new Page<>(pageNum, pageSize);
        Page<CashierShift> result = this.page(page, wrapper);

        for (CashierShift shift : result.getRecords()) {
            List<Order> orders = getShiftOrders(shift.getShiftId(), cashierId);
            Map<String, Object> data = buildShiftData(shift, orders);
            shift.setTotalOrders(orders.size());
            shift.setCashTotal(data.get("cashTotal") != null ? (Double) data.get("cashTotal") : 0.0);
            shift.setSimPayTotal(data.get("simPayTotal") != null ? (Double) data.get("simPayTotal") : 0.0);
        }

        return Result.success(result);
    }

    /**
     * 班次差额报表
     * 计算：理论现金 = startCash + cashTotal，应收现金 = endCash，差异 = endCash - 理论现金
     */
    public Result<?> getShiftReport(Integer shiftId) {
        CashierShift shift = this.getById(shiftId);
        if (shift == null) throw new BusinessException(404, "班次不存在");

        List<Order> orders = getShiftOrders(shiftId, shift.getCashierId());

        double cashTotal = 0.0;
        double simPayTotal = 0.0;
        double totalRevenue = 0.0;
        int orderCount = 0;
        double cashOrderTotal = 0.0;
        double cardOrderTotal = 0.0;
        double otherOrderTotal = 0.0;

        for (Order order : orders) {
            orderCount++;
            double payAmount = order.getPayAmount() != null ? order.getPayAmount() : 0;
            totalRevenue += payAmount;

            if ("CASH".equals(order.getPayMethod())) {
                cashTotal += payAmount;
                cashOrderTotal += payAmount;
            } else if ("MOCK_CARD".equals(order.getPayMethod()) || "CARD".equals(order.getPayMethod())) {
                simPayTotal += payAmount;
                cardOrderTotal += payAmount;
            } else {
                simPayTotal += payAmount;
                otherOrderTotal += payAmount;
            }
        }

        double theoreticalCash = (shift.getStartCash() != null ? shift.getStartCash() : 0)
                               + cashTotal;
        double actualCash = shift.getEndCash() != null ? shift.getEndCash() : 0;
        double difference = actualCash - theoreticalCash;

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("shiftId", shift.getShiftId());
        report.put("cashierId", shift.getCashierId());
        report.put("startTime", shift.getStartTime());
        report.put("endTime", shift.getEndTime());
        report.put("startCash", shift.getStartCash());        // 备用金
        report.put("cashTotal", cashTotal);                   // 本班现金收款
        report.put("simPayTotal", simPayTotal);               // 本班非现金收款
        report.put("totalRevenue", totalRevenue);             // 本班总收入
        report.put("orderCount", orderCount);                 // 本班总单数
        report.put("cashOrderCount", orders.stream().filter(o -> "CASH".equals(o.getPayMethod())).count());
        report.put("cardOrderCount", orders.stream().filter(o -> "CARD".equals(o.getPayMethod()) || "MOCK_CARD".equals(o.getPayMethod())).count());
        report.put("cashOrderTotal", cashOrderTotal);
        report.put("cardOrderTotal", cardOrderTotal);
        report.put("otherOrderTotal", otherOrderTotal);
        report.put("theoreticalCash", Math.round(theoreticalCash * 100.0) / 100.0); // 理论现金
        report.put("actualCash", actualCash);                // 实收现金
        report.put("difference", Math.round(difference * 100.0) / 100.0);          // 差异（长款/短款）
        report.put("status", shift.getStatus());

        return Result.success(report);
    }

    /**
     * 收银台汇总统计（当前班次）
     */
    public Result<?> getShiftSummary(Integer shiftId, Integer cashierId) {
        CashierShift shift = this.getById(shiftId);
        if (shift == null) throw new BusinessException(404, "班次不存在");
        if (!shift.getCashierId().equals(cashierId)) throw new BusinessException(403, "无权查看");

        List<Order> orders = getShiftOrders(shiftId, cashierId);
        Map<String, Object> data = buildShiftData(shift, orders);
        return Result.success(data);
    }

    // ==================== 收银台商品搜索 ====================

    /**
     * 商品快速搜索（收银台扫码/关键字）
     * 支持条码精确搜索 + 关键字模糊搜索
     */
    public Result<?> searchProducts(String keyword, String barcode, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsDeleted, 0);

        if (StringUtils.hasText(barcode)) {
            // 优先按条码精确搜索
            wrapper.eq(Product::getBarcode, barcode);
        } else if (StringUtils.hasText(keyword)) {
            // 关键字模糊搜索商品名
            wrapper.like(Product::getProductName, keyword);
        } else {
            return Result.error("请输入条码或关键字");
        }

        wrapper.orderByDesc(Product::getSalesCount);
        Page<Product> page = new Page<>(pageNum, pageSize);
        Page<Product> result = productMapper.selectPage(page, wrapper);

        // 填充 SKU 列表
        for (Product product : result.getRecords()) {
            LambdaQueryWrapper<ProductSku> skuWrapper = new LambdaQueryWrapper<>();
            skuWrapper.eq(ProductSku::getProductId, product.getProductId())
                      .eq(ProductSku::getStatus, "active");
            List<ProductSku> skus = productSkuMapper.selectList(skuWrapper);
            product.setSkus(skus);
        }

        return Result.success(result);
    }

    // ==================== 私有辅助方法 ====================

    private CashierShift getCurrentOpenShift(Integer cashierId) {
        LambdaQueryWrapper<CashierShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashierShift::getCashierId, cashierId)
               .eq(CashierShift::getStatus, "open")
               .orderByDesc(CashierShift::getStartTime)
               .last("FETCH FIRST 1 ROWS ONLY");
        return this.getOne(wrapper);
    }

    /**
     * 获取指定班次的所有收银订单（按班次开始时间过滤）
     */
    private List<Order> getShiftOrders(Integer shiftId, Integer cashierId) {
        CashierShift shift = this.getById(shiftId);
        if (shift == null) return Collections.emptyList();

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getSource, "CASHIER")
               .eq(Order::getCashierId, cashierId)
               .ge(Order::getCreateTime, shift.getStartTime());
        if (shift.getEndTime() != null) {
            wrapper.le(Order::getCreateTime, shift.getEndTime());
        }
        return orderMapper.selectList(wrapper);
    }

    /**
     * 构建班次数据（汇总统计）
     */
    private Map<String, Object> buildShiftData(CashierShift shift, List<Order> orders) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("shiftId", shift.getShiftId());
        data.put("cashierId", shift.getCashierId());
        data.put("startTime", shift.getStartTime());
        data.put("endTime", shift.getEndTime());
        data.put("startCash", shift.getStartCash());
        data.put("endCash", shift.getEndCash());
        data.put("status", shift.getStatus());

        double cashTotal = 0.0;
        double simPayTotal = 0.0;
        int orderCount = orders.size();

        for (Order order : orders) {
            double amount = order.getPayAmount() != null ? order.getPayAmount() : 0;
            if ("CASH".equals(order.getPayMethod())) {
                cashTotal += amount;
            } else {
                simPayTotal += amount;
            }
        }

        data.put("orderCount", orderCount);
        data.put("cashTotal", Math.round(cashTotal * 100.0) / 100.0);
        data.put("simPayTotal", Math.round(simPayTotal * 100.0) / 100.0);
        data.put("totalRevenue", Math.round((cashTotal + simPayTotal) * 100.0) / 100.0);
        return data;
    }
}
