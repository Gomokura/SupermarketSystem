package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("CASHIER_SHIFTS")
public class CashierShift {
    @TableId(type = IdType.AUTO)
    private Integer shiftId;
    @TableField("cashier_id")
    private Integer cashierId;    // 收银员（ADMINS表）
    @TableField("cash_start")
    private Double startCash;     // 备用金
    @TableField("cash_end")
    private Double endCash;       // 交班时清点现金
    @TableField("total_orders")
    private Integer totalOrders;  // 本班总单数
    @TableField("cash_total")
    private Double cashTotal;     // 现金收款合计
    @TableField("sim_pay_total")
    private Double simPayTotal;   // 模拟支付合计
    @TableField("start_time")
    private Date startTime;
    @TableField("end_time")
    private Date endTime;
    private String status;        // open开班中 / closed已交班
}
