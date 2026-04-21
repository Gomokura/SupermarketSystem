package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.Date;

@Data
@TableName("USERS")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer userId;
    private String username;
    @JsonIgnore
    private String password;
    private String realName;
    private String phone;
    private String status;        // active / banned
    private Date createTime;
    private Date updateTime;

    // 补充字段
    private String nickname;      // 昵称
    @TableField("AVATAR_URL")
    private String avatarUrl;     // 头像路径，数据库字段 AVATAR_URL
    private String gender;        // 男/女/保密
    private Date birthday;        // 生日
    private String email;         // 邮箱
    private String memberLevel;   // normal/silver/gold/diamond
    private Integer points;       // 积分余额
    private String banReason;     // 封禁原因

    // 非数据库字段（联表查询统计用）
    @TableField(exist = false)
    private Integer orderCount;   // 订单数
    @TableField(exist = false)
    private Double totalSpend;    // 累计消费金额
}
