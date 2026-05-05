package com.supermarket.dto;

import lombok.Data;

@Data
public class CartRequest {
    private Integer productId;
    private Integer quantity;
}
