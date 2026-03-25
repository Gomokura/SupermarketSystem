package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.AfterSale;
import com.supermarket.entity.Order;
import com.supermarket.entity.OrderItem;
import com.supermarket.entity.Product;
import com.supermarket.entity.InventoryLog;
import com.supermarket.entity.User;
import com.supermarket.mapper.AfterSaleMapper;
import com.supermarket.mapper.OrderMapper;
import com.supermarket.mapper.OrderItemMapper;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.mapper.InventoryLogMapper;
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
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private InventoryLogMapper inventoryLogMapper;
    @Autowired private UserMapper userMapper;

    /** C端：提交售后申请 */
    @Transactional
    public Result<?> applyAfterSale(AfterSale afterSale, Integer userId) {
        // 校验订单归属
        Order order = orderMapper.selectById(afterSale.getOrderId());
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException(403, "无权操作");
        if (!"COMPLETED".equals(order.getStatus()) && !"SHIPPING".equals(order.getStatus()))
            throw new BusinessException("当前订单状态不支持售后");

        // 检查是否已有售后申请
        LambdaQueryWrapper<AfterSale> check = new LambdaQueryWrapper<>();
        check.eq(AfterSale::getOrderId, afterSale.getOrderId())
             .ne(AfterSale::getStatus, "rejected");
        if (this.count(check) > 0) throw new BusinessException("该订单已有售后申请");

        afterSale.setUserId(userId);
        afterSale.setStatus("pending");
        afterSale.setCreateTime(new Date());
        this.save(afterSale);

        // 更新订单状态
        order.setStatus("after_sale");
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
        if (!"pending".equals(as.getStatus())) throw new BusinessException("该申请已处理");

        if ("approve".equals(action)) {
            as.setStatus("approved");
            as.setHandlerId(1); // TODO: 从上下文获取当前管理员ID
        } else if ("reject".equals(action)) {
            as.setStatus("rejected");
            as.setRejectReason(remark);
            as.setHandlerId(1); // TODO: 从上下文获取当前管理员ID
            // 恢复订单状态
            Order order = orderMapper.selectById(as.getOrderId());
            if (order != null) {
                order.setStatus("completed");
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
        if (!"approved".equals(as.getStatus())) throw new BusinessException("请先审批通过");

        // 回补库存
        Order order = orderMapper.selectById(as.getOrderId());
        if (order != null) {
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getOrderId());
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

            for (OrderItem item : items) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    int before = product.getStock() != null ? product.getStock() : 0;
                    product.setStock(before + item.getQuantity());
                    product.setSalesCount(Math.max(0, product.getSalesCount() - item.getQuantity()));
                    productMapper.updateById(product);

                    InventoryLog log = new InventoryLog();
                    log.setProductId(product.getProductId());
                    log.setSkuId(item.getSkuId());
                    log.setLogType("REFUND_IN");
                    log.setChangeAmount(item.getQuantity());
                    log.setBalanceAfter(product.getStock());
                    log.setBeforeStock(before);
                    log.setAfterStock(product.getStock());
                    log.setRefId(order.getOrderId());
                    log.setRemark("售后退款回库，单号：" + order.getOrderNo());
                    log.setCreateTime(new Date());
                    inventoryLogMapper.insert(log);
                }
            }

            order.setStatus("refunded");
            order.setCancelTime(new Date());
            orderMapper.updateById(order);
        }

        as.setStatus("completed");
        this.updateById(as);
        return Result.success();
    }

    private void fillOrderInfo(List<AfterSale> list) {
        for (AfterSale as : list) {
            Order o = orderMapper.selectById(as.getOrderId());
            if (o != null) as.setOrderNo(o.getOrderNo());
        }
    }
}
