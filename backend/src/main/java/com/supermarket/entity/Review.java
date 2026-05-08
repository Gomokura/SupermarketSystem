package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("REVIEWS")
public class Review {
    @TableId(value = "REVIEW_ID", type = IdType.INPUT)
    @TableField("review_id")
    private Integer reviewId;
    @TableField("order_id")
    private Integer orderId;
    /** v3: order_item_id（防重复评价） */
    @TableField("order_item_id")
    private Integer orderItemId;
    @TableField("product_id")
    private Integer productId;
    @TableField("user_id")
    private Integer userId;
    private Integer rating;
    private String content;
    private String images;
    private String tags;
    @TableField("is_anonymous")
    private Integer isAnonymous;
    @TableField("is_hidden")
    private Integer isHidden;
    private String reply;
    @TableField("reply_time")
    private Date replyTime;
    @TableField("create_time")
    private Date createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String username;      // 用户名（匿名时显示"匿名用户"）
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private String productName;
}
