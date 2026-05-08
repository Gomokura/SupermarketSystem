package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.Date;

@Data
@TableName("USERS")
public class User {
    @TableId(value = "USER_ID", type = IdType.INPUT)
    @TableField("user_id")
    private Integer userId;
    private String username;
    @JsonIgnore
    private String password;
    private String realName;
    private String phone;
    private String status;
    @TableField("create_time")
    private Date createTime;
    @TableField("update_time")
    private Date updateTime;

    // 补充字段
    private String nickname;
    @TableField("avatar_url")
    private String avatarUrl;
    private String gender;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    private Date birthday;
    private String email;
    @TableField("member_level")
    private String memberLevel;
    private Integer points;
    private String banReason;
    @TableField("total_consume")
    private Double totalConsume;
    private Integer orderCount;
    private Date lastOrderTime;

    // 非数据库字段（联表查询统计用）
    @TableField(exist = false)
    private Double totalSpend;    // 累计消费金额
}
