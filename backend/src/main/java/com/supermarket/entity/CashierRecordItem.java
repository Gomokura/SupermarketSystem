package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("CASHIER_RECORD_ITEMS")
public class CashierRecordItem {
    @TableId(type = IdType.AUTO)
    private Integer itemId;
    private Integer recordId;
    private Integer productId;
    private Integer skuId;
    private String productName;
    private String skuName;
    private Double unitPrice;
    private Integer quantity;
    private Double subtotal;
}

