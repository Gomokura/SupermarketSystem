package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("CASHIER_SHIFTS")
public class CashierShift {
    @TableId(type = IdType.AUTO)
    private Integer shiftId;
    private Integer cashierId;    // 收银员（ADMINS表）
    private Double startCash;     // 备用金，数据库字段 START_CASH
    private Double endCash;       // 交班时清点现金，数据库字段 END_CASH
    private Integer totalOrders;  // 本班总单数
    private Double cashTotal;     // 现金收款合计
    private Double simPayTotal;   // 模拟支付合计
    private Date startTime;
    private Date endTime;
    private String status;        // open开班中 / closed已交班
}
