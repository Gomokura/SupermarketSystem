package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Cart;
import com.supermarket.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/list")
    public Result<?> getCartList(@RequestAttribute Integer userId) {
        return cartService.getCartList(userId);
    }

    @PostMapping("/add")
    public Result<?> addToCart(
            @RequestAttribute Integer userId,
            @RequestParam Integer productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            @RequestParam(required = false) Integer skuId) {
        return cartService.addToCart(userId, productId, quantity, skuId);
    }

    @PutMapping("/update")
    public Result<?> updateCartQuantity(
            @RequestParam Integer cartId,
            @RequestParam Integer quantity) {
        return cartService.updateCartQuantity(cartId, quantity);
    }

    @DeleteMapping("/{cartId}")
    public Result<?> removeFromCart(@PathVariable Integer cartId) {
        return cartService.removeFromCart(cartId);
    }

    @DeleteMapping("/clear")
    public Result<?> clearCart(@RequestAttribute Integer userId) {
        return cartService.clearCart(userId);
    }
}
