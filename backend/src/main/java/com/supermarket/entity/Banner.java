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
    @TableId(type = IdType.AUTO)
    private Integer bannerId;
    private String imageUrl;
    /** 跳转类型：product商品 / category分类 / activity活动 / none无 */
    private String linkType;
    @TableField("LINK_ID")
    private String linkTarget;    // 跳转目标ID，数据库字段 LINK_ID
    private Integer sortOrder;
    @TableField("STATUS")
    private String status;        // active/inactive，数据库字段 STATUS（用 status 代替 is_active）
    private Date createTime;

    // 非数据库字段（兼容旧接口）
    @TableField(exist = false)
    private Integer isActive;     // 前端兼容字段，从 status 转换
    @TableField(exist = false)
    private Date startTime;
    @TableField(exist = false)
    private Date endTime;
}
