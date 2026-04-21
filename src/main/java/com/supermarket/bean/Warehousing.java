package com.supermarket.bean;

import java.util.Date;

public class Warehousing {
    private int warehousingId;
    private int productId;
    private int quantity;
    private int operatorId;
    private String remark;
    private Date createTime;

    public int getWarehousingId() { return warehousingId; }
    public void setWarehousingId(int warehousingId) { this.warehousingId = warehousingId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getOperatorId() { return operatorId; }
    public void setOperatorId(int operatorId) { this.operatorId = operatorId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
