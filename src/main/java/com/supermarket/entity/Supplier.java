package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("SUPPLIERS")
public class Supplier {
    @TableId(type = IdType.INPUT)
    private Integer supplierId;
    private String supplierName;
    @TableField("CONTACT_NAME")
    private String contact;       // 联系人姓名
    @TableField("CONTACT_PHONE")
    private String phone;
    private String status;        // active / inactive
    private String email;
    private String address;
    private String bankAccount;
    @TableField("PAYMENT_DAYS")
    private Integer paymentPeriod;
}
