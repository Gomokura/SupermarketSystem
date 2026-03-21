package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("USERS")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer userId;
    private String username;
    private String password;
    private String realName;
    private String role;
    private String phone;
    private String status;
    private Date createTime;
}
