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

    @PutMapping("/update")
    public Result<?> updateCartQuantity(
            @RequestAttribute Integer userId,
            @RequestParam Integer cartId,
            @RequestParam Integer quantity) {
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

    /** 勾选/取消勾选单条 */
    @PutMapping("/{cartId}/check")
    public Result<?> checkItem(
            @RequestAttribute Integer userId,
            @PathVariable Integer cartId,
            @RequestParam Integer checked) {
        return cartService.checkItem(userId, cartId, checked);
    }

    /** 全选/全不选 */
    @PutMapping("/check-all")
    public Result<?> checkAll(
            @RequestAttribute Integer userId,
            @RequestParam Integer checked) {
        return cartService.checkAll(userId, checked);
    }

    /** 批量删除 */
    @DeleteMapping("/batch")
    public Result<?> batchDelete(
            @RequestAttribute Integer userId,
            @RequestBody Map<String, List<Integer>> body) {
        return cartService.batchDelete(userId, body.get("cartIds"));
    }

    /** 已勾选项汇总金额与件数 */
    @GetMapping("/summary")
    public Result<?> checkedSummary(@RequestAttribute Integer userId) {
        return cartService.checkedSummary(userId);
    }
}
