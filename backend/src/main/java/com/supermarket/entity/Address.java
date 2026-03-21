package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ADDRESS")
public class Address {
    @TableId(type = IdType.AUTO)
    private Integer addressId;
    private Integer userId;
    private String receiver;
    private String phone;
    private String detail;
    private Integer isDefault;
}
