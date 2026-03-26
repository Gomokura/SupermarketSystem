package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.CashierShift;
import com.supermarket.mapper.CashierShiftMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CashierService extends ServiceImpl<CashierShiftMapper, CashierShift> {

    /** 开班 */
    @Transactional
    public Result<?> openShift(Integer cashierId, Double startCash) {
        // 检查是否已有开班中的班次
        LambdaQueryWrapper<CashierShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashierShift::getCashierId, cashierId).eq(CashierShift::getStatus, "open");
        if (this.count(wrapper) > 0) return Result.error("已有开班中的班次，请先交班");

        CashierShift shift = new CashierShift();
        shift.setCashierId(cashierId);
        shift.setStartCash(startCash != null ? startCash : 0.0);
        shift.setTotalOrders(0);
        shift.setCashTotal(0.0);
        shift.setSimPayTotal(0.0);
        shift.setStatus("open");
        shift.setStartTime(new Date());
        this.save(shift);
        return Result.success(shift);
    }

    /** 获取当前班次 */
    public Result<?> getCurrentShift(Integer cashierId) {
        LambdaQueryWrapper<CashierShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashierShift::getCashierId, cashierId).eq(CashierShift::getStatus, "open");
        CashierShift shift = this.getOne(wrapper);
        if (shift == null) return Result.error(404, "无开班中的班次");
        return Result.success(shift);
    }

    /** 交班 */
    @Transactional
    public Result<?> closeShift(Integer cashierId, Double endCash) {
        LambdaQueryWrapper<CashierShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashierShift::getCashierId, cashierId).eq(CashierShift::getStatus, "open");
        CashierShift shift = this.getOne(wrapper);
        if (shift == null) return Result.error("无开班中的班次");

        shift.setEndCash(endCash);
        shift.setStatus("closed");
        shift.setEndTime(new Date());
        this.updateById(shift);

        Map<String, Object> summary = new HashMap<>();
        summary.put("shift", shift);
        summary.put("cashDiff", endCash - shift.getCashTotal() - shift.getStartCash());
        return Result.success(summary);
    }

    /** 班次统计更新（由 OrderService 调用） */
    @Transactional
    public void recordShiftOrder(Integer cashierId, String payMethod, Double amount) {
        LambdaQueryWrapper<CashierShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashierShift::getCashierId, cashierId).eq(CashierShift::getStatus, "open");
        CashierShift shift = this.getOne(wrapper);
        if (shift == null) return;
        shift.setTotalOrders(shift.getTotalOrders() + 1);
        if ("cash".equals(payMethod)) {
            shift.setCashTotal(shift.getCashTotal() + amount);
        } else {
            shift.setSimPayTotal(shift.getSimPayTotal() + amount);
        }
        this.updateById(shift);
    }

    /** 历史班次列表 */
    public Result<?> getShiftHistory(Integer cashierId) {
        LambdaQueryWrapper<CashierShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashierShift::getCashierId, cashierId).orderByDesc(CashierShift::getStartTime);
        List<CashierShift> list = this.list(wrapper);
        return Result.success(list);
    }
}
