package com.supermarket.servlet.product;

import com.supermarket.entity.Result;
import com.supermarket.service.ProductService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ProductBarcodeServlet - 条码查询
 * action=productBarcode
 */
public class ProductBarcodeServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String barcode = getString(req, "barcode");
        if (barcode == null || barcode.isEmpty()) {
            jsonError(resp, "条码不能为空");
            return;
        }
        ProductService productService = new ProductService();
        Result<?> result = productService.getProductByBarcode(barcode);
        if (result.getCode() == 200) json(req, resp, result.getData());
        else jsonError(resp, result.getMessage());
    }
}
