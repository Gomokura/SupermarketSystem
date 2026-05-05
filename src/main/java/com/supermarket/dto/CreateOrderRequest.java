package com.supermarket.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Integer addressId;
    private String paymentMethod;
    private Integer couponId;     // 使用的优惠券ID（可选）
    private Integer pointsUsed;   // 使用积分（可选）
    private String remark;        // 订单备注（可选）
    private String deliveryTimeSlot; // 期望配送时间段（可选，如"明日上午"）
    private List<CartItem> items;

    @Data
    public static class CartItem {
        private Integer productId;
        private Integer quantity;
        private Integer skuId;    // 可选，有SKU时传
        private String specName;  // 可选，规格描述
    }
}
