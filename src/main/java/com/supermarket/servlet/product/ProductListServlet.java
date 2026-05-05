package com.supermarket.servlet.product;

import com.supermarket.entity.Result;
import com.supermarket.service.ProductService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ProductListServlet - 商品列表
 * action=productList
 */
public class ProductListServlet extends BaseServlet {

    public void doAction(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer categoryId = getInteger(req, "categoryId");
        String keyword = getString(req, "keyword");
        Integer brandId = getInteger(req, "brandId");
        String sortBy = getString(req, "sortBy");
        String sortOrder = getString(req, "sortOrder");
        Double minPrice = getDouble(req, "minPrice");
        Double maxPrice = getDouble(req, "maxPrice");
        int pageNum = getPageNum(req);
        int pageSize = getPageSize(req);

        ProductService productService = new ProductService();
        Result<?> result = productService.getProductList(categoryId, keyword, brandId,
                sortBy, sortOrder, minPrice, maxPrice, pageNum, pageSize);

        String ajax = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(ajax)) {
            if (result.getCode() == 200) {
                json(req, resp, result.getData());
            } else {
                jsonError(resp, result.getMessage());
            }
        } else {
            req.setAttribute("productPage", result.getData());
            req.setAttribute("categoryId", categoryId);
            req.setAttribute("keyword", keyword);
            forward(req, resp, "/views/productList.jsp");
        }
    }
}
