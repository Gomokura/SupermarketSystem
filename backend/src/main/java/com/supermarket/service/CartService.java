package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.Cart;
import com.supermarket.entity.Product;
import com.supermarket.mapper.CartMapper;
import com.supermarket.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService extends ServiceImpl<CartMapper, Cart> {

    @Autowired
    private ProductMapper productMapper;

    public Result<?> getCartList(Integer userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        List<Cart> list = this.list(wrapper);

        for (Cart cart : list) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product != null) {
                cart.setProductName(product.getProductName());
                cart.setPrice(product.getPrice());
                cart.setSubtotal(cart.getPrice() * cart.getQuantity());
            }
        }

        return Result.success(list);
    }

    public Result<?> addToCart(Integer userId, Integer productId, Integer quantity) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.eq(Cart::getProductId, productId);
        Cart existCart = this.getOne(wrapper);

        Product product = productMapper.selectById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }

        if (existCart != null) {
            existCart.setQuantity(existCart.getQuantity() + quantity);
            this.updateById(existCart);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            this.save(cart);
        }

        return Result.success();
    }

    public Result<?> updateCartQuantity(Integer cartId, Integer quantity) {
        Cart cart = this.getById(cartId);
        if (cart == null) {
            return Result.error("购物车项不存在");
        }
        cart.setQuantity(quantity);
        this.updateById(cart);
        return Result.success();
    }

    public Result<?> removeFromCart(Integer cartId) {
        this.removeById(cartId);
        return Result.success();
    }

    public Result<?> clearCart(Integer userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        this.remove(wrapper);
        return Result.success();
    }
}
