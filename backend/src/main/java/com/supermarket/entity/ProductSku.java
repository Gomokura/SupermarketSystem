package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("PRODUCT_SKUS")
public class ProductSku {
    @TableId(type = IdType.AUTO)
    private Integer skuId;
    private Integer productId;
    /** 规格名称，如"500ml/瓶"，对应数据库 SKU_NAME */
    private String skuName;
    /** 规格属性 JSON，如 {"颜色":"红","容量":"500ml"}，对应数据库 SKU_SPEC */
    private String skuSpec;
    /** SKU 售价，对应数据库 PRICE */
    private Double price;
    /** SKU 划线价，对应数据库 ORIGINAL_PRICE */
    private Double originalPrice;
    /** SKU 成本价，对应数据库 COST_PRICE */
    private Double costPrice;
    /** SKU 独立库存，对应数据库 STOCK */
    private Integer stock;
    /** SKU 级条码，对应数据库 BARCODE */
    private String barcode;
    /** 显示排序，对应数据库 SORT_ORDER */
    private Integer sortOrder;
    /** active / off_shelf */
    private String status;
}
