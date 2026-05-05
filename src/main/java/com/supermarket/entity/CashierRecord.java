package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName("CASHIER_RECORDS")
public class CashierRecord {
    @TableId(type = IdType.INPUT)
    private Integer recordId;
    private Integer shiftId;
    private Integer userId;
    private String memberPhone;
    private Double totalAmount;
    private Double discountAmount;
    private Integer couponId;
    private Integer ucId;
    private Double payAmount;
    /** CASH / MOCK_CARD */
    private String payMethod;
    private Double receivedAmount;
    private Double changeAmount;
    private Integer cashierId;
    private Date createTime;

    @TableField(exist = false)
    private List<CashierRecordItem> items;
}

