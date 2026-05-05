package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("TOKEN_BLACKLIST")
public class TokenBlacklist {
    @TableId(type = IdType.INPUT)
    private String token;

    private Integer userId;

    private String userType;

    private Date blacklistedAt;

    private Date expiresAt;
}