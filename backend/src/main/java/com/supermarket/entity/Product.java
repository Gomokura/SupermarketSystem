package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@TableName("PRODUCTS")
public class Product {
    @TableId(value = "PRODUCT_ID", type = IdType.INPUT)
    @TableField("product_id")
    private Integer productId;
    @TableField("product_name")
    private String productName;
    @TableField("category_id")
    private Integer categoryId;
    @TableField("brand_id")
    private Integer brandId;
    @TableField("supplier_id")
    private Integer supplierId;
    private String description;
    @TableField("cover_image")
    private String coverImage;
    private String unit;
    @TableField("original_price")
    private Double originalPrice;
    private Double price;
    private Integer stock;
    @TableField("stock_warning")
    private Integer stockWarning;
    @TableField("sales_count")
    private Integer salesCount;
    @TableField("avg_rating")
    private Double avgRating;
    @TableField("is_recommend")
    private Integer isRecommend;
    private String status;
    @TableField("create_time")
    private Date createTime;
    @TableField("update_time")
    private Date updateTime;
    private String barcode;
    @TableField("is_deleted")
    private Integer isDeleted;
    @TableField("cost_price")
    private Double costPrice;

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
