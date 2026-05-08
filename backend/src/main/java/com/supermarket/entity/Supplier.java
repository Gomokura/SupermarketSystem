package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("SUPPLIERS")
public class Supplier {
    @TableId(value = "SUPPLIER_ID", type = IdType.INPUT)
    @TableField("supplier_id")
    private Integer supplierId;
    @TableField("supplier_name")
    private String supplierName;
    @TableField("contact_name")
    private String contactName;
    @TableField("contact_phone")
    private String contactPhone;
    private String email;
    private String address;
    @TableField("bank_account")
    private String bankAccount;
    private Integer paymentDays;
    private String status;
    @TableField("create_time")
    private Date createTime;
    @TableField("update_time")
    private Date updateTime;
}
