package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /** 我的收藏 */
    @GetMapping("/my")
    public Result<?> getMyFavorites(@RequestAttribute Integer userId) {
        return favoriteService.getMyFavorites(userId);
    }

    /** 收藏商品 */
    @PostMapping("/{productId}")
    public Result<?> addFavorite(
            @RequestAttribute Integer userId,
            @PathVariable Integer productId) {
        return favoriteService.addFavorite(userId, productId);
    }

    /** 取消收藏 */
    @DeleteMapping("/{productId}")
    public Result<?> removeFavorite(
            @RequestAttribute Integer userId,
            @PathVariable Integer productId) {
        return favoriteService.removeFavorite(userId, productId);
    }
}

