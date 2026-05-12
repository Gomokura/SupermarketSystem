package com.supermarket.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private Integer addressId;
    private String paymentMethod;
    private Integer couponId;
    private Integer userCouponId;
    private Integer pointsUsed;
    private String remark;
    private String deliveryTimeSlot;
    private List<CartItem> items;

    @Data
    public static class CartItem {
        private Integer productId;
        private Integer quantity;
        private Integer skuId;
        private String specName;
    }
}
