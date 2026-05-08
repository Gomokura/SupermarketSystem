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
    @TableId(value = "RECORD_ID", type = IdType.INPUT)
    @TableField("record_id")
    private Integer recordId;
    @TableField("shift_id")
    private Integer shiftId;
    @TableField("user_id")
    private Integer userId;
    private String memberPhone;
    @TableField("total_amount")
    private Double totalAmount;
    @TableField("discount_amount")
    private Double discountAmount;
    @TableField("coupon_id")
    private Integer couponId;
    @TableField("uc_id")
    private Integer ucId;
    @TableField("pay_amount")
    private Double payAmount;
    /** CASH / WECHAT / ALIPAY / MEMBER_CARD */
    @TableField("pay_method")
    private String payMethod;
    @TableField("received_amount")
    private Double receivedAmount;
    @TableField("change_amount")
    private Double changeAmount;
    @TableField("cashier_id")
    private Integer cashierId;
    @TableField("create_time")
    private Date createTime;

    @TableField(exist = false)
    private List<CashierRecordItem> items;
}
