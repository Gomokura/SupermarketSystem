package com.supermarket.servlet.product;

import com.supermarket.entity.Result;
import com.supermarket.service.ProductService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ProductDetailServlet - 商品详情
 * action=productDetail
 */
public class ProductDetailServlet extends BaseServlet {

    public void doAction(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer productId = getInteger(req, "productId");

        if (productId == null) {
            jsonError(resp, "商品ID不能为空");
            return;
        }

        ProductService productService = new ProductService();
        Result<?> result = productService.getProductById(productId);

        String ajax = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(ajax)) {
            if (result.getCode() == 200) {
                json(req, resp, result.getData());
            } else {
                jsonError(resp, result.getMessage());
            }
        } else {
            if (result.getCode() == 200) {
                req.setAttribute("product", result.getData());
                forward(req, resp, "/views/productDetail.jsp");
            } else {
                req.setAttribute("error", result.getMessage());
                forward(req, resp, "/views/productList.jsp");
            }
        }
    }
}
