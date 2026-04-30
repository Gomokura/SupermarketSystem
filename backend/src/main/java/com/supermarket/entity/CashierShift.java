package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("CASHIER_SHIFTS")
public class CashierShift {
    @TableId(type = IdType.INPUT)
    private Integer shiftId;
    private Integer cashierId;
    private Double startCash;
    private Double endCash;
    /** v3: total_cash_amount */
    private Double totalCashAmount;
    /** v3: total_mock_amount */
    private Double totalMockAmount;
    /** v3: total_order_count */
    private Integer totalOrderCount;
    private Double cashDiff;
    private Date startTime;
    private Date endTime;
    /** OPEN / CLOSED */
    private String status;
}
