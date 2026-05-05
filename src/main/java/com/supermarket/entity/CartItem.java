package com.supermarket.entity;

import java.util.Date;

/**
 * CartItem - 购物车项（含联表字段）
 */
public class CartItem {
    private Integer cartId;
    private Integer userId;
    private Integer productId;
    private Integer skuId;
    private Integer quantity;
    private Date addTime;
    private Integer isChecked;
    private String productName;
    private Double price;
    private Double subtotal;
    private String imageUrl;
    private String specName;
    private Integer stock;
    private String productStatus;

    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getSkuId() { return skuId; }
    public void setSkuId(Integer skuId) { this.skuId = skuId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Date getAddTime() { return addTime; }
    public void setAddTime(Date addTime) { this.addTime = addTime; }
    public Integer getIsChecked() { return isChecked; }
    public void setIsChecked(Integer isChecked) { this.isChecked = isChecked; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getSpecName() { return specName; }
    public void setSpecName(String specName) { this.specName = specName; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getProductStatus() { return productStatus; }
    public void setProductStatus(String productStatus) { this.productStatus = productStatus; }
}
