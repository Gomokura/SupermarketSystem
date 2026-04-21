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
    @TableId(type = IdType.AUTO)
    private Integer reviewId;
    private Integer orderId;
    /** v3: order_item_id（防重复评价） */
    @TableField("ORDER_ITEM_ID")
    private Integer orderItemId;
    private Integer productId;
    private Integer userId;
    private Integer rating;       // 1-5星
    private String content;
    private String images;        // 评价图片路径，逗号分隔
    /** v3: tags 评价标签逗号分隔 */
    private String tags;
    private Integer isAnonymous;  // 0实名/1匿名，数据库字段 IS_ANONYMOUS
    private Integer isHidden;     // 0显示/1隐藏，数据库字段 IS_HIDDEN
    private String reply;         // 商家回复，数据库字段 REPLY（默认映射正确）
    private Date replyTime;       // 回复时间，数据库字段 REPLY_TIME（默认映射正确）
    private Date createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String username;      // 用户名（匿名时显示"匿名用户"）
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private String productName;
}
