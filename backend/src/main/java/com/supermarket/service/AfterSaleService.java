package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.AfterSale;
import com.supermarket.entity.Order;
import com.supermarket.entity.User;
import com.supermarket.mapper.AfterSaleMapper;
import com.supermarket.mapper.OrderMapper;
import com.supermarket.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class AfterSaleService extends ServiceImpl<AfterSaleMapper, AfterSale> {

    @Autowired private OrderMapper orderMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private AfterSaleMapper afterSaleMapper;

    /** C端：提交售后申请 */
    @Transactional
    public Result<?> applyAfterSale(AfterSale afterSale, Integer userId) {
        // 校验订单归属
        Order order = orderMapper.selectById(afterSale.getOrderId());
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException(403, "无权操作");
        if (!"COMPLETED".equals(order.getStatus()) && !"SHIPPING".equals(order.getStatus()))
            throw new BusinessException("当前订单状态不支持售后");

        // 检查是否已有售后申请（排除已拒绝的）
        LambdaQueryWrapper<AfterSale> check = new LambdaQueryWrapper<>();
        check.eq(AfterSale::getOrderId, afterSale.getOrderId())
             .ne(AfterSale::getStatus, "REJECTED");
        if (this.count(check) > 0) throw new BusinessException("该订单已有售后申请");

        afterSale.setUserId(userId);
        afterSale.setStatus("PENDING");
        afterSale.setCreateTime(new Date());
        afterSale.setAfterSaleId(afterSaleMapper.getNextId());
        this.save(afterSale);

        // 更新订单状态
        order.setStatus("AFTER_SALE");
        orderMapper.updateById(order);
        return Result.success();
    }

    /** C端：我的售后列表 */
    public Result<?> getUserAfterSales(Integer userId) {
        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AfterSale::getUserId, userId)
               .orderByDesc(AfterSale::getCreateTime);
        List<AfterSale> list = this.list(wrapper);
        fillOrderInfo(list);
        return Result.success(list);
    }

    /** C端：售后详情 */
    public Result<?> getAfterSaleDetail(Integer afterSaleId, Integer userId) {
        AfterSale as = this.getById(afterSaleId);
        if (as == null) throw new BusinessException(404, "售后申请不存在");
        if (!as.getUserId().equals(userId)) throw new BusinessException(403, "无权查看");
        return Result.success(as);
    }

    /** B端：分页查询所有售后 */
    public Result<?> adminGetAfterSales(Integer pageNum, Integer pageSize, String status) {
        Page<AfterSale> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) wrapper.eq(AfterSale::getStatus, status);
        wrapper.orderByDesc(AfterSale::getCreateTime);
        this.page(page, wrapper);

        page.getRecords().forEach(as -> {
            User u = userMapper.selectById(as.getUserId());
            if (u != null) as.setUsername(u.getNickname() != null ? u.getNickname() : u.getUsername());
            Order o = orderMapper.selectById(as.getOrderId());
            if (o != null) as.setOrderNo(o.getOrderNo());
        });
        return Result.success(page);
    }

    /** B端：审批售后（同意/拒绝） */
    @Transactional
    public Result<?> handleAfterSale(Integer afterSaleId, String action, String remark) {
        AfterSale as = this.getById(afterSaleId);
        if (as == null) throw new BusinessException(404, "售后申请不存在");
        if (!"PENDING".equals(as.getStatus())) throw new BusinessException("该申请已处理");

        if ("APPROVE".equals(action)) {
            as.setStatus("APPROVED");
        } else if ("REJECT".equals(action)) {
            as.setStatus("rejected");
            // 恢复订单状态
            Order order = orderMapper.selectById(as.getOrderId());
            if (order != null) {
                order.setStatus("COMPLETED");
                orderMapper.updateById(order);
            }
        } else {
            throw new BusinessException("无效操作：" + action);
        }

        as.setAdminRemark(remark);
        as.setHandleTime(new Date());
        this.updateById(as);
        return Result.success();
    }

    /** B端：完成退款 */
    @Transactional
    public Result<?> completeRefund(Integer afterSaleId) {
        AfterSale as = this.getById(afterSaleId);
        if (as == null) throw new BusinessException(404, "售后申请不存在");
        if (!"APPROVED".equals(as.getStatus())) throw new BusinessException("请先审批通过");
        as.setStatus("COMPLETED");
        this.updateById(as);

        // 更新订单状态为已退款
        Order order = orderMapper.selectById(as.getOrderId());
        if (order != null) {
            order.setStatus("REFUNDED");
            orderMapper.updateById(order);
        }
        return Result.success();
    }

    private void fillOrderInfo(List<AfterSale> list) {
        for (AfterSale as : list) {
            Order o = orderMapper.selectById(as.getOrderId());
            if (o != null) as.setOrderNo(o.getOrderNo());
        }
    }
}
