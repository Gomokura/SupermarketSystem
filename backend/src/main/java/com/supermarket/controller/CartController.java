package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Cart;
import com.supermarket.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    /** 前端发 JSON: { cartId, quantity } */
    @PutMapping("/update")
    public Result<?> updateCartQuantity(
            @RequestAttribute Integer userId,
            @RequestBody Map<String, Integer> body) {
        Integer cartId = body.get("cartId");
        Integer quantity = body.get("quantity");
        if (cartId == null || quantity == null) return Result.error("参数不完整");
        return cartService.updateCartQuantity(userId, cartId, quantity);
    }

    @DeleteMapping("/{cartId}")
    public Result<?> removeFromCart(
            @RequestAttribute Integer userId,
            @PathVariable Integer cartId) {
        return cartService.removeFromCart(userId, cartId);
    }

    @DeleteMapping("/clear")
    public Result<?> clearCart(@RequestAttribute Integer userId) {
        return cartService.clearCart(userId);
    }

    /** 前端发 PUT /cart/check  JSON: { cartId, checked } */
    @PutMapping("/check")
    public Result<?> checkItem(
            @RequestAttribute Integer userId,
            @RequestBody Map<String, Integer> body) {
        Integer cartId = body.get("cartId");
        Integer checked = body.get("checked");
        if (cartId == null) return Result.error("参数不完整");
        return cartService.checkItem(userId, cartId, checked);
    }

    /** 前端发 PUT /cart/check-all  JSON: { checked } */
    @PutMapping("/check-all")
    public Result<?> checkAll(
            @RequestAttribute Integer userId,
            @RequestBody Map<String, Integer> body) {
        Integer checked = body.get("checked");
        return cartService.checkAll(userId, checked);
    }

    /** 批量删除：前端发数组 [cartId1, cartId2, ...] */
    @DeleteMapping("/batch")
    public Result<?> batchDelete(
            @RequestAttribute Integer userId,
            @RequestBody List<Integer> cartIds) {
        return cartService.batchDelete(userId, cartIds);
    }

    /** 前端调 /cart/checked-summary */
    @GetMapping("/checked-summary")
    public Result<?> checkedSummary(@RequestAttribute Integer userId) {
        return cartService.checkedSummary(userId);
    }
}

