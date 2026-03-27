package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.Courier;
import com.supermarket.entity.DeliveryTask;
import com.supermarket.entity.Order;
import com.supermarket.mapper.CourierMapper;
import com.supermarket.mapper.DeliveryTaskMapper;
import com.supermarket.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DeliveryTaskService extends ServiceImpl<DeliveryTaskMapper, DeliveryTask> {

    @Autowired private OrderMapper orderMapper;
    @Autowired private CourierMapper courierMapper;

    /** P-02 配送员查看个人信息（含今日已送/累计送单数） */
    public Result<?> getCourierProfile(Integer courierId) {
        Courier courier = courierMapper.selectById(courierId);
        if (courier == null) throw new BusinessException(404, "配送员不存在");

        // 今日已完成单数
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date todayStart = cal.getTime();

        long todayCount = this.count(new LambdaQueryWrapper<DeliveryTask>()
                .eq(DeliveryTask::getCourierId, courierId)
                .eq(DeliveryTask::getStatus, "DELIVERED")
                .ge(DeliveryTask::getDeliverTime, todayStart));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("courierId",   courier.getCourierId());
        data.put("courierName", courier.getCourierName());
        data.put("phone",       courier.getPhone());
        data.put("status",      courier.getStatus());
        data.put("todayCount",  todayCount);
        data.put("totalCount",  courier.getTotalCount() != null ? courier.getTotalCount() : 0);
        return Result.success(data);
    }

    /** P-03 配送员修改密码 */
    @Transactional
    public Result<?> changeCourierPassword(Integer courierId, String oldPassword, String newPassword) {
        if (!StringUtils.hasText(oldPassword)) throw new BusinessException("旧密码不能为空");
        if (!StringUtils.hasText(newPassword)) throw new BusinessException("新密码不能为空");
        if (newPassword.length() < 6)          throw new BusinessException("新密码长度不能少于6位");

        Courier courier = courierMapper.selectById(courierId);
        if (courier == null) throw new BusinessException(404, "配送员不存在");

        String oldMd5 = DigestUtils.md5DigestAsHex(oldPassword.getBytes(StandardCharsets.UTF_8));
        if (!oldMd5.equals(courier.getPassword())) throw new BusinessException("旧密码错误");

        courier.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes(StandardCharsets.UTF_8)));
        courierMapper.updateById(courier);
        return Result.success();
    }

    /** P-09 历史任务记录（已完成 + 已失败） */
    public Result<?> getHistoryTasks(Integer courierId) {
        LambdaQueryWrapper<DeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryTask::getCourierId, courierId)
                .in(DeliveryTask::getStatus, "DELIVERED", "FAILED")
                .orderByDesc(DeliveryTask::getDeliverTime);
        List<DeliveryTask> tasks = this.list(wrapper);
        for (DeliveryTask t : tasks) {
            Order order = orderMapper.selectById(t.getOrderId());
            if (order != null) {
                t.setOrderNo(order.getOrderNo());
                t.setAddress(order.getReceiverSnapshot());
            }
        }
        return Result.success(tasks);
    }
    public Result<?> getMyCourierTasks(Integer courierId, String status) {
        LambdaQueryWrapper<DeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryTask::getCourierId, courierId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(DeliveryTask::getStatus, status);
        }
        wrapper.orderByDesc(DeliveryTask::getAssignTime);
        List<DeliveryTask> tasks = this.list(wrapper);

        // 填充订单信息
        for (DeliveryTask t : tasks) {
            Order order = orderMapper.selectById(t.getOrderId());
            if (order != null) {
                t.setOrderNo(order.getOrderNo());
                // 地址快照直接显示
                t.setAddress(order.getReceiverSnapshot());
            }
        }
        return Result.success(tasks);
    }

    /** 骑手端：取件（开始配送） */
    @Transactional
    public Result<?> pickupTask(Integer taskId, Integer courierId) {
        DeliveryTask task = this.getById(taskId);
        if (task == null) throw new BusinessException(404, "配送任务不存在");
        if (!task.getCourierId().equals(courierId)) throw new BusinessException(403, "无权操作");
        if (!"ASSIGNED".equals(task.getStatus())) throw new BusinessException("任务状态不允许取件");

        task.setStatus("PICKED_UP");
        task.setPickupTime(new Date());
        this.updateById(task);

        // 回写订单状态（配送中）
        Order order = orderMapper.selectById(task.getOrderId());
        if (order != null && "PENDING_SHIP".equals(order.getStatus())) {
            order.setStatus("SHIPPING");
            order.setPickupTime(task.getPickupTime());
            order.setUpdateTime(new Date());
            orderMapper.updateById(order);
        }
        return Result.success();
    }

    /** 骑手端：完成配送 */
    @Transactional
    public Result<?> completeTask(Integer taskId, Integer courierId) {
        DeliveryTask task = this.getById(taskId);
        if (task == null) throw new BusinessException(404, "配送任务不存在");
        if (!task.getCourierId().equals(courierId)) throw new BusinessException(403, "无权操作");
        if (!"PICKED_UP".equals(task.getStatus())) throw new BusinessException("任务状态不允许完成");

        task.setStatus("DELIVERED");
        task.setDeliverTime(new Date());
        this.updateById(task);

        // 更新订单状态为待收货
        Order order = orderMapper.selectById(task.getOrderId());
        if (order != null) {
            if ("SHIPPING".equals(order.getStatus())) {
                order.setStatus("PENDING_RECEIVED");
            }
            order.setDeliverTime(task.getDeliverTime());
            order.setUpdateTime(new Date());
            orderMapper.updateById(order);
        }

        // 更新骑手累计送达数（使用原子 UPDATE，避免并发问题）
        courierMapper.incrementDelivered(courierId.longValue());

        return Result.success();
    }

    /** 骑手端：标记配送失败 */
    @Transactional
    public Result<?> failTask(Integer taskId, Integer courierId, String failReason) {
        DeliveryTask task = this.getById(taskId);
        if (task == null) throw new BusinessException(404, "配送任务不存在");
        if (!task.getCourierId().equals(courierId)) throw new BusinessException(403, "无权操作");
        if (!"PICKED_UP".equals(task.getStatus())) throw new BusinessException("任务状态不允许标记失败");

        task.setStatus("FAILED");
        task.setFailReason(failReason);
        this.updateById(task);

        // 回写 Order 失败原因 + 状态（不在 v3 状态枚举中，这里只记录 remark，不强行改状态）
        Order order = orderMapper.selectById(task.getOrderId());
        if (order != null) {
            order.setRemark((order.getRemark() != null ? order.getRemark() + " | " : "") + "配送失败：" + failReason);
            order.setUpdateTime(new Date());
            orderMapper.updateById(order);
        }

        return Result.success();
    }

    /** 骑手端：更新在线状态 */
    @Transactional
    public Result<?> updateOnlineStatus(Integer courierId, String status) {
        Courier courier = courierMapper.selectById(courierId);
        if (courier == null) throw new BusinessException(404, "骑手不存在");
        courier.setStatus(status);
        courierMapper.updateById(courier);
        return Result.success();
    }
}