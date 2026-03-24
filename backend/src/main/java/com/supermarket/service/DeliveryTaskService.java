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

import java.util.Date;
import java.util.List;

@Service
public class DeliveryTaskService extends ServiceImpl<DeliveryTaskMapper, DeliveryTask> {

    @Autowired private OrderMapper orderMapper;
    @Autowired private CourierMapper courierMapper;

    /** 骑手端：查看自己的配送任务 */
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
                t.setAddress(order.getAddressSnapshot());
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
        if (!"pending".equals(task.getStatus())) throw new BusinessException("任务状态不允许取件");

        task.setStatus("picking");
        task.setPickupTime(new Date());
        this.updateById(task);
        return Result.success();
    }

    /** 骑手端：完成配送 */
    @Transactional
    public Result<?> completeTask(Integer taskId, Integer courierId) {
        DeliveryTask task = this.getById(taskId);
        if (task == null) throw new BusinessException(404, "配送任务不存在");
        if (!task.getCourierId().equals(courierId)) throw new BusinessException(403, "无权操作");
        if (!"picking".equals(task.getStatus())) throw new BusinessException("任务状态不允许完成");

        task.setStatus("done");
        task.setDeliverTime(new Date());
        this.updateById(task);

        // 更新订单状态为已送达
        Order order = orderMapper.selectById(task.getOrderId());
        if (order != null && "shipped".equals(order.getStatus())) {
            order.setStatus("completed");
            order.setCompleteTime(new Date());
            orderMapper.updateById(order);
        }

        // 更新骑手累计送达数（使用原子 UPDATE，避免并发问题）
        courierMapper.incrementDelivered(courierId.longValue());

        // 回写 Order 实际到门时间
        order.setDeliveredAt(new Date());
        orderMapper.updateById(order);

        return Result.success();
    }

    /** 骑手端：标记配送失败 */
    @Transactional
    public Result<?> failTask(Integer taskId, Integer courierId, String failReason) {
        DeliveryTask task = this.getById(taskId);
        if (task == null) throw new BusinessException(404, "配送任务不存在");
        if (!task.getCourierId().equals(courierId)) throw new BusinessException(403, "无权操作");
        if (!"picking".equals(task.getStatus())) throw new BusinessException("任务状态不允许标记失败");

        task.setStatus("failed");
        task.setFailReason(failReason);
        this.updateById(task);

        // 回写 Order 失败原因 + 状态
        Order order = orderMapper.selectById(task.getOrderId());
        if (order != null) {
            order.setDeliveryFailReason(failReason);
            order.setStatus("DELIVERY_FAILED");
            orderMapper.updateById(order);
        }

        // 更新骑手失败计数
        courierMapper.incrementFailed(courierId.longValue());

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
