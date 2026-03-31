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
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

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
            int newQty = existCart.getQuantity() + quantity;

            // 合并后再做一次库存校验，避免“加购后超卖”
            if (existCart.getSkuId() != null) {
                ProductSku sku = productSkuMapper.selectById(existCart.getSkuId());
                if (sku == null || !"active".equals(sku.getStatus())) {
                    return Result.error("该规格不存在或已下架");
                }
                if (sku.getStock() < newQty) {
                    return Result.error("该规格库存不足，剩余 " + sku.getStock());
                }
            } else {
                if (product.getStock() < newQty) {
                    return Result.error("商品库存不足，剩余 " + product.getStock());
                }
            }

            existCart.setQuantity(newQty);
            this.updateById(existCart);
        } else {
            // 第一次加入购物车：先校验库存
            if (skuId != null) {
                ProductSku sku = productSkuMapper.selectById(skuId);
                if (sku == null || !"active".equals(sku.getStatus())) {
                    return Result.error("该规格不存在或已下架");
                }
                if (sku.getStock() < quantity) {
                    return Result.error("该规格库存不足，剩余 " + sku.getStock());
                }
            } else {
                if (product.getStock() < quantity) {
                    return Result.error("商品库存不足，剩余 " + product.getStock());
                }
            }

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
    public Result<?> updateCartQuantity(Integer userId, Integer cartId, Integer quantity) {
        Cart cart = this.getById(cartId);
        if (cart == null) return Result.error("购物车项不存在");
        if (!cart.getUserId().equals(userId)) return Result.error("无权操作购物车项");
        if (quantity <= 0) return Result.error("数量必须大于 0");

        // 校验库存（以当前购物车维度为准：SKU 或主表）
        Product product = productMapper.selectById(cart.getProductId());
        if (product == null || product.getIsDeleted() == 1) return Result.error("商品不存在");

        if (cart.getSkuId() != null) {
            ProductSku sku = productSkuMapper.selectById(cart.getSkuId());
            if (sku == null || !"active".equals(sku.getStatus())) return Result.error("该规格不存在或已下架");
            if (sku.getStock() < quantity) return Result.error("该规格库存不足，剩余 " + sku.getStock());
        } else {
            if (product.getStock() < quantity) return Result.error("商品库存不足，剩余 " + product.getStock());
        }

        cart.setQuantity(quantity);
        this.updateById(cart);
        return Result.success();
    }

    /**
     * 删除购物车项
     */
    public Result<?> removeFromCart(Integer userId, Integer cartId) {
        Cart cart = this.getById(cartId);
        if (cart == null) return Result.error("购物车项不存在");
        if (!cart.getUserId().equals(userId)) return Result.error("无权操作购物车项");
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

    /** 勾选/取消勾选单个购物车项 */
    public Result<?> checkItem(Integer userId, Integer cartId, Integer checked) {
        Cart cart = this.getById(cartId);
        if (cart == null) return Result.error("购物车项不存在");
        if (!cart.getUserId().equals(userId)) return Result.error("无权操作购物车项");
        cart.setIsChecked((checked != null && checked == 1) ? 1 : 0);
        this.updateById(cart);
        return Result.success();
    }

    /** 全选/全不选 */
    public Result<?> checkAll(Integer userId, Integer checked) {
        int v = (checked != null && checked == 1) ? 1 : 0;
        List<Cart> carts = this.list(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        for (Cart c : carts) {
            c.setIsChecked(v);
            this.updateById(c);
        }
        return Result.success();
    }

    /** 批量删除购物车项 */
    public Result<?> batchDelete(Integer userId, List<Integer> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) return Result.success();
        List<Cart> carts = this.listByIds(cartIds);
        List<Integer> ownedIds = carts.stream()
                .filter(c -> c != null && userId.equals(c.getUserId()))
                .map(Cart::getCartId)
                .collect(Collectors.toList());
        if (!ownedIds.isEmpty()) {
            this.removeByIds(ownedIds);
        }
        return Result.success();
    }

    /** 选中商品统计（件数、金额） */
    public Result<?> checkedSummary(Integer userId) {
        List<Cart> list = this.list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getIsChecked, 1));
        double totalAmount = 0;
        int totalCount = 0;
        for (Cart c : list) {
            Product p = productMapper.selectById(c.getProductId());
            if (p == null || p.getIsDeleted() == 1 || !"active".equals(p.getStatus())) continue;
            double unitPrice = p.getPrice() != null ? p.getPrice() : 0;
            if (c.getSkuId() != null) {
                ProductSku sku = productSkuMapper.selectById(c.getSkuId());
                if (sku == null || !"active".equals(sku.getStatus())) continue;
                if (sku.getPrice() != null) unitPrice = sku.getPrice();
            }
            totalCount += c.getQuantity();
            totalAmount += unitPrice * c.getQuantity();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("totalCount", totalCount);
        data.put("totalAmount", Math.round(totalAmount * 100.0) / 100.0);
        return Result.success(data);
    }
}
