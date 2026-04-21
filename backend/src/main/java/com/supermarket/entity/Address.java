package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ADDRESSES")
public class Address {
    @TableId(type = IdType.AUTO)
    private Integer addressId;
    private Integer userId;
    private String receiverName;  // 数据库字段 RECEIVER_NAME
    private String phone;
    private String province;      // 省份
    private String city;          // 城市
    private String district;      // 区/县
    private String detail;        // 详细地址
    private Integer isDefault;    // 是否默认 1/0
    private Date createTime;      // 创建时间
}
