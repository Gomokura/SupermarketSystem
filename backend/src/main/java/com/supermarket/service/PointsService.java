package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.Order;
import com.supermarket.entity.PointsLog;
import com.supermarket.entity.User;
import com.supermarket.mapper.OrderMapper;
import com.supermarket.mapper.PointsLogMapper;
import com.supermarket.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PointsService extends ServiceImpl<PointsLogMapper, PointsLog> {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PointsLogMapper pointsLogMapper;

    @Autowired
    private OrderMapper orderMapper;

    public Result<?> getMyPoints(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return Result.error("用户不存在");
        return Result.success(user.getPoints());
    }

    public Result<?> getMyPointsLogs(Integer userId, Integer pageNum, Integer pageSize, String reason) {
        Page<PointsLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsLog::getUserId, userId)
                .orderByDesc(PointsLog::getCreateTime);
        if (reason != null && !reason.isEmpty()) {
            wrapper.eq(PointsLog::getReason, reason);
        }
        this.page(page, wrapper);
        return Result.success(page);
    }

    @org.springframework.transaction.annotation.Transactional
    public Result<?> adminAdjust(Integer adminId, Integer userId, Integer amount, String remark) {
        User user = userMapper.selectById(userId);
        if (user == null) return Result.error("用户不存在");
        int before = user.getPoints() != null ? user.getPoints() : 0;
        int after = before + amount;
        if (after < 0) return Result.error("积分不足，无法扣减");
        user.setPoints(after);
        userMapper.updateById(user);
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setChangeAmount(amount);
        log.setBalanceAfter(after);
        log.setReason("ADMIN_ADJUST");
        log.setOperatorId(adminId);
        log.setCreateTime(new java.util.Date());
        log.setLogId(pointsLogMapper.getNextId());
        this.save(log);
        return Result.success(after);
    }

    public Result<?> getUserPointsLogs(Integer userId, Integer pageNum, Integer pageSize) {
        Page<PointsLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsLog::getUserId, userId).orderByDesc(PointsLog::getCreateTime);
        this.page(page, wrapper);
        return Result.success(page);
    }

    /**
     * 添加用户积分（用于订单支付后的积分累计）
     * @param userId 用户ID
     * @param amount 增加的积分数量
     * @param reason 原因（如"ORDER_PAY"）
     * @param refId 关联ID（如orderId）
     */
    @org.springframework.transaction.annotation.Transactional
    public void addPoints(Integer userId, Integer amount, String reason, Integer refId) {
        if (userId == null || amount == null || amount <= 0) return;
        
        User user = userMapper.selectById(userId);
        if (user == null) return;
        
        int before = user.getPoints() != null ? user.getPoints() : 0;
        int after = before + amount;
        user.setPoints(after);
        userMapper.updateById(user);
        
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setChangeAmount(amount);
        log.setBalanceAfter(after);
        log.setReason(reason != null ? reason : "ORDER_PAY");
        log.setRefId(refId);
        log.setCreateTime(new java.util.Date());
        log.setLogId(pointsLogMapper.getNextId());
        this.save(log);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deductPoints(Integer userId, Integer amount, String reason, Integer refId, Integer operatorId) {
        if (userId == null || amount == null || amount <= 0) return;

        User user = userMapper.selectById(userId);
        if (user == null) return;

        int before = user.getPoints() != null ? user.getPoints() : 0;
        if (before < amount) {
            throw new com.supermarket.common.BusinessException("会员积分不足");
        }
        int after = before - amount;
        user.setPoints(after);
        userMapper.updateById(user);

        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setChangeAmount(-amount);
        log.setBalanceAfter(after);
        log.setReason(reason != null ? reason : "CASHIER_DEDUCT");
        log.setRefId(refId);
        log.setOperatorId(operatorId);
        log.setCreateTime(new java.util.Date());
        log.setLogId(pointsLogMapper.getNextId());
        this.save(log);
    }

    /**
     * 根据订单历史初始化用户积分
     * 规则：消费¥1 = 积分1（按支付金额的整数部分）
     * @return 初始化结果摘要
     */
    @org.springframework.transaction.annotation.Transactional
    public Result<?> initializePointsFromOrders() {
        try {
            // 1. 查询所有已支付订单
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Order::getStatus, "PAID", "PENDING_SHIP", "SHIPPED", "COMPLETED", "CLOSED");
            List<Order> paidOrders = orderMapper.selectList(wrapper);
            
            // 2. 按用户分组，计算每个用户的总消费额
            Map<Integer, Double> userTotalAmount = paidOrders.stream()
                    .collect(Collectors.groupingBy(
                            Order::getUserId,
                            Collectors.summingDouble(Order::getPayAmount)
                    ));
            
            // 3. 更新每个用户的积分
            int updatedCount = 0;
            for (Map.Entry<Integer, Double> entry : userTotalAmount.entrySet()) {
                Integer userId = entry.getKey();
                Integer points = (int) Math.floor(entry.getValue());
                
                User user = userMapper.selectById(userId);
                if (user != null) {
                    user.setPoints(points);
                    userMapper.updateById(user);
                    updatedCount++;
                }
            }
            
            // 4. 返回结果
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("totalUsers", userTotalAmount.size());
            result.put("updatedCount", updatedCount);
            result.put("message", String.format("成功为 %d 个用户初始化积分", updatedCount));
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("初始化失败: " + e.getMessage());
        }
    }
}

