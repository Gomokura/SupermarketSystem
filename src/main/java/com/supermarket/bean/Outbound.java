package com.supermarket.bean;

import java.util.Date;

public class Outbound {
    private int outboundId;
    private int orderId;
    private int productId;
    private int quantity;
    private Date createTime;

    public int getOutboundId() { return outboundId; }
    public void setOutboundId(int outboundId) { this.outboundId = outboundId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
