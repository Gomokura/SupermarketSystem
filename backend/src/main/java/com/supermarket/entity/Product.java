package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@TableName("PRODUCTS")
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer productId;
    private String productName;
    private Integer categoryId;
    private Integer brandId;        // 品牌ID（外键 BRANDS）
    private Integer supplierId;     // 供应商ID（外键 SUPPLIERS）
    private String description;     // 商品描述
    private String coverImage;      // 主图路径，数据库字段 COVER_IMAGE
    private String unit;
    private Double originalPrice;   // 原价（划线价），数据库字段 ORIGINAL_PRICE
    private Double price;
    private Integer stock;
    private Integer stockWarning;   // 库存预警阈值，数据库字段 STOCK_WARNING
    private Integer salesCount;     // 累计销量，数据库字段 SALES_COUNT
    private Double avgRating;       // 平均评分，数据库字段 AVG_RATING
    private Integer isRecommend;    // 是否首页推荐 0/1，数据库字段 IS_RECOMMEND
    private String status;          // active / off_shelf / deleted
    private Date createTime;
    private Date updateTime;
    private String barcode;         // 商品条码
    private Integer isDeleted;      // 逻辑删除 0/1，数据库字段 IS_DELETED
    private Double costPrice;       // 成本价，数据库字段 COST_PRICE
    private String images;         // 商品图片列表（逗号分隔）

    // 非数据库字段（联表查询用）
    @TableField(exist = false)
    private String categoryName;    // 分类名
    @TableField(exist = false)
    private String brandName;       // 品牌名
    @TableField(exist = false)
    private String supplierName;    // 供应商名
    @TableField(exist = false)
    private List<ProductSku> skus;  // SKU列表（详情页）
}
