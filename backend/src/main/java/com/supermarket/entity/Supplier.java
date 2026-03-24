package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("SUPPLIERS")
public class Supplier {
    @TableId(type = IdType.AUTO)
    private Integer supplierId;
    private String supplierName;
    private String contact;       // 联系人姓名
    private String phone;
    private String status;        // active / inactive

    // 新增字段
    private String email;         // 邮箱
    private String address;       // 供应商地址
    private Integer paymentPeriod; // 结算账期（天），默认30
}
