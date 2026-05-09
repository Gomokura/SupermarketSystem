package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("BANNERS")
public class Banner {
    @TableId(value = "BANNER_ID", type = IdType.INPUT)
    @TableField("banner_id")
    private Integer bannerId;
    @TableField("image_url")
    private String imageUrl;
    @TableField("link_type")
    private String linkType;
    @TableField("link_id")
    private Integer linkTarget;
    @TableField("sort_order")
    private Integer sortOrder;
    private String status;
    @TableField("create_time")
    private Date createTime;

    // 非数据库字段（兼容旧接口）
    @TableField(exist = false)
    private Integer isActive;     // 前端兼容字段，从 status 转换
    @TableField(exist = false)
    private Date startTime;
    @TableField(exist = false)
    private Date endTime;
}
