package com.supermarket.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Integer addressId;
    private String paymentMethod;
    private List<CartItem> items;

    @Data
    public static class CartItem {
        private Integer productId;
        private Integer quantity;
    }
}
