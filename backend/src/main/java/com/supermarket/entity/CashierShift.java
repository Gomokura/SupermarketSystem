package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("CASHIER_SHIFTS")
public class CashierShift {
    @TableId(value = "SHIFT_ID", type = IdType.INPUT)
    @TableField("shift_id")
    private Integer shiftId;
    private Integer cashierId;
    @TableField("start_cash")
    private Double startCash;
    @TableField("end_cash")
    private Double endCash;
    @TableField("total_cash_amount")
    private Double totalCashAmount;
    @TableField("total_mock_amount")
    private Double totalMockAmount;
    @TableField("total_order_count")
    private Integer totalOrderCount;
    @TableField("cash_diff")
    private Double cashDiff;
    @TableField("start_time")
    private Date startTime;
    @TableField("end_time")
    private Date endTime;
    /** OPEN / CLOSED */
    private String status;
}
