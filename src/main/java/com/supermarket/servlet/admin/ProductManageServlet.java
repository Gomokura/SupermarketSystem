package com.supermarket.servlet.admin;

import com.supermarket.entity.Product;
import com.supermarket.entity.ProductSku;
import com.supermarket.entity.Result;
import com.supermarket.service.ProductService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * ProductManageServlet - 管理后台商品管理
 * action=adminProductList / adminProductAdd / adminProductUpdate / adminProductDelete / adminProductStatus / adminProductLowStock / adminProductStock / adminProductSkuList / adminProductSkuSave / adminProductSkuDelete
 */
public class ProductManageServlet extends BaseServlet {

    /** 商品列表 */
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = getString(req, "action");
        if ("add".equals(action)) {
            doAdd(req, resp);
        } else if ("update".equals(action)) {
            doUpdate(req, resp);
        } else if ("delete".equals(action)) {
            doDelete(req, resp);
        } else if ("status".equals(action)) {
            doStatus(req, resp);
        } else if ("lowStock".equals(action)) {
            doLowStock(req, resp);
        } else if ("stock".equals(action)) {
            doStock(req, resp);
        } else if ("skuList".equals(action)) {
            doSkuList(req, resp);
        } else if ("skuSave".equals(action)) {
            doSkuSave(req, resp);
        } else if ("skuDelete".equals(action)) {
            doSkuDelete(req, resp);
        } else {
            doList(req, resp);
        }
    }

    private void doList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer categoryId = getInteger(req, "categoryId");
        String keyword = getString(req, "keyword");
        String status = getString(req, "status");
        int pageNum = getPageNum(req);
        int pageSize = getPageSize(req);

        ProductService productService = new ProductService();
        Result<?> result = productService.adminGetProductList(categoryId, keyword, status, pageNum, pageSize);

        String ajax = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(ajax)) {
            if (result.getCode() == 200) json(req, resp, result.getData());
            else jsonError(resp, result.getMessage());
        } else {
            req.setAttribute("productPage", result.getData());
            forward(req, resp, "/views/admin/productManage.jsp");
        }
    }

    private void doAdd(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer adminId = getLoginAdminId(req);
        if (adminId == null) { jsonError(resp, "请先登录"); return; }

        Product product = new Product();
        product.setProductName(getString(req, "productName"));
        product.setCategoryId(getInteger(req, "categoryId"));
        product.setBrandId(getInteger(req, "brandId"));
        product.setSupplierId(getInteger(req, "supplierId"));
        product.setDescription(getString(req, "description"));
        product.setCoverImage(getString(req, "coverImage"));
        product.setUnit(getString(req, "unit"));
        product.setOriginalPrice(getDouble(req, "originalPrice"));
        product.setPrice(getDouble(req, "price"));
        product.setStock(getInteger(req, "stock", 0));
        product.setBarcode(getString(req, "barcode"));
        product.setCostPrice(getDouble(req, "costPrice"));

        ProductService productService = new ProductService();
        Result<?> result = productService.addProduct(product);
        if (result.getCode() == 200) json(req, resp, result.getData());
        else jsonError(resp, result.getMessage());
    }

    private void doUpdate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer productId = getInteger(req, "productId");
        if (productId == null) { jsonError(resp, "商品ID不能为空"); return; }

        Product product = new Product();
        product.setProductId(productId);
        product.setProductName(getString(req, "productName"));
        product.setCategoryId(getInteger(req, "categoryId"));
        product.setBrandId(getInteger(req, "brandId"));
        product.setSupplierId(getInteger(req, "supplierId"));
        product.setDescription(getString(req, "description"));
        product.setCoverImage(getString(req, "coverImage"));
        product.setUnit(getString(req, "unit"));
        product.setOriginalPrice(getDouble(req, "originalPrice"));
        product.setPrice(getDouble(req, "price"));
        product.setStock(getInteger(req, "stock", 0));
        product.setBarcode(getString(req, "barcode"));
        product.setCostPrice(getDouble(req, "costPrice"));

        ProductService productService = new ProductService();
        Result<?> result = productService.updateProduct(product);
        if (result.getCode() == 200) jsonOk(req, resp);
        else jsonError(resp, result.getMessage());
    }

    private void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer productId = getInteger(req, "productId");
        if (productId == null) { jsonError(resp, "商品ID不能为空"); return; }
        ProductService productService = new ProductService();
        Result<?> result = productService.deleteProduct(productId);
        if (result.getCode() == 200) jsonMsg(req, resp, result.getMessage());
        else jsonError(resp, result.getMessage());
    }

    private void doStatus(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer productId = getInteger(req, "productId");
        String status = getString(req, "status");
        if (productId == null || status == null) { jsonError(resp, "参数不完整"); return; }
        ProductService productService = new ProductService();
        Result<?> result = productService.updateProductStatus(productId, status);
        if (result.getCode() == 200) jsonOk(req, resp);
        else jsonError(resp, result.getMessage());
    }

    private void doLowStock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int pageNum = getPageNum(req);
        int pageSize = getPageSize(req);
        ProductService productService = new ProductService();
        Result<?> result = productService.getLowStockProducts(pageNum, pageSize);
        if (result.getCode() == 200) json(req, resp, result.getData());
        else jsonError(resp, result.getMessage());
    }

    private void doStock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer productId = getInteger(req, "productId");
        Integer newStock = getInteger(req, "newStock");
        Integer adminId = getLoginAdminId(req);
        String remark = getString(req, "remark");
        if (productId == null || newStock == null) { jsonError(resp, "参数不完整"); return; }
        ProductService productService = new ProductService();
        Result<?> result = productService.updateStock(productId, newStock, adminId, remark);
        if (result.getCode() == 200) jsonOk(req, resp);
        else jsonError(resp, result.getMessage());
    }

    private void doSkuList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer productId = getInteger(req, "productId");
        if (productId == null) { jsonError(resp, "商品ID不能为空"); return; }
        ProductService productService = new ProductService();
        Result<?> result = productService.getProductSkus(productId);
        if (result.getCode() == 200) json(req, resp, result.getData());
        else jsonError(resp, result.getMessage());
    }

    private void doSkuSave(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer productId = getInteger(req, "productId");
        if (productId == null) { jsonError(resp, "商品ID不能为空"); return; }
        ProductService productService = new ProductService();
        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> skus = om.readValue(req.getParameter("skus"), List.class);
            java.util.List<ProductSku> skuList = new java.util.ArrayList<>();
            for (Map<String, Object> s : skus) {
                ProductSku sku = new ProductSku();
                if (s.get("skuId") != null) sku.setSkuId(((Number) s.get("skuId")).intValue());
                sku.setSkuName((String) s.get("skuName"));
                if (s.get("price") != null) sku.setPrice(((Number) s.get("price")).doubleValue());
                if (s.get("stock") != null) sku.setStock(((Number) s.get("stock")).intValue());
                skuList.add(sku);
            }
            Result<?> result = productService.saveProductSkus(productId, skuList);
            if (result.getCode() == 200) jsonOk(req, resp);
            else jsonError(resp, result.getMessage());
        } catch (Exception e) {
            jsonError(resp, "SKU数据解析失败");
        }
    }

    private void doSkuDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer skuId = getInteger(req, "skuId");
        if (skuId == null) { jsonError(resp, "SKU ID不能为空"); return; }
        ProductService productService = new ProductService();
        Result<?> result = productService.deleteProductSku(skuId);
        if (result.getCode() == 200) jsonOk(req, resp);
        else jsonError(resp, result.getMessage());
    }
}
