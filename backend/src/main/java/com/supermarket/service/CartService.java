package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.Cart;
import com.supermarket.entity.Product;
import com.supermarket.entity.ProductSku;
import com.supermarket.mapper.CartMapper;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.mapper.ProductSkuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService extends ServiceImpl<CartMapper, Cart> {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductSkuMapper productSkuMapper;

    /**
     * 购物车列表（含 SKU 规格名称、实时价格、库存状态）
     */
    public Result<?> getCartList(Integer userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        List<Cart> list = this.list(wrapper);

        for (Cart cart : list) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null) continue;

            cart.setProductName(product.getProductName());
            cart.setImageUrl(product.getCoverImage());
            cart.setProductStatus(product.getStatus());

            if (cart.getSkuId() != null) {
                // ✅ 有 SKU：取 SKU 的价格和库存
                ProductSku sku = productSkuMapper.selectById(cart.getSkuId());
                if (sku != null) {
                    cart.setPrice(sku.getPrice());
                    cart.setStock(sku.getStock());
                    cart.setSpecName(sku.getSkuName());
                }
            } else {
                // 无 SKU：取主表价格和库存
                cart.setPrice(product.getPrice());
                cart.setStock(product.getStock());
            }

            if (cart.getPrice() != null) {
                cart.setSubtotal(cart.getPrice() * cart.getQuantity());
            }
        }

        return Result.success(list);
    }

    /**
     * 加入购物车（支持 SKU 维度合并）
     */
    public Result<?> addToCart(Integer userId, Integer productId, Integer quantity, Integer skuId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            return Result.error("商品不存在");
        }
        if (!"active".equals(product.getStatus())) {
            return Result.error("商品已下架");
        }

        // 校验 SKU 库存
        if (skuId != null) {
            ProductSku sku = productSkuMapper.selectById(skuId);
            if (sku == null || !"active".equals(sku.getStatus())) {
                return Result.error("该规格不存在或已下架");
            }
            if (sku.getStock() < quantity) {
                return Result.error("该规格库存不足，剩余 " + sku.getStock());
            }
        } else {
            // 无 SKU 时校验主表库存
            if (product.getStock() < quantity) {
                return Result.error("商品库存不足，剩余 " + product.getStock());
            }
        }

        // 同一用户 + 同一商品 + 同一 SKU → 合并数量
        LambdaQueryWrapper<Cart> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Cart::getUserId, userId);
        existWrapper.eq(Cart::getProductId, productId);
        if (skuId != null) {
            existWrapper.eq(Cart::getSkuId, skuId);
        } else {
            existWrapper.isNull(Cart::getSkuId);
        }
        Cart existCart = this.getOne(existWrapper);

        if (existCart != null) {
            existCart.setQuantity(existCart.getQuantity() + quantity);
            this.updateById(existCart);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setSkuId(skuId);
            cart.setQuantity(quantity);
            cart.setIsChecked(1);
            this.save(cart);
        }

        return Result.success("已加入购物车");
    }

    /**
     * 修改购物车数量
     */
    public Result<?> updateCartQuantity(Integer cartId, Integer quantity) {
        Cart cart = this.getById(cartId);
        if (cart == null) return Result.error("购物车项不存在");
        if (quantity <= 0) return Result.error("数量必须大于 0");
        cart.setQuantity(quantity);
        this.updateById(cart);
        return Result.success();
    }

    /**
     * 删除购物车项
     */
    public Result<?> removeFromCart(Integer cartId) {
        this.removeById(cartId);
        return Result.success();
    }

    /**
     * 清空购物车
     */
    public Result<?> clearCart(Integer userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        this.remove(wrapper);
        return Result.success();
    }
}
