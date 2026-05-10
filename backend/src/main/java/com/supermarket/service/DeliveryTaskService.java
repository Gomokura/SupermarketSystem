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
import org.springframework.util.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeliveryTaskService extends ServiceImpl<DeliveryTaskMapper, DeliveryTask> {

    @Autowired private OrderMapper orderMapper;
    @Autowired private CourierMapper courierMapper;

    public Result<?> getCourierProfile(Integer courierId) {
        Courier courier = courierMapper.selectById(courierId);
        if (courier == null) throw new BusinessException(404, "配送员不存在");

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
        data.put("courierId", courier.getCourierId());
        data.put("courierName", courier.getCourierName());
        data.put("phone", courier.getPhone());
        data.put("status", courier.getStatus());
        data.put("todayCount", todayCount);
        data.put("totalCount", courier.getTotalCount() != null ? courier.getTotalCount() : 0);
        return Result.success(data);
    }

    @Transactional
    public Result<?> changeCourierPassword(Integer courierId, String oldPassword, String newPassword) {
        if (!StringUtils.hasText(oldPassword)) throw new BusinessException("旧密码不能为空");
        if (!StringUtils.hasText(newPassword)) throw new BusinessException("新密码不能为空");
        if (newPassword.length() < 6) throw new BusinessException("新密码长度不能少于 6 位");

        Courier courier = courierMapper.selectById(courierId);
        if (courier == null) throw new BusinessException(404, "配送员不存在");
        if (!oldPassword.equals(courier.getPassword())) throw new BusinessException("旧密码错误");

        courier.setPassword(newPassword);
        courierMapper.updateById(courier);
        return Result.success();
    }

    public Result<?> getHistoryTasks(Integer courierId) {
        LambdaQueryWrapper<DeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryTask::getCourierId, courierId)
                .in(DeliveryTask::getStatus, "DELIVERED", "FAILED")
                .orderByDesc(DeliveryTask::getDeliverTime)
                .orderByDesc(DeliveryTask::getPickupTime)
                .orderByDesc(DeliveryTask::getAssignTime);
        List<DeliveryTask> tasks = this.list(wrapper);
        tasks.forEach(this::fillOrderSnapshot);
        return Result.success(tasks);
    }

    public Result<?> getMyCourierTasks(Integer courierId, String status) {
        LambdaQueryWrapper<DeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryTask::getCourierId, courierId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(DeliveryTask::getStatus, status);
        } else {
            wrapper.in(DeliveryTask::getStatus, "ASSIGNED", "PICKED_UP");
        }
        wrapper.orderByDesc(DeliveryTask::getAssignTime);
        List<DeliveryTask> tasks = this.list(wrapper);
        tasks.forEach(this::fillOrderSnapshot);
        return Result.success(tasks);
    }

    @Transactional
    public Result<?> pickupTask(Integer taskId, Integer courierId) {
        DeliveryTask task = this.getById(taskId);
        if (task == null) throw new BusinessException(404, "配送任务不存在");
        if (!task.getCourierId().equals(courierId)) throw new BusinessException(403, "无权操作");
        if (!"ASSIGNED".equals(task.getStatus())) throw new BusinessException("任务状态不允许取件");

        task.setStatus("PICKED_UP");
        task.setPickupTime(new Date());
        this.updateById(task);

        Order order = orderMapper.selectById(task.getOrderId());
        if (order != null && "PENDING_SHIP".equals(order.getStatus())) {
            order.setStatus("SHIPPING");
            order.setPickupTime(task.getPickupTime());
            order.setUpdateTime(new Date());
            orderMapper.updateById(order);
        }
        return Result.success();
    }

    @Transactional
    public Result<?> completeTask(Integer taskId, Integer courierId) {
        DeliveryTask task = this.getById(taskId);
        if (task == null) throw new BusinessException(404, "配送任务不存在");
        if (!task.getCourierId().equals(courierId)) throw new BusinessException(403, "无权操作");
        if (!"PICKED_UP".equals(task.getStatus())) throw new BusinessException("任务状态不允许完成");

        task.setStatus("DELIVERED");
        task.setDeliverTime(new Date());
        this.updateById(task);

        Order order = orderMapper.selectById(task.getOrderId());
        if (order != null) {
            if ("SHIPPING".equals(order.getStatus())) {
                order.setStatus("PENDING_RECEIVED");
            }
            order.setDeliverTime(task.getDeliverTime());
            order.setUpdateTime(new Date());
            orderMapper.updateById(order);
        }

        courierMapper.incrementDelivered(courierId.longValue());
        return Result.success();
    }

    @Transactional
    public Result<?> failTask(Integer taskId, Integer courierId, String failReason) {
        if (!StringUtils.hasText(failReason)) throw new BusinessException("请填写配送异常原因");

        DeliveryTask task = this.getById(taskId);
        if (task == null) throw new BusinessException(404, "配送任务不存在");
        if (!task.getCourierId().equals(courierId)) throw new BusinessException(403, "无权操作");
        if (!"PICKED_UP".equals(task.getStatus())) throw new BusinessException("任务状态不允许标记异常");

        task.setStatus("FAILED");
        task.setFailReason(failReason);
        this.updateById(task);

        Order order = orderMapper.selectById(task.getOrderId());
        if (order != null) {
            order.setRemark((order.getRemark() != null ? order.getRemark() + " | " : "") + "配送异常：" + failReason);
            order.setUpdateTime(new Date());
            orderMapper.updateById(order);
        }

        return Result.success();
    }

    @Transactional
    public Result<?> updateOnlineStatus(Integer courierId, String status) {
        if (!"online".equals(status) && !"offline".equals(status) && !"active".equals(status)) {
            throw new BusinessException("配送员状态不合法");
        }
        Courier courier = courierMapper.selectById(courierId);
        if (courier == null) throw new BusinessException(404, "配送员不存在");
        courier.setStatus(status);
        courierMapper.updateById(courier);
        return Result.success();
    }

    private void fillOrderSnapshot(DeliveryTask task) {
        Order order = orderMapper.selectById(task.getOrderId());
        if (order == null) return;

        task.setOrderNo(order.getOrderNo());
        String snapshot = order.getReceiverSnapshot();
        if (!StringUtils.hasText(snapshot)) return;

        String[] parts = snapshot.trim().split("\\s+", 3);
        if (parts.length > 0) task.setReceiverName(parts[0]);
        if (parts.length > 1) task.setReceiverPhone(parts[1]);
        task.setAddress(parts.length > 2 ? parts[2] : snapshot);
    }
}
